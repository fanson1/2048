package com.finley.android.merge2048

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.finley.android.merge2048.data.ProvideAppContext
import com.finley.android.merge2048.presentation.GameViewModel
import com.finley.android.merge2048.presentation.rememberGameViewModel

@Composable
@Preview
fun App() {
    ProvideAppContext()
    val viewModel: GameViewModel = rememberGameViewModel()
    AppNavigation(viewModel)
}