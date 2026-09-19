package com.finley.android.merge2048.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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
    /**
     * Emitted whenever a game leaves the active table: on game-over / win resets and
     * when an in-progress game is abandoned for a fresh round. In the latter case the
     * record carries [GameRecord.incomplete] = true plus a resumable [GameSnapshot]
     * so the player can pick the game back up from the history list.
     */
    private val onRecord: (GameRecord) -> Unit = {}
) {
    private var engine: GameEngine = GameEngine(mergeRule = MergeRules.byId("classic"))
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

    /**
     * Everything that describes the round currently in play — its [GameMode], the
     * active daily-challenge day and an optional [TimedClock]. Grouped into a single
     * value object so round bookkeeping is one concept instead of a handful of loose
     * fields scattered through the reducer.
     */
    private var session: GameSession = GameSession()

    /** Mutable countdown for the timed challenge; mutated per tick, never replaced. */
    private class TimedClock(durationSeconds: Int) {
        var remainingSeconds: Int = durationSeconds
        var durationSeconds: Int = durationSeconds
        var bestScore: Int = 0
            private set

        val isExpired: Boolean get() = remainingSeconds <= 0

        /** Folds the current score into [bestScore] and returns true when time ran out. */
        fun tick(currentScore: Int): Boolean {
            remainingSeconds--
            if (currentScore > bestScore) bestScore = currentScore
            return isExpired
        }
    }

    /** Immutable description of the round in play. The [timed] clock is the one
     *  mutable part and only exists while a timed challenge is running. */
    private data class GameSession(
        val mode: GameMode = GameMode.NORMAL,
        val dayNumber: Int = 0,
        val timed: TimedClock? = null
    ) {
        val isTimed: Boolean get() = timed != null
    }

    /** Serializer for building [GameSnapshot] JSON that is stored in incomplete records. */
    private val snapshotJson = Json { ignoreUnknownKeys = true }

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
            is GameIntent.ChangeMergeRule -> handleChangeMergeRule(intent.mergeRuleId)
        }
    }

    /**
     * Template method shared by every "a fresh round starts" path (new game,
     * board-size change, daily & timed challenges, restore). It resets the
     * per-round bookkeeping and records home much the player had before the
     * round began, so the new-record flash never fires on the board's first move.
     * Concrete paths call it after setting up [engine] and pass the mode they
     * enter (see [GameSession]).
     */
    private fun beginRound(mode: GameMode, dayNumber: Int = 0, timed: TimedClock? = null) {
        didUseUndoThisGame = false
        winDialogShown = false
        recordEmittedForCurrentGame = false
        pendingAchievements.clear()
        session = GameSession(mode = mode, dayNumber = dayNumber, timed = timed)
        sessionBestAtStart = prefs.bestScoreByBoardSize[engine.boardSize] ?: prefs.bestScore
    }

    /**
     * If the player is mid-game (has progress and hasn't already emitted a
     * record for this round), archive the in-progress game as an incomplete
     * [GameRecord] carrying a resumable [GameSnapshot]. Called at the very
     * start of every handler that replaces [engine] for a fresh round —
     * NewGame, board-size / merge-rule changes, challenge starts, and restore.
     *
     * No-op when the engine is fresh (no merges/score), the game is already
     * over, or a record was already emitted for the current game.
     */
    private fun archiveAbandonedGameIfAny() {
        val worthSaving = engine.moveCount > 0 &&
            engine.score > 0 &&
            !engine.isGameOver &&
            !recordEmittedForCurrentGame
        if (!worthSaving) return

        val board = engine.getBoard()
        val snapshot = GameSnapshot(
            board = board,
            score = engine.score,
            moveCount = engine.moveCount,
            hasWon = engine.hasWon,
            boardSize = engine.boardSize,
            mergeRuleId = engine.mergeRule.id
        )
        val record = GameRecord(
            finishedAtMs = nowMillis(),
            boardSize = engine.boardSize,
            score = engine.score,
            maxTile = engine.maxTile,
            moveCount = engine.moveCount,
            totalMerges = engine.totalMergesThisGame,
            won = engine.hasWon,
            didUndo = didUseUndoThisGame,
            scoreOverTime = engine.scoreOverTime.takeLast(200),
            bestMove = engine.bestMoveThisGame,
            mode = session.mode,
            mergeRuleId = engine.mergeRule.id,
            incomplete = true,
            snapshotJson = snapshotJson.encodeToString(GameSnapshot.serializer(), snapshot)
        )
        onRecord(record)
        recordEmittedForCurrentGame = true
    }

    /**
     * Compute a deterministic seed from today's date so all players get the same board.
     * Platform layer should call this to get today's seed:
     *   val epoch2024 = 1704067200000L
     *   val dayNumber = ((System.currentTimeMillis() - epoch2024) / (24L * 60 * 60 * 1000)).toInt()
     *   val seed = dayNumber * 7919 + 1
     */
    private fun handleDailyChallenge(boardSize: Int, seed: Int): GameState {
        archiveAbandonedGameIfAny()
        engine = GameEngine(boardSize = boardSize, seed = seed, mergeRule = MergeRules.byId(prefs.mergeRuleId))
        beginRound(GameMode.DAILY, dayNumber = DailyChallenge.dayFromSeed(seed))
        recordGameStarted()
        return emitState()
    }

    private fun handleChangeBoardSize(boardSize: Int): GameState {
        archiveAbandonedGameIfAny()
        prefs = prefs.copy(boardSize = boardSize)
        engine = GameEngine(boardSize = boardSize, mergeRule = MergeRules.byId(prefs.mergeRuleId))
        beginRound(GameMode.NORMAL)
        recordGameStarted()
        return emitState()
    }

    private fun handleTimedChallenge(boardSize: Int, durationSeconds: Int): GameState {
        archiveAbandonedGameIfAny()
        prefs = prefs.copy(boardSize = boardSize)
        engine = GameEngine(boardSize = boardSize, mergeRule = MergeRules.byId(prefs.mergeRuleId))
        beginRound(GameMode.TIMED, timed = TimedClock(durationSeconds))
        return emitState()
    }

    private fun handleTimerTick(): GameState {
        val clock = session.timed ?: return emitState()
        if (clock.isExpired) return emitState()
        return if (clock.tick(engine.score)) {
            handleTimerExpired()
        } else {
            emitState()
        }
    }

    private fun handleTimerExpired(): GameState {
        if (session.isTimed) session = session.copy(timed = null)
        // A finished timed session counts as a played game and writes a history
        // record, but only if the board isn't already game-over (that path is
        // recorded by handleMove and must not be double-counted here).
        if (!engine.isGameOver && !recordEmittedForCurrentGame) {
            recordGameStarted()
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
        archiveAbandonedGameIfAny()
        engine = GameEngine(boardSize = prefs.boardSize, mergeRule = MergeRules.byId(prefs.mergeRuleId))
        beginRound(GameMode.NORMAL)
        recordGameStarted()
        return emitState()
    }

    private fun handleChangeMergeRule(mergeRuleId: String): GameState {
        archiveAbandonedGameIfAny()
        prefs = prefs.copy(mergeRuleId = mergeRuleId)
        engine = GameEngine(boardSize = prefs.boardSize, mergeRule = MergeRules.byId(mergeRuleId))
        beginRound(GameMode.NORMAL)
        recordGameStarted()
        return emitState()
    }

    /**
     * Every started (or, for timed mode, finished) round counts as a played game.
     * This single helper increments the lifetime counter **and** re-evaluates the
     * session-scoped achievements in the same step, so "play N games" unlocks at
     * the exact round that crosses the threshold — regardless of whether that round
     * was a normal game, a daily challenge, a board-size change, or a timed run.
     * (Previously only [handleNewGame] ran the evaluation, so achievements could be
     * silently postponed until the next normal new game.)
     */
    private fun recordGameStarted() {
        val newGamesPlayed = prefs.gamesPlayed + 1
        prefs = prefs.copy(gamesPlayed = newGamesPlayed)

        val newly = achievementEngine.evaluate(
            alreadyUnlocked = prefs.unlockedAchievementIds,
            move = null,
            session = GameSessionContext(gamesPlayed = newGamesPlayed),
            winContext = null
        )
        if (newly.isNotEmpty()) {
            pendingAchievements.addAll(newly)
            prefs = prefs.copy(unlockedAchievementIds = prefs.unlockedAchievementIds + newly.map { it.id })
        }
    }

    private fun handleUndo(previous: GameState): GameState {
        if (!engine.undo()) return previous
        didUseUndoThisGame = true
        return emitState()
    }

    private fun handleRestore(intent: GameIntent.RestoreGame): GameState {
        archiveAbandonedGameIfAny()
        prefs = intent.prefs
        engine = GameEngine(boardSize = intent.snapshot.boardSize, mergeRule = MergeRules.byId(intent.snapshot.mergeRuleId))
        engine.restore(intent.snapshot.board, restoredScore = intent.snapshot.score)
        beginRound(GameMode.NORMAL)
        // The win dialog was already shown when the player quit — don't show it again.
        winDialogShown = intent.snapshot.hasWon
        return emitState().copy(moveCount = intent.snapshot.moveCount)
    }

    private fun handleApplyPrefs(previous: GameState, intent: GameIntent.ApplyPreferences): GameState {
        val boardSizeChanged = prefs.boardSize != intent.prefs.boardSize
        val mergeRuleChanged = prefs.mergeRuleId != intent.prefs.mergeRuleId
        prefs = intent.prefs
        return if ((boardSizeChanged || mergeRuleChanged) && !engine.isGameOver) {
            // Starting a fresh round from the settings screen follows the exact
            // same lifecycle as NewGame / ChangeBoardSize / ChangeMergeRule:
            // archive the current round first, then reset the engine & bookkeeping.
            archiveAbandonedGameIfAny()
            engine = GameEngine(boardSize = prefs.boardSize, mergeRule = MergeRules.byId(prefs.mergeRuleId))
            beginRound(GameMode.NORMAL)
            recordGameStarted()
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
                user = prefs,
                mergeRuleId = prefs.mergeRuleId
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
            isTimedMode = session.isTimed,
            timedRemainingSeconds = session.timed?.remainingSeconds ?: 0,
            timedDurationSeconds = session.timed?.durationSeconds ?: 0,
            timedBestScore = session.timed?.bestScore ?: 0,
            bestAtSessionStart = sessionBestAtStart,
            mergeRuleId = prefs.mergeRuleId
        )
    }

    internal fun seedBoardForTesting(values: List<List<Int>>) {
        engine.restore(values)
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
            scoreOverTime = engine.scoreOverTime.takeLast(200),
            bestMove = engine.bestMoveThisGame,
            mode = session.mode,
            mergeRuleId = prefs.mergeRuleId
        )
        onRecord(record)
        if (session.mode == GameMode.DAILY) {
            // Persist a dedicated per-day record, keeping only the best score
            // ever achieved for that day's challenge. HistoryScreen reads this
            // straight from UserPreferences.
            val result = DailyChallengeResult(
                dayNumber = session.dayNumber,
                finishedAtMs = record.finishedAtMs,
                boardSize = record.boardSize,
                score = record.score,
                maxTile = record.maxTile,
                moveCount = record.moveCount,
                won = record.won
            )
            val previous = prefs.dailyChallengeResults[session.dayNumber]
            if (previous == null || result.score > previous.score) {
                prefs = prefs.copy(
                    dailyChallengeResults = prefs.dailyChallengeResults + (session.dayNumber to result)
                )
            }
        }
    }

    /** Wall-clock millis. Override in tests to make records deterministic. */
    internal var nowMillis: () -> Long = { kotlin.time.Clock.System.now().toEpochMilliseconds() }
}