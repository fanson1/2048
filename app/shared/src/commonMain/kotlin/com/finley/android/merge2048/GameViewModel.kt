package com.finley.android.merge2048

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    private val engine = GameEngine()

    private var bestScore = 0

    private var bestMaxTile = 0

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var winDialogShown = false

    init {
        emitState()
    }

    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.Move -> handleMove(intent.direction)
            is GameIntent.NewGame -> handleNewGame()
            is GameIntent.DismissWinDialog -> _state.update { it.copy(showWinDialog = false) }
            is GameIntent.ContinueAfterWin -> _state.update { it.copy(showWinDialog = false) }
            is GameIntent.Undo -> handleUndo()
        }
    }

    private fun handleMove(direction: Direction) {
        val moved = engine.move(direction)
        if (moved || engine.isGameOver) {
            bestScore = maxOf(bestScore, engine.score)
            emitState()
        }
    }

    private fun handleNewGame() {
        engine.resetGame()
        winDialogShown = false
        emitState()
    }

    private fun handleUndo() {
        if (engine.undo()) {
            winDialogShown = false
            emitState()
        }
    }

    private fun emitState() {
        val hasWon = engine.hasWon
        val shouldShowWinDialog = hasWon && !winDialogShown
        if (shouldShowWinDialog) {
            winDialogShown = true
        }
        bestMaxTile = maxOf(bestMaxTile, engine.maxTile)

        _state.update { current ->
            GameState(
                board = engine.getBoard(),
                score = engine.score,
                bestScore = bestScore,
                isGameOver = engine.isGameOver,
                hasWon = hasWon,
                showWinDialog = shouldShowWinDialog,
                maxTile = bestMaxTile,
                canUndo = engine.canUndo,
                moveCount = engine.moveCount
            )
        }
    }
}