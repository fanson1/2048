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
    val lastMergePoints: Int = 0
)