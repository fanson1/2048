package com.finley.android.merge2048.data

/**
 * iOS implementation. Compose Multiplatform on iOS follows the system locale
 * by default. A future implementation can override the locale by updating
 * [NSUserDefaults] AppleLanguages and restarting the app.
 *
 * @param tag BCP 47 language tag (e.g. "en", "zh") or "system".
 */
actual fun setAppLocale(tag: String) {
    // No-op on iOS for now.
}
