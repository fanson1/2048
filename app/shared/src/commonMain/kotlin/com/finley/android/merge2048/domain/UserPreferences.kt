package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable

/**
 * Persistent user settings and the player's best record.
 * Held in [GameState.user] so the UI can read them in one place; persisted via
 * [com.finley.android.merge2048.data.SettingsRepository].
 */
@Serializable
data class UserPreferences(
    val bestScore: Int = 0,
    val bestMaxTile: Int = 0,
    val bestScoreByBoardSize: Map<Int, Int> = emptyMap(),
    val bestMaxTileByBoardSize: Map<Int, Int> = emptyMap(),
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalMerges: Long = 0L,
    val totalScore: Long = 0L,
    val soundEnabled: Boolean = true,
    val animationLevel: AnimationLevel = AnimationLevel.FULL,
    val darkMode: Boolean = false,
    val boardSize: Int = 4,
    val hasSeenTutorial: Boolean = false,
    val unlockedAchievementIds: Set<String> = emptySet(),
    val themeId: String = "classic"
) {
    companion object {
        val Default = UserPreferences()
    }
}

@Serializable
enum class AnimationLevel {
    FULL,        // all animations
    REDUCED,     // skip long transition
    OFF          // no animations
}