package com.finley.android.merge2048

import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameReducer
import com.finley.android.merge2048.domain.GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameReducerTest {

    private fun freshReducer(): GameReducer {
        val reducer = GameReducer()
        reducer.reduce(GameState(), GameIntent.NewGame)
        return reducer
    }

    @Test
    fun `move computes score moveCount best and canUndo`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )

        val state = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))

        assertEquals(4, state.score)
        assertEquals(4, state.bestScore)
        assertEquals(1, state.moveCount)
        assertTrue(state.canUndo)
    }

    @Test
    fun `blocked board surfaces game over even when move is invalid`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )

        // A move that changes nothing on a full, unmergeable board is "invalid",
        // yet the reducer surfaces the resulting game-over state.
        val state = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))

        assertTrue(state.isGameOver)
        assertEquals(0, state.moveCount)
    }

    @Test
    fun `best score is sticky across games`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))

        // New game resets score but keeps best
        val freshState = reducer.reduce(GameState(), GameIntent.NewGame)
        assertEquals(0, freshState.score)
        assertEquals(4, freshState.bestScore)
    }

    @Test
    fun `undo restores previous board and turns off game over`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val afterMove = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        assertTrue(afterMove.canUndo)

        val undone = reducer.reduce(afterMove, GameIntent.Undo)
        assertFalse(undone.canUndo)
        assertEquals(0, undone.score)
        // Original two 2s restored at left
        assertEquals(2, undone.board[0][0])
        assertEquals(2, undone.board[0][1])
    }

    @Test
    fun `dismissing win dialog keeps game state but clears flag`() {
        val reducer = freshReducer()
        val muted = reducer.reduce(GameState(), GameIntent.DismissWinDialog)
        assertFalse(muted.showWinDialog)
    }
}
