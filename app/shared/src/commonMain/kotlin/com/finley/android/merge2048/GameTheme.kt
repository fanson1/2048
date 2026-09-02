package com.finley.android.merge2048

import androidx.compose.ui.graphics.Color

object GameColors {
    // Backgrounds
    val AppBackground = Color(0xFFFAF8EF)
    val BoardBackground = Color(0xFFBBADA0)
    val HeaderText = Color(0xFF776E65)
    val SubText = Color(0xFF8F7A66)
    val ButtonBackground = Color(0xFF8F7A66)

    // Score blocks
    val ScoreBlockBackground = Color(0xFFB5A695)

    // Overlay

    val OverlayScrim = Color(0x99FAF8EF)

    // Tiles
    val TileEmpty = Color(0xFFCDC1B4)
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

    // Text colors
    val TextDark = Color(0xFF776E65)
    val TextLight = Color(0xFFF9F6F2)
    val ScoreLabel = Color(0xFFEEE4DA)
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