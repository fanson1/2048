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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.GameColors
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.domain.LifetimeStats

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ---- Header ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2190",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = GameColors.HeaderText,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 12.dp)
            )
            Text(
                text = "Stats & History",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---- Lifetime aggregates card ----
        StatsCard(title = "LIFETIME") {
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(
                    label = "GAMES",
                    value = stats.gamesPlayed.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBlock(
                    label = "WIN RATE",
                    value = if (stats.gamesPlayed == 0) "—"
                    else "${(stats.winRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(
                    label = "BEST SCORE",
                    value = stats.bestScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBlock(
                    label = "BEST MAX",
                    value = if (stats.bestMaxTile == 0) "—" else stats.bestMaxTile.toString(),
                    accent = GameColors.Tile2048,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(
                    label = "AVG SCORE",
                    value = stats.averageScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatBlock(
                    label = "BEST MOVE",
                    value = stats.bestSingleMove.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---- Last game curve ----
        val lastRecord = records.firstOrNull()
        if (lastRecord != null && lastRecord.scoreOverTime.size > 1) {
            Spacer(modifier = Modifier.height(20.dp))
            StatsCard(title = "LAST GAME") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatBlock(
                        label = "SCORE",
                        value = lastRecord.score.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatBlock(
                        label = "MAX TILE",
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
            StatsCard(title = "BY BOARD SIZE") {
                for ((size, s) in stats.perBoardSize.toSortedMap()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${size}x$size",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GameColors.HeaderText
                        )
                        Text(
                            text = "best ${s.bestScore} \u2022 ${s.gamesPlayed} games",
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
                text = "RECENT GAMES",
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
                text = "Play your first game to see your stats here.",
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
            text = record.boardSize.toString() + "x" + record.boardSize,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GameColors.SubText,
            modifier = Modifier.width(34.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${record.score} pts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.HeaderText
            )
            Text(
                text = "${record.moveCount} moves \u2022 max ${record.maxTile}${if (record.won) " \u2022 \uD83C\uDFC6" else ""}",
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

/** Lightweight, locale-free "x minutes ago" formatter for compact timestamps. */
private fun formatTimeAgo(epochMs: Long): String {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val delta = (now - epochMs).coerceAtLeast(0)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        delta < minute -> "now"
        delta < hour -> "${delta / minute}m"
        delta < day -> "${delta / hour}h"
        delta < 7 * day -> "${delta / day}d"
        else -> "${delta / (7 * day)}w"
    }
}