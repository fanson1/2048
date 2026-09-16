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
 *
 * The engine is deliberately a thin interpreter: it does not know any concrete
 * trigger condition — each [Achievement] declares its own [AchievementTrigger]
 * strategy, and the engine only iterates the catalog, skips already-unlocked
 * entries and collects the ones whose trigger now fires.
 */
class AchievementEngine {

    fun evaluate(
        alreadyUnlocked: Set<String>,
        move: MoveContext?,
        session: GameSessionContext,
        winContext: WinContext?
    ): List<Achievement> {
        return Achievement.All
            .asSequence()
            .filter { it.id !in alreadyUnlocked }
            .filter { it.trigger.isNewlyUnlocked(move, session, winContext) }
            .toList()
    }
}

data class WinContext(
    val didWin: Boolean,
    val didUndo: Boolean,
    val boardSize: Int
)
