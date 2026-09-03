package com.finley.android.merge2048.presentation

import androidx.compose.runtime.Composable

/**
 * Platform-aware retrieval of the [GameViewModel].
 *
 * Android uses the platform [androidx.lifecycle.viewmodel.compose.viewModel] so the
 * instance survives configuration changes. Other targets have no lifecycle owner that
 * can build the ViewModel, so they fall back to constructing one directly.
 */
@Composable
expect fun rememberGameViewModel(): GameViewModel