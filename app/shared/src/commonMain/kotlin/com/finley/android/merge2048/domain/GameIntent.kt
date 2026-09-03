package com.finley.android.merge2048.domain

/** User actions / intentions, the input to the [GameReducer]. */
sealed class GameIntent {
    data class Move(val direction: Direction) : GameIntent()
    data object NewGame : GameIntent()
    data object DismissWinDialog : GameIntent()
    data object ContinueAfterWin : GameIntent()
    data object Undo : GameIntent()
}
