package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.GameViewModel
import com.example.model.BallColor
import com.example.model.BallItem
import com.example.model.Difficulty

/**
 * Main Window / Home Screen displaying the 3 game difficulty levels.
 * Displays saved progress for each mode (e.g. Level 10 for Medium) and allows
 * resuming seamlessly where the player left off.
 */
@Composable
fun DifficultySelectionScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val savedLevels by viewModel.savedLevels.collectAsState()
    var levelSelectDialogFor by remember { mutableStateOf<Difficulty?>(null) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF231A3E),
            Color(0xFF18132F),
            Color(0xFF100C22)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Brand Title with 3D Glass Marble Visuals
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                BallView(
                    ball = BallItem(id = "brand_ball_1", color = BallColor.ORANGE),
                    size = 36.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "BALL SORT",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                BallView(
                    ball = BallItem(id = "brand_ball_2", color = BallColor.BLUE),
                    size = 36.dp
                )
            }

            Text(
                text = "COLOR SORTING PUZZLE",
                color = Color(0xFFB39DDB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            Text(
                text = "SELECT DIFFICULTY",
                color = Color(0xFFECE7F6),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )

            // 1. EASY DIFFICULTY CARD
            DifficultyCard(
                difficulty = Difficulty.EASY,
                savedLevel = savedLevels[Difficulty.EASY] ?: 1,
                accentColor = Color(0xFF00D166),
                badgeText = "BEGINNER FRIENDLY",
                colorsPreview = BallColor.entries.take(4),
                onPlay = { lvl -> viewModel.selectDifficultyAndPlay(Difficulty.EASY, lvl) },
                onOpenLevelSelect = { levelSelectDialogFor = Difficulty.EASY }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. MEDIUM DIFFICULTY CARD
            DifficultyCard(
                difficulty = Difficulty.MEDIUM,
                savedLevel = savedLevels[Difficulty.MEDIUM] ?: 1,
                accentColor = Color(0xFF00C4FF),
                badgeText = "MOST POPULAR",
                colorsPreview = BallColor.entries.take(6),
                onPlay = { lvl -> viewModel.selectDifficultyAndPlay(Difficulty.MEDIUM, lvl) },
                onOpenLevelSelect = { levelSelectDialogFor = Difficulty.MEDIUM }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. HARD DIFFICULTY CARD
            DifficultyCard(
                difficulty = Difficulty.HARD,
                savedLevel = savedLevels[Difficulty.HARD] ?: 1,
                accentColor = Color(0xFFFF2A7A),
                badgeText = "PUZZLE MASTER",
                colorsPreview = BallColor.entries.take(8),
                onPlay = { lvl -> viewModel.selectDifficultyAndPlay(Difficulty.HARD, lvl) },
                onOpenLevelSelect = { levelSelectDialogFor = Difficulty.HARD }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Jump to Level Dialog
        levelSelectDialogFor?.let { diff ->
            val curLevel = savedLevels[diff] ?: 1
            QuickLevelJumpDialog(
                difficulty = diff,
                currentLevel = curLevel,
                onJump = { selectedLvl ->
                    levelSelectDialogFor = null
                    viewModel.selectDifficultyAndPlay(diff, selectedLvl)
                },
                onDismiss = { levelSelectDialogFor = null }
            )
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: Difficulty,
    savedLevel: Int,
    accentColor: Color,
    badgeText: String,
    colorsPreview: List<com.example.model.BallColor>,
    onPlay: (Int) -> Unit,
    onOpenLevelSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(accentColor.copy(alpha = 0.8f), Color(0x33FFFFFF))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("difficulty_card_${difficulty.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF261D45))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Difficulty Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = difficulty.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.18f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${difficulty.numColors} Colors • ${difficulty.capacity} Balls per Tube • 2 Empty Tubes",
                color = Color(0xFFC7BEDE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Palette preview dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorsPreview.forEach { col ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(col.lightShade, col.primaryColor, col.darkShade)
                                )
                            )
                            .border(1.dp, Color(0x66FFFFFF), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Play / Continue Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Big Play / Continue Button
                Button(
                    onClick = { onPlay(savedLevel) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF121020)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("play_button_${difficulty.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (savedLevel > 1) "CONTINUE LEVEL $savedLevel" else "PLAY LEVEL 1",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Level Jump / Select Button
                IconButton(
                    onClick = onOpenLevelSelect,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF352B58))
                        .border(1.dp, Color(0xFF4D3F78), RoundedCornerShape(14.dp))
                        .testTag("level_select_${difficulty.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatListNumbered,
                        contentDescription = "Select Level",
                        tint = Color(0xFFEDE7F6),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLevelJumpDialog(
    difficulty: Difficulty,
    currentLevel: Int,
    onJump: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf(currentLevel.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF261D45),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C3E7C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${difficulty.title} - Select Level",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Current saved level is $currentLevel",
                    color = Color(0xFFB39DDB),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            textInput = input
                        }
                    },
                    label = { Text("Enter Level (1 to 999)", color = Color(0xFFB39DDB)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C4FF),
                        unfocusedBorderColor = Color(0xFF5A498A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFFB39DDB))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lvl = textInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            onJump(lvl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4FF))
                    ) {
                        Text("Go", color = Color(0xFF100C22), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
