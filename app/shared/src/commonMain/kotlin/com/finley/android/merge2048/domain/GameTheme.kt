package com.finley.android.merge2048.domain

import org.jetbrains.compose.resources.StringResource
import androidx.compose.ui.graphics.Color
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.theme_classic_name
import merge2048.app.shared.generated.resources.theme_classic_unlock
import merge2048.app.shared.generated.resources.theme_dark_name
import merge2048.app.shared.generated.resources.theme_dark_unlock
import merge2048.app.shared.generated.resources.theme_neon_name
import merge2048.app.shared.generated.resources.theme_neon_unlock
import merge2048.app.shared.generated.resources.theme_pastel_name
import merge2048.app.shared.generated.resources.theme_pastel_unlock
import merge2048.app.shared.generated.resources.theme_retro_name
import merge2048.app.shared.generated.resources.theme_retro_unlock
import merge2048.app.shared.generated.resources.theme_ocean_name
import merge2048.app.shared.generated.resources.theme_ocean_unlock

/**
 * Theme definitions for the game. Each theme provides a complete color palette
 * for all game elements. Themes are unlockable based on game achievements.
 *
 * User-visible fields are backed by i18n resources so the UI can be translated.
 */
sealed class GameTheme(
    val id: String,
    val displayName: StringResource,
    val unlockRequirement: StringResource
) {
    /** All available themes. */
    companion object {
        val all get() = listOf(Classic, Dark, Neon, Pastel, Retro, Ocean)
        fun byId(id: String) = when (id) {
            "dark" -> Dark
            "neon" -> Neon
            "pastel" -> Pastel
            "retro" -> Retro
            "ocean" -> Ocean
            else -> Classic
        }
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
        Pastel -> true
        Retro -> bestMaxTile >= 512
        Ocean -> bestMaxTile >= 2048
    }

    // ---------- Classic Theme ----------
    data object Classic : GameTheme(
        id = "classic",
        displayName = Res.string.theme_classic_name,
        unlockRequirement = Res.string.theme_classic_unlock
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
            value < 8 -> Color(0xFFEDE0C8)
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
        displayName = Res.string.theme_dark_name,
        unlockRequirement = Res.string.theme_dark_unlock
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
        displayName = Res.string.theme_neon_name,
        unlockRequirement = Res.string.theme_neon_unlock
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

    // ---------- Pastel Theme ----------
    data object Pastel : GameTheme(
        id = "pastel",
        displayName = Res.string.theme_pastel_name,
        unlockRequirement = Res.string.theme_pastel_unlock
    ) {
        override val appBackground = Color(0xFFFDF6F0)
        override val surface = Color(0xFFFFFFFF)
        override val headerText = Color(0xFF8B7E74)
        override val subText = Color(0xFFA89B8F)
        override val scoreBlockBackground = Color(0xFFE8DFD8)
        override val scoreLabel = Color(0xFFF5E6E0)
        override val buttonBackground = Color(0xFFD4C4B8)
        override val buttonLabel = Color(0xFF5D544F)
        override val tileEmpty = Color(0xFFE8E0D8)

        override fun tileBackgroundColor(value: Int) = when {
            value == 0 -> tileEmpty
            value <= 4 -> Color(0xFFF5E6E0)
            value <= 8 -> Color(0xFFF0D8C8)
            value <= 16 -> Color(0xFFE8C8B0)
            value <= 32 -> Color(0xFFE0B898)
            value <= 64 -> Color(0xFFD8A880)
            value <= 128 -> Color(0xFFE8D098)
            value <= 256 -> Color(0xFFE0C888)
            value <= 512 -> Color(0xFFD8C078)
            value <= 1024 -> Color(0xFFD0B868)
            value <= 2048 -> Color(0xFFC8B058)
            else -> Color(0xFF8B8070)
        }

        override fun tileTextColor(value: Int) = when {
            value <= 4 -> Color(0xFF8B7E74)
            else -> Color(0xFFFFFFFF)
        }
    }

    // ---------- Retro Theme ----------
    data object Retro : GameTheme(
        id = "retro",
        displayName = Res.string.theme_retro_name,
        unlockRequirement = Res.string.theme_retro_unlock
    ) {
        override val appBackground = Color(0xFF1A1A2E)
        override val surface = Color(0xFF16213E)
        override val headerText = Color(0xFF00FFFF)
        override val subText = Color(0xFFFF00FF)
        override val scoreBlockBackground = Color(0xFF0F3460)
        override val scoreLabel = Color(0xFF00FFFF)
        override val buttonBackground = Color(0xFFFF00FF)
        override val buttonLabel = Color(0xFF000000)
        override val tileEmpty = Color(0xFF1A1A3E)

        override fun tileBackgroundColor(value: Int) = when {
            value == 0 -> tileEmpty
            value <= 4 -> Color(0xFF2A2A4E)
            value <= 8 -> Color(0xFF3A3A6E)
            value <= 16 -> Color(0xFF4A4A8E)
            value <= 32 -> Color(0xFF00AAAA)
            value <= 64 -> Color(0xFF00CCCC)
            value <= 128 -> Color(0xFF00EEEE)
            value <= 256 -> Color(0xFFFFCC00)
            value <= 512 -> Color(0xFFFF8800)
            value <= 1024 -> Color(0xFFFF4444)
            value <= 2048 -> Color(0xFFFF0088)
            else -> Color(0xFFAA00FF)
        }

        override fun tileTextColor(value: Int) = when {
            value <= 4 -> Color(0xFF00FFFF)
            value <= 64 -> Color(0xFFFFFFFF)
            else -> Color(0xFF000000)
        }
    }

    // ---------- Ocean Theme ----------
    data object Ocean : GameTheme(
        id = "ocean",
        displayName = Res.string.theme_ocean_name,
        unlockRequirement = Res.string.theme_ocean_unlock
    ) {
        override val appBackground = Color(0xFF0A1A2E)
        override val surface = Color(0xFF142D4A)
        override val headerText = Color(0xFF80DEFF)
        override val subText = Color(0xFF4DD0E1)
        override val scoreBlockBackground = Color(0xFF0D3B5E)
        override val scoreLabel = Color(0xFF4FC3F7)
        override val buttonBackground = Color(0xFF00ACC1)
        override val buttonLabel = Color(0xFFFFFFFF)
        override val tileEmpty = Color(0xFF0D2B4A)

        override fun tileBackgroundColor(value: Int) = when {
            value == 0 -> tileEmpty
            value <= 4 -> Color(0xFF1A3A5E)
            value <= 8 -> Color(0xFF2A4A6E)
            value <= 16 -> Color(0xFF3A5A7E)
            value <= 32 -> Color(0xFF0088AA)
            value <= 64 -> Color(0xFF0099BB)
            value <= 128 -> Color(0xFF00AACC)
            value <= 256 -> Color(0xFF00BBCC)
            value <= 512 -> Color(0xFF00CCDD)
            value <= 1024 -> Color(0xFF00DDEE)
            value <= 2048 -> Color(0xFF00EEFF)
            else -> Color(0xFF4DD0E1)
        }

        override fun tileTextColor(value: Int) = when {
            value <= 4 -> Color(0xFF80DEFF)
            value <= 128 -> Color(0xFFFFFFFF)
            else -> Color(0xFF0A1A2E)
        }
    }
}