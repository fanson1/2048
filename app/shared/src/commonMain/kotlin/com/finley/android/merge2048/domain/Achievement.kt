package com.finley.android.merge2048.domain

import org.jetbrains.compose.resources.StringResource
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.ach_big_board_desc
import merge2048.app.shared.generated.resources.ach_big_board_emoji
import merge2048.app.shared.generated.resources.ach_big_board_title
import merge2048.app.shared.generated.resources.ach_chain_5_desc
import merge2048.app.shared.generated.resources.ach_chain_5_emoji
import merge2048.app.shared.generated.resources.ach_chain_5_title
import merge2048.app.shared.generated.resources.ach_chain_8_desc
import merge2048.app.shared.generated.resources.ach_chain_8_emoji
import merge2048.app.shared.generated.resources.ach_chain_8_title
import merge2048.app.shared.generated.resources.ach_fast_1024_desc
import merge2048.app.shared.generated.resources.ach_fast_1024_emoji
import merge2048.app.shared.generated.resources.ach_fast_1024_title
import merge2048.app.shared.generated.resources.ach_first_1024_desc
import merge2048.app.shared.generated.resources.ach_first_1024_emoji
import merge2048.app.shared.generated.resources.ach_first_1024_title
import merge2048.app.shared.generated.resources.ach_first_128_desc
import merge2048.app.shared.generated.resources.ach_first_128_emoji
import merge2048.app.shared.generated.resources.ach_first_128_title
import merge2048.app.shared.generated.resources.ach_first_2048_desc
import merge2048.app.shared.generated.resources.ach_first_2048_emoji
import merge2048.app.shared.generated.resources.ach_first_2048_title
import merge2048.app.shared.generated.resources.ach_first_32_desc
import merge2048.app.shared.generated.resources.ach_first_32_emoji
import merge2048.app.shared.generated.resources.ach_first_32_title
import merge2048.app.shared.generated.resources.ach_first_4096_desc
import merge2048.app.shared.generated.resources.ach_first_4096_emoji
import merge2048.app.shared.generated.resources.ach_first_4096_title
import merge2048.app.shared.generated.resources.ach_first_512_desc
import merge2048.app.shared.generated.resources.ach_first_512_emoji
import merge2048.app.shared.generated.resources.ach_first_512_title
import merge2048.app.shared.generated.resources.ach_first_8192_desc
import merge2048.app.shared.generated.resources.ach_first_8192_emoji
import merge2048.app.shared.generated.resources.ach_first_8192_title
import merge2048.app.shared.generated.resources.ach_first_8_desc
import merge2048.app.shared.generated.resources.ach_first_8_emoji
import merge2048.app.shared.generated.resources.ach_first_8_title
import merge2048.app.shared.generated.resources.ach_first_merge_desc
import merge2048.app.shared.generated.resources.ach_first_merge_emoji
import merge2048.app.shared.generated.resources.ach_first_merge_title
import merge2048.app.shared.generated.resources.ach_games_10_desc
import merge2048.app.shared.generated.resources.ach_games_10_emoji
import merge2048.app.shared.generated.resources.ach_games_10_title
import merge2048.app.shared.generated.resources.ach_games_100_desc
import merge2048.app.shared.generated.resources.ach_games_100_emoji
import merge2048.app.shared.generated.resources.ach_games_100_title
import merge2048.app.shared.generated.resources.ach_max_board_desc
import merge2048.app.shared.generated.resources.ach_max_board_emoji
import merge2048.app.shared.generated.resources.ach_max_board_title
import merge2048.app.shared.generated.resources.ach_moves_100_desc
import merge2048.app.shared.generated.resources.ach_moves_100_emoji
import merge2048.app.shared.generated.resources.ach_moves_100_title
import merge2048.app.shared.generated.resources.ach_moves_1000_desc
import merge2048.app.shared.generated.resources.ach_moves_1000_emoji
import merge2048.app.shared.generated.resources.ach_moves_1000_title
import merge2048.app.shared.generated.resources.ach_moves_500_desc
import merge2048.app.shared.generated.resources.ach_moves_500_emoji
import merge2048.app.shared.generated.resources.ach_moves_500_title
import merge2048.app.shared.generated.resources.ach_score_100k_desc
import merge2048.app.shared.generated.resources.ach_score_100k_emoji
import merge2048.app.shared.generated.resources.ach_score_100k_title
import merge2048.app.shared.generated.resources.ach_score_10k_desc
import merge2048.app.shared.generated.resources.ach_score_10k_emoji
import merge2048.app.shared.generated.resources.ach_score_10k_title
import merge2048.app.shared.generated.resources.ach_score_50k_desc
import merge2048.app.shared.generated.resources.ach_score_50k_emoji
import merge2048.app.shared.generated.resources.ach_score_50k_title
import merge2048.app.shared.generated.resources.ach_small_board_desc
import merge2048.app.shared.generated.resources.ach_small_board_emoji
import merge2048.app.shared.generated.resources.ach_small_board_title
import merge2048.app.shared.generated.resources.ach_undo_abstain_desc
import merge2048.app.shared.generated.resources.ach_undo_abstain_emoji
import merge2048.app.shared.generated.resources.ach_undo_abstain_title

/**
 * Catalog of unlockable achievements. Each entry declares a deterministic
 * [trigger] function over the pre/post move context, plus a stable [id] and
 * presentation fields backed by i18n resources so the UI can be translated.
 *
 * The catalog is the single source of truth for both the "is this unlocked"
 * test and the "how do I show it" UI.
 */
enum class Achievement(
    val id: String,
    val emoji: StringResource,
    val title: StringResource,
    val description: StringResource,
    val trigger: AchievementTrigger
) {
    FirstMerge(
        "first_merge",
        Res.string.ach_first_merge_emoji,
        Res.string.ach_first_merge_title,
        Res.string.ach_first_merge_desc,
        AchievementTrigger.MergeAny
    ),

    First8(
        "first_8",
        Res.string.ach_first_8_emoji,
        Res.string.ach_first_8_title,
        Res.string.ach_first_8_desc,
        AchievementTrigger.ReachTile(8)
    ),

    First32(
        "first_32",
        Res.string.ach_first_32_emoji,
        Res.string.ach_first_32_title,
        Res.string.ach_first_32_desc,
        AchievementTrigger.ReachTile(32)
    ),

    First128(
        "first_128",
        Res.string.ach_first_128_emoji,
        Res.string.ach_first_128_title,
        Res.string.ach_first_128_desc,
        AchievementTrigger.ReachTile(128)
    ),

    First512(
        "first_512",
        Res.string.ach_first_512_emoji,
        Res.string.ach_first_512_title,
        Res.string.ach_first_512_desc,
        AchievementTrigger.ReachTile(512)
    ),

    First1024(
        "first_1024",
        Res.string.ach_first_1024_emoji,
        Res.string.ach_first_1024_title,
        Res.string.ach_first_1024_desc,
        AchievementTrigger.ReachTile(1024)
    ),

    First2048(
        "first_2048",
        Res.string.ach_first_2048_emoji,
        Res.string.ach_first_2048_title,
        Res.string.ach_first_2048_desc,
        AchievementTrigger.ReachTile(2048)
    ),

    First4096(
        "first_4096",
        Res.string.ach_first_4096_emoji,
        Res.string.ach_first_4096_title,
        Res.string.ach_first_4096_desc,
        AchievementTrigger.ReachTile(4096)
    ),

    First8192(
        "first_8192",
        Res.string.ach_first_8192_emoji,
        Res.string.ach_first_8192_title,
        Res.string.ach_first_8192_desc,
        AchievementTrigger.ReachTile(8192)
    ),

    Chain5(
        "chain_5",
        Res.string.ach_chain_5_emoji,
        Res.string.ach_chain_5_title,
        Res.string.ach_chain_5_desc,
        AchievementTrigger.ChainInOneMove(5)
    ),

    Chain8(
        "chain_8",
        Res.string.ach_chain_8_emoji,
        Res.string.ach_chain_8_title,
        Res.string.ach_chain_8_desc,
        AchievementTrigger.ChainInOneMove(8)
    ),

    Moves100(
        "moves_100",
        Res.string.ach_moves_100_emoji,
        Res.string.ach_moves_100_title,
        Res.string.ach_moves_100_desc,
        AchievementTrigger.MovesReached(100)
    ),

    Moves500(
        "moves_500",
        Res.string.ach_moves_500_emoji,
        Res.string.ach_moves_500_title,
        Res.string.ach_moves_500_desc,
        AchievementTrigger.MovesReached(500)
    ),

    Moves1000(
        "moves_1000",
        Res.string.ach_moves_1000_emoji,
        Res.string.ach_moves_1000_title,
        Res.string.ach_moves_1000_desc,
        AchievementTrigger.MovesReached(1000)
    ),

    Score10k(
        "score_10k",
        Res.string.ach_score_10k_emoji,
        Res.string.ach_score_10k_title,
        Res.string.ach_score_10k_desc,
        AchievementTrigger.ScoreReached(10_000)
    ),

    Score50k(
        "score_50k",
        Res.string.ach_score_50k_emoji,
        Res.string.ach_score_50k_title,
        Res.string.ach_score_50k_desc,
        AchievementTrigger.ScoreReached(50_000)
    ),

    Score100k(
        "score_100k",
        Res.string.ach_score_100k_emoji,
        Res.string.ach_score_100k_title,
        Res.string.ach_score_100k_desc,
        AchievementTrigger.ScoreReached(100_000)
    ),

    GamePlayed10(
        "games_10",
        Res.string.ach_games_10_emoji,
        Res.string.ach_games_10_title,
        Res.string.ach_games_10_desc,
        AchievementTrigger.GamesPlayedReached(10)
    ),

    GamePlayed100(
        "games_100",
        Res.string.ach_games_100_emoji,
        Res.string.ach_games_100_title,
        Res.string.ach_games_100_desc,
        AchievementTrigger.GamesPlayedReached(100)
    ),

    UndoAbstain(
        "undo_abstain",
        Res.string.ach_undo_abstain_emoji,
        Res.string.ach_undo_abstain_title,
        Res.string.ach_undo_abstain_desc,
        AchievementTrigger.WinWithoutUndo
    ),

    Fast1024(
        "fast_1024",
        Res.string.ach_fast_1024_emoji,
        Res.string.ach_fast_1024_title,
        Res.string.ach_fast_1024_desc,
        AchievementTrigger.FastToTile(tile = 1024, maxMoves = 50)
    ),

    SmallBoard(
        "small_board",
        Res.string.ach_small_board_emoji,
        Res.string.ach_small_board_title,
        Res.string.ach_small_board_desc,
        AchievementTrigger.WinOnBoardSize(3)
    ),

    BigBoard(
        "big_board",
        Res.string.ach_big_board_emoji,
        Res.string.ach_big_board_title,
        Res.string.ach_big_board_desc,
        AchievementTrigger.WinOnBoardSize(5)
    ),

    MaxBoard(
        "max_board",
        Res.string.ach_max_board_emoji,
        Res.string.ach_max_board_title,
        Res.string.ach_max_board_desc,
        AchievementTrigger.WinOnBoardSize(6)
    );

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
