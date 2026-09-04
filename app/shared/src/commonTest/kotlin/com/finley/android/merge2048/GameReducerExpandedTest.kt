package com.finley.android.merge2048

import com.finley.android.merge2048.domain.Achievement
import com.finley.android.merge2048.domain.AnimationLevel
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameReducer
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.domain.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behaviour tests for the freshly-rewritten reducer covering multi-board-size,
 * preferences, achievements and the in-progress game restoration path.
 */
class GameReducerExpandedTest {

    private fun freshReducer(): GameReducer {
        val reducer = GameReducer()
        reducer.reduce(GameState(), GameIntent.NewGame)
        return reducer
    }

    // ----- multi-board-size -----

    @Test
    fun `engine can be configured for 3x3 board`() {
        val reducer = GameReducer()
        val state = reducer.reduce(
            GameState(),
            GameIntent.ApplyPreferences(UserPreferences.Default.copy(boardSize = 3))
        )
        assertEquals(3, state.boardSize)
        assertEquals(3, state.board.size)
        assertTrue(state.board.all { it.size == 3 })
    }

    @Test
    fun `changing board size starts a new game`() {
        val reducer = GameReducer()
        // First, get to 4x4 default
        reducer.reduce(GameState(), GameIntent.ApplyPreferences(UserPreferences.Default))
        reducer.reduce(GameState(), GameIntent.NewGame)
        // Then switch to 5x5
        val state = reducer.reduce(
            GameState(),
            GameIntent.ApplyPreferences(UserPreferences.Default.copy(boardSize = 5))
        )
        assertEquals(5, state.boardSize)
        assertEquals(0, state.score)
    }

    // ----- achievement detection -----

    @Test
    fun `first merge triggers achievement`() {
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
        assertEquals(Achievement.FirstMerge.id, state.pendingAchievementId)
    }

    @Test
    fun `reaching 2048 triggers classic and fires win dialog`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(1024, 1024, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val state = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        assertTrue(state.hasWon)
        assertTrue(state.showWinDialog)
        assertNotNull(state.pendingAchievementId)
        // The pending achievement is either First2048 (moved into 2048) or
        // First1024 (the 1024 already on the board wasn't a new max this move
        // because the previous max was already 1024 from the seed). Either
        // way, the achievement queue must include First2048 after consumption
        // chain.
        val queue = mutableListOf(state.pendingAchievementId)
        var s = state
        while (s.pendingAchievementId != null) {
            val id = s.pendingAchievementId ?: break
            s = reducer.reduce(s, GameIntent.ConsumeAchievement(id))
            queue.add(s.pendingAchievementId ?: "")
        }
        assertTrue(Achievement.First2048.id in queue)
    }

    @Test
    fun `consuming achievement moves to next in queue or null`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val s1 = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        val first = s1.pendingAchievementId
        assertNotNull(first)
        val s2 = reducer.reduce(s1, GameIntent.ConsumeAchievement(first))
        // After consumption the state should have no more pending items.
        assertNull(s2.pendingAchievementId)
    }

    // ----- preferences -----

    @Test
    fun `apply preferences stores prefs on state`() {
        val reducer = freshReducer()
        val prefs = UserPreferences.Default.copy(
            soundEnabled = false,
            animationLevel = AnimationLevel.OFF,
            darkMode = true
        )
        val state = reducer.reduce(GameState(), GameIntent.ApplyPreferences(prefs))
        assertEquals(prefs, state.user)
    }

    @Test
    fun `best score by board size is tracked independently`() {
        val reducer = freshReducer()
        // 4x4 game
        reducer.reduce(
            GameState(),
            GameIntent.ApplyPreferences(UserPreferences.Default.copy(boardSize = 4))
        )
        reducer.reduce(GameState(), GameIntent.NewGame)
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val after4x4Move = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        // 4x4 best should be 4 in the state returned from the move itself
        assertEquals(4, after4x4Move.bestScoreByBoardSize[4])
    }

    // ----- restoration -----

    @Test
    fun `restoring a snapshot rebuilds the engine state`() {
        val reducer = freshReducer()
        val snapshot = com.finley.android.merge2048.domain.GameSnapshot(
            board = listOf(
                listOf(2, 4, 8, 16),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            ),
            score = 0,
            moveCount = 7,
            hasWon = false,
            boardSize = 4
        )
        val state = reducer.reduce(
            GameState(),
            GameIntent.RestoreGame(snapshot, UserPreferences.Default)
        )
        assertEquals(7, state.moveCount)
        assertEquals(2, state.board[0][0])
        assertEquals(16, state.board[0][3])
    }

    // ----- new game on big board -----

    @Test
    fun `new game on 6x6 creates a 6x6 board`() {
        val reducer = freshReducer()
        reducer.reduce(
            GameState(),
            GameIntent.ApplyPreferences(UserPreferences.Default.copy(boardSize = 6))
        )
        val state = reducer.reduce(GameState(), GameIntent.NewGame)
        assertEquals(6, state.boardSize)
        assertEquals(6, state.board.size)
        assertTrue(state.board.all { it.size == 6 })
    }

    // ----- merge count + lastMergePoints plumbing -----

    @Test
    fun `lastMergePoints reflects the sum of the latest move's merges`() {
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
        // 2 + 2 = 4 -> score += 4 -> lastMergePoints = 4
        assertEquals(4, state.lastMergePoints)
    }

    // ----- did use undo flag for the no-regrets achievement -----

    @Test
    fun `undo does not unlock no-regrets achievement`() {
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
        reducer.reduce(GameState(), GameIntent.Undo)
        // No-regrets should NOT be unlocked
        assertFalse(Achievement.UndoAbstain.id in reducer.let { _ -> UserPreferences.Default.unlockedAchievementIds })
    }

    // ----- multi-step undo (history stack) -----

    @Test
    fun `multi-step undo restores board and score through history`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val afterMove1 = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        assertEquals(1, afterMove1.undoCount)
        assertEquals(4, afterMove1.score)
        // After undo we should be back to the seeded state with no score
        val undone = reducer.reduce(afterMove1, GameIntent.Undo)
        assertEquals(0, undone.score)
        assertEquals(0, undone.undoCount)
        // Board restored to the pre-move layout
        assertEquals(2, undone.board[0][0])
        assertEquals(2, undone.board[0][1])
    }

    @Test
    fun `history accumulates across consecutive moves`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val s1 = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        assertEquals(1, s1.undoCount)
        // Move right — a no-op against the just-merged state, but we treat
        // the move request as "moved" if the engine changed. With the seed
        // [2,2,0,0] after LEFT we get [4,0,0,0]. RIGHT merges 4 to the right
        // -> [0,0,0,4] which is a different board, so moved=true.
        val s2 = reducer.reduce(s1, GameIntent.Move(Direction.RIGHT))
        assertEquals(2, s2.undoCount)
        val s1Undone = reducer.reduce(s2, GameIntent.Undo)
        assertEquals(1, s1Undone.undoCount)
        val s0Undone = reducer.reduce(s1Undone, GameIntent.Undo)
        assertEquals(0, s0Undone.undoCount)
    }

    @Test
    fun `undo when history is empty does not change state`() {
        val reducer = freshReducer()
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        val before = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        val undone = reducer.reduce(before, GameIntent.Undo)
        // Second undo should be a no-op
        val stillUndone = reducer.reduce(undone, GameIntent.Undo)
        assertEquals(undone, stillUndone)
        assertFalse(stillUndone.canUndo)
    }

    @Test
    fun `new game clears undo history`() {
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
        val newGame = reducer.reduce(afterMove, GameIntent.NewGame)
        assertFalse(newGame.canUndo)
        assertEquals(0, newGame.undoCount)
    }
}