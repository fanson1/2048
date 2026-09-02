package com.finley.android.merge2048

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun `detects game over when board is full with no merges`() {
        val engine = GameEngine()
        // A board with no empty cells and no adjacent equal tiles
        engine.setBoardForTesting(
            listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )

        // Any move should not change the board (no merges possible)
        val moved = engine.move(Direction.LEFT)

        assertFalse(moved)
        assertTrue(engine.isGameOver)
    }

    @Test
    fun `game is not over when a move is possible`() {
        val engine = GameEngine()
        engine.setBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val moved = engine.move(Direction.LEFT)

        assertTrue(moved)
        assertFalse(engine.isGameOver)
    }

    @Test
    fun `moving right aligns tiles to the right edge`() {
        val engine = GameEngine()
        engine.setBoardForTesting(
            listOf(
                listOf(2, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        assertTrue(engine.move(Direction.RIGHT))

        // A lone 2 sliding right must sit at the rightmost column
        assertEquals(2, engine.getBoard()[0][3])
    }

    @Test
    fun `moving right merges at the right edge`() {
        val engine = GameEngine()
        engine.setBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        assertTrue(engine.move(Direction.RIGHT))

        // The two 2s merge to 4 and must sit at the rightmost column
        assertEquals(4, engine.getBoard()[0][3])
    }

    @Test
    fun `moving down aligns tiles to the bottom edge`() {
        val engine = GameEngine()
        engine.setBoardForTesting(
            listOf(
                listOf(2, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        assertTrue(engine.move(Direction.DOWN))

        // A lone 2 sliding down must sit at the bottom row
        assertEquals(2, engine.getBoard()[3][0])
    }

    @Test
    fun `moving down merges at the bottom edge`() {
        val engine = GameEngine()
        engine.setBoardForTesting(
            listOf(
                listOf(2, 0, 0, 0),
                listOf(2, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        assertTrue(engine.move(Direction.DOWN))

        // The two 2s merge to 4 and must sit at the bottom row
        assertEquals(4, engine.getBoard()[3][0])
    }
}