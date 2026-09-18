package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable

/**
 * Distinguishes where a finished game came from.
 * Used to tag history records and to keep a dedicated daily-challenge record.
 */
@Serializable
enum class GameMode { NORMAL, DAILY, TIMED }

/**
 * One game record, persisted to local storage. Powers the history screen,
 * charts and lifetime statistics. The board itself is not stored — only the
 * summary metrics, plus an optional score-over-time curve for short games.
 *
 * When [incomplete] is true, the game was not finished (e.g. the player
 * tapped "Restart") and [snapshotJson] holds the serialized [GameSnapshot]
 * so the player can resume it later from the history list.
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
    val scoreOverTime: List<Int> = emptyList(),
    /** Highest single-move score in this game. Tracked independently of [scoreOverTime],
     *  which may be truncated — so it stays accurate even for very long games. */
    val bestMove: Int = 0,
    /** Where this game came from (normal / daily / timed). */
    val mode: GameMode = GameMode.NORMAL,
    /** Merge rule variant used (classic, threes, fibonacci). */
    val mergeRuleId: String = "classic",
    /** True when the game was not finished (e.g. player tapped "Restart" while
     *  in progress) — the record is kept in history so the player can resume. */
    val incomplete: Boolean = false,
    /** JSON-serialized [GameSnapshot] captured at the moment the game was
     *  abandoned. Only non-null when [incomplete] is true. */
    val snapshotJson: String? = null
) {
    /** Average points per move. */
    val avgPerMove: Double
        get() = if (moveCount == 0) 0.0 else score.toDouble() / moveCount
}
