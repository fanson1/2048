package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable

/**
 * One finished game, persisted to local storage. Powers the history screen,
 * charts and lifetime statistics. The board itself is not stored — only the
 * summary metrics, plus an optional score-over-time curve for short games.
 */
@Serializable
data class GameRecord(
    /** Wall-clock timestamp when the game ended, in ms since epoch. */
    val finishedAtMs: Long,
    /** Board size used for this game (3..6). */
    val boardSize: Int,
    /** Final score. */
    val score: Int,
    /** Highest tile reached. */
    val maxTile: Int,
    /** Total moves made. */
    val moveCount: Int,
    /** How many tiles were merged across the whole game. */
    val totalMerges: Int,
    /** True if the player reached 2048 (or higher). */
    val won: Boolean,
    /** True if the player used Undo at any point. */
    val didUndo: Boolean,
    /** Score over time, sampled per move. Capped to ~200 points to keep storage small. */
    val scoreOverTime: List<Int> = emptyList()
) {
    /** Best single move score in this game. */
    val bestMove: Int
        get() = scoreOverTime.zipWithNext { a, b -> b - a }.maxOrNull() ?: 0

    /** Average points per move. */
    val avgPerMove: Double
        get() = if (moveCount == 0) 0.0 else score.toDouble() / moveCount
}