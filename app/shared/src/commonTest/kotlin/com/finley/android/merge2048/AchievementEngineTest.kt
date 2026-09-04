package com.finley.android.merge2048

import com.finley.android.merge2048.domain.Achievement
import com.finley.android.merge2048.domain.AchievementEngine
import com.finley.android.merge2048.domain.GameSessionContext
import com.finley.android.merge2048.domain.MoveContext
import com.finley.android.merge2048.domain.WinContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AchievementEngineTest {

    private val engine = AchievementEngine()

    @Test
    fun `chain in one move of 5 unlocks Chain5`() {
        val move = MoveContext(
            newMaxTile = 64,
            previousMaxTile = 0,
            mergesInThisMove = 5,
            newScore = 0,
            previousScore = 0,
            moveCount = 0,
            boardSize = 4,
            didUndo = false
        )
        val unlocked = engine.evaluate(emptySet(), move, GameSessionContext(0), null)
            .map { it.id }
        assertTrue(Achievement.Chain5.id in unlocked)
    }

    @Test
    fun `100 moves unlocks Moves100`() {
        val move = MoveContext(
            newMaxTile = 0,
            previousMaxTile = 0,
            mergesInThisMove = 0,
            newScore = 0,
            previousScore = 0,
            moveCount = 100,
            boardSize = 4,
            didUndo = false
        )
        val unlocked = engine.evaluate(emptySet(), move, GameSessionContext(0), null)
            .map { it.id }
        assertTrue(Achievement.Moves100.id in unlocked)
    }

    @Test
    fun `win without undo on 5x5 unlocks BigBoard and UndoAbstain`() {
        val win = WinContext(didWin = true, didUndo = false, boardSize = 5)
        val unlocked = engine.evaluate(emptySet(), null, GameSessionContext(0), win)
            .map { it.id }
        assertTrue(Achievement.BigBoard.id in unlocked)
        assertTrue(Achievement.UndoAbstain.id in unlocked)
    }

    @Test
    fun `win with undo does not unlock UndoAbstain`() {
        val win = WinContext(didWin = true, didUndo = true, boardSize = 4)
        val unlocked = engine.evaluate(emptySet(), null, GameSessionContext(0), win)
            .map { it.id }
        assertFalse(Achievement.UndoAbstain.id in unlocked)
    }

    @Test
    fun `already unlocked achievements are not re-emitted`() {
        val move = MoveContext(
            newMaxTile = 8,
            previousMaxTile = 0,
            mergesInThisMove = 1,
            newScore = 8,
            previousScore = 0,
            moveCount = 0,
            boardSize = 4,
            didUndo = false
        )
        val unlocked = engine.evaluate(
            alreadyUnlocked = setOf(Achievement.FirstMerge.id, Achievement.First8.id),
            move = move,
            session = GameSessionContext(0),
            winContext = null
        )
        assertEquals(emptyList(), unlocked)
    }

    @Test
    fun `fast to 1024 within 50 moves unlocks Fast1024`() {
        val move = MoveContext(
            newMaxTile = 1024,
            previousMaxTile = 0,
            mergesInThisMove = 1,
            newScore = 2048,
            previousScore = 0,
            moveCount = 30,
            boardSize = 4,
            didUndo = false
        )
        val unlocked = engine.evaluate(emptySet(), move, GameSessionContext(0), null)
            .map { it.id }
        assertTrue(Achievement.Fast1024.id in unlocked)
    }

    @Test
    fun `10 games played unlocks GamePlayed10`() {
        val unlocked = engine.evaluate(emptySet(), null, GameSessionContext(10), null)
            .map { it.id }
        assertTrue(Achievement.GamePlayed10.id in unlocked)
    }
}