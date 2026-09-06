package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable

/**
 * Cross-platform share service for sharing the player's score.
 * The default [NoOpShareService] is used on platforms without a share
 * backend; platforms with native sharing implement it.
 */
interface ShareService {
    /** Whether the platform can launch a native share sheet. */
    val supportsSystemShare: Boolean

    /**
     * Share the given message.
     * @return a localized status string describing what happened
     *         (e.g. "Copied to clipboard!") or null if no-op.
     */
    fun share(message: String): String?
}

@Composable
expect fun createShareService(): ShareService

/** Default no-op implementation for platforms without a share backend. */
class NoOpShareService : ShareService {
    override val supportsSystemShare: Boolean = false
    override fun share(message: String): String? = null
}