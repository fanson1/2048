package com.finley.android.merge2048.domain

import kotlin.random.Random

class GameEngine(val boardSize: Int = 4) {
    init {
        require(boardSize in 3..6) { "Board size must be 3..6 (got $boardSize)" }
    }

    companion object {
        const val WIN_VALUE = 2048
        const val MAX_HISTORY = 50
    }

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

    private data class HistoryFrame(
        val board: Array<IntArray>,
        val score: Int
    )

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
        hasUsedUndo = false
        isGameOver = false
        hasWon = false
        moveCount = 0
        history.clear()
        addRandomTile()
        addRandomTile()
    }

    fun resetGame() {
        initializeBoard()
    }

    fun undo(): Boolean {
        if (history.isEmpty()) return false
        val frame = history.removeLast()
        if (isGameOver) isGameOver = false
        board = frame.board
        score = frame.score
        moveCount = (moveCount - 1).coerceAtLeast(0)
        lastMoveScore = 0
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
            val (row, col) = emptyCells[Random.nextInt(emptyCells.size)]
            board[row][col] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    fun move(direction: Direction): Boolean {
        if (isGameOver) return false

        val previousBoard = Array(boardSize) { board[it].copyOf() }
        val previousScore = score
        lastMoveMergeCount = 0

        when (direction) {
            Direction.LEFT -> moveLeft()
            Direction.RIGHT -> moveRight()
            Direction.UP -> moveUp()
            Direction.DOWN -> moveDown()
        }

        val moved = !board.contentDeepEquals(previousBoard)

        if (moved) {
            // Push the pre-move state onto the history stack (before the new
            // random tile is spawned). Cap at MAX_HISTORY to bound memory.
            history.addLast(HistoryFrame(previousBoard, previousScore))
            if (history.size > MAX_HISTORY) history.removeFirst()
            moveCount++
            lastMoveScore = score - previousScore
            addRandomTile()
        } else {
            lastMoveScore = 0
        }

        checkGameState()

        return moved
    }

    private fun moveLeft() {
        for (i in 0 until boardSize) {
            val row = board[i].filter { it != 0 }.toMutableList()
            val mergedRow = mergeRow(row)
            board[i] = IntArray(boardSize) { index ->
                if (index < mergedRow.size) mergedRow[index] else 0
            }
        }
    }

    private fun moveRight() {
        for (i in 0 until boardSize) {
            val row = board[i].filter { it != 0 }.reversed().toMutableList()
            val mergedRow = mergeRow(row)
            board[i] = IntArray(boardSize) { index ->
                val fromRight = boardSize - 1 - index
                if (fromRight < mergedRow.size) mergedRow[fromRight] else 0
            }
        }
    }

    private fun moveUp() {
        for (j in 0 until boardSize) {
            val column = mutableListOf<Int>()
            for (i in 0 until boardSize) {
                if (board[i][j] != 0) {
                    column.add(board[i][j])
                }
            }
            val mergedColumn = mergeRow(column)
            for (i in 0 until boardSize) {
                board[i][j] = if (i < mergedColumn.size) mergedColumn[i] else 0
            }
        }
    }

    private fun moveDown() {
        for (j in 0 until boardSize) {
            val column = mutableListOf<Int>()
            for (i in 0 until boardSize) {
                if (board[i][j] != 0) {
                    column.add(board[i][j])
                }
            }
            val mergedColumn = mergeRow(column.reversed().toMutableList())
            for (i in 0 until boardSize) {
                val fromBottom = boardSize - 1 - i
                board[i][j] = if (fromBottom < mergedColumn.size) mergedColumn[fromBottom] else 0
            }
        }
    }

    private fun mergeRow(row: MutableList<Int>): MutableList<Int> {
        val merged = mutableListOf<Int>()
        var i = 0
        while (i < row.size) {
            if (i + 1 < row.size && row[i] == row[i + 1]) {
                val mergedValue = row[i] * 2
                merged.add(mergedValue)
                score += mergedValue
                lastMoveMergeCount += 1
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

    internal fun setBoardForTesting(values: List<List<Int>>) {
        require(values.size == boardSize && values.all { it.size == boardSize }) {
            "Test board must be ${boardSize}x${boardSize}"
        }
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                board[i][j] = values[i][j]
            }
        }
        score = 0
        lastMoveScore = 0
        lastMoveMergeCount = 0
        hasUsedUndo = false
        isGameOver = false
        hasWon = false
        moveCount = 0
        history.clear()
    }
}