package com.finley.android.merge2048.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.domain.MoveAnimationData
import com.finley.android.merge2048.domain.TileMovement
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.ui.theme.tileBackgroundColor
import com.finley.android.merge2048.ui.theme.tileFontFraction
import com.finley.android.merge2048.ui.theme.tileTextColor
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.tile_content_desc_empty
import merge2048.app.shared.generated.resources.tile_content_desc_value
import org.jetbrains.compose.resources.stringResource

private const val TILE_ANIM_DURATION_MS = 120
private const val SPAWN_ANIM_DURATION_MS = 200
private const val MERGE_POP_DURATION_MS = 200

/**
 * A single tile that animates from its previous position to its current position.
 *
 * KEY FIX: [key(moveId, movement) { ... }] wraps the entire animation block.
 * When the move changes, Compose discards the old subtree (including old Animatables)
 * and creates NEW ones with correct initial values. The moveId is unique per move, so
 * even two structurally identical moves (same merge into the same cell) restart the
 * animation. This eliminates the ghost that occurred because LaunchedEffect runs
 * asynchronously — the old Animatable value was rendered (or stale-reset) before the
 * effect could start.
 */
@Composable
internal fun AnimatedTile(
    value: Int,
    row: Int,
    col: Int,
    boardSize: Int,
    moveAnimationData: MoveAnimationData?,
    modifier: Modifier = Modifier
) {
    val movement = remember(moveAnimationData, row, col) {
        moveAnimationData?.movements?.find { m ->
            when (m) {
                is TileMovement.Stayed -> m.row == row && m.col == col
                is TileMovement.Slid -> m.toRow == row && m.toCol == col
                is TileMovement.Merged -> m.toRow == row && m.toCol == col
                is TileMovement.Spawned -> m.row == row && m.col == col
            }
        }
    }

    // key(moveId, movement) forces full recomposition of this subtree whenever the
    // move changes. The moveId is monotonic, so even two structurally identical moves
    // (same merge into the same cell) count as different keys — otherwise Compose would
    // reuse the old settled Animatables and the tile would pop in place instead of
    // animating. Animatable initial values are computed from the movement synchronously,
    // so there is never a stale frame.
    key(moveAnimationData?.moveId ?: -1L, movement) {
        val offsetX = remember {
            Animatable(
                when (movement) {
                    is TileMovement.Slid -> (movement.fromCol - col).toFloat()
                    is TileMovement.Merged -> (movement.from1Col - col).toFloat()
                    else -> 0f
                }
            )
        }
        val offsetY = remember {
            Animatable(
                when (movement) {
                    is TileMovement.Slid -> (movement.fromRow - row).toFloat()
                    is TileMovement.Merged -> (movement.from1Row - row).toFloat()
                    else -> 0f
                }
            )
        }
        val mergeOffsetX = remember {
            Animatable(
                if (movement is TileMovement.Merged) (movement.from2Col - col).toFloat() else 0f
            )
        }
        val mergeOffsetY = remember {
            Animatable(
                if (movement is TileMovement.Merged) (movement.from2Row - row).toFloat() else 0f
            )
        }
        val mergeSecondAlpha = remember {
            Animatable(if (movement is TileMovement.Merged) 1f else 0f)
        }
        val scale = remember {
            Animatable(
                when (movement) {
                    is TileMovement.Spawned -> 0.3f
                    is TileMovement.Merged -> 0.8f
                    else -> 1f
                }
            )
        }

        LaunchedEffect(boardSize) {
            when (movement) {
                is TileMovement.Slid -> {
                    offsetX.animateTo(0f, tween(TILE_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                }
                is TileMovement.Merged -> {
                    offsetX.animateTo(0f, tween(TILE_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                    mergeOffsetX.animateTo(0f, tween(TILE_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                    mergeSecondAlpha.animateTo(0f, tween(80, easing = LinearEasing))
                    scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
                }
                is TileMovement.Spawned -> {
                    scale.animateTo(1f, tween(SPAWN_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                }
                is TileMovement.Stayed, null -> { }
            }
        }

        // --- Visual properties ---
        val tileDescEmpty = stringResource(Res.string.tile_content_desc_empty)
        val tileDescValue = stringResource(Res.string.tile_content_desc_value, 0)
        val isHigh = value >= 256
        val isMilestone = value >= 2048
        val glowColor = when {
            value >= 2048 -> GameColors.Tile2048
            value >= 1024 -> Color(0xFFEDC53F)
            value >= 512 -> Color(0xFFEDC850)
            value >= 256 -> Color(0xFFEDCC61)
            else -> Color(0x00000000)
        }

        // Only high tiles get the pulsing glow + scale animation. Empty cells and
        // ordinary tiles render fully static — an infinite transition per cell would
        // otherwise drive recomposition for the entire 6x6 board every frame.
        var glowAlpha = 0f
        var scalePulse = 1f
        if (isHigh) {
            val infiniteTransition = rememberInfiniteTransition(label = "tile-glow")
            glowAlpha = infiniteTransition.animateFloat(
                initialValue = if (isMilestone) 0.3f else 0.1f,
                targetValue = if (isMilestone) 0.7f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(if (isMilestone) 800 else 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow-pulse"
            ).value
            scalePulse = infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isMilestone) 1.02f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale-pulse"
            ).value
        }

        val glowModifier = if (isHigh) {
            modifier
                .shadow(if (isMilestone) 16.dp else 10.dp, RoundedCornerShape(8.dp), spotColor = glowColor.copy(alpha = glowAlpha))
                .fillMaxSize()
        } else {
            modifier.fillMaxSize()
        }

        // --- Main tile + optional ghost tile for merge animation ---
        // The background lives INSIDE the graphicsLayer (inner Box) so it
        // transforms together with the Text.  Previously the background was on
        // the outer Box while the Text was in the inner Box with graphicsLayer —
        // during any animation the background stayed put while the number moved
        // away or scaled down, making tiles appear as "background only, no number".
        Box(
            modifier = glowModifier
                .semantics {
                    contentDescription = if (value == 0) tileDescEmpty
                    else tileDescValue.replace("%1\$d", value.toString())
                },
            contentAlignment = Alignment.Center
        ) {
            // Main tile content — background + text slide/scale together.
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX.value * size.width
                        translationY = offsetY.value * size.height
                        scaleX = scale.value * scalePulse
                        scaleY = scale.value * scalePulse
                    }
                    .background(if (value == 0) GameColors.TileEmpty else tileBackgroundColor(value), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (value != 0) {
                    val cellFont = (maxWidth.value * tileFontFraction(value)).sp
                    Text(
                        text = value.toString(),
                        fontSize = cellFont,
                        fontWeight = FontWeight.Bold,
                        color = tileTextColor(value),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            // Ghost tile for merge animation — the second tile slides from its origin
            // to the merge point and fades out.  Its graphicsLayer is independent of
            // the main tile's so the two translations do NOT add up.
            if (movement is TileMovement.Merged && mergeSecondAlpha.value > 0f) {
                val halfValue = movement.value / 2
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = mergeOffsetX.value * size.width
                            translationY = mergeOffsetY.value * size.height
                            alpha = mergeSecondAlpha.value
                        }
                        .background(tileBackgroundColor(halfValue), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val cellFont = (maxWidth.value * tileFontFraction(halfValue)).sp
                    Text(
                        text = halfValue.toString(),
                        fontSize = cellFont,
                        fontWeight = FontWeight.Bold,
                        color = tileTextColor(halfValue),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}