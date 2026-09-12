package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable
import org.w3c.dom.Window
import kotlinx.browser.window

/** JS share: uses the Web Share API when available, else copies to clipboard. */
class JsShareService : ShareService {
    override val supportsSystemShare: Boolean = true

    override fun share(message: String): String? {
        val nav = window.navigator
        val share = nav.asDynamic().share
        if (share != null) {
            return try {
                val promise = nav.asDynamic().share(js("{ text: message }") as Any)
                null
            } catch (e: Exception) {
                clipboardFallback(message)
            }
        }
        return clipboardFallback(message)
    }

    private fun clipboardFallback(message: String): String? {
        return try {
            window.navigator.clipboard?.writeText(message)
            message
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
actual fun createShareService(): ShareService = JsShareService()
