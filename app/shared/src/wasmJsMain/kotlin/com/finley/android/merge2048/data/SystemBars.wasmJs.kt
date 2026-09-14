package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable

@Composable
actual fun SyncSystemBars(darkMode: Boolean) {
    // No-op on JS/Wasm; no system bars.
}