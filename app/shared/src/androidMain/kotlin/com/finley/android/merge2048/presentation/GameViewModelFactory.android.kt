package com.finley.android.merge2048.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.finley.android.merge2048.data.GameHistoryRepository
import com.finley.android.merge2048.data.GameRepository
import com.finley.android.merge2048.data.SettingsRepository
import com.finley.android.merge2048.data.createSoundService

@Composable
actual fun rememberGameViewModel(): GameViewModel = viewModel(
    factory = viewModelFactory {
        initializer {
            GameViewModel(
                settingsRepository = SettingsRepository(),
                gameRepository = GameRepository(),
                historyRepository = GameHistoryRepository(),
                soundService = createSoundService()
            )
        }
    }
)