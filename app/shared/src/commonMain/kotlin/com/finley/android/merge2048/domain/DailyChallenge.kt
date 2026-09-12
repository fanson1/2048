package com.finley.android.merge2048.domain

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

    /** Day number (0 on the epoch day) derived from an epoch-ms timestamp. */
    fun dayNumberAt(nowEpochMillis: Long): Int {
        return ((nowEpochMillis - EPOCH_MS) / DAY_MS).toInt()
    }

    /** Seed for today's challenge derived from a epoch-ms timestamp. */
    fun seedAt(nowEpochMillis: Long): Int {
        return dayNumberAt(nowEpochMillis) * 7919 + 1
    }
}
