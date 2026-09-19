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
import merge2048.app.shared.generated.resources.daily_challenge_section_title
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
import com.finley.android.merge2048.domain.DailyChallengeResult
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.domain.LifetimeStats
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.ui.theme.formatScore
import org.jetbrains.compose.resources.stringResource

/**
 * Stats + history screen. Shows lifetime aggregates at the top, the last
 * game's score curve, and a scrollable list of the most recent games.
 * Abandoned in-progress games are shown as of the last finished game
 * aggregates (via [com.finley.android.merge2048.domain.LifetimeStats]) and
 * appear in the recent list with a resume affordance ([onResumeRecord]).
 */
@Composable
fun HistoryScreen(
    stats: LifetimeStats,
    records: List<GameRecord>,
    onBack: () -> Unit,
    dailyResults: Map<Int, DailyChallengeResult> = emptyMap(),
    todayDayNumber: Int = 0,
    onResumeRecord: (GameRecord) -> Unit = {}
) {
    val emDash = stringResource(Res.string.placeholder_em_dash)
    // Lifetime, leaderboard and the "last game" curve only make sense for
    // completed games — abandoned (incomplete) records are shown below.
    val finishedRecords = records.filter { !it.incomplete }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground)
            .padding(horizontal = 20.dp)
    ) {
        // ---- Header (pinned: stays at the top while the content scrolls) ----
        val backDesc = stringResource(Res.string.access_back)
        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 24.dp)
        ) {

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
                    value = formatScore(stats.bestScore),
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
                    value = formatScore(stats.averageScore),
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
        val leaderboard = finishedRecords.sortedByDescending { it.score }.take(5)
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
        val lastRecord = finishedRecords.firstOrNull()
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
                for ((size, s) in stats.perBoardSize.entries.sortedBy { it.key }) {
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

        // ---- Daily challenge records ----
        if (dailyResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.daily_challenge_section_title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = GameColors.SubText
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatsCard(title = "") {
                val ordered = dailyResults.entries
                    .sortedByDescending { it.key }
                    .take(14)
                for ((index, entry) in ordered.withIndex()) {
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x22FFFFFF))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    DailyChallengeRow(result = entry.value, todayDayNumber = todayDayNumber)
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
                GameRecordRow(record, onResume = onResumeRecord)
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
