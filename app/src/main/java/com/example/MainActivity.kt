package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.PaceRunnerTheme
import com.example.viewmodel.WorkoutViewModel

enum class AppScreen {
    WORKOUT,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaceRunnerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = DarkBg
                ) {
                    PaceRunnerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PaceRunnerApp(viewModel: WorkoutViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.WORKOUT) }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            AppScreen.WORKOUT -> {
                WorkoutScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                )
            }
            AppScreen.HISTORY -> {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.WORKOUT }
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.WORKOUT }
                )
            }
        }
    }
}
