package com.finley.android.merge2048

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.finley.android.merge2048.data.ProvideAppContext
import com.finley.android.merge2048.data.SyncSystemBars
import com.finley.android.merge2048.presentation.GameViewModel
import com.finley.android.merge2048.presentation.rememberGameViewModel
import com.finley.android.merge2048.ui.navigation.AppNavigation

/**
 * Application entry-point composable. Provides platform context and wires the
 * root [AppNavigation] to the [GameViewModel].
 */
@Composable
@Preview
fun App() {
    ProvideAppContext()
    val viewModel: GameViewModel = rememberGameViewModel()
    val prefs by viewModel.preferences.collectAsState()
    SyncSystemBars(darkMode = prefs.darkMode)
    AppNavigation(viewModel)
}

