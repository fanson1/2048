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
    private val achievementEngine: AchievementEngine = AchievementEngine(),
    private val onGameOver: (GameRecord) -> Unit = {}
) {
    private var engine: GameEngine = GameEngine()
    private var prefs: UserPreferences = UserPreferences.Default
    private var pendingAchievements: ArrayDeque<Achievement> = ArrayDeque()
    private var didUseUndoThisGame: Boolean = false
    private var winDialogShown: Boolean = false
    /**
     * Guards against writing more than one history record for the same game.
     * Set once a finished-game record is emitted; cleared whenever a fresh game
     * starts (new game, board-size change, challenges, restore). Not cleared by
     * undo, so a player who dies, undoes, and dies again is only recorded once.
     */
    private var recordEmittedForCurrentGame: Boolean = false
    /**
     * Best score at the moment the current game started. Used by the UI to decide
     * whether the running score is a genuine new record (score > this), instead of
     * flashing "NEW RECORD!" on the very first move just because bestScore caught up.
     */
    private var sessionBestAtStart: Int = 0

    // Timed challenge state
    private var isTimedMode: Boolean = false
    private var timedRemainingSeconds: Int = 0
    private var timedDurationSeconds: Int = 0
    private var timedBestScore: Int = 0

    fun reduce(previous: GameState, intent: GameIntent): GameState {
        return when (intent) {
            is GameIntent.Move -> handleMove(previous, intent.direction)
            is GameIntent.NewGame -> handleNewGame()
            is GameIntent.TogglePause -> previous.copy(isPaused = !previous.isPaused)
            is GameIntent.StartDailyChallenge -> handleDailyChallenge(intent.boardSize, intent.seed)
            is GameIntent.ChangeBoardSize -> handleChangeBoardSize(intent.boardSize)
            is GameIntent.StartTimedChallenge -> handleTimedChallenge(intent.boardSize, intent.durationSeconds)
            is GameIntent.TimerTick -> handleTimerTick()
            is GameIntent.TimerExpired -> handleTimerExpired()
            is GameIntent.DismissWinDialog -> previous.copy(showWinDialog = false)
            is GameIntent.ContinueAfterWin -> previous.copy(showWinDialog = false)
            is GameIntent.Undo -> handleUndo(previous)
            is GameIntent.RestoreGame -> handleRestore(intent)
            is GameIntent.ApplyPreferences -> handleApplyPrefs(previous, intent)
            is GameIntent.ConsumeAchievement -> handleConsumeAchievement(previous, intent.id)
            is GameIntent.ClearMoveAnimation -> handleClearMoveAnimation(previous)
        }
    }

    /**
     * Compute a deterministic seed from today's date so all players get the same board.
     * Platform layer should call this to get today's seed:
     *   val epoch2024 = 1704067200000L
     *   val dayNumber = ((System.currentTimeMillis() - epoch2024) / (24L * 60 * 60 * 1000)).toInt()
     *   val seed = dayNumber * 7919 + 1
     */
    private fun handleDailyChallenge(boardSize: Int, seed: Int): GameState {
        engine = GameEngine(boardSize = boardSize, seed = seed)
        sessionBestAtStart = prefs.bestScoreByBoardSize[engine.boardSize] ?: prefs.bestScore
        didUseUndoThisGame = false
        winDialogShown = false
        recordEmittedForCurrentGame = false
        pendingAchievements.clear()
        prefs = prefs.copy(gamesPlayed = prefs.gamesPlayed + 1)
        return emitState()
    }

    private fun handleChangeBoardSize(boardSize: Int): GameState {
        prefs = prefs.copy(boardSize = boardSize)
        engine = GameEngine(boardSize = boardSize)
        sessionBestAtStart = prefs.bestScoreByBoardSize[engine.boardSize] ?: prefs.bestScore
        didUseUndoThisGame = false
        winDialogShown = false
        recordEmittedForCurrentGame = false
        pendingAchievements.clear()
        val newGamesPlayed = prefs.gamesPlayed + 1
        prefs = prefs.copy(gamesPlayed = newGamesPlayed)
        return emitState()
    }

    private fun handleTimedChallenge(boardSize: Int, durationSeconds: Int): GameState {
        prefs = prefs.copy(boardSize = boardSize)
        engine = GameEngine(boardSize = boardSize)
        sessionBestAtStart = prefs.bestScoreByBoardSize[engine.boardSize] ?: prefs.bestScore
        didUseUndoThisGame = false
        winDialogShown = false
        recordEmittedForCurrentGame = false
        pendingAchievements.clear()
        isTimedMode = true
        timedRemainingSeconds = durationSeconds
        timedDurationSeconds = durationSeconds
        timedBestScore = 0
        return emitState()
    }

    private fun handleTimerTick(): GameState {
        if (!isTimedMode || timedRemainingSeconds <= 0) return emitState()
        timedRemainingSeconds--
        // Track best score during timed mode
        if (engine.score > timedBestScore) {
            timedBestScore = engine.score
        }
        if (timedRemainingSeconds <= 0) {
            return handleTimerExpired()
        }
        return emitState()
    }

    private fun handleTimerExpired(): GameState {
        isTimedMode = false
        timedRemainingSeconds = 0
        // A finished timed session counts as a played game and writes a history
        // record, but only if the board isn't already game-over (that path is
        // recorded by handleMove and must not be double-counted here).
        if (!engine.isGameOver && !recordEmittedForCurrentGame) {
            prefs = prefs.copy(gamesPlayed = prefs.gamesPlayed + 1)
            emitGameOverRecord()
            recordEmittedForCurrentGame = true
        }
        // The game continues but timed mode is over — player sees their final score
        return emitState()
    }

    private fun handleMove(previous: GameState, direction: Direction): GameState {
        if (previous.isPaused) return previous
        val beforeMaxTile = engine.maxTile
        val beforeScore = engine.score
        val wasGameOver = engine.isGameOver

        val moved = engine.move(direction)
        if (!moved && !engine.isGameOver) return previous

        // Detect transition into game-over and emit a GameRecord (at most once per game).
        if (!wasGameOver && engine.isGameOver && !recordEmittedForCurrentGame) {
            emitGameOverRecord()
            recordEmittedForCurrentGame = true
        }

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

        val comboBonus = computeComboBonus()
        prefs = prefs.copy(
            bestScore = maxOf(prefs.bestScore, engine.score + comboBonus),
            bestMaxTile = maxOf(prefs.bestMaxTile, engine.maxTile),
            totalMerges = prefs.totalMerges + engine.lastMoveMergeCount,
            totalScore = prefs.totalScore + engine.lastMoveScore,
            bestScoreByBoardSize = prefs.bestScoreByBoardSize.toMutableMap().apply {
                this[engine.boardSize] = maxOf(this[engine.boardSize] ?: 0, engine.score + comboBonus)
            },
            bestMaxTileByBoardSize = prefs.bestMaxTileByBoardSize.toMutableMap().apply {
                this[engine.boardSize] = maxOf(this[engine.boardSize] ?: 0, engine.maxTile)
            }
        )

        return emitState()
    }

    private fun handleNewGame(): GameState {
        engine = GameEngine(boardSize = prefs.boardSize)
        sessionBestAtStart = prefs.bestScoreByBoardSize[engine.boardSize] ?: prefs.bestScore
        didUseUndoThisGame = false
        winDialogShown = false
        recordEmittedForCurrentGame = false
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
        engine.setBoardForTesting(intent.snapshot.board, restoredScore = intent.snapshot.score)
        sessionBestAtStart = prefs.bestScoreByBoardSize[engine.boardSize] ?: prefs.bestScore
        didUseUndoThisGame = false
        winDialogShown = intent.snapshot.hasWon // already shown, don't show again
        recordEmittedForCurrentGame = false
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

    /** Combo bonus for display: extra points from combo multiplier on top of raw merge score. */
    private fun computeComboBonus(): Int {
        val raw = engine.lastMoveScore
        return if (engine.comboCount > 1 && raw > 0) {
            (raw * (engine.comboMultiplier - 1f)).toInt()
        } else 0
    }

    private fun emitState(): GameState {
        val shouldShowWin = engine.hasWon && !winDialogShown
        if (shouldShowWin) winDialogShown = true

        val comboBonus = computeComboBonus()
        val displayedScore = engine.score + comboBonus

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
                lastMergePoints = engine.lastMoveScore,
                comboCount = engine.comboCount,
                comboMultiplier = engine.comboMultiplier,
                lastMergePositions = engine.lastMergePositions,
                totalMerges = engine.totalMergesThisGame,
                moveAnimationData = engine.lastMoveAnimationData,
                isTimedMode = isTimedMode,
                timedRemainingSeconds = timedRemainingSeconds,
                timedDurationSeconds = timedDurationSeconds,
                timedBestScore = timedBestScore,
                bestAtSessionStart = sessionBestAtStart
            )
    }

    internal fun seedBoardForTesting(values: List<List<Int>>) {
        engine.setBoardForTesting(values)
    }

    /**
     * Clears the stale per-move animation data once the UI animations have had time
     * to finish. Called by the ViewModel ~500ms after a move.
     */
    private fun handleClearMoveAnimation(previous: GameState): GameState {
        engine.clearMoveAnimationData()
        return previous.copy(moveAnimationData = null)
    }

    private fun emitGameOverRecord() {
        val record = GameRecord(
            finishedAtMs = nowMillis(),
            boardSize = engine.boardSize,
            score = engine.score,
            maxTile = engine.maxTile,
            moveCount = engine.moveCount,
            totalMerges = engine.totalMergesThisGame,
            won = engine.hasWon,
            didUndo = didUseUndoThisGame,
            scoreOverTime = engine.scoreOverTime.takeLast(200)
        )
        onGameOver(record)
    }

    /** Wall-clock millis. Override in tests to make records deterministic. */
    internal var nowMillis: () -> Long = { kotlin.time.Clock.System.now().toEpochMilliseconds() }
}
