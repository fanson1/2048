package com.finley.android.merge2048.ui.navigation

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
import com.finley.android.merge2048.domain.DailyChallenge
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.domain.GameTheme
import com.finley.android.merge2048.domain.LifetimeStats
import com.finley.android.merge2048.domain.UserPreferences
import com.finley.android.merge2048.data.setAppLocale
import com.finley.android.merge2048.presentation.GameViewModel
import com.finley.android.merge2048.ui.AchievementToast
import com.finley.android.merge2048.ui.HistoryScreen
import com.finley.android.merge2048.ui.SettingsScreen
import com.finley.android.merge2048.ui.TutorialOverlay
import com.finley.android.merge2048.ui.screen.GameContent
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.ui.theme.ProvideGameColors

/**
 * Type-safe navigation destination. Each sealed class variant represents a
 * distinct screen in the application. This replaces the previous enum-based
 * approach to provide better extensibility and compile-time safety.
 */
sealed class Screen {
    data object Game : Screen()
    data object Settings : Screen()
    data object History : Screen()
}

/**
 * Root navigation composable. Manages screen transitions between the main game,
 * settings, and history screens. Also hosts the global achievement toast and
 * first-launch tutorial overlays.
 */
@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val records by viewModel.history.collectAsState()
    var screen by remember { mutableStateOf<Screen>(Screen.Game) }
    val dailySeed = remember { DailyChallenge.seedAt(kotlin.time.Clock.System.now().toEpochMilliseconds()) }

    ProvideGameColors(darkMode = prefs.darkMode, themeId = prefs.themeId) {
        LaunchedEffect(prefs.darkMode, prefs.themeId) {
            GameColors.apply(GameTheme.byId(prefs.themeId))
        }

        // Apply the persisted locale on first composition and whenever it changes.
        LaunchedEffect(prefs.language) {
            setAppLocale(prefs.language)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            when (screen) {
                is Screen.Game -> {
                    GameContent(
                        state = state,
                        onIntent = viewModel::onIntent,
                        onOpenSettings = { screen = Screen.Settings },
                        onOpenHistory = { screen = Screen.History },
                        onDismissAchievement = { id ->
                            viewModel.onIntent(GameIntent.ConsumeAchievement(id))
                        },
                        dailyChallengeSeed = dailySeed
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        prefs = prefs,
                        onUpdate = { newPrefs -> viewModel.updatePreference { newPrefs } },
                        onBack = { screen = Screen.Game }
                    )
                }
                is Screen.History -> {
                    HistoryScreen(
                        stats = LifetimeStats.from(records),
                        records = records,
                        onBack = { screen = Screen.Game }
                    )
                }
            }

            AchievementToast(
                achievementId = state.pendingAchievementId,
                onConsume = {
                    state.pendingAchievementId?.let {
                        viewModel.onIntent(GameIntent.ConsumeAchievement(it))
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
            )

            TutorialOverlay(
                visible = !prefs.hasSeenTutorial,
                onDismiss = {
                    viewModel.updatePreference { it.copy(hasSeenTutorial = true) }
                }
            )
        }
    }
}
