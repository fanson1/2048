package com.finley.android.merge2048

import com.finley.android.merge2048.domain.DailyChallenge
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameMode
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.domain.GameReducer
import com.finley.android.merge2048.domain.GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DailyChallengeRecordTest {

    @Test
    fun `daily challenge game over tags record as DAILY and persists dedicated result`() {
        var emittedRecord: GameRecord? = null
        val reducer = GameReducer(
            onGameOver = { emittedRecord = it }
        )

        val seed = DailyChallenge.seedAt(1704153600000L) // a known day
        val s0 = reducer.reduce(GameState(), GameIntent.StartDailyChallenge(boardSize = 4, seed = seed))
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )
        val s1 = reducer.reduce(s0, GameIntent.Move(Direction.LEFT))

        assertTrue(s1.isGameOver)
        assertEquals(GameMode.DAILY, emittedRecord?.mode)

        // The dedicated per-day result is persisted in the user prefs carried in state.
        val day = DailyChallenge.dayFromSeed(seed)
        val result = s1.user.dailyChallengeResults[day]
        assertNotNull(result, "daily result should be persisted for day $day")
        assertEquals(day, result.dayNumber)
        assertEquals(4, result.boardSize)
        assertEquals(emittedRecord?.score, result.score)
    }

    @Test
    fun `normal game over tags record as NORMAL and writes no daily result`() {
        var emittedRecord: GameRecord? = null
        val reducer = GameReducer(
            onGameOver = { emittedRecord = it }
        )
        reducer.reduce(GameState(), GameIntent.NewGame)
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )
        val s1 = reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))

        assertEquals(GameMode.NORMAL, emittedRecord?.mode)
        assertTrue(s1.user.dailyChallengeResults.isEmpty(), "no daily result for normal game")
    }
}