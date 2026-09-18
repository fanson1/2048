package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Persistent user settings and the player's best record.
 * Held in [GameState.user] so the UI can read them in one place; persisted via
 * [com.finley.android.merge2048.data.SettingsRepository].
 */
@OptIn(ExperimentalUuidApi::class)
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
    val themeId: String = "classic",
    /** Merge rule variant (classic, threes, fibonacci). */
    val mergeRuleId: String = "classic",
    /** BCP 47 language tag. "system" defers to the OS locale. */
    val language: String = "system",
    /** Per-day results of the daily challenge, keyed by day number (see [DailyChallenge]). */
    val dailyChallengeResults: Map<Int, DailyChallengeResult> = emptyMap(),
    /** Cloud sync account ID. Null if not signed in. */
    val accountId: String? = null,
    /** Unique device identifier for conflict detection. */
    val deviceId: String = Uuid.random().toString()
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
