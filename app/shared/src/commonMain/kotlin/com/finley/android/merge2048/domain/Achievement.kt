package com.finley.android.merge2048.domain

/**
 * Catalog of unlockable achievements. Each entry declares a deterministic
 * [trigger] function over the pre/post move context, plus presentation fields.
 *
 * The catalog is the single source of truth for both the "is this unlocked"
 * test and the "how do I show it" UI, so the achievement wall always
 * stays in sync.
 */
enum class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val trigger: AchievementTrigger
) {
    FirstMerge("first_merge", "Hello, world", "Make your first merge.", "👋",
        AchievementTrigger.MergeAny),

    First8("first_8", "On the ladder", "Create a tile of 8.", "🪜",
        AchievementTrigger.ReachTile(8)),

    First32("first_32", "Double digits", "Create a tile of 32.", "🔟",
        AchievementTrigger.ReachTile(32)),

    First128("first_128", "Three digits!", "Create a tile of 128.", "1️⃣2️⃣8️⃣",
        AchievementTrigger.ReachTile(128)),

    First512("first_512", "Five-twelve", "Create a tile of 512.", "🔥",
        AchievementTrigger.ReachTile(512)),

    First1024("first_1024", "One-zero-two-four", "Create a tile of 1024.", "💎",
        AchievementTrigger.ReachTile(1024)),

    First2048("first_2048", "The classic", "Create a tile of 2048.", "🏆",
        AchievementTrigger.ReachTile(2048)),

    First4096("first_4096", "Beyond the original", "Create a tile of 4096.", "🚀",
        AchievementTrigger.ReachTile(4096)),

    First8192("first_8192", "Double-overkill", "Create a tile of 8192.", "🌌",
        AchievementTrigger.ReachTile(8192)),

    Chain5("chain_5", "Combo starter", "Merge five times in a single move.", "⚡",
        AchievementTrigger.ChainInOneMove(5)),

    Chain8("chain_8", "Combo master", "Merge eight times in a single move.", "🌪️",
        AchievementTrigger.ChainInOneMove(8)),

    Moves100("moves_100", "Practiced", "Reach 100 moves in a single game.", "💯",
        AchievementTrigger.MovesReached(100)),

    Moves500("moves_500", "Dedicated", "Reach 500 moves in a single game.", "🧘",
        AchievementTrigger.MovesReached(500)),

    Moves1000("moves_1000", "Marathoner", "Reach 1000 moves in a single game.", "🏃",
        AchievementTrigger.MovesReached(1000)),

    Score10k("score_10k", "Five figures", "Reach 10,000 in a single game.", "💵",
        AchievementTrigger.ScoreReached(10_000)),

    Score50k("score_50k", "Five-zero-K", "Reach 50,000 in a single game.", "💰",
        AchievementTrigger.ScoreReached(50_000)),

    Score100k("score_100k", "Six figures", "Reach 100,000 in a single game.", "🤑",
        AchievementTrigger.ScoreReached(100_000)),

    GamePlayed10("games_10", "Warming up", "Play 10 games.", "🎮",
        AchievementTrigger.GamesPlayedReached(10)),

    GamePlayed100("games_100", "Century club", "Play 100 games.", "💯",
        AchievementTrigger.GamesPlayedReached(100)),

    UndoAbstain("undo_abstain", "No regrets", "Win a game without using Undo.", "🧱",
        AchievementTrigger.WinWithoutUndo),

    Fast1024("fast_1024", "Speed cuber", "Reach 1024 within 50 moves.", "⏱️",
        AchievementTrigger.FastToTile(tile = 1024, maxMoves = 50)),

    SmallBoard("small_board", "Compact expert", "Win on a 3x3 board.", "🧩",
        AchievementTrigger.WinOnBoardSize(3)),

    BigBoard("big_board", "Big board boss", "Win on a 5x5 board.", "🗺️",
        AchievementTrigger.WinOnBoardSize(5)),

    MaxBoard("max_board", "Marathon board", "Win on a 6x6 board.", "🧭",
        AchievementTrigger.WinOnBoardSize(6));

    companion object {
        val All: List<Achievement> = values().toList()
        fun byId(id: String): Achievement? = values().firstOrNull { it.id == id }
    }
}

/** Trigger kinds. Each is checked in [AchievementEngine]. */
sealed class AchievementTrigger {
    data object MergeAny : AchievementTrigger()
    data class ReachTile(val tile: Int) : AchievementTrigger()
    data class ChainInOneMove(val count: Int) : AchievementTrigger()
    data class MovesReached(val count: Int) : AchievementTrigger()
    data class ScoreReached(val value: Int) : AchievementTrigger()
    data class GamesPlayedReached(val count: Int) : AchievementTrigger()
    data object WinWithoutUndo : AchievementTrigger()
    data class FastToTile(val tile: Int, val maxMoves: Int) : AchievementTrigger()
    data class WinOnBoardSize(val size: Int) : AchievementTrigger()
}