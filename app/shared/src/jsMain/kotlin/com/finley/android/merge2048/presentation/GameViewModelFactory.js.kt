package com.finley.android.merge2048.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGameViewModel(): GameViewModel = remember { GameViewModel() }