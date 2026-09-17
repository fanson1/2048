package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable

/**
 * Merge rule strategy — defines how tiles merge, what initial tiles spawn,
 * and what constitutes a win. The [GameEngine] is parameterised by a [MergeRule]
 * so different variants (Classic 2048, Threes!, Fibonacci, etc.) share the same
 * move logic but with different merge/score semantics.
 *
 * Each rule is serialisable so it can be stored in [UserPreferences] and
 * [GameSnapshot].
 *
 * Display names and descriptions are resolved at the UI layer via string resources
 * keyed by the rule's [id] (e.g., "rule_classic_name", "rule_threes_desc").
 */
@Serializable
sealed interface MergeRule {
    /** Unique machine-readable ID (e.g. "classic", "threes", "fibonacci"). */
    val id: String

    /** Values that may appear as new random tiles. */
    val spawnValues: List<Int>

    /** Probability of spawning the first value in [spawnValues]. Remaining prob goes to the second. */
    val spawnProbability: Float

    /** Win condition check — true if board contains a winning tile. */
    fun checkWin(board: List<List<Int>>): Boolean

    /** Whether a move is possible (empty cell or mergeable pair). */
    fun canMove(board: List<List<Int>>): Boolean

    /** Default target win value for UI (e.g. 2048). */
    val defaultWinValue: Int

    /** Whether two adjacent tiles can merge. */
    fun canMerge(a: Int, b: Int): Boolean

    /** Resulting tile value when [a] and [b] merge. Assumes [canMerge(a,b)] is true. */
    fun mergeResult(a: Int, b: Int): Int

    /** Score awarded for this merge (typically equals [mergeResult]). */
    fun mergeScore(a: Int, b: Int): Int = mergeResult(a, b)
}

/** Classic 2048: equal tiles merge into double. */
@Serializable
data object ClassicMergeRule : MergeRule {
    override val id = "classic"
    override val spawnValues = listOf(2, 4)
    override val spawnProbability = 0.9f
    override val defaultWinValue = 2048

    override fun canMerge(a: Int, b: Int): Boolean = a == b && a != 0
    override fun mergeResult(a: Int, b: Int): Int = a * 2
    override fun checkWin(board: List<List<Int>>): Boolean =
        board.any { row -> row.any { it >= defaultWinValue } }
    override fun canMove(board: List<List<Int>>): Boolean {
        val n = board.size
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (board[i][j] == 0) return true
                if (j + 1 < n && board[i][j] == board[i][j + 1]) return true
                if (i + 1 < n && board[i][j] == board[i + 1][j]) return true
            }
        }
        return false
    }
}

/** Threes! variant: 1+2=3, then equal tiles merge (3+3=6, 6+6=12...). */
@Serializable
data object ThreesMergeRule : MergeRule {
    override val id = "threes"
    override val spawnValues = listOf(1, 2)
    override val spawnProbability = 0.5f
    override val defaultWinValue = 6144 // 3*2^11

    override fun canMerge(a: Int, b: Int): Boolean {
        if (a == 0 || b == 0) return false
        if (a == b) return a >= 3 // only merge equal tiles >= 3
        return (a == 1 && b == 2) || (a == 2 && b == 1) // 1+2=3
    }

    override fun mergeResult(a: Int, b: Int): Int {
        return when {
            a == 1 && b == 2 -> 3
            a == 2 && b == 1 -> 3
            a == b -> a * 2
            else -> throw IllegalArgumentException("Cannot merge $a and $b")
        }
    }

    override fun checkWin(board: List<List<Int>>): Boolean =
        board.any { row -> row.any { it >= defaultWinValue } }

    override fun canMove(board: List<List<Int>>): Boolean {
        val n = board.size
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (board[i][j] == 0) return true
                if (j + 1 < n && canMerge(board[i][j], board[i][j + 1])) return true
                if (i + 1 < n && canMerge(board[i][j], board[i + 1][j])) return true
            }
        }
        return false
    }
}

/** Fibonacci variant: adjacent Fibonacci numbers merge to the next one. */
@Serializable
data object FibonacciMergeRule : MergeRule {
    override val id = "fibonacci"
    override val spawnValues = listOf(1, 2) // F1, F2 - need both 1 and 2 to start the chain
    override val spawnProbability = 0.9f // 90% 1, 10% 2
    override val defaultWinValue = 2584 // F18

    private val fibPairs: Set<Pair<Int, Int>> = setOf(
        1 to 1,  // 1+1=2 (F1+F1=F2)
        1 to 2, 2 to 1,
        2 to 3, 3 to 2,
        3 to 5, 5 to 3,
        5 to 8, 8 to 5,
        8 to 13, 13 to 8,
        13 to 21, 21 to 13,
        21 to 34, 34 to 21,
        34 to 55, 55 to 34,
        55 to 89, 89 to 55,
        89 to 144, 144 to 89,
        144 to 233, 233 to 144,
        233 to 377, 377 to 233,
        377 to 610, 610 to 377,
        610 to 987, 987 to 610,
        987 to 1597, 1597 to 987,
        1597 to 2584, 2584 to 1597
    )

    private val nextFib: Map<Pair<Int, Int>, Int> = mapOf(
        Pair(1, 1) to 2,  // 1+1=2
        Pair(1, 2) to 3, Pair(2, 1) to 3,
        Pair(2, 3) to 5, Pair(3, 2) to 5,
        Pair(3, 5) to 8, Pair(5, 3) to 8,
        Pair(5, 8) to 13, Pair(8, 5) to 13,
        Pair(8, 13) to 21, Pair(13, 8) to 21,
        Pair(13, 21) to 34, Pair(21, 13) to 34,
        Pair(21, 34) to 55, Pair(34, 21) to 55,
        Pair(34, 55) to 89, Pair(55, 34) to 89,
        Pair(55, 89) to 144, Pair(89, 55) to 144,
        Pair(89, 144) to 233, Pair(144, 89) to 233,
        Pair(144, 233) to 377, Pair(233, 144) to 377,
        Pair(233, 377) to 610, Pair(377, 233) to 610,
        Pair(377, 610) to 987, Pair(610, 377) to 987,
        Pair(610, 987) to 1597, Pair(987, 610) to 1597,
        Pair(987, 1597) to 2584, Pair(1597, 987) to 2584,
        Pair(1597, 2584) to 4181, Pair(2584, 1597) to 4181
    )

    override fun canMerge(a: Int, b: Int): Boolean =
        a != 0 && b != 0 && (a to b) in fibPairs

    override fun mergeResult(a: Int, b: Int): Int {
        val key = if (a <= b) a to b else b to a
        return nextFib[key] ?: throw IllegalArgumentException("Cannot merge $a and $b")
    }

    override fun checkWin(board: List<List<Int>>): Boolean =
        board.any { row -> row.any { it >= defaultWinValue } }

    override fun canMove(board: List<List<Int>>): Boolean {
        val n = board.size
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (board[i][j] == 0) return true
                if (j + 1 < n && canMerge(board[i][j], board[i][j + 1])) return true
                if (i + 1 < n && canMerge(board[i][j], board[i + 1][j])) return true
            }
        }
        return false
    }
}

/** Registry of all available merge rules. */
object MergeRules {
    val all = listOf<MergeRule>(ClassicMergeRule, ThreesMergeRule, FibonacciMergeRule)

    fun byId(id: String): MergeRule = when (id) {
        "threes" -> ThreesMergeRule
        "fibonacci" -> FibonacciMergeRule
        else -> ClassicMergeRule
    }
}