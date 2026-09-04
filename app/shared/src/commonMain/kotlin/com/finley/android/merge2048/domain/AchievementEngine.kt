package com.finley.android.merge2048.domain

/**
 * Context for a single move, provided to the achievement engine.
 */
data class MoveContext(
    val newMaxTile: Int,
    val previousMaxTile: Int,
    val mergesInThisMove: Int,
    val newScore: Int,
    val previousScore: Int,
    val moveCount: Int,
    val boardSize: Int,
    val didUndo: Boolean
)

/**
 * Context that only lives across the whole game (used by triggers like
 * "play 100 games").
 */
data class GameSessionContext(
    val gamesPlayed: Int
)

/**
 * Pure detection of newly-unlocked achievements. The caller (the reducer)
 * hands in the pre/post move context plus a session context, and the engine
 * returns the list of ids that *just* became unlocked.
 */
class AchievementEngine {

    fun evaluate(
        alreadyUnlocked: Set<String>,
        move: MoveContext?,
        session: GameSessionContext,
        winContext: WinContext?
    ): List<Achievement> {
        val newly = mutableListOf<Achievement>()

        fun unlock(id: String) {
            if (id !in alreadyUnlocked) Achievement.byId(id)?.let(newly::add)
        }

        // Move-scoped
        move?.let { m ->
            if (m.mergesInThisMove > 0) unlock(Achievement.FirstMerge.id)
            if (m.newMaxTile >= 8 && m.previousMaxTile < 8) unlock(Achievement.First8.id)
            if (m.newMaxTile >= 32 && m.previousMaxTile < 32) unlock(Achievement.First32.id)
            if (m.newMaxTile >= 128 && m.previousMaxTile < 128) unlock(Achievement.First128.id)
            if (m.newMaxTile >= 512 && m.previousMaxTile < 512) unlock(Achievement.First512.id)
            if (m.newMaxTile >= 1024 && m.previousMaxTile < 1024) unlock(Achievement.First1024.id)
            if (m.newMaxTile >= 2048 && m.previousMaxTile < 2048) unlock(Achievement.First2048.id)
            if (m.newMaxTile >= 4096 && m.previousMaxTile < 4096) unlock(Achievement.First4096.id)
            if (m.newMaxTile >= 8192 && m.previousMaxTile < 8192) unlock(Achievement.First8192.id)

            if (m.mergesInThisMove >= 5) unlock(Achievement.Chain5.id)
            if (m.mergesInThisMove >= 8) unlock(Achievement.Chain8.id)

            if (m.moveCount >= 100) unlock(Achievement.Moves100.id)
            if (m.moveCount >= 500) unlock(Achievement.Moves500.id)
            if (m.moveCount >= 1000) unlock(Achievement.Moves1000.id)

            if (m.newScore >= 10_000 && m.previousScore < 10_000) unlock(Achievement.Score10k.id)
            if (m.newScore >= 50_000 && m.previousScore < 50_000) unlock(Achievement.Score50k.id)
            if (m.newScore >= 100_000 && m.previousScore < 100_000) unlock(Achievement.Score100k.id)

            if (m.newMaxTile >= 1024 && m.moveCount <= 50) {
                unlock(Achievement.Fast1024.id)
            }
        }

        // Session-scoped (games played)
        if (session.gamesPlayed >= 10) unlock(Achievement.GamePlayed10.id)
        if (session.gamesPlayed >= 100) unlock(Achievement.GamePlayed100.id)

        // Win-scoped
        winContext?.let { w ->
            if (w.didWin) {
                if (!w.didUndo) unlock(Achievement.UndoAbstain.id)
                when (w.boardSize) {
                    3 -> unlock(Achievement.SmallBoard.id)
                    5 -> unlock(Achievement.BigBoard.id)
                    6 -> unlock(Achievement.MaxBoard.id)
                }
            }
        }

        return newly
    }
}

data class WinContext(
    val didWin: Boolean,
    val didUndo: Boolean,
    val boardSize: Int
)