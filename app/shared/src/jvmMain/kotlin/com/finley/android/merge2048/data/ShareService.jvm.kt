package com.finley.android.merge2048.data

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/** JVM/Desktop share: copies the message to the system clipboard. */
class JvmShareService : ShareService {
    override val supportsSystemShare: Boolean = false

    override fun share(message: String): String? {
        return try {
            val selection = StringSelection(message)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
            message
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
actual fun createShareService(): ShareService = JvmShareService()