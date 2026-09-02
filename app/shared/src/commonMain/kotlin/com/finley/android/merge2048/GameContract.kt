package com.finley.android.merge2048

data class GameState(
    val board: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val showWinDialog: Boolean = false
)

sealed class GameIntent {
    data class Move(val direction: Direction) : GameIntent()
    data object NewGame : GameIntent()
    data object DismissWinDialog : GameIntent()
    data object ContinueAfterWin : GameIntent()
}