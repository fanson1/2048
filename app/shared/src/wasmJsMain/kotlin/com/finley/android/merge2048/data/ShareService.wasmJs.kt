package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable
import org.w3c.dom.Window
import kotlinx.browser.window

/** Wasm/JS share: copies the message to the clipboard. */
class WasmShareService : ShareService {
    override val supportsSystemShare: Boolean = false

    override fun share(message: String): String? {
        return try {
            window.navigator.clipboard?.writeText(message)
            message
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
actual fun createShareService(): ShareService = WasmShareService()
