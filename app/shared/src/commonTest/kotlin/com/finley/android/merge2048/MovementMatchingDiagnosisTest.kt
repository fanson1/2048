package com.finley.android.merge2048

import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameEngine
import com.finley.android.merge2048.domain.TileMovement
import kotlin.test.Test
import kotlin.test.fail

/**
 * Diagnostic: verify that computeMovements produces a consistent, complete,
 * and unique mapping from "before" tiles to "after" tiles for every
 * brute-forced board state on a 3x3 board with values drawn from {0,2,4}.
 */
class MovementMatchingDiagnosisTest {

    private fun boardValueAt(
        board: List<List<Int>>,
        r: Int,
        c: Int
    ): Int {
        return if (r in board.indices && c in board[r].indices) board[r][c] else 0
    }

    private fun checkBoard(before: List<List<Int>>, direction: Direction) {
        val engine = GameEngine(boardSize = 3, seed = 42)
        engine.setBoardForTesting(before)
        val beforeCopy = engine.getBoard()

        val moved = engine.move(direction)
        val after = engine.getBoard()
        val data = if (moved) engine.lastMoveAnimationData else null

        if (!moved) {
            // If the board did not move, lastMoveAnimationData is null and
            // the after board equals before. Nothing to animate.
            if (beforeCopy != after) {
                fail("unmoved board changed: $beforeCopy -> $after for $direction")
            }
            return
        }

        if (data == null) {
            fail("moved board missing animation data:\nbefore=$before\nafter=$after\ndir=$direction")
        }

        val movements = data.movements
        val size = 3

        // ---- Invariant 1: every after cell with a value >= 2 has a movement ----
        val coveredAfter = mutableSetOf<Pair<Int, Int>>()

        // Invariant 2: no duplicate destinations
        val destCount = mutableMapOf<Pair<Int, Int>, MutableList<TileMovement>>()

        for (m in movements) {
            when (m) {
                is TileMovement.Stayed -> {
                    coveredAfter.add(Pair(m.row, m.col))
                    destCount.getOrPut(Pair(m.row, m.col)) { mutableListOf() }.add(m)
                }
                is TileMovement.Slid -> {
                    coveredAfter.add(Pair(m.toRow, m.toCol))
                    destCount.getOrPut(Pair(m.toRow, m.toCol)) { mutableListOf() }.add(m)
                }
                is TileMovement.Merged -> {
                    coveredAfter.add(Pair(m.toRow, m.toCol))
                    destCount.getOrPut(Pair(m.toRow, m.toCol)) { mutableListOf() }.add(m)
                }
                is TileMovement.Spawned -> {
                    coveredAfter.add(Pair(m.row, m.col))
                    destCount.getOrPut(Pair(m.row, m.col)) { mutableListOf() }.add(m)
                }
            }
        }

        for (r in 0 until size) {
            for (c in 0 until size) {
                val v = after[r][c]
                if (v != 0) {
                    val dests = destCount[Pair(r, c)] ?: emptyList()
                    if (dests.size != 1) {
                        fail(
                            "cell ($r,$c) after=$v has ${dests.size} movements in " +
                                "before=$before dir=$direction after=$after\n" +
                                "movements=$movements"
                        )
                    }
                }
            }
        }

        // ---- Invariant 3: every movement's claimed "to" cell exists in after ----
        for (m in movements) {
            when (m) {
                is TileMovement.Stayed -> {
                    if (after[m.row][m.col] != m.value) {
                        fail("Stayed value mismatch at (${m.row},${m.col}): expect ${m.value}, board has ${after[m.row][m.col]} (dir=$direction before=$before after=$after)")
                    }
                }
                is TileMovement.Slid -> {
                    if (after[m.toRow][m.toCol] != m.value) {
                        fail("Slid value mismatch to (${m.toRow},${m.toCol}): expect ${m.value}, board has ${after[m.toRow][m.toCol]} (dir=$direction before=$before after=$after)")
                    }
                }
                is TileMovement.Merged -> {
                    if (after[m.toRow][m.toCol] != m.value) {
                        fail("Merged value mismatch to (${m.toRow},${m.toCol}): expect ${m.value}, board has ${after[m.toRow][m.toCol]} (dir=$direction before=$before after=$after)")
                    }
                    val half = m.value / 2
                    if (boardValueAt(before, m.from1Row, m.from1Col) != half ||
                        boardValueAt(before, m.from2Row, m.from2Col) != half
                    ) {
                        fail("Merged from-sources not half value: ${m} (dir=$direction before=$before after=$after)")
                    }
                }
                is TileMovement.Spawned -> {
                    if (after[m.row][m.col] != m.value) {
                        fail("Spawned value mismatch at (${m.row},${m.col}): expect ${m.value}, board has ${after[m.row][m.col]} (dir=$direction before=$before after=$after)")
                    }
                    // A spawned tile may land on a cell that a before-tile vacated,
                    // so we cannot require boardBefore == 0 here.
                }
            }
        }
    }

    @Test
    fun exhaustive3x3MovementConsistency() {
        val values = listOf(0, 2, 4)
        var count = 0
        for (a0 in values) for (a1 in values) for (a2 in values)
        for (b0 in values) for (b1 in values) for (b2 in values)
        for (c0 in values) for (c1 in values) for (c2 in values) {
            val board = listOf(
                listOf(a0, a1, a2),
                listOf(b0, b1, b2),
                listOf(c0, c1, c2)
            )
            for (d in Direction.entries) {
                checkBoard(board, d)
                count++
            }
        }
        // reached here = no failures
        println("Checked $count before-state/direction combinations OK")
    }
}
