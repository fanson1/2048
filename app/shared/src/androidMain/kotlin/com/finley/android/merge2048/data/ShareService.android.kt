package com.finley.android.merge2048.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable

/** Android share: launches the system share sheet with the score message. */
class AndroidShareService(
    private val context: Context
) : ShareService {
    override val supportsSystemShare: Boolean = true

    override fun share(message: String): String? {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            val chooser = Intent.createChooser(sendIntent, null)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            null
        } catch (e: Exception) {
            // Fall back to clipboard if no share target is available.
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("2048 score", message))
                message
            } catch (e2: Exception) {
                null
            }
        }
    }
}

@Composable
actual fun createShareService(): ShareService {
    val context = getAppContext() ?: return NoOpShareService()
    return AndroidShareService(context)
}