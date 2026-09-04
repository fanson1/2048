package com.finley.android.merge2048

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.finley.android.merge2048.domain.GameTheme

object GameColors {
    internal var currentTheme: GameTheme = GameTheme.Classic

    val AppBackground get() = currentTheme.appBackground
    val BoardBackground get() = Color(0xFFBBADA0) // fixed for all themes
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
    val SettingsBackground get() = currentTheme.appBackground

    // Tile accent colors (fixed, same in light & dark)
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

    /** Apply a theme to the global color palette. */
    fun apply(theme: GameTheme) {
        currentTheme = theme
    }

    /** Legacy: apply based on dark-mode flag (uses Classic or Dark theme). */
    fun apply(darkMode: Boolean) {
        currentTheme = if (darkMode) GameTheme.Dark else GameTheme.Classic
    }
}

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

fun tileBackgroundColor(value: Int): Color {
    return GameColors.currentTheme.tileBackgroundColor(value)
}

fun tileTextColor(value: Int): Color {
    return GameColors.currentTheme.tileTextColor(value)
}

fun tileFontSize(value: Int): Int = when {
    value >= 1000 -> 20
    value >= 100 -> 26
    else -> 36
}
