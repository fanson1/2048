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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.combo_badge_text_format
import org.jetbrains.compose.resources.stringResource
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.ui.theme.formatScore

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