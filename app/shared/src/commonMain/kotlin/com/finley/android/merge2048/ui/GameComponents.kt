package com.finley.android.merge2048.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.access_pause
import merge2048.app.shared.generated.resources.access_resume
import merge2048.app.shared.generated.resources.button_pause
import merge2048.app.shared.generated.resources.button_resume
import merge2048.app.shared.generated.resources.challenge_daily
import merge2048.app.shared.generated.resources.challenge_timer
import merge2048.app.shared.generated.resources.combo_badge_text_format
import merge2048.app.shared.generated.resources.dialog_action_cancel
import merge2048.app.shared.generated.resources.dialog_action_confirm
import merge2048.app.shared.generated.resources.dialog_confirm_message
import merge2048.app.shared.generated.resources.dialog_confirm_title
import merge2048.app.shared.generated.resources.icon_undo
import merge2048.app.shared.generated.resources.new_game_button_desc
import merge2048.app.shared.generated.resources.new_game_button_text
import merge2048.app.shared.generated.resources.overlay_best_label
import merge2048.app.shared.generated.resources.overlay_close_button
import merge2048.app.shared.generated.resources.overlay_max_label
import merge2048.app.shared.generated.resources.overlay_score_label
import merge2048.app.shared.generated.resources.placeholder_em_dash
import merge2048.app.shared.generated.resources.score_block_content_desc_format
import merge2048.app.shared.generated.resources.undo_button_desc
import merge2048.app.shared.generated.resources.undo_button_desc_with_count
import merge2048.app.shared.generated.resources.undo_button_text
import merge2048.app.shared.generated.resources.undo_button_text_with_count
import org.jetbrains.compose.resources.stringResource
import com.finley.android.merge2048.GameColors
import com.finley.android.merge2048.formatScore

/**
 * Reusable presentational building blocks of the Merge2048 design system.
 * These are pure UI components with no game-state awareness; they are wired
 * up by the screen layer ([com.finley.android.merge2048.GameScreen]).
 */
@Composable
fun ScoreBlock(
    label: String,
    value: Int,
    compact: Boolean = false,
    highlight: Boolean = false
) {
    val animatedValue = remember { Animatable(0f) }
    val displayedValue = animatedValue.value.toInt()

    LaunchedEffect(value) {
        val start = animatedValue.value
        val target = value.toFloat()
        animatedValue.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Brief celebration pulse when a new record is set: scales the block up and back
    // down once (~900ms) instead of pulsing forever.
    val pulse = remember { Animatable(1f) }
    var wasHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(highlight) {
        if (highlight && !wasHighlighted) {
            wasHighlighted = true
            pulse.animateTo(1.15f, tween(150, easing = FastOutSlowInEasing))
            pulse.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 600f))
        } else if (!highlight) {
            wasHighlighted = false
        }
    }
    val scale = pulse.value

    Column(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.ScoreBlockBackground)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription = "${label} ${value}"
                liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
            }
            .padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 4.dp else 7.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.ScoreLabel
        )
        Text(
            text = formatScore(displayedValue),
            fontSize = if (compact) 18.sp else 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.width(5.dp))
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(tween(200)) togetherWith fadeOut(tween(100)))
            },
            label = "stat"
        ) { target ->
            Text(
                text = target,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }
    }
}

@Composable
fun OverlayStat(
    label: String,
    value: String,
    accent: Color = GameColors.SubText
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
fun UndoButton(
    enabled: Boolean,
    undoCount: Int = 0,
    onClick: () -> Unit
) {
    val desc = if (undoCount > 0)
        stringResource(Res.string.undo_button_desc_with_count, undoCount)
    else
        stringResource(Res.string.undo_button_desc)
    val text = if (undoCount > 0)
        stringResource(Res.string.undo_button_text_with_count, undoCount)
    else
        stringResource(Res.string.undo_button_text)
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground.copy(alpha = if (enabled) 1f else 0.4f),
            disabledContainerColor = GameColors.ButtonBackground.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        ),
        modifier = Modifier.semantics { contentDescription = desc }
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White
        )
    }
}

@Composable
fun NewGameButton(
    onClick: () -> Unit
) {
    val newGameDesc = stringResource(Res.string.new_game_button_desc)
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        ),
        modifier = Modifier.semantics {
            contentDescription = newGameDesc
        }
    ) {
        Text(
            text = stringResource(Res.string.new_game_button_text),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White
        )
    }
}

@Composable
fun PauseButton(
    paused: Boolean,
    onClick: () -> Unit
) {
    val desc = if (paused)
        stringResource(Res.string.access_resume)
    else
        stringResource(Res.string.access_pause)
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        ),
        modifier = Modifier.semantics { contentDescription = desc }
    ) {
        Text(
            text = if (paused)
                stringResource(Res.string.button_resume)
            else
                stringResource(Res.string.button_pause),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun DailyChallengeButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF8B5CF6) // Purple accent
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = stringResource(Res.string.challenge_daily),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun TimedChallengeButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE63B2E) // Fiery red accent
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = stringResource(Res.string.challenge_timer),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun TimedModeBanner(
    remainingSeconds: Int,
    bestScore: Int
) {
    val isUrgent = remainingSeconds <= 15
    val pulseTransition = rememberInfiniteTransition(label = "timer-pulse")
    val bgAlpha by pulseTransition.animateFloat(
        initialValue = if (isUrgent) 0.95f else 0.75f,
        targetValue = if (isUrgent) 0.6f else 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isUrgent) 400 else 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer-alpha"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE63B2E).copy(alpha = bgAlpha))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⏱ ${remainingSeconds}s",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Best: $bestScore",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun BoardSizeSelector(
    currentSize: Int,
    onSelect: (Int) -> Unit
) {
    val sizes = listOf(3, 4, 5, 6)
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.ScoreBlockBackground)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        for (size in sizes) {
            val isSelected = size == currentSize
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) GameColors.ButtonBackground
                        else Color.Transparent
                    )
                    .clickable { onSelect(size) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${size}",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else GameColors.SubText
                )
            }
        }
    }
}

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
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

@Composable
fun ComboBadge(
    count: Int,
    multiplier: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "combo-pulse")
    val scaleState = infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "combo-scale"
    )
    val scale = scaleState.value

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GameColors.Tile2048)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.combo_badge_text_format, count),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = GameColors.SubText
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = GameColors.HeaderText
        )
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