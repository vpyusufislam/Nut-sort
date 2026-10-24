package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.ActiveMovingBall
import com.example.model.GameState
import kotlin.math.roundToInt

/**
 * Geometric positioning data for each test tube relative to the root GameBoard container.
 */
data class TubeGeometry(
    val centerX: Float,
    val bottomY: Float,
    val mouthY: Float,
    val hoverY: Float,
    val capacity: Int,
    val ballSizePx: Float,
    val bottomPaddingPx: Float
) {
    /**
     * Exact resting Y center of a ball in the given slot index inside this test tube.
     */
    fun getSlotCenterY(slotIndex: Int): Float {
        return bottomY - bottomPaddingPx - (slotIndex * ballSizePx) - (ballSizePx / 2f)
    }
}

/**
 * Main Ball Sort Game Board displaying glass test tubes in 2 balanced rows,
 * coordinating ball selection, multi-ball chain auto-moves, and smooth marble flight.
 */
@Composable
fun GameBoardView(
    gameState: GameState,
    onRodClick: (Int) -> Unit,
    onAnimationCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val tubeCoordinatesMap = remember { mutableStateMapOf<Int, LayoutCoordinates>() }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .onGloballyPositioned { boardCoordinates = it }
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        val tubes = gameState.tubes
        val totalTubes = tubes.size

        // Calculate columns per row (e.g. 6 in Hard lv > 5, 5 in Medium lv > 5, 4 in Easy lv > 5)
        val columnsPerRow = when {
            totalTubes <= 4 -> totalTubes
            totalTubes <= 6 -> (totalTubes + 1) / 2
            totalTubes <= 8 -> 4
            totalTubes <= 10 -> 5
            totalTubes <= 12 -> 6
            else -> 7
        }

        val ballSize = remember(availableWidth, availableHeight, columnsPerRow) {
            val calcW = (availableWidth - (10.dp * (columnsPerRow + 1))) / (columnsPerRow * 1.18f)
            val numRows = if (totalTubes > columnsPerRow) 2 else 1
            val capacity = gameState.difficulty.capacity
            val spacingY = if (numRows > 1) 22.dp else 0.dp
            // Ensure the tubes fit vertically within availableHeight:
            val calcH = ((availableHeight - spacingY - 16.dp) / numRows - 48.dp) / (capacity + 1)
            minOf(calcW, calcH).coerceIn(24.dp, 56.dp)
        }

        val tubeWidth = ballSize + 10.dp
        val maxTubeHeight = ballSize * gameState.difficulty.capacity + 28.dp
        val maxTubeBoxHeight = maxTubeHeight + ballSize + 20.dp

        val topRowTubes = remember(tubes, columnsPerRow) {
            tubes.take(columnsPerRow)
        }
        val bottomRowTubes = remember(tubes, columnsPerRow) {
            tubes.drop(columnsPerRow)
        }

        /**
         * Computes the exact screen geometry for a tube.
         * Accounts for individual tube capacity so small (capacity = 1) tubes calculate correctly.
         */
        fun getTubeGeometry(tubeId: Int): TubeGeometry {
            val targetTube = tubes.find { it.id == tubeId }
            val actualCapacity = targetTube?.capacity ?: gameState.difficulty.capacity
            val thisTubeHeight = ballSize * actualCapacity + 28.dp
            val tubeHeightPx = with(density) { thisTubeHeight.toPx() }
            val ballSizePx = with(density) { ballSize.toPx() }
            val bottomPaddingPx = with(density) { 8.dp.toPx() }
            val hoverClearancePx = with(density) { (8.dp + ballSize / 2f).toPx() }

            val board = boardCoordinates
            val tubeCoords = tubeCoordinatesMap[tubeId]
            if (board != null && tubeCoords != null && board.isAttached && tubeCoords.isAttached) {
                val localOffset = board.localPositionOf(tubeCoords, Offset.Zero)
                val width = tubeCoords.size.width.toFloat()
                val height = tubeCoords.size.height.toFloat()

                val centerX = localOffset.x + width / 2f
                val bottomY = localOffset.y + height
                val mouthY = bottomY - tubeHeightPx
                val hoverY = mouthY - hoverClearancePx

                return TubeGeometry(
                    centerX = centerX,
                    bottomY = bottomY,
                    mouthY = mouthY,
                    hoverY = hoverY,
                    capacity = actualCapacity,
                    ballSizePx = ballSizePx,
                    bottomPaddingPx = bottomPaddingPx
                )
            }

            // Mathematical grid fallback
            val itemWidthPx = with(density) { (tubeWidth + 8.dp).toPx() }
            val itemHeightPx = with(density) { maxTubeBoxHeight.toPx() }
            val spacingXPx = with(density) { 8.dp.toPx() }
            val spacingYPx = with(density) { 22.dp.toPx() }
            val availableWidthPx = with(density) { availableWidth.toPx() }
            val availableHeightPx = with(density) { availableHeight.toPx() }

            val isTopRow = tubeId < columnsPerRow
            val colIndex = if (isTopRow) tubeId else tubeId - columnsPerRow
            val rowCount = if (isTopRow) topRowTubes.size else bottomRowTubes.size
            val rowWidthPx = rowCount * itemWidthPx + (rowCount - 1).coerceAtLeast(0) * spacingXPx
            val rowStartX = (availableWidthPx - rowWidthPx) / 2f

            val hasBottomRow = bottomRowTubes.isNotEmpty()
            val totalColHeightPx = if (hasBottomRow) (itemHeightPx * 2f + spacingYPx) else itemHeightPx
            val colStartY = (availableHeightPx - totalColHeightPx) / 2f
            val rowTopY = if (isTopRow) colStartY else colStartY + itemHeightPx + spacingYPx

            val itemLeftX = rowStartX + colIndex * (itemWidthPx + spacingXPx)
            val centerX = itemLeftX + itemWidthPx / 2f
            val bottomY = rowTopY + itemHeightPx
            val mouthY = bottomY - tubeHeightPx
            val hoverY = mouthY - hoverClearancePx

            return TubeGeometry(
                centerX = centerX,
                bottomY = bottomY,
                mouthY = mouthY,
                hoverY = hoverY,
                capacity = actualCapacity,
                ballSizePx = ballSizePx,
                bottomPaddingPx = bottomPaddingPx
            )
        }

        // --- Centered Tubes Grid ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row of Test Tubes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.wrapContentSize()
                ) {
                    topRowTubes.forEach { tube ->
                        val isSelected = gameState.selectedTubeIndex == tube.id
                        val isHintSrc = gameState.hint?.fromTubeIndex == tube.id
                        val isHintDst = gameState.hint?.toTubeIndex == tube.id
                        val isFlyingFromThis = gameState.activeMovingBall?.fromTubeIndex == tube.id

                        TestTubeView(
                            tube = tube,
                            isSelected = isSelected,
                            isHintSource = isHintSrc,
                            isHintTarget = isHintDst,
                            capacity = gameState.difficulty.capacity,
                            ballSize = ballSize,
                            isTopBallInFlight = isFlyingFromThis,
                            onTubeClick = { onRodClick(tube.id) },
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                tubeCoordinatesMap[tube.id] = coordinates
                            }
                        )
                    }
                }

                // Bottom Row of Test Tubes
                if (bottomRowTubes.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        bottomRowTubes.forEach { tube ->
                            val isSelected = gameState.selectedTubeIndex == tube.id
                            val isHintSrc = gameState.hint?.fromTubeIndex == tube.id
                            val isHintDst = gameState.hint?.toTubeIndex == tube.id
                            val isFlyingFromThis = gameState.activeMovingBall?.fromTubeIndex == tube.id

                            TestTubeView(
                                tube = tube,
                                isSelected = isSelected,
                                isHintSource = isHintSrc,
                                isHintTarget = isHintDst,
                                capacity = gameState.difficulty.capacity,
                                ballSize = ballSize,
                                isTopBallInFlight = isFlyingFromThis,
                                onTubeClick = { onRodClick(tube.id) },
                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                    tubeCoordinatesMap[tube.id] = coordinates
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- REALISTIC FLYING GLASS BALL ANIMATION OVERLAY ---
        // Anchored at TopStart of the GameBoard container with full size so (x, y) coordinates
        // correspond strictly to exact pixel positions across the board.
        val activeBall = gameState.activeMovingBall
        if (activeBall != null) {
            val fromGeom = getTubeGeometry(activeBall.fromTubeIndex)
            val toGeom = getTubeGeometry(activeBall.toTubeIndex)

            FlyingBallOverlay(
                activeBall = activeBall,
                fromGeom = fromGeom,
                toGeom = toGeom,
                ballSize = ballSize,
                onComplete = onAnimationCompleted,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Evaluates a 1D cubic Bézier curve at parameter t in [0, 1].
 */
private fun cubicBezier(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
    val u = 1f - t
    return u * u * u * p0 + 3f * u * u * t * p1 + 3f * u * t * t * p2 + t * t * t * p3
}

/**
 * Ultra-smooth marble flight animation overlay:
 * 1. Opening-Only Tube Trajectory:
 *    - Vertical Selection Lift: Top marble rises smoothly up through the tube cylinder and clears the open top rim.
 *    - Glide Arc: Ball travels in a Bézier arc elevated safely above all tube mouths across both rows, arriving directly centered above the destination tube's opening.
 *    - Vertical Axis Entry: Ball descends strictly along the central vertical axis directly through the top opening of the destination tube, eliminating any clipping through side walls.
 * 2. Realistic Bouncing Effect:
 *    - Two-Stage Elastic Bounce: Physics-based damped parabolic rebound (primary bounce ~9.5dp followed by subtle micro-bounce ~2.8dp).
 *    - Dynamic Elastic Squish: Subtle compression on initial impact to give marble a tactile physical feel.
 */
@Composable
private fun FlyingBallOverlay(
    activeBall: ActiveMovingBall,
    fromGeom: TubeGeometry,
    toGeom: TubeGeometry,
    ballSize: Dp,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val progress = remember(activeBall) { Animatable(0f) }

    val ballSizePx = fromGeom.ballSizePx
    val startX = fromGeom.centerX
    val targetX = toGeom.centerX

    val fromHoverY = fromGeom.hoverY
    val toHoverY = toGeom.hoverY

    val fromSlotY = fromGeom.getSlotCenterY(activeBall.fromSlotIndex)
    val targetSlotY = toGeom.getSlotCenterY(activeBall.targetSlotIndex)

    // Clearance apex ensuring the ball travels comfortably elevated above all tube openings
    val clearancePx = with(density) { 34.dp.toPx() }
    val apexY = minOf(fromHoverY, toHoverY) - clearancePx

    val p1X = startX + (targetX - startX) * 0.20f
    val p2X = startX + (targetX - startX) * 0.80f

    // Ultra fast, responsive duration for instant and smooth ball motion
    val duration = if (activeBall.isLiftAlreadyDone) 140 else 180

    LaunchedEffect(activeBall) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration, easing = LinearEasing)
        )
        onComplete()
    }

    val p = progress.value
    val currentX: Float
    val currentY: Float
    var scaleX = 1.0f
    var scaleY = 1.0f

    val primaryBouncePx = with(density) { 9.5.dp.toPx() }
    val secondaryBouncePx = with(density) { 2.8.dp.toPx() }

    if (activeBall.isLiftAlreadyDone) {
        // Ball was already lifted to hover above the source tube mouth by selection.
        // Phase 1 (0.00 .. 0.54): Glide smoothly in elevated Bézier arc to target tube opening
        // Phase 2 (0.54 .. 0.77): Drop straight DOWN along vertical centerline through target opening
        // Phase 3 (0.77 .. 1.00): Two-phase elastic marble bounce & squish upon impact
        if (p <= 0.54f) {
            val t = p / 0.54f
            currentX = cubicBezier(startX, p1X, p2X, targetX, t)
            currentY = cubicBezier(fromHoverY, apexY, apexY, toHoverY, t)
        } else if (p <= 0.77f) {
            val t = (p - 0.54f) / 0.23f
            // STRICTLY LOCKED at destination tube center X (no side clipping)
            currentX = targetX
            // Accelerates downwards with natural gravity (t^2)
            val dropFactor = t * t
            currentY = toHoverY + (targetSlotY - toHoverY) * dropFactor

            // Aerodynamic stretch during vertical plunge
            val stretch = t * 0.07f
            scaleY = 1.0f + stretch
            scaleX = 1.0f - stretch * 0.5f
        } else {
            val t = (p - 0.77f) / 0.23f
            // STRICTLY LOCKED at destination tube center X
            currentX = targetX

            // Realistic two-phase damped elastic bounce
            val bounceOffset = if (t < 0.65f) {
                val u = t / 0.65f
                4f * u * (1f - u) * primaryBouncePx
            } else {
                val u = (t - 0.65f) / 0.35f
                4f * u * (1f - u) * secondaryBouncePx
            }
            currentY = targetSlotY - bounceOffset

            // Subtle elastic squish on impact
            if (t < 0.15f) {
                val sq = 1f - (t / 0.15f)
                scaleX = 1.0f + sq * 0.12f
                scaleY = 1.0f - sq * 0.12f
            }
        }
    } else {
        // Subsequent auto-chain balls:
        // Phase 0 (0.00 .. 0.22): Lift straight UP through source tube opening to hover position
        // Phase 1 (0.22 .. 0.58): Glide smoothly across above tube openings to target tube opening
        // Phase 2 (0.58 .. 0.79): Drop straight DOWN along vertical centerline into the tube
        // Phase 3 (0.79 .. 1.00): Two-phase elastic marble bounce & squish upon impact
        if (p < 0.22f) {
            val t = p / 0.22f
            // STRICTLY LOCKED at source tube center X
            currentX = startX
            // Smooth ease-out vertical lift
            val liftFactor = 1f - (1f - t) * (1f - t)
            currentY = fromSlotY + (fromHoverY - fromSlotY) * liftFactor
        } else if (p <= 0.58f) {
            val t = (p - 0.22f) / 0.36f
            currentX = cubicBezier(startX, p1X, p2X, targetX, t)
            currentY = cubicBezier(fromHoverY, apexY, apexY, toHoverY, t)
        } else if (p <= 0.79f) {
            val t = (p - 0.58f) / 0.21f
            // STRICTLY LOCKED at destination tube center X
            currentX = targetX
            val dropFactor = t * t
            currentY = toHoverY + (targetSlotY - toHoverY) * dropFactor

            val stretch = t * 0.07f
            scaleY = 1.0f + stretch
            scaleX = 1.0f - stretch * 0.5f
        } else {
            val t = (p - 0.79f) / 0.21f
            currentX = targetX

            val bounceOffset = if (t < 0.65f) {
                val u = t / 0.65f
                4f * u * (1f - u) * primaryBouncePx
            } else {
                val u = (t - 0.65f) / 0.35f
                4f * u * (1f - u) * secondaryBouncePx
            }
            currentY = targetSlotY - bounceOffset

            if (t < 0.15f) {
                val sq = 1f - (t / 0.15f)
                scaleX = 1.0f + sq * 0.12f
                scaleY = 1.0f - sq * 0.12f
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (currentX - ballSizePx / 2f).roundToInt(),
                        y = (currentY - ballSizePx / 2f).roundToInt()
                    )
                }
                .graphicsLayer {
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                }
        ) {
            BallView(
                ball = activeBall.ball,
                size = ballSize,
                isSelectedHover = true
            )
        }
    }
}
