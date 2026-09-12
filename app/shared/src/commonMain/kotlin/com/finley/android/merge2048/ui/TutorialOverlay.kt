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
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.tutorial_back_button
import merge2048.app.shared.generated.resources.tutorial_got_it_button
import merge2048.app.shared.generated.resources.tutorial_next_button
import merge2048.app.shared.generated.resources.tutorial_step_1_body
import merge2048.app.shared.generated.resources.tutorial_step_1_emoji
import merge2048.app.shared.generated.resources.tutorial_step_1_title
import merge2048.app.shared.generated.resources.tutorial_step_2_body
import merge2048.app.shared.generated.resources.tutorial_step_2_emoji
import merge2048.app.shared.generated.resources.tutorial_step_2_title
import merge2048.app.shared.generated.resources.tutorial_step_3_body
import merge2048.app.shared.generated.resources.tutorial_step_3_emoji
import merge2048.app.shared.generated.resources.tutorial_step_3_title
import com.finley.android.merge2048.ui.theme.GameColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
            emoji = Res.string.tutorial_step_1_emoji,
            title = Res.string.tutorial_step_1_title,
            body = Res.string.tutorial_step_1_body
        ),
        TutorialStep(
            emoji = Res.string.tutorial_step_2_emoji,
            title = Res.string.tutorial_step_2_title,
            body = Res.string.tutorial_step_2_body
        ),
        TutorialStep(
            emoji = Res.string.tutorial_step_3_emoji,
            title = Res.string.tutorial_step_3_title,
            body = Res.string.tutorial_step_3_body
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
                        text = stringResource(item.emoji),
                        fontSize = 56.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(item.title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GameColors.HeaderText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(item.body),
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
                            text = stringResource(Res.string.tutorial_back_button),
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
                        text = if (step < steps.lastIndex)
                            stringResource(Res.string.tutorial_next_button)
                        else
                            stringResource(Res.string.tutorial_got_it_button),
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
    val emoji: StringResource,
    val title: StringResource,
    val body: StringResource
)
