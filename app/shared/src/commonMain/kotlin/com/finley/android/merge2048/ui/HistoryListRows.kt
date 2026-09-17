package com.finley.android.merge2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.domain.MergeRules
import com.finley.android.merge2048.domain.DailyChallengeResult
import com.finley.android.merge2048.domain.GameMode
import com.finley.android.merge2048.domain.GameRecord
import com.finley.android.merge2048.ui.theme.GameColors
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.daily_challenge_day_days_ago_format
import merge2048.app.shared.generated.resources.daily_challenge_day_today
import merge2048.app.shared.generated.resources.daily_challenge_day_yesterday
import merge2048.app.shared.generated.resources.daily_challenge_row_summary_format
import merge2048.app.shared.generated.resources.daily_challenge_row_won_format
import merge2048.app.shared.generated.resources.history_row_details_format
import merge2048.app.shared.generated.resources.history_row_details_won_format
import merge2048.app.shared.generated.resources.history_row_score_format
import merge2048.app.shared.generated.resources.mode_tag_daily
import merge2048.app.shared.generated.resources.mode_tag_timer
import merge2048.app.shared.generated.resources.stats_board_size_format
import merge2048.app.shared.generated.resources.time_ago_days_format
import merge2048.app.shared.generated.resources.time_ago_hours_format
import merge2048.app.shared.generated.resources.time_ago_minutes_format
import merge2048.app.shared.generated.resources.time_ago_now
import merge2048.app.shared.generated.resources.time_ago_weeks_format
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ModeBadge(mode: GameMode) {
    when (mode) {
        GameMode.NORMAL -> return
        GameMode.DAILY -> {
            Text(
                text = stringResource(Res.string.mode_tag_daily),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF8B5CF6))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        GameMode.TIMED -> {
            Text(
                text = stringResource(Res.string.mode_tag_timer),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE63B2E))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
internal fun MergeRuleBadge(ruleId: String) {
    val (label, bgColor) = when (ruleId) {
        "threes" -> "Threes!" to Color(0xFF8B5CF6)
        "fibonacci" -> "Fib" to Color(0xFFE63B2E)
        else -> "Classic" to Color(0xFF8F7A66)
    }
    Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun LeaderboardRow(rank: Int, record: GameRecord) {
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
        ModeBadge(mode = record.mode)
        MergeRuleBadge(ruleId = record.mergeRuleId)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatTimeAgo(record.finishedAtMs),
            fontSize = 11.sp,
            color = GameColors.SubText
        )
    }
}

@Composable
internal fun DailyChallengeRow(result: DailyChallengeResult, todayDayNumber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dayLabel(result.dayNumber, todayDayNumber),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.HeaderText
            )
            Text(
                text = if (result.won)
                    stringResource(
                        Res.string.daily_challenge_row_won_format,
                        result.score,
                        result.maxTile,
                        result.moveCount
                    )
                else
                    stringResource(
                        Res.string.daily_challenge_row_summary_format,
                        result.score,
                        result.maxTile,
                        result.moveCount
                    ),
                fontSize = 11.sp,
                color = GameColors.SubText
            )
        }
        Text(
            text = formatTimeAgo(result.finishedAtMs),
            fontSize = 11.sp,
            color = GameColors.SubText
        )
    }
}

@Composable
private fun dayLabel(dayNumber: Int, todayDayNumber: Int): String {
    val diff = todayDayNumber - dayNumber
    return when {
        diff <= 0 -> stringResource(Res.string.daily_challenge_day_today)
        diff == 1 -> stringResource(Res.string.daily_challenge_day_yesterday)
        else -> stringResource(Res.string.daily_challenge_day_days_ago_format, diff)
    }
}

@Composable
internal fun GameRecordRow(record: GameRecord) {
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
        ModeBadge(mode = record.mode)
        MergeRuleBadge(ruleId = record.mergeRuleId)
        Spacer(modifier = Modifier.width(8.dp))
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