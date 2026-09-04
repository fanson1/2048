package com.finley.android.merge2048.data

import com.russhwolf.settings.Settings

/** No-op on non-Android targets. */
internal actual fun initPlatformStorage(context: Any) {
}

actual fun createSettings(): Settings = Settings()