package com.finley.android.merge2048.data

/**
 * Platform-specific locale helper. On Android, delegates to
 * [AppCompatDelegate.setApplicationLocales] so that all existing
 * [stringResource] calls automatically pick up the new locale without any
 * changes to UI code. On other platforms this is a no-op for now since
 * Compose Multiplatform resources follow the OS locale.
 *
 * @param tag BCP 47 language tag (e.g. "en", "zh", "system").
 */
expect fun setAppLocale(tag: String)
