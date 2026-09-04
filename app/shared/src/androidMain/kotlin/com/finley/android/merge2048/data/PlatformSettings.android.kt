package com.finley.android.merge2048.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private const val PREF_NAME = "merge2048_prefs"

private lateinit var appContext: Context

internal actual fun initPlatformStorage(context: Any) {
    appContext = (context as Context).applicationContext
}

actual fun createSettings(): Settings {
    check(::appContext.isInitialized) {
        "Android context not initialised. Call initPlatformStorage() in your App composable."
    }
    return SharedPreferencesSettings(appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE))
}