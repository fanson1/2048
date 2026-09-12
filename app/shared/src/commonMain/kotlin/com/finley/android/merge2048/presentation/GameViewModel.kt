package com.finley.android.merge2048.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.merge2048.data.GameHistoryRepository
import com.finley.android.merge2048.data.GameRepository
import com.finley.android.merge2048.data.SettingsRepository
import com.finley.android.merge2048.data.SoundEvent
import com.finley.android.merge2048.data.SoundService
import com.finley.android.merge2048.GameColors
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.domain.GameTheme
import com.finley.android.merge2048.domain.GameSnapshot
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.domain.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Thin MVI shell: holds the [GameState] flow, forwards [GameIntent]s to the
 * [com.finley.android.merge2048.domain.GameReducer], and persists the
 * in-progress game, the user's [UserPreferences], and a rolling history of
 * finished games across launches.
 */
class GameViewModel(
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository,
    private val historyRepository: GameHistoryRepository,
    private val soundService: SoundService
) : ViewModel() {

    private val reducer = com.finley.android.merge2048.domain.GameReducer(
        onGameOver = { record -> historyRepository.append(record) }
    )
    private val _state: MutableStateFlow<GameState> by lazy { MutableStateFlow(initialState()) }
    val state: StateFlow<GameState> by lazy { _state.asStateFlow() }

    /** Public read-only view of the user's preferences. */
    val preferences: StateFlow<UserPreferences> = settingsRepository.flow
        .stateIn(viewModelScope, SharingStarted.Eagerly, settingsRepository.snapshot())

    /** Public read-only view of the rolling finished-game history (newest first). */
    val history: StateFlow<List<GameRecord>> = historyRepository.flow
        .stateIn(viewModelScope, SharingStarted.Eagerly, historyRepository.snapshot())

    init {
        // Apply the user's theme immediately so GameColors is correct from the
        // very first composition (before ProvideGameColors' SideEffect fires).
        val themeId = settingsRepository.snapshot().themeId
        val theme = when (themeId) {
            "dark" -> GameTheme.Dark
            "neon" -> GameTheme.Neon
            else -> GameTheme.Classic
        }
        GameColors.apply(theme)

        // Persist any state changes back to the repositories.
        viewModelScope.launch {
            _state.collect { current ->
                if (current.user != settingsRepository.snapshot()) {
                    settingsRepository.save(current.user)
                }
                // Only auto-save in-progress games that are not over and not won
                // (so the player can resume from where they left off).
                if (!current.isGameOver && !current.isTimedMode) {
                    gameRepository.save(GameSnapshot.fromState(current))
                } else {
                    gameRepository.clear()
                }
            }
        }

        // Timed challenge countdown ticker.
        //
        // Deliberately a single periodic loop that reads the CURRENT state, not a
        // state-collector. A collector would launch one delayed coroutine per state
        // emission, so rapid moves during a timed challenge could fire several ticks
        // within one second and run the countdown several times too fast.
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val current = _state.value
                if (current.isTimedMode && current.timedRemainingSeconds > 0 && !current.isPaused) {
                    onIntent(GameIntent.TimerTick)
                }
            }
        }
    }

    private fun initialState(): GameState {
        val prefs = settingsRepository.snapshot()
        // Try to restore a previously-saved game.
        val snapshot = gameRepository.load()
        return if (snapshot != null && !snapshot.board.all { row -> row.all { it == 0 } }) {
            reducer.reduce(GameState(), GameIntent.RestoreGame(snapshot, prefs))
        } else {
            reducer.reduce(GameState(), GameIntent.ApplyPreferences(prefs))
                .let { reducer.reduce(it, GameIntent.NewGame) }
        }
    }

    fun onIntent(intent: GameIntent) {
        val previous = _state.value
        val next = reducer.reduce(previous, intent)
        _state.value = next
        playSoundFor(previous, next, intent)

        // After a move, schedule a clear of the per-move animation data so it doesn't
        // persist in the state. This avoids the "tile background without number" bug
        // where a stale Spawned movement keeps the tile in a partially-animated state.
        // We capture the moveId and only clear data that still matches it, so a rapid
        // second move is never cleared prematurely by the first move's timer.
        if (intent is GameIntent.Move) {
            val moveId = next.moveAnimationData?.moveId
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                if (moveId != null) {
                    _state.update { current ->
                        if (current.moveAnimationData?.moveId == moveId) {
                            reducer.reduce(current, GameIntent.ClearMoveAnimation)
                        } else {
                            current
                        }
                    }
                }
            }
        }
    }

    private fun playSoundFor(previous: GameState, next: GameState, intent: GameIntent) {
        if (!next.user.soundEnabled) return
        when (intent) {
            is GameIntent.Move -> {
                if (next.lastMergePoints > 0) {
                    if (next.maxTile >= 128 || next.lastMergePoints >= 128) {
                        soundService.play(SoundEvent.BigMerge)
                    } else {
                        soundService.play(SoundEvent.Merge)
                    }
                } else if (!next.isGameOver && previous.board == next.board) {
                    soundService.play(SoundEvent.InvalidMove)
                }
                if (next.isGameOver && !previous.isGameOver) {
                    soundService.play(SoundEvent.GameOver)
                }
            }
            is GameIntent.NewGame -> soundService.play(SoundEvent.NewGame)
            is GameIntent.StartDailyChallenge -> soundService.play(SoundEvent.NewGame)
            is GameIntent.ChangeBoardSize -> soundService.play(SoundEvent.NewGame)
            is GameIntent.StartTimedChallenge -> soundService.play(SoundEvent.NewGame)
            is GameIntent.TimerTick -> { /* silent */ }
            is GameIntent.TimerExpired -> soundService.play(SoundEvent.GameOver)
            is GameIntent.Undo -> soundService.play(SoundEvent.Undo)
            is GameIntent.TogglePause -> { /* silent */ }
            is GameIntent.RestoreGame -> { /* silent */ }
            is GameIntent.ApplyPreferences -> { /* silent */ }
            is GameIntent.ConsumeAchievement -> soundService.play(SoundEvent.Achievement)
            is GameIntent.DismissWinDialog -> { /* silent */ }
            is GameIntent.ContinueAfterWin -> { /* silent */ }
            is GameIntent.ClearMoveAnimation -> { /* silent */ }
        }
    }

    override fun onCleared() {
        soundService.shutdown()
        super.onCleared()
    }

    /** Update a single preference field and propagate to the reducer. */
    fun updatePreference(transform: (UserPreferences) -> UserPreferences) {
        val newPrefs = transform(settingsRepository.snapshot())
        settingsRepository.save(newPrefs)
        _state.update { current -> reducer.reduce(current, GameIntent.ApplyPreferences(newPrefs)) }
    }

    /** Wipe the saved game history. */
    fun clearHistory() {
        historyRepository.clear()
    }
}