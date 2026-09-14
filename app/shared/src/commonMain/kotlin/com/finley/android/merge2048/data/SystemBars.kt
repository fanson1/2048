package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable

/**
 * Keeps the platform system bar icon/appearance style in sync with the app's
 * own light/dark theme (which is independent of the system theme). On Android
 * this controls status bar / navigation bar icon legibility; a no-op elsewhere.
 */
@Composable
expect fun SyncSystemBars(darkMode: Boolean)