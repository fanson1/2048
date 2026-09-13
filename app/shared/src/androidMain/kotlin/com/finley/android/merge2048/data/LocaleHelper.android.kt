package com.finley.android.merge2048.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Android implementation using [AppCompatDelegate.setApplicationLocales].
 * This is the Google-recommended approach for in-app locale switching. It
 * triggers an activity recreation and all [stringResource] calls automatically
 * resolve to the correct locale.
 *
 * @param tag BCP 47 language tag (e.g. "en", "zh") or "system" to reset.
 */
actual fun setAppLocale(tag: String) {
    val locales = if (tag == "system" || tag.isBlank()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }
    AppCompatDelegate.setApplicationLocales(locales)
}
