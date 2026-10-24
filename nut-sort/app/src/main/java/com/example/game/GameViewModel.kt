package com.example.game

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiCoachService
import com.example.model.ActiveMovingBall
import com.example.model.AppScreen
import com.example.model.BallItem
import com.example.model.Difficulty
import com.example.model.GameState
import com.example.model.MoveRecord
import com.example.model.Tube
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ball_sort_prefs", Context.MODE_PRIVATE)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // App screen: Starts at DIFFICULTY_SELECTION on opening the app
    private val _currentScreen = MutableStateFlow(AppScreen.DIFFICULTY_SELECTION)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Saved levels for each difficulty so user can continue wherever they left off
    private val _savedLevels = MutableStateFlow<Map<Difficulty, Int>>(emptyMap())
    val savedLevels: StateFlow<Map<Difficulty, Int>> = _savedLevels.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    init {
        loadSavedProgress()
    }

    private fun loadSavedProgress() {
        val easyLvl = prefs.getInt("saved_level_${Difficulty.EASY.name}", 1).coerceAtLeast(1)
        val medLvl = prefs.getInt("saved_level_${Difficulty.MEDIUM.name}", 1).coerceAtLeast(1)
        val hardLvl = prefs.getInt("saved_level_${Difficulty.HARD.name}", 1).coerceAtLeast(1)

        _savedLevels.value = mapOf(
            Difficulty.EASY to easyLvl,
            Difficulty.MEDIUM to medLvl,
            Difficulty.HARD to hardLvl
        )

        // Always show the difficulty selection screen on initial app opening
        _currentScreen.value = AppScreen.DIFFICULTY_SELECTION
    }

    /**
     * User chooses a difficulty from the main screen and continues wherever they left off.
     */
    fun selectDifficultyAndPlay(difficulty: Difficulty, targetLevel: Int? = null) {
        val levelToPlay = targetLevel ?: (_savedLevels.value[difficulty] ?: 1)
        prefs.edit().putString("saved_difficulty", difficulty.name).apply()
        startLevel(difficulty, levelToPlay)
        _currentScreen.value = AppScreen.GAME
    }

    /**
     * Navigates back from game to the Difficulty Selection Main Screen.
     */
    fun returnToDifficultyMenu() {
        val easyLvl = prefs.getInt("saved_level_${Difficulty.EASY.name}", 1).coerceAtLeast(1)
        val medLvl = prefs.getInt("saved_level_${Difficulty.MEDIUM.name}", 1).coerceAtLeast(1)
        val hardLvl = prefs.getInt("saved_level_${Difficulty.HARD.name}", 1).coerceAtLeast(1)

        _savedLevels.value = mapOf(
            Difficulty.EASY to easyLvl,
            Difficulty.MEDIUM to medLvl,
            Difficulty.HARD to hardLvl
        )
        _currentScreen.value = AppScreen.DIFFICULTY_SELECTION
    }

    private fun getExtraTubesInventory(difficulty: Difficulty): Int {
        return prefs.getInt("extra_tubes_inventory_${difficulty.name}", 2)
    }

    private fun saveExtraTubesInventory(difficulty: Difficulty, count: Int) {
        prefs.edit().putInt("extra_tubes_inventory_${difficulty.name}", count).apply()
    }

    fun setDifficulty(newDifficulty: Difficulty) {
        prefs.edit().putString("saved_difficulty", newDifficulty.name).apply()
        val savedLevel = prefs.getInt("saved_level_${newDifficulty.name}", 1).coerceAtLeast(1)
        startLevel(newDifficulty, savedLevel)
    }

    fun startLevel(difficulty: Difficulty, level: Int) {
        tapQueue.clear()
        currentChainRemaining = 0
        currentChainMovedBalls.clear()

        val (tubes, isChallenge) = PuzzleGenerator.generateLevel(difficulty, level)
        val challengeDesc = if (isChallenge) {
            "⚡ Mystery Challenge: Some ball colors are hidden until uncovered!"
        } else null

        val availableInventory = getExtraTubesInventory(difficulty)

        _gameState.value = GameState(
            difficulty = difficulty,
            level = level,
            tubes = tubes,
            selectedTubeIndex = null,
            activeMovingBall = null,
            moveHistory = emptyList(),
            movesCount = 0,
            isChallengeLevel = isChallenge,
            challengeDescription = challengeDesc,
            isWon = false,
            hint = null,
            extraTubesAdded = 0,
            availableExtraTubes = availableInventory,
            userMessage = null
        )

        prefs.edit().putInt("saved_level_${difficulty.name}", level).apply()
        _savedLevels.update { it + (difficulty to level) }
    }

    fun restartLevel() {
        val current = _gameState.value
        startLevel(current.difficulty, current.level)
    }

    fun nextLevel() {
        val current = _gameState.value
        val completedLevel = current.level
        val nextLvl = completedLevel + 1

        // Milestone reward: after each 5 level of completion the additional tube should be incremented by 1
        if (completedLevel % 5 == 0) {
            val currentInventory = getExtraTubesInventory(current.difficulty)
            saveExtraTubesInventory(current.difficulty, currentInventory + 1)
        }

        prefs.edit().putInt("saved_level_${current.difficulty.name}", nextLvl).apply()
        _savedLevels.update { it + (current.difficulty to nextLvl) }
        startLevel(current.difficulty, nextLvl)
    }

    // Tracking state for consecutive same-color ball chain move
    private var currentChainRemaining: Int = 0
    private val currentChainMovedBalls = mutableListOf<BallItem>()
    // Queue to hold user tube taps while ball animations are in flight (Requirement 4)
    private val tapQueue = ArrayDeque<Int>()

    fun onTubeClicked(tubeIndex: Int) {
        val state = _gameState.value
        if (state.isWon) return

        // If an animation is currently flying, queue the tap so no fast moves are dropped!
        if (state.activeMovingBall != null) {
            if (tapQueue.size < 8) {
                tapQueue.add(tubeIndex)
            }
            return
        }

        processTubeClick(tubeIndex)
    }

    private fun processTubeClick(tubeIndex: Int) {
        val state = _gameState.value
        if (state.activeMovingBall != null || state.isWon) return

        val selected = state.selectedTubeIndex

        if (selected == null) {
            // Nothing selected yet. Can only select a tube that has balls and is not completed
            val tube = state.tubes.getOrNull(tubeIndex) ?: return
            if (tube.balls.isNotEmpty() && !tube.isCompleted) {
                _gameState.update { it.copy(selectedTubeIndex = tubeIndex, hint = null, justCompletedTubeIndex = null) }
                vibrateTap(40)
            }
        } else if (selected == tubeIndex) {
            // Tapped same tube: deselect (ball drops back down)
            _gameState.update { it.copy(selectedTubeIndex = null) }
            vibrateTap(30)
        } else {
            // Tapped different tube: attempt move from selected to tubeIndex
            val fromTube = state.tubes[selected]
            val toTube = state.tubes.getOrNull(tubeIndex) ?: return

            if (fromTube.balls.isEmpty()) {
                _gameState.update { it.copy(selectedTubeIndex = null) }
                return
            }

            val movingBall = fromTube.balls.last()

            // Check if valid destination:
            // 1. Target not full
            // 2. Target is empty OR target top ball is visible and colors match
            val canMove = !toTube.isFull && (
                toTube.isEmpty || (!toTube.balls.last().isHidden && toTube.balls.last().color == movingBall.color)
            )

            if (canMove) {
                // Calculate how many consecutive matching balls can move one by one automatically:
                val matchingColor = movingBall.color
                val consecutiveBallsCount = fromTube.balls.takeLastWhile { !it.isHidden && it.color == matchingColor }.size
                val availableSlots = toTube.capacity - toTube.balls.size
                val totalToMove = minOf(consecutiveBallsCount, availableSlots).coerceAtLeast(1)

                currentChainRemaining = totalToMove - 1
                currentChainMovedBalls.clear()

                // ACCURATE MOVE COUNT: Increment movesCount immediately when valid move is executed
                _gameState.update {
                    it.copy(
                        movesCount = it.movesCount + 1,
                        activeMovingBall = ActiveMovingBall(
                            ball = movingBall,
                            fromTubeIndex = selected,
                            toTubeIndex = tubeIndex,
                            fromSlotIndex = fromTube.balls.size - 1,
                            targetSlotIndex = toTube.balls.size,
                            isLiftAlreadyDone = true
                        ),
                        selectedTubeIndex = null,
                        hint = null,
                        justCompletedTubeIndex = null
                    )
                }
                vibrateFlight()
            } else {
                // Invalid move: if clicked tube has selectable balls, switch selection; otherwise deselect
                if (toTube.balls.isNotEmpty() && !toTube.isCompleted) {
                    _gameState.update { it.copy(selectedTubeIndex = tubeIndex) }
                    vibrateTap(40)
                } else {
                    _gameState.update { it.copy(selectedTubeIndex = null) }
                    vibrateError()
                }
            }
        }
    }

    // Compatibility method for existing UI calls
    fun onRodClicked(rodIndex: Int) = onTubeClicked(rodIndex)

    fun onAnimationCompleted() {
        val state = _gameState.value
        val moving = state.activeMovingBall ?: return

        val fromTube = state.tubes[moving.fromTubeIndex]
        val toTube = state.tubes[moving.toTubeIndex]

        if (fromTube.balls.isEmpty()) {
            _gameState.update { it.copy(activeMovingBall = null) }
            return
        }

        val ball = fromTube.balls.last()
        currentChainMovedBalls.add(ball)

        val newFromBalls = fromTube.balls.dropLast(1).toMutableList()
        val newToBalls = toTube.balls + ball

        val updatedTubes = state.tubes.mapIndexed { idx, t ->
            when (idx) {
                moving.fromTubeIndex -> t.copy(balls = newFromBalls)
                moving.toTubeIndex -> t.copy(balls = newToBalls)
                else -> t
            }
        }

        // Check if there are more consecutive matching balls to move one by one automatically
        if (currentChainRemaining > 0 &&
            newFromBalls.isNotEmpty() &&
            !newFromBalls.last().isHidden &&
            newFromBalls.last().color == ball.color &&
            newToBalls.size < toTube.capacity
        ) {
            currentChainRemaining--
            val nextBall = newFromBalls.last()

            // Trigger next consecutive ball flight (lifts from slot, arcs, and drops down)
            _gameState.update {
                it.copy(
                    tubes = updatedTubes,
                    activeMovingBall = ActiveMovingBall(
                        ball = nextBall,
                        fromTubeIndex = moving.fromTubeIndex,
                        toTubeIndex = moving.toTubeIndex,
                        fromSlotIndex = newFromBalls.size - 1,
                        targetSlotIndex = newToBalls.size,
                        isLiftAlreadyDone = false
                    ),
                    selectedTubeIndex = null
                )
            }
            vibrateFlight()
        } else {
            // Entire chain move finished!
            currentChainRemaining = 0

            // Check if the newly exposed top ball on fromTube was hidden
            var revealedUnderneath = false
            if (newFromBalls.isNotEmpty() && newFromBalls.last().isHidden) {
                val topHidden = newFromBalls.removeAt(newFromBalls.size - 1)
                newFromBalls.add(topHidden.copy(isHidden = false))
                revealedUnderneath = true
            }

            val finalTubes = state.tubes.mapIndexed { idx, t ->
                when (idx) {
                    moving.fromTubeIndex -> t.copy(balls = newFromBalls)
                    moving.toTubeIndex -> t.copy(balls = newToBalls)
                    else -> t
                }
            }

            val record = MoveRecord(
                fromTubeIndex = moving.fromTubeIndex,
                toTubeIndex = moving.toTubeIndex,
                balls = currentChainMovedBalls.toList(),
                revealedUnderneath = revealedUnderneath
            )

            val isToTubeComplete = finalTubes[moving.toTubeIndex].isCompleted
            val isWon = checkWin(finalTubes)

            _gameState.update {
                it.copy(
                    tubes = finalTubes,
                    activeMovingBall = null,
                    selectedTubeIndex = null,
                    moveHistory = it.moveHistory + record,
                    isWon = isWon,
                    justCompletedTubeIndex = if (isToTubeComplete) moving.toTubeIndex else null
                )
            }

            vibrateThud()
            if (isWon) {
                vibrateWin()
                tapQueue.clear()
                return
            }

            // Drain queued taps (Requirement 4: immediately process moves queued during flight)
            while (tapQueue.isNotEmpty() && _gameState.value.activeMovingBall == null && !_gameState.value.isWon) {
                val nextTap = tapQueue.removeFirst()
                processTubeClick(nextTap)
            }
        }
    }

    fun requestAiCoach() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiAdvice.value = null
            _aiError.value = null
            val result = GeminiCoachService.askPuzzleCoach(_gameState.value)
            result.onSuccess { text ->
                _aiAdvice.value = text
                _isAiLoading.value = false
            }.onFailure { err ->
                _aiError.value = err.message ?: "Could not get AI advice"
                _isAiLoading.value = false
            }
        }
    }

    fun clearAiCoach() {
        _aiAdvice.value = null
        _aiError.value = null
        _isAiLoading.value = false
    }

    private fun checkWin(tubes: List<Tube>): Boolean {
        // Any small buffer tube (< 4 capacity) must be empty for the puzzle to be won
        val smallTubes = tubes.filter { it.capacity < 4 }
        if (smallTubes.any { !it.isEmpty }) return false

        val standardTubes = tubes.filter { it.capacity >= 4 }
        var completedCount = 0
        for (tube in standardTubes) {
            if (tube.isEmpty) continue
            if (tube.isCompleted) {
                completedCount++
            } else {
                return false
            }
        }

        val totalBalls = tubes.sumOf { it.balls.size }
        val expectedCompleted = totalBalls / 4
        return completedCount == expectedCompleted && completedCount > 0
    }

    fun undo() {
        val state = _gameState.value
        if (state.activeMovingBall != null || state.isWon) return
        if (state.moveHistory.isEmpty()) return

        val lastMove = state.moveHistory.last()
        val newHistory = state.moveHistory.dropLast(1)

        val fromTube = state.tubes[lastMove.fromTubeIndex]
        val toTube = state.tubes[lastMove.toTubeIndex]

        val countToReturn = lastMove.balls.size
        if (toTube.balls.size < countToReturn) return

        val returnedBalls = toTube.balls.takeLast(countToReturn)
        val newToBalls = toTube.balls.dropLast(countToReturn)

        val newFromBalls = fromTube.balls.toMutableList()
        // If a ball was revealed underneath when moving away, re-hide it
        if (lastMove.revealedUnderneath && newFromBalls.isNotEmpty()) {
            val revealed = newFromBalls.removeAt(newFromBalls.size - 1)
            newFromBalls.add(revealed.copy(isHidden = true))
        }
        newFromBalls.addAll(returnedBalls)

        val updatedTubes = state.tubes.mapIndexed { idx, t ->
            when (idx) {
                lastMove.fromTubeIndex -> t.copy(balls = newFromBalls)
                lastMove.toTubeIndex -> t.copy(balls = newToBalls)
                else -> t
            }
        }

        _gameState.update {
            it.copy(
                tubes = updatedTubes,
                moveHistory = newHistory,
                movesCount = (it.movesCount - 1).coerceAtLeast(0),
                selectedTubeIndex = null,
                hint = null,
                justCompletedTubeIndex = null
            )
        }

        vibrateTap(40)
    }

    fun showHint() {
        val state = _gameState.value
        if (state.activeMovingBall != null || state.isWon) return

        val hint = PuzzleSolver.findHint(state.tubes)
        if (hint != null) {
            _gameState.update { it.copy(hint = hint) }
            vibrateTap(60)
        }
    }

    private var messageDismissJob: Job? = null

    fun showUserMessage(msg: String) {
        _gameState.update { it.copy(userMessage = msg) }
        messageDismissJob?.cancel()
        messageDismissJob = viewModelScope.launch {
            delay(3000)
            _gameState.update { if (it.userMessage == msg) it.copy(userMessage = null) else it }
        }
    }

    fun clearUserMessage() {
        messageDismissJob?.cancel()
        _gameState.update { it.copy(userMessage = null) }
    }

    fun addExtraTube() {
        val state = _gameState.value
        if (state.activeMovingBall != null || state.isWon) return

        // 1. Single level limit: User can add only 1 additional tube in a single level
        if (state.extraTubesAdded >= 1) {
            showUserMessage("Your additional tube limit for these level has been Reached!")
            vibrateError()
            return
        }

        // 2. Inventory check: Check if player has additional tubes remaining in inventory
        if (state.availableExtraTubes <= 0) {
            showUserMessage("No additional tubes remaining!")
            vibrateError()
            return
        }

        // 3. Add small tube: capacity is strictly 1 ball
        val newTube = Tube(
            id = state.tubes.size,
            balls = emptyList(),
            capacity = 1,
            isSmallTube = true
        )

        // 4. Decrement inventory for this difficulty by 1
        val remainingInventory = (state.availableExtraTubes - 1).coerceAtLeast(0)
        saveExtraTubesInventory(state.difficulty, remainingInventory)

        _gameState.update {
            it.copy(
                tubes = it.tubes + newTube,
                extraTubesAdded = 1,
                availableExtraTubes = remainingInventory,
                userMessage = null,
                hint = null
            )
        }

        vibrateTap(70)
    }

    // Compatibility method
    fun addExtraRod() = addExtraTube()

    // --- Haptics ---
    private fun vibrateTap(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateFlight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, 120))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateThud() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(55, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(55)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateError() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), intArrayOf(0, 180, 0, 180), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateWin() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 80, 120, 80, 200),
                        intArrayOf(0, 150, 0, 200, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300)
            }
        } catch (_: Exception) {}
    }
}
