package com.finley.android.merge2048.domain

/** User actions / intentions, the input to the [GameReducer]. */
sealed class GameIntent {
    data class Move(val direction: Direction) : GameIntent()
    data object NewGame : GameIntent()
    data object DismissWinDialog : GameIntent()
    data object ContinueAfterWin : GameIntent()
    data object Undo : GameIntent()

    /** Restore an in-progress game from a persisted snapshot. */
    data class RestoreGame(val snapshot: GameSnapshot, val prefs: UserPreferences) : GameIntent()

    /** Apply persisted user preferences (best scores, settings, achievements). */
    data class ApplyPreferences(val prefs: UserPreferences) : GameIntent()

    /** Acknowledge and clear a fired achievement event. */
    data class ConsumeAchievement(val id: String) : GameIntent()
}
