package com.finley.android.merge2048.data

/**
 * JS implementation. Browser locale is determined by the user's OS/browser
 * settings and cannot be overridden at runtime without a page reload.
 *
 * @param tag BCP 47 language tag (e.g. "en", "zh") or "system".
 */
actual fun setAppLocale(tag: String) {
    // No-op on JS for now.
}
