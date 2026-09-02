package com.finley.android.merge2048

import kotlin.random.Random

class GameEngine {
    companion object {
        const val BOARD_SIZE = 4
        const val WIN_VALUE = 2048
    }

    var board: Array<IntArray> = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) }
        private set

    var score: Int = 0
        private set

    var isGameOver: Boolean = false
        private set

    var hasWon: Boolean = false
        private set

    init {
        initializeBoard()
    }

    private fun initializeBoard() {
        board = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) }
        score = 0
        isGameOver = false
        hasWon = false
        addRandomTile()
        addRandomTile()
    }

    fun resetGame() {
        initializeBoard()
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
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

        val previousBoard = Array(BOARD_SIZE) { board[it].copyOf() }

        when (direction) {
            Direction.LEFT -> moveLeft()
            Direction.RIGHT -> moveRight()
            Direction.UP -> moveUp()
            Direction.DOWN -> moveDown()
        }

        val moved = !board.contentDeepEquals(previousBoard)

        if (moved) {
            addRandomTile()
        }

        checkGameState()

        return moved
    }

    private fun moveLeft() {
        for (i in 0 until BOARD_SIZE) {
            val row = board[i].filter { it != 0 }.toMutableList()
            val mergedRow = mergeRow(row)
            board[i] = IntArray(BOARD_SIZE) { index ->
                if (index < mergedRow.size) mergedRow[index] else 0
            }
        }
    }

    private fun moveRight() {
        for (i in 0 until BOARD_SIZE) {
            val row = board[i].filter { it != 0 }.reversed().toMutableList()
            val mergedRow = mergeRow(row)
            board[i] = IntArray(BOARD_SIZE) { index ->
                val fromRight = BOARD_SIZE - 1 - index
                if (fromRight < mergedRow.size) mergedRow[fromRight] else 0
            }
        }
    }

    private fun moveUp() {
        for (j in 0 until BOARD_SIZE) {
            val column = mutableListOf<Int>()
            for (i in 0 until BOARD_SIZE) {
                if (board[i][j] != 0) {
                    column.add(board[i][j])
                }
            }
            val mergedColumn = mergeRow(column)
            for (i in 0 until BOARD_SIZE) {
                board[i][j] = if (i < mergedColumn.size) mergedColumn[i] else 0
            }
        }
    }

    private fun moveDown() {
        for (j in 0 until BOARD_SIZE) {
            val column = mutableListOf<Int>()
            for (i in 0 until BOARD_SIZE) {
                if (board[i][j] != 0) {
                    column.add(board[i][j])
                }
            }
            val mergedColumn = mergeRow(column.reversed().toMutableList())
            for (i in 0 until BOARD_SIZE) {
                val fromBottom = BOARD_SIZE - 1 - i
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
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                if (board[i][j] == 0) return true

                if (j + 1 < BOARD_SIZE && board[i][j] == board[i][j + 1]) return true
                if (i + 1 < BOARD_SIZE && board[i][j] == board[i + 1][j]) return true
            }
        }
        return false
    }

    fun getBoard(): List<List<Int>> {
        return board.map { it.toList() }
    }

    internal fun setBoardForTesting(values: List<List<Int>>) {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                board[i][j] = values[i][j]
            }
        }
        score = 0
        isGameOver = false
        hasWon = false
    }
}

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}