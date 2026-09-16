package com.finley.android.merge2048

import com.finley.android.merge2048.data.SoundEvent
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.domain.UserPreferences
import com.finley.android.merge2048.presentation.computeGameSounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameSoundRouterTest {

    private fun state(
        board: List<List<Int>> = List(4) { List(4) { 0 } },
        mergePoints: Int = 0,
        maxTile: Int = 0,
        gameOver: Boolean = false,
        soundEnabled: Boolean = true
    ) = GameState(
        board = board,
        lastMergePoints = mergePoints,
        maxTile = maxTile,
        isGameOver = gameOver,
        user = UserPreferences.Default.copy(soundEnabled = soundEnabled)
    )

    @Test
    fun `silent when sound disabled`() {
        val next = state(soundEnabled = false)
        val sounds = computeGameSounds(state(), next, GameIntent.NewGame)
        assertEquals(emptyList(), sounds)
    }

    @Test
    fun `standard merge plays Merge`() {
        val next = state(mergePoints = 16, maxTile = 64)
        assertEquals(listOf(SoundEvent.Merge), computeGameSounds(state(), next, GameIntent.Move(Direction.LEFT)))
    }

    @Test
    fun `big scores play BigMerge`() {
        val next = state(mergePoints = 32, maxTile = 256)
        assertEquals(
            listOf(SoundEvent.BigMerge),
            computeGameSounds(state(), next, GameIntent.Move(Direction.LEFT))
        )

        val bigPoints = state(mergePoints = 200, maxTile = 64)
        assertEquals(
            listOf(SoundEvent.BigMerge),
            computeGameSounds(state(), bigPoints, GameIntent.Move(Direction.LEFT))
        )
    }

    @Test
    fun `unchanged board plays InvalidMove`() {
        val board = listOf(
            listOf(2, 4, 8, 16),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        val previous = state(board = board)
        val next = state(board = board)
        assertEquals(
            listOf(SoundEvent.InvalidMove),
            computeGameSounds(previous, next, GameIntent.Move(Direction.LEFT))
        )
    }

    @Test
    fun `game over transition plays Merge and GameOver`() {
        val previous = state()
        val next = state(mergePoints = 16, maxTile = 64, gameOver = true)
        assertEquals(
            listOf(SoundEvent.Merge, SoundEvent.GameOver),
            computeGameSounds(previous, next, GameIntent.Move(Direction.LEFT))
        )
    }

    @Test
    fun `new round intents route to NewGame and timed expiry to GameOver`() {
        assertTrue(SoundEvent.NewGame in computeGameSounds(state(), state(), GameIntent.NewGame))
        assertEquals(
            listOf(SoundEvent.GameOver),
            computeGameSounds(state(), state(), GameIntent.TimerExpired)
        )
    }
}