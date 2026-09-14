package com.finley.android.merge2048.data

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * On Android, sync the status bar and navigation bar icon appearance to the
 * app's own dark mode setting (light icons in dark mode, dark icons in light mode).
 * The default `enableEdgeToEdge()` follows the *system* theme, which can disagree
 * with the in-app toggle.
 */
@Composable
actual fun SyncSystemBars(darkMode: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkMode
            controller.isAppearanceLightNavigationBars = !darkMode
        }
    }
}
