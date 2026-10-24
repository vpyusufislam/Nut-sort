package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.random.Random

@Composable
fun WinDialog(
    level: Int,
    moves: Int,
    onNextLevel: () -> Unit,
    onReplayLevel: () -> Unit
) {
    val starScale1 = remember { Animatable(0f) }
    val starScale2 = remember { Animatable(0f) }
    val starScale3 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        starScale1.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing))
        starScale1.animateTo(1.0f, tween(150))

        starScale2.animateTo(1.35f, tween(300, easing = FastOutSlowInEasing))
        starScale2.animateTo(1.15f, tween(150))

        starScale3.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing))
        starScale3.animateTo(1.0f, tween(150))
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Confetti canvas animation in background
            ConfettiParticleEffect()

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .testTag("win_dialog"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F183C)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VICTORY!",
                        color = Color(0xFFFFD54F),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Level $level Complete",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Celebratory Stars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Star 1
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier
                                .size(44.dp)
                                .scale(starScale1.value)
                        )
                        // Star 2 (Center, bigger)
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFE082),
                            modifier = Modifier
                                .size(58.dp)
                                .scale(starScale2.value)
                        )
                        // Star 3
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier
                                .size(44.dp)
                                .scale(starScale3.value)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Moves summary card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2B2352))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sorted in $moves moves",
                            color = Color(0xFFE3DEFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next Level Button
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("next_level_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NEXT LEVEL",
                                color = Color(0xFF281800),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF281800)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Replay Level Button
                    OutlinedButton(
                        onClick = onReplayLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("replay_level_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF534A7E))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFB4ACDE),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Replay Level",
                                color = Color(0xFFB4ACDE),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfettiParticleEffect() {
    val confettiAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        confettiAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    val confettiColors = listOf(
        Color(0xFFFF1744),
        Color(0xFFFFEA00),
        Color(0xFF00E676),
        Color(0xFF00E5FF),
        Color(0xFFE040FB),
        Color(0xFFFF9100)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = confettiAnim.value
        val rng = Random(42)
        for (i in 0 until 50) {
            val startX = (rng.nextFloat() * size.width)
            val drift = (rng.nextFloat() - 0.5f) * 160f * t
            val y = ((rng.nextFloat() * size.height + t * size.height) % size.height)
            val color = confettiColors[i % confettiColors.size]
            val pSize = 7.dp.toPx()

            drawRoundRect(
                color = color,
                topLeft = Offset(startX + drift, y),
                size = Size(pSize, pSize * 1.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
