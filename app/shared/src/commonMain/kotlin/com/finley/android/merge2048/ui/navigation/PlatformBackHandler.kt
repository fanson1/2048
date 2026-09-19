package com.finley.android.merge2048.ui.navigation

import androidx.compose.runtime.Composable

/**
 * Registers a handler for the platform's system back affordance (Android
 * hardware key / gesture, and Esc on desktop). When [enabled] is false the
 * default system behavior is kept. Non-desktop, non-Android platforms have no
 * system back affordance and this is a no-op there.
 */
@Composable
expect fun PlatformBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
)