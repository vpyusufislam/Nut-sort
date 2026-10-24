package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tube
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Authentic Glass Laboratory Test Tube matching the video:
 * - Transparent glass walls with specular reflections
 * - U-shaped rounded bottom
 * - Smooth open top lip
 * - Stacked spherical glass marbles inside
 * - Top ball lifts out of tube when selected
 * - Confetti streamer burst on completion
 */
@Composable
fun TestTubeView(
    tube: Tube,
    isSelected: Boolean,
    isHintSource: Boolean,
    isHintTarget: Boolean,
    ballSize: Dp,
    capacity: Int = 4,
    isTopBallInFlight: Boolean = false,
    onTubeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lift progress for the selected top ball: 0f = in slot, 1f = fully hovering above mouth
    val liftProgress = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            // Smoothly lift ball up through the top open mouth of the tube
            liftProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
            )
        } else {
            liftProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            )
        }
    }

    // Hint pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "tube_hint")
    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    // Tube dimensions: Width slightly wider than ball, height accommodates capacity balls + clearance
    val actualCapacity = tube.capacity
    val tubeWidth = ballSize + 10.dp
    val tubeHeight = ballSize * actualCapacity + 28.dp
    val tubeBoxHeight = tubeHeight + ballSize + 20.dp

    Box(
        modifier = modifier
            .width(tubeWidth + 8.dp)
            .height(tubeBoxHeight)
            .testTag("tube_${tube.id}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 32.dp),
                onClick = onTubeClick
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Confetti celebration bursts upward when tube is completed!
        if (tube.isCompleted) {
            TubeConfettiCelebration(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
            )
        }

        // Small tube 1-ball capacity indicator badge
        if (tube.isSmallTube) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2979FF))
                    .border(0.8.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = "1-BALL",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Glass Test Tube Body Canvas
        Canvas(
            modifier = Modifier
                .width(tubeWidth)
                .height(tubeHeight)
        ) {
            drawGlassTestTube(
                width = size.width,
                height = size.height,
                isSelected = isSelected,
                isHintSource = isHintSource,
                isHintTarget = isHintTarget,
                hintAlpha = hintAlpha,
                isCompleted = tube.isCompleted,
                completedColor = tube.balls.firstOrNull()?.color?.primaryColor
            )
        }

        // Stacked Glass Marble Balls inside the test tube
        val displayBalls = if (isTopBallInFlight && tube.balls.isNotEmpty()) {
            tube.balls.dropLast(1)
        } else {
            tube.balls
        }

        val bottomPadding = 8.dp

        displayBalls.forEachIndexed { slotIndex, ball ->
            val isTopBall = slotIndex == tube.balls.size - 1
            val yOffsetFromBottom = bottomPadding + (ballSize * slotIndex)
            // When fully lifted (liftProgress = 1f), the ball's bottom clears the tube rim (tubeHeight) plus 8.dp
            val targetHoverBottom = tubeHeight + 8.dp
            val requiredLift = targetHoverBottom - yOffsetFromBottom
            val extraY = if (isTopBall) requiredLift * liftProgress.value else 0.dp

            BallView(
                ball = ball,
                size = ballSize,
                isSelectedHover = isTopBall && isSelected && liftProgress.value > 0.8f,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = -(yOffsetFromBottom + extraY).toPx().roundToInt()
                        )
                    }
            )
        }
    }
}

/**
 * Draws realistic transparent test-tube glass:
 * - Rounded U-bottom
 * - Flanged / rounded open top rim
 * - Specular vertical glass reflection
 * - Bottom glass curvature shine
 * - Selection / Hint highlights
 */
private fun DrawScope.drawGlassTestTube(
    width: Float,
    height: Float,
    isSelected: Boolean,
    isHintSource: Boolean,
    isHintTarget: Boolean,
    hintAlpha: Float,
    isCompleted: Boolean,
    completedColor: Color?
) {
    val cornerRadius = width / 2f
    val wallThickness = 2.4.dp.toPx()
    val rimOverhang = 2.dp.toPx()

    // 1. Completed Tube Subtle Interior Glow
    if (isCompleted && completedColor != null) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    completedColor.copy(alpha = 0.20f),
                    completedColor.copy(alpha = 0.05f)
                )
            ),
            topLeft = Offset(0f, 0f),
            size = Size(width, height),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
    }

    // 2. Translucent Glass Tube Interior Tint
    drawRoundRect(
        color = Color(0x0EFFFFFF),
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // 3. Main Glass Wall Outline (U-bottom, open top)
    val glassPath = Path().apply {
        // Start top-left lip
        moveTo(-rimOverhang, 0f)
        lineTo(wallThickness, 0f)
        lineTo(wallThickness, height - cornerRadius)
        // Inner U-bottom curve
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = wallThickness,
                top = height - 2 * cornerRadius + wallThickness,
                right = width - wallThickness,
                bottom = height - wallThickness
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        // Up right wall
        lineTo(width - wallThickness, 0f)
        lineTo(width + rimOverhang, 0f)
    }

    // Outer Glass Body Path with U-curve at bottom and open at top
    val outerPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, height - cornerRadius)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = height - 2 * cornerRadius,
                right = width,
                bottom = height
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        lineTo(width, 0f)
    }

    // Draw Glass Outline Stroke
    val strokeColor = when {
        isSelected -> Color(0xFF00E5FF)
        isHintSource -> Color(0xFFFFD600).copy(alpha = hintAlpha)
        isHintTarget -> Color(0xFF00E676).copy(alpha = hintAlpha)
        isCompleted && completedColor != null -> completedColor.copy(alpha = 0.9f)
        else -> Color(0x40FFFFFF)
    }

    drawPath(
        path = outerPath,
        color = strokeColor,
        style = Stroke(
            width = if (isSelected || isHintSource || isHintTarget) 3.dp.toPx() else wallThickness
        )
    )

    // 4. Rounded Lip / Flange at top mouth of the test tube
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(-rimOverhang, -1.dp.toPx()),
        size = Size(width + 2 * rimOverhang, 3.5.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // 5. Specular Vertical Glass Reflection along left side
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0x60FFFFFF),
                Color(0x25FFFFFF),
                Color(0x05FFFFFF)
            ),
            startY = 4.dp.toPx(),
            endY = height - cornerRadius
        ),
        start = Offset(4.5.dp.toPx(), 6.dp.toPx()),
        end = Offset(4.5.dp.toPx(), height - cornerRadius),
        strokeWidth = 1.8.dp.toPx()
    )

    // 6. U-bottom curvature specular gleam
    drawArc(
        color = Color(0x35FFFFFF),
        startAngle = 130f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(4.dp.toPx(), height - 2 * cornerRadius + 4.dp.toPx()),
        size = Size(width - 8.dp.toPx(), 2 * cornerRadius - 8.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

/**
 * Celebratory confetti streamers burst shooting up out of completed tube
 * (matching 00:09, 00:24, 00:29, 00:34 in the video!)
 */
@Composable
private fun TubeConfettiCelebration(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearEasing)
        )
    }

    val p = progress.value
    if (p < 1f) {
        val particles = remember {
            val colors = listOf(
                Color(0xFFFF1744),
                Color(0xFFFFD600),
                Color(0xFF00E676),
                Color(0xFF00E5FF),
                Color(0xFFAA00FF),
                Color(0xFFFF6D00),
                Color.White
            )
            List(24) { i ->
                val angle = -70f - (i * 4f) + Random.nextFloat() * 10f
                val speed = 80f + Random.nextFloat() * 120f
                val color = colors[i % colors.size]
                val size = 4f + Random.nextFloat() * 5f
                val isRibbon = i % 3 == 0
                ConfettiParticle(
                    angleRad = Math.toRadians(angle.toDouble()).toFloat(),
                    speed = speed,
                    color = color,
                    size = size,
                    isRibbon = isRibbon
                )
            }
        }

        Canvas(
            modifier = modifier
                .size(width = 120.dp, height = 180.dp)
                .graphicsLayer { alpha = (1f - p * 0.8f).coerceIn(0f, 1f) }
        ) {
            val originX = size.width / 2f
            val originY = size.height - 20f

            particles.forEach { pt ->
                val dist = pt.speed * p
                val gravity = 90f * p * p
                val curX = originX + cos(pt.angleRad) * dist
                val curY = originY + sin(pt.angleRad) * dist + gravity

                if (pt.isRibbon) {
                    // Ribbon streamer strip
                    drawLine(
                        color = pt.color,
                        start = Offset(curX, curY),
                        end = Offset(curX + cos(pt.angleRad) * 12f, curY + sin(pt.angleRad) * 12f),
                        strokeWidth = 2.5f
                    )
                } else {
                    // Confetti flake
                    drawCircle(
                        color = pt.color,
                        radius = pt.size * (1f - p * 0.3f),
                        center = Offset(curX, curY)
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val angleRad: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val isRibbon: Boolean
)

// Compatibility wrapper for BoltRodView
@Composable
fun BoltRodView(
    rod: Tube,
    isSelected: Boolean,
    isHintSource: Boolean,
    isHintTarget: Boolean,
    capacity: Int,
    nutWidth: Dp,
    nutHeight: Dp,
    isTopNutInFlight: Boolean,
    onRodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TestTubeView(
        tube = rod,
        isSelected = isSelected,
        isHintSource = isHintSource,
        isHintTarget = isHintTarget,
        ballSize = nutWidth.coerceAtMost(nutHeight * 2f).coerceIn(40.dp, 56.dp),
        capacity = capacity,
        isTopBallInFlight = isTopNutInFlight,
        onTubeClick = onRodClick,
        modifier = modifier
    )
}
