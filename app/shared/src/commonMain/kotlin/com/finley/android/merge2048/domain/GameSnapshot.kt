package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable

/**
 * Serialized snapshot of an in-progress game for persistence. Stored in
 * [com.finley.android.merge2048.data.GameRepository] and replayed into a
 * [GameState] on relaunch.
 */
@Serializable
data class GameSnapshot(
    val board: List<List<Int>>,
    val score: Int,
    val moveCount: Int,
    val hasWon: Boolean,
    val boardSize: Int,
    val lastMoveAtMs: Long = 0L
) {
    fun toGameState(prefs: UserPreferences): GameState = GameState(
        board = board,
        score = score,
        bestScore = prefs.bestScoreByBoardSize[boardSize] ?: prefs.bestScore,
        bestMaxTile = prefs.bestMaxTileByBoardSize[boardSize] ?: prefs.bestMaxTile,
        isGameOver = false,
        hasWon = hasWon,
        showWinDialog = false,
        maxTile = board.flatten().maxOrNull() ?: 0,
        canUndo = false,
        moveCount = moveCount,
        boardSize = boardSize,
        user = prefs
    )

    companion object {
        fun fromState(state: GameState): GameSnapshot = GameSnapshot(
            board = state.board,
            score = state.score,
            moveCount = state.moveCount,
            hasWon = state.hasWon,
            boardSize = state.boardSize
        )
    }
}