package com.finley.android.merge2048.data

/**
 * JVM/Desktop implementation. Compose Desktop does not provide a runtime locale
 * override API equivalent to Android's [AppCompatDelegate]. The preference is
 * persisted so a future implementation can honour it (e.g. by reloading
 * resources with the target [Locale]).
 *
 * @param tag BCP 47 language tag (e.g. "en", "zh") or "system".
 */
actual fun setAppLocale(tag: String) {
    // No-op on Desktop for now.
}
