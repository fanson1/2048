package com.finley.android.merge2048.presentation

import androidx.lifecycle.ViewModel
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameReducer
import com.finley.android.merge2048.domain.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thin MVI shell: holds the [GameState] flow and forwards [GameIntent]s to the
 * domain-level [GameReducer]. All game behavior lives in the reducer (testable),
 * leaving this class with no business logic.
 */
class GameViewModel(
    private val reducer: GameReducer = GameReducer()
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        _state.value = reducer.reduce(GameState(), GameIntent.NewGame)
    }

    fun onIntent(intent: GameIntent) {
        _state.update { current -> reducer.reduce(current, intent) }
    }
}
