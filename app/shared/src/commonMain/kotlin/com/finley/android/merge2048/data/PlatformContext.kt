package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable

/**
 * Composable that hands the host application's context to the platform
 * storage layer. On Android, it also calls [initPlatformStorage] with the
 * application context. On other platforms it is a no-op.
 */
@Composable
expect fun ProvideAppContext()