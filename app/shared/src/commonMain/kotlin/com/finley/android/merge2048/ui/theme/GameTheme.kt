package com.finley.android.merge2048.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.finley.android.merge2048.domain.GameTheme

/**
 * Centralised colour palette for the game UI. Each property delegates to the
 * active [GameTheme] so the entire UI updates when the user switches themes.
 *
 * The mutable singleton is updated via [ProvideGameColors] at the top of the
 * composition tree. Composable helpers [tileBackgroundColor] and
 * [tileTextColor] read from the same source so tile rendering is always
 * consistent with the active theme.
 */
object GameColors {
    internal var currentTheme: GameTheme = GameTheme.Classic

    val AppBackground get() = currentTheme.appBackground
    val BoardBackground get() = Color(0xFFBBADA0)
    val HeaderText get() = currentTheme.headerText
    val SubText get() = currentTheme.subText
    val ButtonBackground get() = currentTheme.buttonBackground
    val ScoreBlockBackground get() = currentTheme.scoreBlockBackground
    val OverlayScrim get() = currentTheme.appBackground.copy(alpha = 0.85f)
    val TileEmpty get() = currentTheme.tileEmpty
    val TextDark get() = currentTheme.headerText
    val TextLight get() = Color(0xFFF9F6F2)
    val ScoreLabel get() = currentTheme.scoreLabel
    val Surface get() = currentTheme.surface
    val ButtonLabel get() = currentTheme.buttonLabel
    val SettingsBackground get() = currentTheme.appBackground

    /** Tile accent colours that remain constant across all themes. */
    val Tile2 = Color(0xFFEEE4DA)
    val Tile4 = Color(0xFFEDE0C8)
    val Tile8 = Color(0xFFF2B179)
    val Tile16 = Color(0xFFF59563)
    val Tile32 = Color(0xFFF67C5F)
    val Tile64 = Color(0xFFF65E3B)
    val Tile128 = Color(0xFFEDCF72)
    val Tile256 = Color(0xFFEDCC61)
    val Tile512 = Color(0xFFEDC850)
    val Tile1024 = Color(0xFFEDC53F)
    val Tile2048 = Color(0xFFEDC22E)
    val TileSuper = Color(0xFF3C3A32)

    /** Apply a theme to the global colour palette. */
    fun apply(theme: GameTheme) {
        currentTheme = theme
    }
}

/**
 * Provides the active [GameTheme] and dark-mode flag via [CompositionLocal]s
 * and synchronises the global [GameColors] singleton. Must be called once at
 * the root of the composition tree.
 */
@Composable
fun ProvideGameColors(darkMode: Boolean, themeId: String = "classic", content: @Composable () -> Unit) {
    val theme = GameTheme.byId(themeId)
    SideEffect { GameColors.apply(theme) }
    CompositionLocalProvider(
        LocalGameDark provides (darkMode || isSystemInDarkTheme()),
        LocalGameTheme provides theme
    ) {
        content()
    }
}

val LocalGameDark = staticCompositionLocalOf { false }
val LocalGameTheme = staticCompositionLocalOf { GameTheme.Classic as GameTheme }

/** Returns the background colour for the given tile [value] using the active theme. */
fun tileBackgroundColor(value: Int): Color {
    return GameColors.currentTheme.tileBackgroundColor(value)
}

/** Returns the text colour for the given tile [value] using the active theme. */
fun tileTextColor(value: Int): Color {
    return GameColors.currentTheme.tileTextColor(value)
}

/**
 * Formats an integer score with thousands separators (e.g. 12480 -> "12,480").
 * Grouping is done with ASCII commas so output is consistent across locales.
 */
fun formatScore(value: Int): String {
    if (value in 0..999) return value.toString()
    val digits = value.toString()
    val sb = StringBuilder(digits.length + digits.length / 3 + 1)
    val firstGroup = digits.length % 3
    for (i in digits.indices) {
        if (i > 0 && (i - firstGroup) % 3 == 0) sb.append(',')
        sb.append(digits[i])
    }
    return sb.toString()
}

/**
 * Fraction of the tile cell width to use as the tile font size. Larger tiles
 * (4+ digits) need a smaller fraction so the number never overflows the cell,
 * and the value adapts to smaller cells (e.g. 6x6 boards or portrait widths).
 */
fun tileFontFraction(value: Int): Float = when {
    value >= 1000 -> 0.24f
    value >= 100 -> 0.30f
    else -> 0.40f
}
