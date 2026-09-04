package com.finley.android.merge2048

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.domain.LifetimeStats
import com.finley.android.merge2048.domain.UserPreferences
import com.finley.android.merge2048.presentation.GameViewModel
import com.finley.android.merge2048.ui.AchievementToast
import com.finley.android.merge2048.ui.HistoryScreen
import com.finley.android.merge2048.ui.SettingsScreen
import com.finley.android.merge2048.ui.TutorialOverlay

enum class GameScreen { GAME, SETTINGS, HISTORY }

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val records by viewModel.history.collectAsState()
    var screen by remember { mutableStateOf(GameScreen.GAME) }

    // Apply dark mode whenever prefs change.
    ProvideGameColors(darkMode = prefs.darkMode) {
        // Re-apply on change.
        LaunchedEffect(prefs.darkMode) { GameColors.apply(prefs.darkMode) }

        Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                GameScreen.GAME -> {
                    GameContent(
                        state = state,
                        onIntent = viewModel::onIntent,
                        onOpenSettings = { screen = GameScreen.SETTINGS },
                        onOpenHistory = { screen = GameScreen.HISTORY },
                        onDismissAchievement = { id ->
                            viewModel.onIntent(GameIntent.ConsumeAchievement(id))
                        }
                    )
                }
                GameScreen.SETTINGS -> {
                    SettingsScreen(
                        prefs = prefs,
                        onUpdate = { newPrefs -> viewModel.updatePreference { newPrefs } },
                        onBack = { screen = GameScreen.GAME }
                    )
                }
                GameScreen.HISTORY -> {
                    HistoryScreen(
                        stats = LifetimeStats.from(records),
                        records = records,
                        onBack = { screen = GameScreen.GAME }
                    )
                }
            }

            // Achievement toast overlay (shown over any screen)
            AchievementToast(
                achievementId = state.pendingAchievementId,
                onConsume = {
                    state.pendingAchievementId?.let {
                        viewModel.onIntent(GameIntent.ConsumeAchievement(it))
                    }
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            )

            // First-launch tutorial overlay
            TutorialOverlay(
                visible = !prefs.hasSeenTutorial,
                onDismiss = {
                    viewModel.updatePreference { it.copy(hasSeenTutorial = true) }
                }
            )
        }
    }
}