package com.finley.android.merge2048.domain

import kotlin.random.Random

class GameEngine(
    val boardSize: Int = 4,
    val seed: Int? = null,
    val mergeRule: MergeRule = ClassicMergeRule
) {
    init {
        require(boardSize in 3..6) { "Board size must be 3..6 (got $boardSize)" }
    }

    companion object {
        const val MAX_HISTORY = 50
    }

    /** Internal random generator: seeded when daily challenge is active. */
    private val rng: Random = seed?.let { Random(it) } ?: Random.Default

    var board: Array<IntArray> = Array(boardSize) { IntArray(boardSize) }
        private set

    var score: Int = 0
        private set

    /** Points earned by the most recent move (sum of merged values). */
    var lastMoveScore: Int = 0
        private set

    /** Highest single-move score earned across the whole current game. */
    var bestMoveThisGame: Int = 0
        private set

    /** Number of merges performed in the most recent move. */
    var lastMoveMergeCount: Int = 0
        private set

    /** Consecutive moves that produced merges (resets on a non-merge move). */
    var comboCount: Int = 0
        private set

    /** Highest combo achieved in the current game. */
    var maxComboThisGame: Int = 0
        private set

    /** Score multiplier derived from current combo (1.0 = no bonus, up to 4.0). */
    val comboMultiplier: Float
        get() = (1f + comboCount * 0.1f).coerceAtMost(4f)

    /** Merge positions from the last move (row, col, merged value) for UI popups. */
    var lastMergePositions: List<Triple<Int, Int, Int>> = emptyList()
        private set

    /** Board state before the last move (used to compute tile movement animations). */
    var lastMoveBoardBefore: List<List<Int>> = emptyList()
        private set

    /** Computed movement data for the last move (used to animate tile sliding). */
    var lastMoveAnimationData: MoveAnimationData? = null
        private set

    /** Monotonic counter that uniquely identifies each successful move's animation. */
    private var moveAnimationSeq: Long = 0

    /** Whether the player has used Undo at any point in the current game. */
    var hasUsedUndo: Boolean = false
        private set

    var isGameOver: Boolean = false
        private set

    var hasWon: Boolean = false
        private set

    val maxTile: Int
        get() = board.maxOf { row -> row.max() }

    var moveCount: Int = 0
        private set

    /** Number of merges performed across the whole current game. */
    var totalMergesThisGame: Int = 0
        private set

    /** Running list of cumulative score at each move start (for charting). */
    val scoreOverTime: List<Int>
        get() = scoreHistory.toList()
    private val scoreHistory: ArrayList<Int> = arrayListOf(0)

    private data class HistoryFrame(
        val board: Array<IntArray>,
        val score: Int
    )

    /** Tracks where merges occur during a single move for UI popup positioning. */
    private class MergePositionTracker {
        val positions = mutableListOf<Triple<Int, Int, Int>>() // row, col, value

        fun add(row: Int, col: Int, value: Int) {
            positions.add(Triple(row, col, value))
        }
    }

    private val history: ArrayDeque<HistoryFrame> = ArrayDeque()

    val canUndo: Boolean
        get() = history.isNotEmpty()

    val undoCount: Int
        get() = history.size

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        board = Array(boardSize) { IntArray(boardSize) }
        score = 0
        lastMoveScore = 0
        bestMoveThisGame = 0
        lastMoveMergeCount = 0
        totalMergesThisGame = 0
        comboCount = 0
        maxComboThisGame = 0
        lastMergePositions = emptyList()
        lastMoveBoardBefore = emptyList()
        lastMoveAnimationData = null
        moveAnimationSeq = 0
        hasUsedUndo = false
        isGameOver = false
        hasWon = false
        moveCount = 0
        history.clear()
        scoreHistory.clear()
        scoreHistory.add(0)
        addRandomTile()
        addRandomTile()
    }

    fun resetGame() {
        initializeBoard()
    }

    /**
     * Force-end the current round. Used when the player declines to continue
     * past a milestone tile (2048, 4096, ...) — the game stops right there.
     */
    fun finishGame() {
        isGameOver = true
    }

    /** Clears the per-move animation data so tiles stop referencing a stale move. */
    fun clearMoveAnimationData() {
        lastMoveAnimationData = null
    }

    fun undo(): Boolean {
        if (history.isEmpty()) return false
        val frame = history.removeLast()
        if (isGameOver) isGameOver = false
        board = frame.board
        score = frame.score
        moveCount = (moveCount - 1).coerceAtLeast(0)
        lastMoveScore = 0
        lastMergePositions = emptyList()
        lastMoveBoardBefore = emptyList()
        lastMoveAnimationData = null
        comboCount = 0
        hasUsedUndo = true
        // Recompute verdicts from restored board
        hasWon = mergeRule.checkWin(board.map { it.toList() })
        if (!canMove()) {
            isGameOver = true
        }
        return true
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells[rng.nextInt(emptyCells.size)]
            val spawn = mergeRule.spawnValues
            val prob = mergeRule.spawnProbability
            board[row][col] = if (spawn.size == 1 || rng.nextFloat() < prob) spawn[0] else spawn[1]
        }
    }

    fun move(direction: Direction): Boolean {
        if (isGameOver) return false

        val previousBoard = Array(boardSize) { board[it].copyOf() }
        val previousScore = score
        lastMoveMergeCount = 0
        lastMergePositions = emptyList()

        val mergeTracker = MergePositionTracker()

        when (direction) {
            Direction.LEFT -> moveLeft(mergeTracker)
            Direction.RIGHT -> moveRight(mergeTracker)
            Direction.UP -> moveUp(mergeTracker)
            Direction.DOWN -> moveDown(mergeTracker)
        }

        val moved = !board.contentDeepEquals(previousBoard)

        if (moved) {
            // Save before board for movement animation computation
            lastMoveBoardBefore = previousBoard.map { it.toList() }

            // Push the pre-move state onto the history stack (before the new
            // random tile is spawned). Cap at MAX_HISTORY to bound memory.
            history.addLast(HistoryFrame(previousBoard, previousScore))
            if (history.size > MAX_HISTORY) history.removeFirst()
            moveCount++

            // Raw merge score = engine.score (already updated by merges) minus pre-move score.
            // This is the authoritative base points; combo multiplier is applied by the
            // reducer at display time only — never mutated here.
            lastMoveScore = score - previousScore
            if (lastMoveScore > bestMoveThisGame) bestMoveThisGame = lastMoveScore

            // Update combo: if merges happened, increment; otherwise reset
            if (lastMoveMergeCount > 0) {
                comboCount++
                maxComboThisGame = maxOf(maxComboThisGame, comboCount)
            } else {
                comboCount = 0
            }

            lastMergePositions = mergeTracker.positions
            scoreHistory.add(score)
            addRandomTile()

            // Compute tile movement data for animation (after random tile added)
            moveAnimationSeq++
            lastMoveAnimationData = computeTileMovements(
                boardBefore = lastMoveBoardBefore,
                boardAfter = getBoard(),
                direction = direction,
                moveId = moveAnimationSeq,
                size = boardSize
            )
        } else {
            lastMoveScore = 0
            lastMoveAnimationData = null
        }

        checkGameState()

        return moved
    }

    private fun moveLeft(mergeTracker: MergePositionTracker) {
        for (i in 0 until boardSize) {
            val row = board[i].filter { it != 0 }.toMutableList()
            val mergedRow = mergeRow(row, mergeTracker, i, isHorizontal = true) { it }
            board[i] = IntArray(boardSize) { index ->
                if (index < mergedRow.size) mergedRow[index] else 0
            }
        }
    }

    private fun moveRight(mergeTracker: MergePositionTracker) {
        for (i in 0 until boardSize) {
            val row = board[i].filter { it != 0 }.reversed().toMutableList()
            val mergedRow = mergeRow(row, mergeTracker, i, isHorizontal = true) { boardSize - 1 - it }
            board[i] = IntArray(boardSize) { index ->
                val fromRight = boardSize - 1 - index
                if (fromRight < mergedRow.size) mergedRow[fromRight] else 0
            }
        }
    }

    private fun moveUp(mergeTracker: MergePositionTracker) {
        for (j in 0 until boardSize) {
            val column = mutableListOf<Int>()
            for (i in 0 until boardSize) {
                if (board[i][j] != 0) {
                    column.add(board[i][j])
                }
            }
            val mergedColumn = mergeRow(column, mergeTracker, j, isHorizontal = false) { it }
            for (i in 0 until boardSize) {
                board[i][j] = if (i < mergedColumn.size) mergedColumn[i] else 0
            }
        }
    }

    private fun moveDown(mergeTracker: MergePositionTracker) {
        for (j in 0 until boardSize) {
            val column = mutableListOf<Int>()
            for (i in 0 until boardSize) {
                if (board[i][j] != 0) {
                    column.add(board[i][j])
                }
            }
            val mergedColumn = mergeRow(column.reversed().toMutableList(), mergeTracker, j, isHorizontal = false) { boardSize - 1 - it }
            for (i in 0 until boardSize) {
                val fromBottom = boardSize - 1 - i
                board[i][j] = if (fromBottom < mergedColumn.size) mergedColumn[fromBottom] else 0
            }
        }
    }

    private fun mergeRow(
        row: MutableList<Int>,
        mergeTracker: MergePositionTracker,
        lineIndex: Int,
        isHorizontal: Boolean,
        physicalIndex: (Int) -> Int
    ): MutableList<Int> {
        val merged = mutableListOf<Int>()
        var i = 0
        while (i < row.size) {
            if (i + 1 < row.size && mergeRule.canMerge(row[i], row[i + 1])) {
                val mergedValue = mergeRule.mergeResult(row[i], row[i + 1])
                merged.add(mergedValue)
                score += mergeRule.mergeScore(row[i], row[i + 1])
                lastMoveMergeCount += 1
                totalMergesThisGame += 1

                // Record merge position at its PHYSICAL location in the grid
                // (not the compacted line index), so UI popups and animation
                // data land on the correct cell for every direction.
                val pos = merged.size - 1
                val phys = physicalIndex(pos)
                if (isHorizontal) {
                    mergeTracker.add(lineIndex, phys, mergedValue)
                } else {
                    mergeTracker.add(phys, lineIndex, mergedValue)
                }

                i += 2
            } else {
                merged.add(row[i])
                i++
            }
        }
        return merged
    }

    private fun checkGameState() {
        if (!hasWon) {
            hasWon = mergeRule.checkWin(board.map { it.toList() })
        }

        if (!canMove()) {
            isGameOver = true
        }
    }

    private fun canMove(): Boolean = mergeRule.canMove(board.map { it.toList() })

    fun getBoard(): List<List<Int>> {
        return board.map { it.toList() }
    }

    /**
     * Restore a previously-saved board and score (used on app relaunch to resume an
     * in-progress game) or to set up a board for tests. Resets all derived state.
     */
    fun restore(values: List<List<Int>>, restoredScore: Int = 0) {
        require(values.size == boardSize && values.all { it.size == boardSize }) {
            "Test board must be ${boardSize}x${boardSize}"
        }
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                board[i][j] = values[i][j]
            }
        }
        score = restoredScore
        lastMoveScore = 0
        bestMoveThisGame = 0
        lastMoveMergeCount = 0
        totalMergesThisGame = 0
        comboCount = 0
        maxComboThisGame = 0
        lastMergePositions = emptyList()
        lastMoveBoardBefore = emptyList()
        lastMoveAnimationData = null
        moveAnimationSeq = 0
        hasUsedUndo = false
        isGameOver = false
        hasWon = mergeRule.checkWin(board.map { it.toList() })
        moveCount = 0
        history.clear()
        scoreHistory.clear()
        scoreHistory.add(0)
    }
}