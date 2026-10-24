package com.example.model

data class BallItem(
    val id: String,
    val color: BallColor,
    val isHidden: Boolean = false
)

typealias NutItem = BallItem

data class Tube(
    val id: Int,
    val balls: List<BallItem>,
    val capacity: Int = 4,
    val isChallengeTube: Boolean = false,
    val isSwappedColorTube: Boolean = false,
    val isSmallTube: Boolean = false
) {
    val isFull: Boolean get() = balls.size >= capacity
    val isEmpty: Boolean get() = balls.isEmpty()
    val topBall: BallItem? get() = balls.lastOrNull()

    /**
     * A tube is completed only if it is a standard tube (capacity >= 4), full, and all balls match color.
     */
    val isCompleted: Boolean
        get() = capacity >= 4 && isFull && balls.all { it.color == balls.first().color && !it.isHidden }

    // Compat accessors
    val nuts: List<BallItem> get() = balls
    val topNut: BallItem? get() = topBall
    val isChallengeRod: Boolean get() = isChallengeTube
    val isSwappedColorRod: Boolean get() = isSwappedColorTube
}

typealias Rod = Tube

data class MoveRecord(
    val fromTubeIndex: Int,
    val toTubeIndex: Int,
    val balls: List<BallItem>,
    val revealedUnderneath: Boolean
) {
    // Compat accessors
    val fromRodIndex: Int get() = fromTubeIndex
    val toRodIndex: Int get() = toTubeIndex
    val nuts: List<BallItem> get() = balls
}

data class ActiveMovingBall(
    val ball: BallItem,
    val fromTubeIndex: Int,
    val toTubeIndex: Int,
    val fromSlotIndex: Int,
    val targetSlotIndex: Int,
    val isLiftAlreadyDone: Boolean = false
) {
    // Compat accessors
    val nut: BallItem get() = ball
    val fromRodIndex: Int get() = fromTubeIndex
    val toRodIndex: Int get() = toTubeIndex
}

typealias ActiveMovingNut = ActiveMovingBall

data class HintInfo(
    val fromTubeIndex: Int,
    val toTubeIndex: Int,
    val reason: String
) {
    val fromRodIndex: Int get() = fromTubeIndex
    val toRodIndex: Int get() = toTubeIndex
}

data class GameState(
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val level: Int = 1,
    val tubes: List<Tube> = emptyList(),
    val selectedTubeIndex: Int? = null,
    val activeMovingBall: ActiveMovingBall? = null,
    val moveHistory: List<MoveRecord> = emptyList(),
    val movesCount: Int = 0,
    val isChallengeLevel: Boolean = false,
    val challengeDescription: String? = null,
    val isWon: Boolean = false,
    val hint: HintInfo? = null,
    val extraTubesAdded: Int = 0,
    val availableExtraTubes: Int = 2,
    val userMessage: String? = null,
    val justCompletedTubeIndex: Int? = null
) {
    // Compat accessors
    val rods: List<Tube> get() = tubes
    val selectedRodIndex: Int? get() = selectedTubeIndex
    val activeMovingNut: ActiveMovingBall? get() = activeMovingBall
    val extraRodsAdded: Int get() = extraTubesAdded
    val justCompletedRodIndex: Int? get() = justCompletedTubeIndex
}
