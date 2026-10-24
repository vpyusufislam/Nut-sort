package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.model.AppScreen
import com.example.ui.DifficultySelectionScreen
import com.example.ui.NutSortGameScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val gameViewModel: GameViewModel = viewModel()
        val currentScreen by gameViewModel.currentScreen.collectAsState()

        AnimatedContent(
          targetState = currentScreen,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "screen_transition"
        ) { screen ->
          when (screen) {
            AppScreen.DIFFICULTY_SELECTION -> {
              DifficultySelectionScreen(viewModel = gameViewModel)
            }
            AppScreen.GAME -> {
              BackHandler {
                gameViewModel.returnToDifficultyMenu()
              }
              NutSortGameScreen(
                viewModel = gameViewModel,
                onBackToMenu = { gameViewModel.returnToDifficultyMenu() }
              )
            }
          }
        }
      }
    }
  }
}
