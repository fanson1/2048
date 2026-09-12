package com.finley.android.merge2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.access_back
import merge2048.app.shared.generated.resources.history_row_details_format
import merge2048.app.shared.generated.resources.history_row_details_won_format
import merge2048.app.shared.generated.resources.history_row_score_format
import merge2048.app.shared.generated.resources.icon_back_arrow
import merge2048.app.shared.generated.resources.leaderboard_section_title
import merge2048.app.shared.generated.resources.placeholder_em_dash
import merge2048.app.shared.generated.resources.recent_games_section_title
import merge2048.app.shared.generated.resources.screen_stats_history_title
import merge2048.app.shared.generated.resources.stat_label_avg_score
import merge2048.app.shared.generated.resources.stat_label_best_max
import merge2048.app.shared.generated.resources.stat_label_best_move
import merge2048.app.shared.generated.resources.stat_label_best_score
import merge2048.app.shared.generated.resources.stat_label_games
import merge2048.app.shared.generated.resources.stat_label_max_tile
import merge2048.app.shared.generated.resources.stat_label_score
import merge2048.app.shared.generated.resources.stat_label_win_rate
import merge2048.app.shared.generated.resources.stat_value_win_rate_format
import merge2048.app.shared.generated.resources.stats_board_size_format
import merge2048.app.shared.generated.resources.stats_card_by_board_size
import merge2048.app.shared.generated.resources.stats_card_last_game
import merge2048.app.shared.generated.resources.stats_card_lifetime
import merge2048.app.shared.generated.resources.stats_empty_message
import merge2048.app.shared.generated.resources.stats_per_size_summary_format
import merge2048.app.shared.generated.resources.time_ago_days_format
import merge2048.app.shared.generated.resources.time_ago_hours_format
import merge2048.app.shared.generated.resources.time_ago_minutes_format
import merge2048.app.shared.generated.resources.time_ago_now
import merge2048.app.shared.generated.resources.time_ago_weeks_format
import com.finley.android.merge2048.GameColors
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.domain.LifetimeStats
import org.jetbrains.compose.resources.stringResource

/**
 * Stats + history screen. Shows lifetime aggregates at the top, the last
 * game's score curve, and a scrollable list of the most recent finished games.
 */
@Composable
fun HistoryScreen(
    stats: LifetimeStats,
    records: List<GameRecord>,
    onBack: () -> Unit
) {
    val emDash = stringResource(Res.string.placeholder_em_dash)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ---- Header ----
        val backDesc = stringResource(Res.string.access_back)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.icon_back_arrow),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = GameColors.HeaderText,
                modifier = Modifier
                    .clickable { onBack() }
                    .semantics { contentDescription = backDesc }
                    .padding(end = 12.dp)
            )
            Text(
                text = stringResource(Res.string.screen_stats_history_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---- Lifetime aggregates card ----
        StatsCard(title = stringResource(Res.string.stats_card_lifetime)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(
                    label = stringResource(Res.string.stat_label_games),
                    value = stats.gamesPlayed.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBlock(
                    label = stringResource(Res.string.stat_label_win_rate),
                    value = if (stats.gamesPlayed == 0) emDash
                    else stringResource(Res.string.stat_value_win_rate_format, (stats.winRate * 100).toInt()),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(
                    label = stringResource(Res.string.stat_label_best_score),
                    value = stats.bestScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBlock(
                    label = stringResource(Res.string.stat_label_best_max),
                    value = if (stats.bestMaxTile == 0) emDash else stats.bestMaxTile.toString(),
                    accent = GameColors.Tile2048,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(
                    label = stringResource(Res.string.stat_label_avg_score),
                    value = stats.averageScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBlock(
                    label = stringResource(Res.string.stat_label_best_move),
                    value = stats.bestSingleMove.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---- Local leaderboard (top all-time) ----
        val leaderboard = records.sortedByDescending { it.score }.take(5)
        if (leaderboard.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.leaderboard_section_title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = GameColors.SubText
            )
            Spacer(modifier = Modifier.height(8.dp))
            for ((index, record) in leaderboard.withIndex()) {
                LeaderboardRow(rank = index + 1, record = record)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // ---- Last game curve ----
        val lastRecord = records.firstOrNull()
        if (lastRecord != null && lastRecord.scoreOverTime.size > 1) {
            Spacer(modifier = Modifier.height(20.dp))
            StatsCard(title = stringResource(Res.string.stats_card_last_game)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatBlock(
                        label = stringResource(Res.string.stat_label_score),
                        value = lastRecord.score.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatBlock(
                        label = stringResource(Res.string.stat_label_max_tile),
                        value = lastRecord.maxTile.toString(),
                        accent = GameColors.Tile2048,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                SparkLine(
                    points = lastRecord.scoreOverTime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }

        // ---- Per board size ----
        if (stats.perBoardSize.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            StatsCard(title = stringResource(Res.string.stats_card_by_board_size)) {
                for ((size, s) in stats.perBoardSize.toSortedMap()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.stats_board_size_format, size),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GameColors.HeaderText
                        )
                        Text(
                            text = stringResource(
                                Res.string.stats_per_size_summary_format,
                                s.bestScore,
                                s.gamesPlayed
                            ),
                            fontSize = 12.sp,
                            color = GameColors.SubText
                        )
                    }
                }
            }
        }

        // ---- Recent games list ----
        if (records.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.recent_games_section_title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = GameColors.SubText
            )
            Spacer(modifier = Modifier.height(8.dp))
            for (record in records.take(20)) {
                GameRecordRow(record)
                Spacer(modifier = Modifier.height(6.dp))
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(Res.string.stats_empty_message),
                fontSize = 14.sp,
                color = GameColors.SubText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(GameColors.Surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = GameColors.SubText
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun StatBlock(
    label: String,
    value: String,
    accent: Color = GameColors.HeaderText,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.SubText
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}

@Composable
private fun LeaderboardRow(rank: Int, record: GameRecord) {
    val medalColor = when (rank) {
        1 -> Color(0xFFF2B705) // gold
        2 -> Color(0xFFB0C4D8) // silver
        3 -> Color(0xFFCD7F32) // bronze
        else -> GameColors.Surface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(26.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(medalColor)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.history_row_score_format, record.score),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.HeaderText
            )
            Text(
                text = stringResource(Res.string.stats_board_size_format, record.boardSize),
                fontSize = 11.sp,
                color = GameColors.SubText
            )
        }
        Text(
            text = formatTimeAgo(record.finishedAtMs),
            fontSize = 11.sp,
            color = GameColors.SubText
        )
    }
}

@Composable
private fun GameRecordRow(record: GameRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.Surface)
            .clickable { /* future: open detail */ }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.stats_board_size_format, record.boardSize),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GameColors.SubText,
            modifier = Modifier.width(34.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.history_row_score_format, record.score),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.HeaderText
            )
            Text(
                text = if (record.won)
                    stringResource(Res.string.history_row_details_won_format, record.moveCount, record.maxTile)
                else
                    stringResource(Res.string.history_row_details_format, record.moveCount, record.maxTile),
                fontSize = 11.sp,
                color = GameColors.SubText
            )
        }
        Text(
            text = formatTimeAgo(record.finishedAtMs),
            fontSize = 11.sp,
            color = GameColors.SubText
        )
    }
}

/** Lightweight "x minutes ago" formatter. Uses stringResource so each label is localizable. */
@Composable
private fun formatTimeAgo(epochMs: Long): String {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val delta = (now - epochMs).coerceAtLeast(0)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        delta < minute -> stringResource(Res.string.time_ago_now)
        delta < hour -> stringResource(Res.string.time_ago_minutes_format, (delta / minute).toInt())
        delta < day -> stringResource(Res.string.time_ago_hours_format, (delta / hour).toInt())
        delta < 7 * day -> stringResource(Res.string.time_ago_days_format, (delta / day).toInt())
        else -> stringResource(Res.string.time_ago_weeks_format, (delta / (7 * day)).toInt())
    }
}
