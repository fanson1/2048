package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

/** iOS share: copies the message to the general pasteboard. */
class IosShareService : ShareService {
    override val supportsSystemShare: Boolean = true

    override fun share(message: String): String? {
        UIPasteboard.generalPasteboard.string = message
        return message
    }
}

@Composable
actual fun createShareService(): ShareService = IosShareService()
