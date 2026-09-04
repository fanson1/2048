package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ProvideAppContext() {
    initPlatformStorage(LocalContext.current.applicationContext)
}