package com.finley.android.merge2048.domain

/** Immutable snapshot of the game presented to the UI. */
data class GameState(
    val board: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val showWinDialog: Boolean = false,
    val maxTile: Int = 0,
    val canUndo: Boolean = false,
    val moveCount: Int = 0
)
