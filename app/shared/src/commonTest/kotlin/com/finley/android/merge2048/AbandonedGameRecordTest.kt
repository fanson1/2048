package com.finley.android.merge2048

import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameMode
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.domain.GameReducer
import com.finley.android.merge2048.domain.GameSnapshot
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.domain.UserPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies that an in-progress game which the player leaves behind for a fresh
 * round (New Game, settings board-size / merge-rule change, daily / timed
 * challenge, restore) is archived to history as an incomplete, resumable
 * [GameRecord] whose [GameRecord.snapshotJson] round-trips through
 * [GameIntent.RestoreGame].
 */
class AbandonedGameRecordTest {

    private val snapshotJson = Json { ignoreUnknownKeys = true }

    /** Plays one real merged move so the round has genuine score + move count. */
    private fun playOneMove(reducer: GameReducer) {
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0)
            )
        )
        reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
    }

    @Test
    fun `new game while a round has progress archives an incomplete resumable record`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        reducer.reduce(GameState(), GameIntent.NewGame)
        playOneMove(reducer)
        assertTrue(emitted.isEmpty(), "no record yet while the round is in progress")

        reducer.reduce(GameState(), GameIntent.NewGame)

        assertEquals(1, emitted.size)
        val record = emitted[0]
        assertTrue(record.incomplete, "abandoned round must be tagged incomplete")
        assertFalse(record.won)
        assertEquals(GameMode.NORMAL, record.mode)
        assertEquals("classic", record.mergeRuleId)
        assertEquals(4, record.score)
        assertEquals(1, record.moveCount)
        assertEquals(4, record.maxTile)

        // The snapshot must round-trip into a restorable game.
        val snapshot = snapshotJson.decodeFromString(GameSnapshot.serializer(), record.snapshotJson!!)
        assertEquals(4, snapshot.score)
        assertEquals(1, snapshot.moveCount)
        assertEquals(4, snapshot.boardSize)
        assertEquals(4, snapshot.board[0][0])

        val restored = reducer.reduce(
            GameState(),
            GameIntent.RestoreGame(snapshot, UserPreferences.Default)
        )
        assertEquals(4, restored.score)
        assertEquals(1, restored.moveCount)
        assertEquals(4, restored.board[0][0])
    }

    @Test
    fun `changing board size in settings archives the in-progress round`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        reducer.reduce(GameState(), GameIntent.NewGame)
        playOneMove(reducer)

        val state = reducer.reduce(
            GameState(),
            GameIntent.ApplyPreferences(UserPreferences.Default.copy(boardSize = 5))
        )

        assertEquals(1, emitted.size)
        assertTrue(emitted[0].incomplete)
        assertEquals(4, emitted[0].boardSize, "captured before the new round started")
        assertEquals(5, state.boardSize, "new round uses the new board size")
        assertNotNull(emitted[0].snapshotJson)
    }

    @Test
    fun `changing merge rule in settings archives the in-progress round`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        reducer.reduce(GameState(), GameIntent.NewGame)
        playOneMove(reducer)

        reducer.reduce(
            GameState(),
            GameIntent.ApplyPreferences(UserPreferences.Default.copy(mergeRuleId = "threes"))
        )

        assertEquals(1, emitted.size)
        assertEquals("classic", emitted[0].mergeRuleId, "archived under the rule the abandoned round used")
        assertTrue(emitted[0].incomplete)
        // No second archive: the round is still active afterwards.
        reducer.reduce(GameState(), GameIntent.ChangeMergeRule("fibonacci"))
        assertEquals(1, emitted.size)
    }

    @Test
    fun `starting a daily challenge archives the in-progress round`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        reducer.reduce(GameState(), GameIntent.NewGame)
        playOneMove(reducer)

        reducer.reduce(GameState(), GameIntent.StartDailyChallenge(boardSize = 4, seed = 1))

        assertEquals(1, emitted.size)
        assertEquals(GameMode.NORMAL, emitted[0].mode, "abandoned round was a normal game")
        assertTrue(emitted[0].incomplete)
    }

    @Test
    fun `starting a timed challenge archives the in-progress round`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        reducer.reduce(GameState(), GameIntent.NewGame)
        playOneMove(reducer)

        val state = reducer.reduce(GameState(), GameIntent.StartTimedChallenge(boardSize = 4, durationSeconds = 60))

        assertEquals(1, emitted.size)
        assertTrue(emitted[0].incomplete)
        assertTrue(state.isTimedMode, "new round is now a timed challenge")
    }

    @Test
    fun `a fresh untouched game is not archived`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        reducer.reduce(GameState(), GameIntent.NewGame)
        reducer.reduce(GameState(), GameIntent.NewGame)

        assertTrue(emitted.isEmpty(), "no progress, nothing worth archiving")
    }

    @Test
    fun `a finished game is not duplicated as incomplete when restarted`() {
        val emitted = mutableListOf<GameRecord>()
        val reducer = GameReducer(onRecord = { emitted += it })

        // The classic deterministic game-over board: no legal move anywhere.
        reducer.reduce(GameState(), GameIntent.NewGame)
        reducer.seedBoardForTesting(
            listOf(
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2),
                listOf(2, 4, 2, 4),
                listOf(4, 2, 4, 2)
            )
        )
        reducer.reduce(GameState(), GameIntent.Move(Direction.LEFT))
        assertEquals(1, emitted.size, "game over emits its finished record")
        assertNull(emitted[0].snapshotJson)

        reducer.reduce(GameState(), GameIntent.NewGame)
        assertEquals(
            1, emitted.size,
            "restarting a finished game must not add another (incomplete) record"
        )
    }
}