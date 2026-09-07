package com.finley.android.merge2048.domain

/** User actions / intentions, the input to the [GameReducer]. */
sealed class GameIntent {
    data class Move(val direction: Direction) : GameIntent()
    data object NewGame : GameIntent()
    /** Start a daily challenge with a date-based seed. The seed should be computed
     *  by the platform layer (e.g. dayNumber * 7919 + 1). */
    data class StartDailyChallenge(val boardSize: Int = 4, val seed: Int) : GameIntent()
    /** Change board size and start a new game. */
    data class ChangeBoardSize(val boardSize: Int) : GameIntent()
    /** Start a timed challenge (score attack within a time limit). */
    data class StartTimedChallenge(val boardSize: Int = 4, val durationSeconds: Int = 60) : GameIntent()
    /** Timer tick — called every second during timed mode. */
    data object TimerTick : GameIntent()
    /** Time's up — end the timed challenge. */
    data object TimerExpired : GameIntent()
    data object DismissWinDialog : GameIntent()
    data object ContinueAfterWin : GameIntent()
    data object Undo : GameIntent()

    /** Restore an in-progress game from a persisted snapshot. */
    data class RestoreGame(val snapshot: GameSnapshot, val prefs: UserPreferences) : GameIntent()

    /** Apply persisted user preferences (best scores, settings, achievements). */
    data class ApplyPreferences(val prefs: UserPreferences) : GameIntent()

    /** Acknowledge and clear a fired achievement event. */
    data class ConsumeAchievement(val id: String) : GameIntent()

    /** Internal: clear the stale per-move animation data after animations finish. */
    data object ClearMoveAnimation : GameIntent()
}
