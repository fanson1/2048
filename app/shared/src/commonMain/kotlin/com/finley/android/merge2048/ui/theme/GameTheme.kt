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
 *
 * When dark mode is enabled (via user preference or system setting), the UI
 * chrome colours (backgrounds, text, buttons) are overridden with dark
 * variants while tile colours remain from the selected [GameTheme].
 */
object GameColors {
    // Backed by Compose state so every composable reading the palette below
    // subscribes to theme/dark-mode changes and recomposes automatically.
    internal var currentTheme: GameTheme by mutableStateOf(GameTheme.Classic)
    private var darkMode: Boolean by mutableStateOf(false)

    // ── Dark-mode override palette ──────────────────────────────────────
    private val DarkAppBackground   = Color(0xFF121212)
    private val DarkSurface         = Color(0xFF1E1E1E)
    private val DarkHeaderText      = Color(0xFFE0E0E0)
    private val DarkSubText         = Color(0xFF9E9E9E)
    private val DarkScoreBlock      = Color(0xFF2C2C2C)
    private val DarkScoreLabel      = Color(0xFFBDBDBD)
    // Mid-tone: must work both as a button fill (contrast with light label)
    // and as accent/selected-text on the dark background.
    private val DarkButtonBackground= Color(0xFF6B7280)
    private val DarkButtonLabel     = Color(0xFFF3F4F6)
    private val DarkTileEmpty       = Color(0xFF2A2A2A)
    private val DarkBoardBackground = Color(0xFF3C3C3C)

    // ── Light-mode defaults (from theme) ────────────────────────────────
    val AppBackground   get() = if (darkMode) DarkAppBackground   else currentTheme.appBackground
    val BoardBackground get() = if (darkMode) DarkBoardBackground else Color(0xFFBBADA0)
    val HeaderText      get() = if (darkMode) DarkHeaderText      else currentTheme.headerText
    val SubText         get() = if (darkMode) DarkSubText         else currentTheme.subText
    val ButtonBackground get() = if (darkMode) DarkButtonBackground else currentTheme.buttonBackground
    val ScoreBlockBackground get() = if (darkMode) DarkScoreBlock else currentTheme.scoreBlockBackground
    val OverlayScrim    get() = if (darkMode) DarkAppBackground.copy(alpha = 0.88f)
                                else currentTheme.appBackground.copy(alpha = 0.85f)
    val TileEmpty       get() = if (darkMode) DarkTileEmpty       else currentTheme.tileEmpty
    val TextDark        get() = if (darkMode) DarkHeaderText      else currentTheme.headerText
    val TextLight       get() = Color(0xFFF9F6F2)
    val ScoreLabel      get() = if (darkMode) DarkScoreLabel      else currentTheme.scoreLabel
    val Surface         get() = if (darkMode) DarkSurface         else currentTheme.surface
    val ButtonLabel     get() = if (darkMode) DarkButtonLabel     else currentTheme.buttonLabel
    val SettingsBackground get() = AppBackground

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

    /** Apply a theme and dark-mode flag to the global colour palette. */
    fun apply(theme: GameTheme, darkMode: Boolean = false) {
        currentTheme = theme
        this.darkMode = darkMode
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
    val isDark = darkMode || isSystemInDarkTheme()
    SideEffect { GameColors.apply(theme, isDark) }
    CompositionLocalProvider(
        LocalGameDark provides isDark,
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
