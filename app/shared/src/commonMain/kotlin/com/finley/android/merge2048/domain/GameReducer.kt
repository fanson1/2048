package com.finley.android.merge2048.domain

/**
 * Pure MVI reducer: the single place that turns a [GameIntent] into a new [GameState].
 *
 * Owns the [GameEngine], the [AchievementEngine], and all derived bookkeeping
 * (best score, best tile, win-dialog gating, pending achievement queue). Centralising
 * the behaviour keeps the UI a passive shell, and the reducer is free of Compose /
 * Android dependencies and fully unit-testable on the JVM.
 */
class GameReducer(
    private val achievementEngine: AchievementEngine = AchievementEngine()
) {
    private var engine: GameEngine = GameEngine()
    private var prefs: UserPreferences = UserPreferences.Default
    private var pendingAchievements: ArrayDeque<Achievement> = ArrayDeque()
    private var didUseUndoThisGame: Boolean = false
    private var winDialogShown: Boolean = false

    fun reduce(previous: GameState, intent: GameIntent): GameState {
        return when (intent) {
            is GameIntent.Move -> handleMove(previous, intent.direction)
            is GameIntent.NewGame -> handleNewGame()
            is GameIntent.DismissWinDialog -> previous.copy(showWinDialog = false)
            is GameIntent.ContinueAfterWin -> previous.copy(showWinDialog = false)
            is GameIntent.Undo -> handleUndo(previous)
            is GameIntent.RestoreGame -> handleRestore(intent)
            is GameIntent.ApplyPreferences -> handleApplyPrefs(previous, intent)
            is GameIntent.ConsumeAchievement -> handleConsumeAchievement(previous, intent.id)
        }
    }

    private fun handleMove(previous: GameState, direction: Direction): GameState {
        val beforeMaxTile = engine.maxTile
        val beforeScore = engine.score

        val moved = engine.move(direction)
        if (!moved && !engine.isGameOver) return previous

        val winContext = if (engine.hasWon && !previous.hasWon) {
            WinContext(didWin = true, didUndo = didUseUndoThisGame, boardSize = engine.boardSize)
        } else null

        val moveContext = MoveContext(
            newMaxTile = engine.maxTile,
            previousMaxTile = beforeMaxTile,
            mergesInThisMove = engine.lastMoveMergeCount,
            newScore = engine.score,
            previousScore = beforeScore,
            moveCount = engine.moveCount,
            boardSize = engine.boardSize,
            didUndo = didUseUndoThisGame
        )

        val session = GameSessionContext(gamesPlayed = prefs.gamesPlayed)
        val newly = achievementEngine.evaluate(prefs.unlockedAchievementIds, moveContext, session, winContext)
        if (newly.isNotEmpty()) {
            pendingAchievements.addAll(newly)
            prefs = prefs.copy(unlockedAchievementIds = prefs.unlockedAchievementIds + newly.map { it.id })
        }

        prefs = prefs.copy(
            bestScore = maxOf(prefs.bestScore, engine.score),
            bestMaxTile = maxOf(prefs.bestMaxTile, engine.maxTile),
            totalMerges = prefs.totalMerges + engine.lastMoveMergeCount,
            totalScore = prefs.totalScore + engine.lastMoveScore,
            bestScoreByBoardSize = prefs.bestScoreByBoardSize.toMutableMap().apply {
                this[engine.boardSize] = maxOf(this[engine.boardSize] ?: 0, engine.score)
            },
            bestMaxTileByBoardSize = prefs.bestMaxTileByBoardSize.toMutableMap().apply {
                this[engine.boardSize] = maxOf(this[engine.boardSize] ?: 0, engine.maxTile)
            }
        )

        return emitState()
    }

    private fun handleNewGame(): GameState {
        engine = GameEngine(boardSize = prefs.boardSize)
        didUseUndoThisGame = false
        winDialogShown = false
        pendingAchievements.clear()

        val newGamesPlayed = prefs.gamesPlayed + 1
        prefs = prefs.copy(gamesPlayed = newGamesPlayed)

        val session = GameSessionContext(gamesPlayed = newGamesPlayed)
        val newly = achievementEngine.evaluate(
            alreadyUnlocked = prefs.unlockedAchievementIds,
            move = null,
            session = session,
            winContext = null
        )
        if (newly.isNotEmpty()) {
            pendingAchievements.addAll(newly)
            prefs = prefs.copy(unlockedAchievementIds = prefs.unlockedAchievementIds + newly.map { it.id })
        }
        return emitState()
    }

    private fun handleUndo(previous: GameState): GameState {
        if (!engine.undo()) return previous
        didUseUndoThisGame = true
        return emitState()
    }

    private fun handleRestore(intent: GameIntent.RestoreGame): GameState {
        prefs = intent.prefs
        engine = GameEngine(boardSize = intent.snapshot.boardSize)
        engine.setBoardForTesting(intent.snapshot.board)
        didUseUndoThisGame = false
        winDialogShown = intent.snapshot.hasWon // already shown, don't show again
        pendingAchievements.clear()
        return emitState().copy(moveCount = intent.snapshot.moveCount)
    }

    private fun handleApplyPrefs(previous: GameState, intent: GameIntent.ApplyPreferences): GameState {
        val boardSizeChanged = prefs.boardSize != intent.prefs.boardSize
        prefs = intent.prefs
        return if (boardSizeChanged && !engine.isGameOver) {
            engine = GameEngine(boardSize = prefs.boardSize)
            didUseUndoThisGame = false
            winDialogShown = false
            emitState()
        } else {
            // Update state to reflect any preference changes (best scores etc.)
            previous.copy(
                bestScore = prefs.bestScore,
                bestScoreByBoardSize = prefs.bestScoreByBoardSize,
                maxTile = maxOf(prefs.bestMaxTile, engine.maxTile),
                bestMaxTile = prefs.bestMaxTile,
                bestMaxTileByBoardSize = prefs.bestMaxTileByBoardSize,
                boardSize = engine.boardSize,
                user = prefs
            )
        }
    }

    private fun handleConsumeAchievement(previous: GameState, id: String): GameState {
        pendingAchievements.removeAll { it.id == id }
        return previous.copy(pendingAchievementId = pendingAchievements.firstOrNull()?.id)
    }

    private fun emitState(): GameState {
        val shouldShowWin = engine.hasWon && !winDialogShown
        if (shouldShowWin) winDialogShown = true
        return GameState(
            board = engine.getBoard(),
            score = engine.score,
            bestScore = prefs.bestScore,
            bestScoreByBoardSize = prefs.bestScoreByBoardSize,
            isGameOver = engine.isGameOver,
            hasWon = engine.hasWon,
            showWinDialog = shouldShowWin,
            maxTile = engine.maxTile,
            bestMaxTile = prefs.bestMaxTile,
            bestMaxTileByBoardSize = prefs.bestMaxTileByBoardSize,
            canUndo = engine.canUndo,
            undoCount = engine.undoCount,
            moveCount = engine.moveCount,
            boardSize = engine.boardSize,
            user = prefs,
            pendingAchievementId = pendingAchievements.firstOrNull()?.id,
            lastMergePoints = engine.lastMoveScore
        )
    }

    internal fun seedBoardForTesting(values: List<List<Int>>) {
        engine.setBoardForTesting(values)
    }
}