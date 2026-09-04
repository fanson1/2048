package com.finley.android.merge2048.domain

/**
 * Lifetime statistics aggregated from a list of [GameRecord]s. Used by the
 * stats screen. Empty inputs yield sensible zeros rather than crashing.
 */
data class LifetimeStats(
    val gamesPlayed: Int,
    val gamesWon: Int,
    val winRate: Double,
    val bestScore: Int,
    val bestMaxTile: Int,
    val totalMerges: Long,
    val averageScore: Int,
    val averageMovesPerGame: Int,
    val bestSingleMove: Int,
    val longestGame: Int,
    val perBoardSize: Map<Int, PerBoardStats>
) {
    companion object {
        val Empty = LifetimeStats(
            gamesPlayed = 0,
            gamesWon = 0,
            winRate = 0.0,
            bestScore = 0,
            bestMaxTile = 0,
            totalMerges = 0L,
            averageScore = 0,
            averageMovesPerGame = 0,
            bestSingleMove = 0,
            longestGame = 0,
            perBoardSize = emptyMap()
        )

        fun from(records: List<GameRecord>): LifetimeStats {
            if (records.isEmpty()) return Empty
            val bestScore = records.maxOf { it.score }
            val bestMaxTile = records.maxOf { it.maxTile }
            val totalMerges = records.sumOf { it.totalMerges.toLong() }
            val averageScore = records.sumOf { it.score } / records.size
            val averageMoves = records.sumOf { it.moveCount } / records.size
            val bestSingleMove = records.maxOf { it.bestMove }
            val longestGame = records.maxOf { it.moveCount }
            val gamesWon = records.count { it.won }
            val perBoard = records.groupBy { it.boardSize }.mapValues { (_, list) ->
                PerBoardStats(
                    gamesPlayed = list.size,
                    bestScore = list.maxOf { it.score },
                    bestMaxTile = list.maxOf { it.maxTile },
                    averageScore = list.sumOf { it.score } / list.size
                )
            }
            return LifetimeStats(
                gamesPlayed = records.size,
                gamesWon = gamesWon,
                winRate = gamesWon.toDouble() / records.size,
                bestScore = bestScore,
                bestMaxTile = bestMaxTile,
                totalMerges = totalMerges,
                averageScore = averageScore,
                averageMovesPerGame = averageMoves,
                bestSingleMove = bestSingleMove,
                longestGame = longestGame,
                perBoardSize = perBoard
            )
        }
    }
}

data class PerBoardStats(
    val gamesPlayed: Int,
    val bestScore: Int,
    val bestMaxTile: Int,
    val averageScore: Int
)