package com.finley.android.merge2048.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
actual fun rememberGameViewModel(): GameViewModel = viewModel()