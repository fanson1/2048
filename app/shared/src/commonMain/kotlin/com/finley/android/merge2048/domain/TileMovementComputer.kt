package com.finley.android.merge2048.domain

/**
 * Single-responsibility helper that turns a "board before / board after" pair into
 * the per-tile [TileMovement] animation data the UI needs.
 *
 * Kept separate from [GameEngine] (which only triggers it after a successful move)
 * so the movement-simulation concern is isolated and read-only: given the same
 * inputs it always returns the same output.
 *
 * Implementation: a full forward simulation of the move. For each line along
 * the move axis we keep track of every tile (its origin and value) and walk
 * it through the same "compact + merge" rules the real move uses, so the
 * resulting [TileMovement] entries exactly mirror what happened:
 *   - tiles that do not move -> Stayed
 *   - tiles that shift -> Slid
 *   - two equal tiles -> Merged (both origins recorded)
 *   - cells that gain a tile out of thin air -> Spawned
 */
internal fun computeTileMovements(
    boardBefore: List<List<Int>>,
    boardAfter: List<List<Int>>,
    direction: Direction,
    moveId: Long,
    size: Int
): MoveAnimationData {
    val movements = mutableListOf<TileMovement>()
    val claimed = mutableSetOf<Pair<Int, Int>>()

    // Simulate one line of the move forward.
    // `line` contains tiles as (originRow, originCol, value) in scan order
    // (the order in which the real move walks the line). We compact them
    // exactly like the real move, emitting movements as tiles get placed.
    //
    // targetFor(index) maps a compacted slot index back to the physical
    // (row, col) that slot occupies in the moved board.
    fun processLine(
        line: List<Triple<Int, Int, Int>>,
        targetFor: (Int) -> Pair<Int, Int>
    ) {
        // Track the true origin of each compacted item. An item is either
        // a single tile (origin set, pending merge candidate) or a merged
        // pair (both origins recorded, cannot merge again).
        data class Item(
            val value: Int,
            val origin: Pair<Int, Int>,
            val mergeSecondOrigin: Pair<Int, Int>? = null,
            val canMergeAgain: Boolean = true
        )

        val items = mutableListOf<Item>()
        for ((r, c, v) in line) {
            val last = items.lastOrNull()
            if (last != null && last.canMergeAgain && last.value == v) {
                // Merge the trailing item with this tile.
                items[items.lastIndex] = Item(
                    value = v * 2,
                    origin = last.origin,
                    mergeSecondOrigin = Pair(r, c),
                    canMergeAgain = false
                )
            } else {
                items.add(Item(value = v, origin = Pair(r, c)))
            }
        }

        // Place each compacted item into its target slot in order.
        for ((index, item) in items.withIndex()) {
            val (toRow, toCol) = targetFor(index)
            val target = Pair(toRow, toCol)
            claimed.add(target)
            if (item.mergeSecondOrigin != null) {
                movements.add(
                    TileMovement.Merged(
                        from1Row = item.origin.first,
                        from1Col = item.origin.second,
                        from2Row = item.mergeSecondOrigin.first,
                        from2Col = item.mergeSecondOrigin.second,
                        toRow = toRow,
                        toCol = toCol,
                        value = item.value
                    )
                )
            } else if (item.origin == target) {
                movements.add(TileMovement.Stayed(toRow, toCol, item.value))
            } else {
                movements.add(
                    TileMovement.Slid(
                        fromRow = item.origin.first,
                        fromCol = item.origin.second,
                        toRow = toRow,
                        toCol = toCol,
                        value = item.value
                    )
                )
            }
        }
    }

    when (direction) {
        Direction.LEFT -> for (r in 0 until size) {
            val line = (0 until size).map { c -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
            processLine(line) { index -> Pair(r, index) }
        }
        Direction.RIGHT -> for (r in 0 until size) {
            val line = (size - 1 downTo 0).map { c -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
            processLine(line) { index -> Pair(r, size - 1 - index) }
        }
        Direction.UP -> for (c in 0 until size) {
            val line = (0 until size).map { r -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
            processLine(line) { index -> Pair(index, c) }
        }
        Direction.DOWN -> for (c in 0 until size) {
            val line = (size - 1 downTo 0).map { r -> Triple(r, c, boardBefore[r][c]) }.filter { it.third != 0 }
            processLine(line) { index -> Pair(size - 1 - index, c) }
        }
    }

    // --- Phase 2: Spawned tiles (tiles that came out of nowhere) ---
    for (r in 0 until size) {
        for (c in 0 until size) {
            val value = boardAfter[r][c]
            if (value != 0 && !claimed.contains(Pair(r, c))) {
                movements.add(TileMovement.Spawned(r, c, value))
            }
        }
    }

    return MoveAnimationData(
        moveId = moveId,
        movements = movements,
        boardBefore = boardBefore,
        boardAfter = boardAfter
    )
}