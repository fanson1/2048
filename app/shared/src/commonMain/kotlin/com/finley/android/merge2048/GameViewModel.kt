package com.finley.android.merge2048

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    private val engine = GameEngine()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        emitState()
    }

    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.Move -> handleMove(intent.direction)
            is GameIntent.NewGame -> handleNewGame()
            is GameIntent.DismissWinDialog -> _state.update { it.copy(showWinDialog = false) }
            is GameIntent.ContinueAfterWin -> handleContinueAfterWin()
        }
    }

    private fun handleMove(direction: Direction) {
        val moved = engine.move(direction)
        if (moved) {
            emitState()
        }
    }

    private fun handleNewGame() {
        engine.resetGame()
        emitState(showWinDialog = false)
    }

    private fun handleContinueAfterWin() {
        _state.update { it.copy(showWinDialog = false) }
    }

    private fun emitState(showWinDialog: Boolean? = null) {
        val hasWon = engine.hasWon
        _state.update {
            GameState(
                board = engine.getBoard(),
                score = engine.score,
                isGameOver = engine.isGameOver,
                hasWon = hasWon,
                showWinDialog = showWinDialog ?: (hasWon && !it.showWinDialog)
            )
        }
    }
}