package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BallColor
import com.example.model.BallItem

/**
 * Ultra-glossy, 3D glass marble ball with authentic volumetric spherical shading,
 * curved specular gleams, ambient rim lighting, and glass reflections matching the video.
 */
@Composable
fun BallView(
    ball: BallItem,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isSelectedHover: Boolean = false
) {
    // Subtle breathing pulse when hovered above tube
    val infiniteTransition = rememberInfiniteTransition(label = "ball_hover")
    val hoverScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSelectedHover) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f
            val radius = (canvasW.coerceAtMost(canvasH) / 2f) * 0.94f * (if (isSelectedHover) hoverScale else 1.0f)

            if (ball.isHidden) {
                // Mystery ball: Sleek obsidian glass sphere
                val mysteryCenter = Offset(centerX - radius * 0.28f, centerY - radius * 0.28f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF616161),
                            Color(0xFF263238),
                            Color(0xFF0D1317)
                        ),
                        center = mysteryCenter,
                        radius = radius * 1.25f
                    ),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
                // Dark glass border rim
                drawCircle(
                    color = Color(0x66FFFFFF),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.2.dp.toPx())
                )
                // Specular gleam
                drawOval(
                    color = Color(0x99FFFFFF),
                    topLeft = Offset(centerX - radius * 0.52f, centerY - radius * 0.65f),
                    size = Size(radius * 0.60f, radius * 0.35f)
                )
            } else {
                val color = ball.color

                // 1. Soft spherical drop shadow
                drawCircle(
                    color = Color(0x40000000),
                    radius = radius * 0.98f,
                    center = Offset(centerX, centerY + radius * 0.06f)
                )

                // 2. Primary 3D Volumetric Sphere Body (Radial Gradient off-center towards top-left)
                val lightSourceOffset = Offset(
                    x = centerX - radius * 0.28f,
                    y = centerY - radius * 0.32f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.lightShade,
                            color.primaryColor,
                            color.darkShade,
                            color.darkShade.copy(alpha = 0.95f)
                        ),
                        center = lightSourceOffset,
                        radius = radius * 1.35f
                    ),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )

                // 3. Inner Fresnel / Rim shadow for rich glass marble realism
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0x35000000)
                        ),
                        center = Offset(centerX, centerY),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )

                // 4. Secondary bounce reflection along bottom-right edge (authentic glass subsurface scatter)
                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.lightShade.copy(alpha = 0.55f),
                            Color.Transparent
                        ),
                        center = Offset(centerX + radius * 0.45f, centerY + radius * 0.45f),
                        radius = radius * 0.55f
                    ),
                    startAngle = 20f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(centerX - radius * 0.85f, centerY - radius * 0.85f),
                    size = Size(radius * 1.7f, radius * 1.7f),
                    style = Stroke(width = radius * 0.24f)
                )

                // 5. Primary Glass Specular Oval Gleam (Top-Right as in video)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xF5FFFFFF),
                            Color(0x80FFFFFF),
                            Color(0x00FFFFFF)
                        ),
                        center = Offset(centerX + radius * 0.22f, centerY - radius * 0.36f),
                        radius = radius * 0.35f
                    ),
                    topLeft = Offset(centerX + radius * 0.08f, centerY - radius * 0.54f),
                    size = Size(radius * 0.48f, radius * 0.32f)
                )

                // 6. Secondary soft specular glint (Top-Left micro-gleam)
                drawCircle(
                    color = Color(0xC0FFFFFF),
                    radius = radius * 0.08f,
                    center = Offset(centerX - radius * 0.36f, centerY - radius * 0.38f)
                )

                // 7. Crisp glass perimeter rim
                drawCircle(
                    color = Color(0x30FFFFFF),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // Question mark for mystery balls
        if (ball.isHidden) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Hidden Ball",
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}

// Compatibility wrapper for NutView
@Composable
fun NutView(
    nut: BallItem,
    modifier: Modifier = Modifier,
    width: Dp = 48.dp,
    height: Dp = 48.dp,
    isSelectedHover: Boolean = false
) {
    BallView(
        ball = nut,
        modifier = modifier,
        size = width.coerceAtMost(height),
        isSelectedHover = isSelectedHover
    )
}
