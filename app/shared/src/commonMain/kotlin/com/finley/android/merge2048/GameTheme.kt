package com.finley.android.merge2048

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

object GameColors {
    private sealed interface Palette {
        val AppBackground: Color; val BoardBackground: Color; val HeaderText: Color
        val SubText: Color; val ButtonBackground: Color; val ScoreBlockBackground: Color
        val OverlayScrim: Color; val TileEmpty: Color; val TextDark: Color
        val TextLight: Color; val ScoreLabel: Color; val Surface: Color; val SettingsBackground: Color
    }

    private object Light : Palette {
        override val AppBackground = Color(0xFFFAF8EF)
        override val BoardBackground = Color(0xFFBBADA0)
        override val HeaderText = Color(0xFF776E65)
        override val SubText = Color(0xFF8F7A66)
        override val ButtonBackground = Color(0xFF8F7A66)
        override val ScoreBlockBackground = Color(0xFFB5A695)
        override val OverlayScrim = Color(0x99FAF8EF)
        override val TileEmpty = Color(0xFFCDC1B4)
        override val TextDark = Color(0xFF776E65)
        override val TextLight = Color(0xFFF9F6F2)
        override val ScoreLabel = Color(0xFFEEE4DA)
        override val Surface = Color(0xFFFFFFFF)
        override val SettingsBackground = Color(0xFFF5F0E8)
    }

    private object Dark : Palette {
        override val AppBackground = Color(0xFF1A1512)
        override val BoardBackground = Color(0xFF3C3830)
        override val HeaderText = Color(0xFFF5F0E8)
        override val SubText = Color(0xFFB8A89A)
        override val ButtonBackground = Color(0xFF8F7A66)
        override val ScoreBlockBackground = Color(0xFF5C5347)
        override val OverlayScrim = Color(0xCC1A1512)
        override val TileEmpty = Color(0xFF4A433B)
        override val TextDark = Color(0xFFF5F0E8)
        override val TextLight = Color(0xFFF9F6F2)
        override val ScoreLabel = Color(0xFF8F7A66)
        override val Surface = Color(0xFF2A2420)
        override val SettingsBackground = Color(0xFF201B17)
    }

    private var current: Palette = Light
    val AppBackground get() = current.AppBackground
    val BoardBackground get() = current.BoardBackground
    val HeaderText get() = current.HeaderText
    val SubText get() = current.SubText
    val ButtonBackground get() = current.ButtonBackground
    val ScoreBlockBackground get() = current.ScoreBlockBackground
    val OverlayScrim get() = current.OverlayScrim
    val TileEmpty get() = current.TileEmpty
    val TextDark get() = current.TextDark
    val TextLight get() = current.TextLight
    val ScoreLabel get() = current.ScoreLabel
    val Surface get() = current.Surface
    val SettingsBackground get() = current.SettingsBackground

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

    /** Push the correct palette according to the dark-mode flag. */
    fun apply(darkMode: Boolean) {
        current = if (darkMode) Dark else Light
    }
}

@Composable
fun ProvideGameColors(darkMode: Boolean, content: @Composable () -> Unit) {
    val effective = darkMode || isSystemInDarkTheme()
    SideEffect { GameColors.apply(effective) }
    CompositionLocalProvider(
        LocalGameDark provides effective
    ) {
        content()
    }
}

val LocalGameDark = staticCompositionLocalOf { false }

fun tileBackgroundColor(value: Int): Color = when (value) {
    0 -> Color.Transparent
    2 -> Color(0xFFEEE4DA)
    4 -> Color(0xFFEDE0C8)
    8 -> Color(0xFFF2B179)
    16 -> Color(0xFFF59563)
    32 -> Color(0xFFF67C5F)
    64 -> Color(0xFFF65E3B)
    128 -> Color(0xFFEDCF72)
    256 -> Color(0xFFEDCC61)
    512 -> Color(0xFFEDC850)
    1024 -> Color(0xFFEDC53F)
    2048 -> Color(0xFFEDC22E)
    else -> Color(0xFF3C3A32)
}

fun tileTextColor(value: Int): Color = when (value) {
    0 -> Color.Transparent
    2 -> Color(0xFF776E65)
    4 -> Color(0xFF776E65)
    8 -> Color(0xFFF08A24)
    16 -> Color(0xFFE96A2A)
    32 -> Color(0xFFE8512E)
    64 -> Color(0xFFD63C1E)
    128 -> Color(0xFFD9A513)
    256 -> Color(0xFFC99700)
    512 -> Color(0xFFB78700)
    1024 -> Color(0xFFD4A017)
    2048 -> Color(0xFFD4A017)
    else -> Color(0xFF3C3A32)
}

fun tileFontSize(value: Int): Int = when {
    value >= 1000 -> 20
    value >= 100 -> 26
    else -> 36
}