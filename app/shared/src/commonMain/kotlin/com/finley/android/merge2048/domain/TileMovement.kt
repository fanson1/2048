package com.finley.android.merge2048.domain

/**
 * Describes how a single tile moved during the last move.
 * Used by the UI layer to animate tiles sliding across the board.
 */
sealed class TileMovement {
    /** A tile that did not move (stayed in the same position). */
    data class Stayed(val row: Int, val col: Int, val value: Int) : TileMovement()

    /** A tile that slid from one position to another without merging. */
    data class Slid(val fromRow: Int, val fromCol: Int, val toRow: Int, val toCol: Int, val value: Int) : TileMovement()

    /** Two tiles merged at [toRow], [toCol]. Both tiles slide toward the target,
     *  then the target tile scales up briefly to show the merge. */
    data class Merged(
        val from1Row: Int, val from1Col: Int,
        val from2Row: Int, val from2Col: Int,
        val toRow: Int, val toCol: Int,
        val value: Int
    ) : TileMovement()

    /** A new tile spawned at [row], [col] (only appears after move completes). */
    data class Spawned(val row: Int, val col: Int, val value: Int) : TileMovement()
}

/**
 * Pre-computed movement data for the entire board, ready for the UI to animate.
 */
data class MoveAnimationData(
    val movements: List<TileMovement>,
    val boardBefore: List<List<Int>>,
    val boardAfter: List<List<Int>>
)
