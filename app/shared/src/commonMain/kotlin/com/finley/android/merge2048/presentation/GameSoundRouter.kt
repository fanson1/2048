package com.finley.android.merge2048.presentation

import com.finley.android.merge2048.data.SoundEvent
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameState

/**
 * Pure, unit-testable mapping from a reducer output (plus the [GameIntent] that
 * produced it) to the list of [SoundEvent]s the UI should play. Kept outside the
 * [GameViewModel] so the ViewModel stays a thin shell and the mapping is easy to
 * reason about / test in isolation.
 */
fun computeGameSounds(previous: GameState, next: GameState, intent: GameIntent): List<SoundEvent> {
    if (!next.user.soundEnabled) return emptyList()

    val sounds = mutableListOf<SoundEvent>()
    when (intent) {
        is GameIntent.Move -> {
            if (next.lastMergePoints > 0) {
                sounds += if (next.maxTile >= 128 || next.lastMergePoints >= 128) {
                    SoundEvent.BigMerge
                } else {
                    SoundEvent.Merge
                }
            } else if (!next.isGameOver && previous.board == next.board) {
                sounds += SoundEvent.InvalidMove
            }
            if (next.isGameOver && !previous.isGameOver) {
                sounds += SoundEvent.GameOver
            }
        }
        is GameIntent.NewGame,
        is GameIntent.StartDailyChallenge,
        is GameIntent.ChangeBoardSize,
        is GameIntent.StartTimedChallenge -> sounds += SoundEvent.NewGame
        is GameIntent.TimerExpired -> sounds += SoundEvent.GameOver
        is GameIntent.Undo -> sounds += SoundEvent.Undo
        is GameIntent.ConsumeAchievement -> sounds += SoundEvent.Achievement
        is GameIntent.TimerTick,
        is GameIntent.TogglePause,
        is GameIntent.RestoreGame,
        is GameIntent.ApplyPreferences,
        is GameIntent.DismissWinDialog,
        is GameIntent.ContinueAfterWin,
        is GameIntent.ClearMoveAnimation -> Unit
    }
    return sounds
}