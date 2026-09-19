package com.finley.android.merge2048.domain

/**
 * Lifetime statistics aggregated from a list of [GameRecord]s. Used by the
 * stats screen. Empty inputs yield sensible zeros rather than crashing.
 *
 * Incomplete records (abandoned, resumable games) are excluded from the
 * aggregates — they have no final score, so counting them would skew the
 * number of games played, win rate, averages, and per-board stats.
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
            val finished = records.filter { !it.incomplete }
            if (finished.isEmpty()) return Empty
            val bestScore = finished.maxOf { it.score }
            val bestMaxTile = finished.maxOf { it.maxTile }
            val totalMerges = finished.sumOf { it.totalMerges.toLong() }
            val averageScore = finished.sumOf { it.score } / finished.size
            val averageMoves = finished.sumOf { it.moveCount } / finished.size
            val bestSingleMove = finished.maxOf { it.bestMove }
            val longestGame = finished.maxOf { it.moveCount }
            val gamesWon = finished.count { it.won }
            val perBoard = finished.groupBy { it.boardSize }.mapValues { (_, list) ->
                PerBoardStats(
                    gamesPlayed = list.size,
                    bestScore = list.maxOf { it.score },
                    bestMaxTile = list.maxOf { it.maxTile },
                    averageScore = list.sumOf { it.score } / list.size
                )
            }
            return LifetimeStats(
                gamesPlayed = finished.size,
                gamesWon = gamesWon,
                winRate = gamesWon.toDouble() / finished.size,
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
