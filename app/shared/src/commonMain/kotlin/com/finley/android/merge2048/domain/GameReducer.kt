package com.finley.android.merge2048.domain

/**
 * Pure MVI reducer: the single place that turns a [GameIntent] into a new [GameState].
 *
 * Owning the engine and all derived bookkeeping (best score, best tile, win-dialog
 * gating) it centralizes the game's behavior so the UI layer stays a passive shell.
 * It is intentionally free of Compose / Android dependencies and unit-testable on JVM.
 */
class GameReducer {
    private val engine = GameEngine()

    private var bestScore = 0
    private var bestMaxTile = 0
    private var winDialogShown = false

    fun reduce(previous: GameState, intent: GameIntent): GameState {
        return when (intent) {
            is GameIntent.Move -> {
                val moved = engine.move(intent.direction)
                if (moved || engine.isGameOver) {
                    emitState()
                } else {
                    previous
                }
            }
            is GameIntent.NewGame -> {
                engine.resetGame()
                winDialogShown = false
                emitState()
            }
            is GameIntent.DismissWinDialog -> previous.copy(showWinDialog = false)
            is GameIntent.ContinueAfterWin -> previous.copy(showWinDialog = false)
            is GameIntent.Undo -> {
                if (engine.undo()) {
                    winDialogShown = false
                    emitState()
                } else {
                    previous
                }
            }
        }
    }

    private fun emitState(): GameState {
        val hasWon = engine.hasWon
        val shouldShowWinDialog = hasWon && !winDialogShown
        if (shouldShowWinDialog) {
            winDialogShown = true
        }
        bestScore = maxOf(bestScore, engine.score)
        bestMaxTile = maxOf(bestMaxTile, engine.maxTile)

        return GameState(
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

    internal fun seedBoardForTesting(values: List<List<Int>>) {
        engine.setBoardForTesting(values)
    }
}
