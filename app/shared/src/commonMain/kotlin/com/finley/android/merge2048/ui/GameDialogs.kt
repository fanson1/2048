package com.finley.android.merge2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.dialog_action_cancel
import merge2048.app.shared.generated.resources.dialog_action_confirm
import merge2048.app.shared.generated.resources.dialog_confirm_message
import merge2048.app.shared.generated.resources.dialog_confirm_title
import merge2048.app.shared.generated.resources.overlay_best_label
import merge2048.app.shared.generated.resources.overlay_close_button
import merge2048.app.shared.generated.resources.overlay_max_label
import merge2048.app.shared.generated.resources.overlay_score_label
import merge2048.app.shared.generated.resources.placeholder_em_dash
import org.jetbrains.compose.resources.stringResource
import com.finley.android.merge2048.ui.theme.GameColors

@Composable
fun GameOverlay(
    title: String,
    subtitle: String,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?,
    highlight: Color,
    onDismiss: (() -> Unit)? = null
) {
    val maxTileValue = if (maxTile == 0)
        stringResource(Res.string.placeholder_em_dash)
    else
        maxTile.toString()
    Dialog(
        onDismissRequest = onDismiss ?: {},
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GameColors.OverlayScrim)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
        Column(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(GameColors.Surface)
                .padding(horizontal = 28.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = GameColors.SubText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground)
                    .padding(horizontal = 32.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.overlay_score_label),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GameColors.ScoreLabel
                )
                Text(
                    text = score.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = GameColors.HeaderText
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OverlayStat(
                    label = stringResource(Res.string.overlay_best_label),
                    value = bestScore.toString()
                )
                OverlayStat(
                    label = stringResource(Res.string.overlay_max_label),
                    value = maxTileValue,
                    accent = GameColors.Tile2048
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = highlight
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = primaryLabel.uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }

            if (secondaryLabel != null && onSecondary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSecondary
                ) {
                    Text(
                        text = secondaryLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.SubText
                    )
                }
            }

            if (onDismiss != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = stringResource(Res.string.overlay_close_button),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GameColors.SubText.copy(alpha = 0.5f)
                    )
                }
            }
            }
        }
    }
}

/**
 * Centered confirmation overlay used before destructive actions (new game,
 * board-size change, challenge start) when a game is in progress. Guards the
 * player against accidentally losing progress by mis-tapping a control.
 */
@Composable
fun ConfirmOverlay(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = stringResource(Res.string.dialog_confirm_title)
    val message = stringResource(Res.string.dialog_confirm_message)
    val cancel = stringResource(Res.string.dialog_action_cancel)
    val confirm = stringResource(Res.string.dialog_action_confirm)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.OverlayScrim)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(GameColors.Surface)
                .clickable { /* swallow taps on the card */ }
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = GameColors.SubText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = cancel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.SubText
                    )
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.ButtonBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = confirm,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameColors.ButtonLabel
                    )
                }
            }
        }
    }
}