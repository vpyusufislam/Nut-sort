package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import com.example.model.Difficulty

@Composable
fun NutSortGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    onBackToMenu: () -> Unit = { viewModel.returnToDifficultyMenu() }
) {
    val gameState by viewModel.gameState.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiAdvice by viewModel.aiAdvice.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showAiCoachDialog by remember { mutableStateOf(false) }

    // Carbon dark slate background matching the video (00:00)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF141720),
            Color(0xFF181C26),
            Color(0xFF0F1218)
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            // Subtle graph grid lines matching the video background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 38.dp.toPx()
                val gridColor = Color(0x12FFFFFF)
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- TOP BAR (Blue action buttons, level & moves pill) ---
                TopBar(
                    difficulty = gameState.difficulty,
                    level = gameState.level,
                    movesCount = gameState.movesCount,
                    isChallenge = gameState.isChallengeLevel,
                    canUndo = gameState.moveHistory.isNotEmpty() && gameState.activeMovingBall == null,
                    canAddTube = gameState.extraTubesAdded < 1 && gameState.availableExtraTubes > 0 && gameState.activeMovingBall == null,
                    availableExtraTubes = gameState.availableExtraTubes,
                    onBackToMenu = onBackToMenu,
                    onOpenDifficulty = { showDifficultyDialog = true },
                    onAskAiCoach = {
                        showAiCoachDialog = true
                        viewModel.requestAiCoach()
                    },
                    onRestart = { viewModel.restartLevel() },
                    onUndo = { viewModel.undo() },
                    onAddTube = { viewModel.addExtraTube() }
                )

                // User feedback message banner (e.g. "Your additional tube limit for these level has been Reached!")
                AnimatedVisibility(
                    visible = gameState.userMessage != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    gameState.userMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF4A1010))
                                .border(
                                    width = 1.2.dp,
                                    color = Color(0xFFFF5252),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("user_message_banner"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    color = Color(0xFFFFEBEE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Hint / Challenge notification banner
                AnimatedVisibility(
                    visible = gameState.hint != null || gameState.isChallengeLevel,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (gameState.hint != null) Color(0xFF382F10) else Color(0xFF1E2638)
                            )
                            .border(
                                width = 1.dp,
                                color = if (gameState.hint != null) Color(0xFFFFD54F) else Color(0xFF00E5FF).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val message = gameState.hint?.reason
                            ?: gameState.challengeDescription
                            ?: ""
                        Text(
                            text = message,
                            color = if (gameState.hint != null) Color(0xFFFFE082) else Color(0xFFE0F7FA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // --- MAIN GAME BOARD (Glass Test Tubes & Smooth Marble Physics) ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardView(
                        gameState = gameState,
                        onRodClick = { viewModel.onRodClicked(it) },
                        onAnimationCompleted = { viewModel.onAnimationCompleted() }
                    )
                }

                // --- BOTTOM ACTION CONTROLS (Undo, Hint, Add Tube) ---
                GameControls(
                    gameState = gameState,
                    onUndo = { viewModel.undo() },
                    onHint = { viewModel.showHint() },
                    onAddRod = { viewModel.addExtraRod() }
                )
            }

            // --- WIN CELEBRATION DIALOG ---
            if (gameState.isWon) {
                WinDialog(
                    level = gameState.level,
                    moves = gameState.movesCount,
                    onNextLevel = { viewModel.nextLevel() },
                    onReplayLevel = { viewModel.restartLevel() }
                )
            }

            // --- DIFFICULTY & LEVEL SELECTOR DIALOG ---
            if (showDifficultyDialog) {
                DifficultyDialog(
                    currentDifficulty = gameState.difficulty,
                    currentLevel = gameState.level,
                    onSelectDifficulty = { diff -> viewModel.setDifficulty(diff) },
                    onJumpToLevel = { lvl -> viewModel.startLevel(gameState.difficulty, lvl) },
                    onDismiss = { showDifficultyDialog = false }
                )
            }

            // --- GEMINI HIGH THINKING AI COACH DIALOG ---
            if (showAiCoachDialog) {
                AiCoachDialog(
                    isLoading = isAiLoading,
                    adviceText = aiAdvice,
                    errorMessage = aiError,
                    onDismiss = {
                        showAiCoachDialog = false
                        viewModel.clearAiCoach()
                    }
                )
            }
        }
    }
}

@Composable
fun BallSortGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    onBackToMenu: () -> Unit = { viewModel.returnToDifficultyMenu() }
) {
    NutSortGameScreen(viewModel, modifier, onBackToMenu)
}

@Composable
private fun TopBar(
    difficulty: Difficulty,
    level: Int,
    movesCount: Int,
    isChallenge: Boolean,
    canUndo: Boolean,
    canAddTube: Boolean,
    availableExtraTubes: Int,
    onBackToMenu: () -> Unit,
    onOpenDifficulty: () -> Unit,
    onAskAiCoach: () -> Unit,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
    onAddTube: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Actions: Home & Restart (Vibrant blue rounded buttons matching the video)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Menu Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2979FF))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 24.dp),
                        onClick = onBackToMenu
                    )
                    .testTag("back_to_menu_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Difficulty Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Restart Button (Refreshes current level)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2979FF))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 24.dp),
                        onClick = onRestart
                    )
                    .testTag("restart_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Restart Level",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Center: Level and accurate Moves pill
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenDifficulty
                )
                .testTag("difficulty_badge")
        ) {
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1B202C))
                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEVEL $level",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = difficulty.title.uppercase(),
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "•",
                            color = Color(0x80FFFFFF),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "MOVES: $movesCount",
                            color = Color(0xFFFFD54F),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            if (isChallenge) {
                Text(
                    text = "⚡ Mystery",
                    color = Color(0xFFFFD54F),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Right Actions: AI Coach & Quick Add Tube
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Puzzle Coach Brain Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
                        )
                    )
                    .border(1.dp, Color(0xFFBA68C8), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 24.dp),
                        onClick = onAskAiCoach
                    )
                    .testTag("ai_coach_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = "AI Puzzle Coach",
                    tint = Color(0xFFE1BEE7),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Quick Add Tube Button with Inventory Count Badge
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canAddTube) Color(0xFF2979FF) else Color(0xFF1F2430))
                        .border(
                            1.dp,
                            if (canAddTube) Color(0x66FFFFFF) else Color(0x20FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 24.dp),
                            onClick = onAddTube
                        )
                        .testTag("quick_add_tube_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add tube",
                            tint = if (canAddTube) Color.White else Color(0xFF8C93A4),
                            modifier = Modifier.size(16.dp)
                        )
                        // Mini test tube glyph
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 2.5.dp, bottomEnd = 2.5.dp))
                                .background(if (canAddTube) Color(0xFFE0F7FA) else Color(0xFF5A6273))
                                .border(
                                    0.8.dp,
                                    if (canAddTube) Color.White else Color(0xFF6B7280),
                                    RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 2.5.dp, bottomEnd = 2.5.dp)
                                )
                        )
                    }
                }

                // Inventory badge indicator on Quick Add Tube button
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(
                            if (availableExtraTubes > 0) Color(0xFF00C853) else Color(0xFFE53935)
                        )
                        .border(0.8.dp, Color.White, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$availableExtraTubes",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

