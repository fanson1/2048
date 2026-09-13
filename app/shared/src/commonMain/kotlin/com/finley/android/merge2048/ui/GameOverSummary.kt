package com.finley.android.merge2048.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.game_over_avg_merge_label
import merge2048.app.shared.generated.resources.game_over_avg_merge_zero
import merge2048.app.shared.generated.resources.game_over_best_label
import merge2048.app.shared.generated.resources.game_over_close
import merge2048.app.shared.generated.resources.game_over_efficiency_format
import merge2048.app.shared.generated.resources.game_over_efficiency_label
import merge2048.app.shared.generated.resources.game_over_efficiency_zero
import merge2048.app.shared.generated.resources.game_over_max_label
import merge2048.app.shared.generated.resources.game_over_merges_label
import merge2048.app.shared.generated.resources.game_over_moves_label
import merge2048.app.shared.generated.resources.game_over_msg_default
import merge2048.app.shared.generated.resources.game_over_msg_decent
import merge2048.app.shared.generated.resources.game_over_msg_good_effort
import merge2048.app.shared.generated.resources.game_over_msg_great
import merge2048.app.shared.generated.resources.game_over_msg_incredible
import merge2048.app.shared.generated.resources.game_over_msg_legendary
import merge2048.app.shared.generated.resources.game_over_msg_nice
import merge2048.app.shared.generated.resources.game_over_msg_strong_score
import merge2048.app.shared.generated.resources.game_over_new_record_title
import merge2048.app.shared.generated.resources.game_over_play_again
import merge2048.app.shared.generated.resources.game_over_score_label
import merge2048.app.shared.generated.resources.game_over_title
import merge2048.app.shared.generated.resources.share_button
import merge2048.app.shared.generated.resources.share_copied
import merge2048.app.shared.generated.resources.share_message_template
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.data.ShareService
import com.finley.android.merge2048.ui.theme.formatScore
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Enhanced game-over summary modal showing detailed session stats.
 * Includes: score, max tile, moves, merges, average merge value,
 * and encouragement message based on performance.
 */
@Composable
fun GameOverSummary(
    visible: Boolean,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    moveCount: Int,
    totalMerges: Int,
    isNewBest: Boolean,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit,
    shareService: ShareService? = null,
    boardSize: Int = 4
) {
    var shareStatus by remember { mutableStateOf<String?>(null) }
    val shareMessage = stringResource(Res.string.share_message_template, score, boardSize)
    val copiedText = stringResource(Res.string.share_copied)

    fun sharePressed() {
        val result = shareService?.share(shareMessage)
        shareStatus = if (result != null) copiedText else null
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f, animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(GameColors.OverlayScrim)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(GameColors.Surface)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = if (isNewBest) stringResource(Res.string.game_over_new_record_title)
                    else stringResource(Res.string.game_over_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isNewBest) GameColors.Tile2048 else GameColors.HeaderText
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getEncouragementMessage(maxTile, score),
                    fontSize = 12.sp,
                    color = GameColors.SubText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Score card
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameColors.ScoreBlockBackground)
                        .padding(horizontal = 32.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.game_over_score_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = GameColors.ScoreLabel
                    )
                    Text(
                        text = formatScore(score),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = GameColors.HeaderText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStat(
                        label = stringResource(Res.string.game_over_best_label),
                        value = formatScore(bestScore)
                    )
                    SummaryStat(
                        label = stringResource(Res.string.game_over_max_label),
                        value = maxTile.toString(),
                        accent = GameColors.Tile2048
                    )
                    SummaryStat(
                        label = stringResource(Res.string.game_over_moves_label),
                        value = moveCount.toString()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStat(
                        label = stringResource(Res.string.game_over_merges_label),
                        value = totalMerges.toString()
                    )
                    SummaryStat(
                        label = stringResource(Res.string.game_over_avg_merge_label),
                        value = if (totalMerges > 0) (score / totalMerges).toString()
                        else stringResource(Res.string.game_over_avg_merge_zero)
                    )
                    SummaryStat(
                        label = stringResource(Res.string.game_over_efficiency_label),
                        value = if (moveCount > 0)
                            stringResource(Res.string.game_over_efficiency_format, totalMerges * 100 / moveCount)
                        else stringResource(Res.string.game_over_efficiency_zero)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Button(
                    onClick = onNewGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.ButtonBackground
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.game_over_play_again),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = GameColors.ButtonLabel
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (shareService != null) {
                    Button(
                        onClick = { sharePressed() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D4DB8)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = if (shareStatus != null) stringResource(Res.string.share_copied)
                            else stringResource(Res.string.share_button),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(Res.string.game_over_close),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.SubText
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    accent: Color = GameColors.HeaderText
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.SubText
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}

@Composable
private fun getEncouragementMessage(maxTile: Int, score: Int): String {
    return when {
        maxTile >= 4096 -> stringResource(Res.string.game_over_msg_legendary)
        maxTile >= 2048 -> stringResource(Res.string.game_over_msg_incredible)
        maxTile >= 1024 -> stringResource(Res.string.game_over_msg_great)
        maxTile >= 512 -> stringResource(Res.string.game_over_msg_nice)
        maxTile >= 256 -> stringResource(Res.string.game_over_msg_good_effort)
        score >= 5000 -> stringResource(Res.string.game_over_msg_strong_score)
        score >= 2000 -> stringResource(Res.string.game_over_msg_decent)
        else -> stringResource(Res.string.game_over_msg_default)
    }
}
