package com.finley.android.merge2048.data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

/** No-op on iOS. */
internal actual fun initPlatformStorage(context: Any) {
}

actual fun createSettings(): Settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)