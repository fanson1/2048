package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable

/**
 * Deterministic seed for the daily challenge: all players get the same board
 * on a given calendar day, and a new one appears each day.
 *
 * The seed is computed from the number of days elapsed since a fixed epoch,
 * so the sequence is stable across app launches and platforms.
 */
object DailyChallenge {
    private const val EPOCH_MS = 1704067200000L // 2024-01-01T00:00:00Z
    private const val DAY_MS = 24L * 60 * 60 * 1000
    const val SEED_MULTIPLIER = 7919

    /** Day number (0 on the epoch day) derived from an epoch-ms timestamp. */
    fun dayNumberAt(nowEpochMillis: Long): Int {
        return ((nowEpochMillis - EPOCH_MS) / DAY_MS).toInt()
    }

    /** Seed for today's challenge derived from a epoch-ms timestamp. */
    fun seedAt(nowEpochMillis: Long): Int {
        return dayNumberAt(nowEpochMillis) * SEED_MULTIPLIER + 1
    }

    /** Decode the day number back out of a challenge seed. */
    fun dayFromSeed(seed: Int): Int {
        return (seed - 1) / SEED_MULTIPLIER
    }
}

/**
 * Persistent result for a single day's daily challenge. Kept separately from the
 * rolling [GameRecord] history so a player can always see how they did on "today's"
 * challenge (or any past day) even after the general history has rolled over.
 */
@Serializable
data class DailyChallengeResult(
    /** Day number this result belongs to (see [DailyChallenge.dayFromSeed]). */
    val dayNumber: Int,
    /** Wall-clock timestamp when the challenge ended, ms since epoch. */
    val finishedAtMs: Long,
    /** Board size used (always the daily board size). */
    val boardSize: Int,
    /** Final score. */
    val score: Int,
    /** Highest tile reached. */
    val maxTile: Int,
    /** Total moves made. */
    val moveCount: Int,
    /** True if the player reached 2048+ on the daily board. */
    val won: Boolean
)
