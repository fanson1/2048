package com.finley.android.merge2048.domain

import kotlin.random.Random

class GameEngine(val boardSize: Int = 4, val seed: Int? = null) {
    init {
        require(boardSize in 3..6) { "Board size must be 3..6 (got $boardSize)" }
    }

    companion object {
        const val WIN_VALUE = 2048
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
        hasWon = board.any { row -> row.any { it == WIN_VALUE } }
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
            board[row][col] = if (rng.nextFloat() < 0.9f) 2 else 4
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
            lastMoveAnimationData = computeMovements(lastMoveBoardBefore, getBoard(), direction, moveAnimationSeq)
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
            if (i + 1 < row.size && row[i] == row[i + 1]) {
                val mergedValue = row[i] * 2
                merged.add(mergedValue)
                score += mergedValue
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
            hasWon = board.any { row -> row.any { it == WIN_VALUE } }
        }

        if (!canMove()) {
            isGameOver = true
        }
    }

    private fun canMove(): Boolean {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                if (board[i][j] == 0) return true

                if (j + 1 < boardSize && board[i][j] == board[i][j + 1]) return true
                if (i + 1 < boardSize && board[i][j] == board[i + 1][j]) return true
            }
        }
        return false
    }

    fun getBoard(): List<List<Int>> {
        return board.map { it.toList() }
    }

    internal fun setBoardForTesting(values: List<List<Int>>, restoredScore: Int = 0) {
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
    }

    /**
     * Compute tile movement data by comparing the board before and after a move.
     * This is used by the UI layer to animate tiles sliding across the board.
     *
     * Implementation: a full forward simulation of the move. For each line along
     * the move axis we keep track of every tile (its origin and value) and walk
     * it through the same "compact + merge" rules the real move uses, so the
     * resulting [TileMovement] entries exactly mirror what happened:
     *   - tiles that do not move -> Stayed
     *   - tiles that shift -> Slid
     *   - two equal tiles -> Merged (both origins recorded)
     *   - cells that gain a tile out of thin air -> Spawned
     */
    private fun computeMovements(
        boardBefore: List<List<Int>>,
        boardAfter: List<List<Int>>,
        direction: Direction,
        moveId: Long
    ): MoveAnimationData {
        val size = boardSize
        val movements = mutableListOf<TileMovement>()
        val claimed = mutableSetOf<Pair<Int, Int>>()

        // Simulate one line of the move forward.
        // `line` contains tiles as (originRow, originCol, value) in scan order
        // (the order in which the real move walks the line). We compact them
        // exactly like the real move, emitting movements as tiles get placed.
        //
        // targetFor(index) maps a compacted slot index back to the physical
        // (row, col) that slot occupies in the moved board.
        fun processLine(
            line: List<Triple<Int, Int, Int>>,
            targetFor: (Int) -> Pair<Int, Int>
        ) {
            // Track the true origin of each compacted item. An item is either
            // a single tile (origin set, pending merge candidate) or a merged
            // pair (both origins recorded, cannot merge again).
            data class Item(
                val value: Int,
                val origin: Pair<Int, Int>,
                val mergeSecondOrigin: Pair<Int, Int>? = null,
                val canMergeAgain: Boolean = true
            )

            val items = mutableListOf<Item>()
            for ((r, c, v) in line) {
                val last = items.lastOrNull()
                if (last != null && last.canMergeAgain && last.value == v) {
                    // Merge the trailing item with this tile.
                    items[items.lastIndex] = Item(
                        value = v * 2,
                        origin = last.origin,
                        mergeSecondOrigin = Pair(r, c),
                        canMergeAgain = false
                    )
                } else {
                    items.add(Item(value = v, origin = Pair(r, c)))
                }
            }

            // Place each compacted item into its target slot in order.
            for ((index, item) in items.withIndex()) {
                val (toRow, toCol) = targetFor(index)
                val target = Pair(toRow, toCol)
                claimed.add(target)
                if (item.mergeSecondOrigin != null) {
                    movements.add(
                        TileMovement.Merged(
                            from1Row = item.origin.first,
                            from1Col = item.origin.second,
                            from2Row = item.mergeSecondOrigin.first,
                            from2Col = item.mergeSecondOrigin.second,
                            toRow = toRow,
                            toCol = toCol,
                            value = item.value
                        )
                    )
                } else if (item.origin == target) {
                    movements.add(TileMovement.Stayed(toRow, toCol, item.value))
                } else {
                    movements.add(
                        TileMovement.Slid(
                            fromRow = item.origin.first,
                            fromCol = item.origin.second,
                            toRow = toRow,
                            toCol = toCol,
                            value = item.value
                        )
                    )
                }
            }
        }

        when (direction) {
            Direction.LEFT -> for (r in 0 until size) {
                val line = (0 until size).map { c -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
                processLine(line) { index -> Pair(r, index) }
            }
            Direction.RIGHT -> for (r in 0 until size) {
                val line = (size - 1 downTo 0).map { c -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
                processLine(line) { index -> Pair(r, size - 1 - index) }
            }
            Direction.UP -> for (c in 0 until size) {
                val line = (0 until size).map { r -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
                processLine(line) { index -> Pair(index, c) }
            }
            Direction.DOWN -> for (c in 0 until size) {
                val line = (size - 1 downTo 0).map { r -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
                processLine(line) { index -> Pair(size - 1 - index, c) }
            }
        }

        // Remove Stayed entries for positions that were claimed by Merged/Slid,
        // so that the find() lookup in AnimatedTile returns the correct movement.
        movements.removeAll { it is TileMovement.Stayed && claimed.contains(Pair(it.row, it.col)) }

        // --- Phase 2: Spawned tiles (tiles that came out of nowhere) ---
        for (r in 0 until size) {
            for (c in 0 until size) {
                val value = boardAfter[r][c]
                if (value != 0 && !claimed.contains(Pair(r, c))) {
                    movements.add(TileMovement.Spawned(r, c, value))
                }
            }
        }

        return MoveAnimationData(
            moveId = moveId,
            movements = movements,
            boardBefore = boardBefore,
            boardAfter = boardAfter
        )
    }
}