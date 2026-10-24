package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameState

@Composable
fun GameControls(
    gameState: GameState,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onAddRod: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canUndo = gameState.moveHistory.isNotEmpty() && gameState.activeMovingBall == null
    val canAdd = gameState.extraTubesAdded < 2 && gameState.activeMovingBall == null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Undo Button (Dark/Slate circular icon)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                enabled = canUndo,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 32.dp),
                onClick = onUndo
            )
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (canUndo) Color(0xFF282444) else Color(0xFF1E1B33)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (canUndo) Color(0xFF4A4470) else Color(0xFF2A2742),
                        shape = CircleShape
                    )
                    .testTag("undo_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo move",
                    tint = if (canUndo) Color(0xFFB4ACDE) else Color(0xFF4E496D),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Undo",
                color = if (canUndo) Color(0xFFB4ACDE) else Color(0xFF5E587E),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 2. Hint Button (Golden circular button with count badge)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 36.dp),
                onClick = onHint
            )
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFE082),
                                    Color(0xFFFFB300),
                                    Color(0xFFFFA000)
                                )
                            )
                        )
                        .border(width = 2.dp, color = Color(0xFFFFF8E1), shape = CircleShape)
                        .testTag("hint_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = "Hint",
                        tint = Color(0xFF4E2C00),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Badge count indicator (like "18" in screenshot)
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF241C3D))
                        .border(1.dp, Color(0xFFFFD54F), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "∞",
                        color = Color(0xFFFFE082),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hint",
                color = Color(0xFFFFD54F),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Add Bolt / Tube Button (Allows adding 1 small tube per level)
        val canAdd = gameState.extraTubesAdded < 1 && gameState.availableExtraTubes > 0 && gameState.activeMovingBall == null

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                enabled = gameState.activeMovingBall == null,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 36.dp),
                onClick = onAddRod
            )
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(
                            if (canAdd) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFE082),
                                        Color(0xFFFFB300),
                                        Color(0xFFFFA000)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF3B355A), Color(0xFF231F3A))
                                )
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (canAdd) Color(0xFFFFF8E1) else Color(0xFF443D69),
                            shape = CircleShape
                        )
                        .testTag("add_rod_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add rod",
                            tint = if (canAdd) Color(0xFF4E2C00) else Color(0xFF8C85A8),
                            modifier = Modifier.size(24.dp)
                        )
                        // Mini tube glyph
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (canAdd) Color(0xFF5D3600) else Color(0xFF8C85A8))
                        )
                    }
                }

                // Inventory badge indicator (remaining additional tubes in stock)
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(
                            if (gameState.availableExtraTubes > 0) Color(0xFF1E88E5) else Color(0xFFE53935)
                        )
                        .border(1.dp, Color.White, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${gameState.availableExtraTubes}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add (1-Ball)",
                color = if (canAdd) Color(0xFFFFD54F) else Color(0xFF9E98BF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
