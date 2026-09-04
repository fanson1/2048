package com.finley.android.merge2048.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Three-step first-launch tutorial. Shown once when [visible] is true.
 * Each step has a short title + body, swiped through with "Next" / "Got it".
 */
@Composable
fun TutorialOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    var step by remember { mutableStateOf(0) }
    val steps = listOf(
        TutorialStep(
            emoji = "🎮",
            title = "Welcome to Merge2048",
            body = "Swipe up, down, left or right to move all tiles. When two tiles with the same number touch, they merge into one!"
        ),
        TutorialStep(
            emoji = "🎯",
            title = "Reach 2048",
            body = "Your goal is to create a tile with 2048. Keep merging to push your best score higher and unlock achievements."
        ),
        TutorialStep(
            emoji = "💪",
            title = "You're in control",
            body = "Tap UNDO to take back a move, NEW GAME to start over, or ⚙ to change board size, animation and theme."
        )
    )
    val current = steps[step.coerceIn(0, steps.lastIndex)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC1A1512))
            .clickable(enabled = false) { /* scrim catches no clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(GameColors.Surface)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                label = "tutorial-step",
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 0.9f))
                }
            ) { s ->
                val item = steps[s.coerceIn(0, steps.lastIndex)]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.emoji,
                        fontSize = 56.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = item.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GameColors.HeaderText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.body,
                        fontSize = 14.sp,
                        color = GameColors.SubText,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (i == step) GameColors.ButtonBackground
                                else GameColors.TileEmpty
                            )
                            .size(if (i == step) 22.dp else 8.dp, 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (step > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { step-- }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Back",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GameColors.SubText
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameColors.ButtonBackground)
                        .clickable {
                            if (step < steps.lastIndex) step++ else onDismiss()
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (step < steps.lastIndex) "Next" else "Got it",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private data class TutorialStep(
    val emoji: String,
    val title: String,
    val body: String
)