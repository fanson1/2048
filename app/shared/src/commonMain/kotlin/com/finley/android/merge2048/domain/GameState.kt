package com.finley.android.merge2048.domain

/**
 * Immutable snapshot of the game presented to the UI.
 */
data class GameState(
    val board: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val bestScore: Int = 0,
    val bestScoreByBoardSize: Map<Int, Int> = emptyMap(),
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val showWinDialog: Boolean = false,
    val maxTile: Int = 0,
    val bestMaxTile: Int = 0,
    val bestMaxTileByBoardSize: Map<Int, Int> = emptyMap(),
    val canUndo: Boolean = false,
    val undoCount: Int = 0,
    val moveCount: Int = 0,
    val boardSize: Int = 4,
    val user: UserPreferences = UserPreferences.Default,
    /** One-shot event consumed by the UI. */
    val pendingAchievementId: String? = null,
    val lastMergePoints: Int = 0,
    val comboCount: Int = 0,
    val comboMultiplier: Float = 1f,
    /** Merge positions from the last move: Triple(row, col, mergedValue). */
    val lastMergePositions: List<Triple<Int, Int, Int>> = emptyList(),
    val totalMerges: Int = 0,
    /** Animation data for tile sliding. Computed by GameEngine after each move. */
    val moveAnimationData: MoveAnimationData? = null,
    /** Timed challenge mode. */
    val isTimedMode: Boolean = false,
    val timedRemainingSeconds: Int = 0,
    val timedDurationSeconds: Int = 0,
    val timedBestScore: Int = 0,
    /** True while the player has paused the round (moves & timer suspended). */
    val isPaused: Boolean = false,
    /**
     * Best score the player had at the moment the current game started.
     * The UI uses `score > bestAtSessionStart` to decide whether to flash
     * "NEW RECORD!" — this avoids a false positive on the first few moves
     * where the running score merely catches up to the stored best.
     */
    val bestAtSessionStart: Int = 0
)