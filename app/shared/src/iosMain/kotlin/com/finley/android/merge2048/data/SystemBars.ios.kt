package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable

@Composable
actual fun SyncSystemBars(darkMode: Boolean) {
    // No-op on iOS; handled by the platform's own system chrome.
}
