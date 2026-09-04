package com.finley.android.merge2048.domain

import androidx.compose.ui.graphics.Color

/**
 * Theme definitions for the game. Each theme provides a complete color palette
 * for all game elements. Themes are unlockable based on game achievements.
 */
sealed class GameTheme(
    val id: String,
    val displayName: String,
    val unlockRequirement: String
) {
    /** All available themes. */
    companion object {
        val all = listOf(Classic, Dark, Neon)
        fun byId(id: String) = all.find { it.id == id } ?: Classic
    }

    abstract val appBackground: Color
    abstract val surface: Color
    abstract val headerText: Color
    abstract val subText: Color
    abstract val scoreBlockBackground: Color
    abstract val scoreLabel: Color
    abstract val buttonBackground: Color
    abstract val buttonLabel: Color
    abstract val tileEmpty: Color

    /** Returns the background color for a given tile value. */
    abstract fun tileBackgroundColor(value: Int): Color

    /** Returns the text color for a given tile value. */
    abstract fun tileTextColor(value: Int): Color

    /** Check if this theme is unlocked based on current stats. */
    fun isUnlocked(gamesPlayed: Int, bestMaxTile: Int): Boolean = when (this) {
        Classic -> true
        Dark -> gamesPlayed >= 5
        Neon -> bestMaxTile >= 1024
    }

    // ---------- Classic Theme ----------
    data object Classic : GameTheme(
        id = "classic",
        displayName = "Classic",
        unlockRequirement = "Always available"
    ) {
        override val appBackground = Color(0xFFFAF8EF)
        override val surface = Color(0xFFFFFFFF)
        override val headerText = Color(0xFF776E65)
        override val subText = Color(0xFF776E65)
        override val scoreBlockBackground = Color(0xFFBBADA0)
        override val scoreLabel = Color(0xFFEEE4DA)
        override val buttonBackground = Color(0xFF8F7A66)
        override val buttonLabel = Color(0xFFF9F6F2)
        override val tileEmpty = Color(0xFFCDC1B4)

        override fun tileBackgroundColor(value: Int) = when {
            value == 0 -> tileEmpty
            value <= 4 -> Color(0xFFEEE4DA)
            value <= 8 -> Color(0xFFEDE0C8)
            value <= 16 -> Color(0xFFF2B179)
            value <= 32 -> Color(0xFFF59563)
            value <= 64 -> Color(0xFFF67C5F)
            value <= 128 -> Color(0xFFEDCF72)
            value <= 256 -> Color(0xFFEDCC61)
            value <= 512 -> Color(0xFFEDC850)
            value <= 1024 -> Color(0xFFEDC53F)
            value <= 2048 -> Color(0xFFEDC22E)
            else -> Color(0xFF3C3A32)
        }

        override fun tileTextColor(value: Int) = when {
            value <= 4 -> Color(0xFF776E65)
            else -> Color(0xFFF9F6F2)
        }
    }

    // ---------- Dark Theme ----------
    data object Dark : GameTheme(
        id = "dark",
        displayName = "Midnight",
        unlockRequirement = "Play 5 games"
    ) {
        override val appBackground = Color(0xFF1A1A2E)
        override val surface = Color(0xFF16213E)
        override val headerText = Color(0xFFEAEAEA)
        override val subText = Color(0xFF8B8FA3)
        override val scoreBlockBackground = Color(0xFF0F3460)
        override val scoreLabel = Color(0xFF53748D)
        override val buttonBackground = Color(0xFF533483)
        override val buttonLabel = Color(0xFFEAEAEA)
        override val tileEmpty = Color(0xFF1A1A3E)

        override fun tileBackgroundColor(value: Int) = when {
            value == 0 -> tileEmpty
            value <= 4 -> Color(0xFF2C3E6D)
            value <= 8 -> Color(0xFF345B8F)
            value <= 16 -> Color(0xFF2980B9)
            value <= 32 -> Color(0xFF1ABC9C)
            value <= 64 -> Color(0xFF27AE60)
            value <= 128 -> Color(0xFFF39C12)
            value <= 256 -> Color(0xFFE67E22)
            value <= 512 -> Color(0xFFE74C3C)
            value <= 1024 -> Color(0xFF9B59B6)
            value <= 2048 -> Color(0xFFE94235)
            else -> Color(0xFFECF0F1)
        }

        override fun tileTextColor(value: Int) = when {
            value <= 4 -> Color(0xFFBDC3C7)
            else -> Color(0xFFECF0F1)
        }
    }

    // ---------- Neon Theme ----------
    data object Neon : GameTheme(
        id = "neon",
        displayName = "Neon",
        unlockRequirement = "Reach 1024 tile"
    ) {
        override val appBackground = Color(0xFF0D0D0D)
        override val surface = Color(0xFF1A1A1A)
        override val headerText = Color(0xFF00FF88)
        override val subText = Color(0xFF00CC6A)
        override val scoreBlockBackground = Color(0xFF1A1A2E)
        override val scoreLabel = Color(0xFF00FF88)
        override val buttonBackground = Color(0xFFFF0080)
        override val buttonLabel = Color(0xFFFFFFFF)
        override val tileEmpty = Color(0xFF1A1A1A)

        override fun tileBackgroundColor(value: Int) = when {
            value == 0 -> tileEmpty
            value <= 4 -> Color(0xFF1A2E1A)
            value <= 8 -> Color(0xFF003300)
            value <= 16 -> Color(0xFF006600)
            value <= 32 -> Color(0xFF009900)
            value <= 64 -> Color(0xFF00CC00)
            value <= 128 -> Color(0xFF00FF00)
            value <= 256 -> Color(0xFF00FFFF)
            value <= 512 -> Color(0xFF0088FF)
            value <= 1024 -> Color(0xFF0044FF)
            value <= 2048 -> Color(0xFFFF00FF)
            else -> Color(0xFFFFFF00)
        }

        override fun tileTextColor(value: Int) = when {
            value <= 4 -> Color(0xFF00FF88)
            value <= 64 -> Color(0xFFFFFFFF)
            else -> Color(0xFF0D0D0D)
        }
    }
}
