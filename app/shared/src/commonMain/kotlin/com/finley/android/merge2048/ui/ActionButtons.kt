package com.finley.android.merge2048.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.access_pause
import merge2048.app.shared.generated.resources.access_resume
import merge2048.app.shared.generated.resources.button_pause
import merge2048.app.shared.generated.resources.button_resume
import merge2048.app.shared.generated.resources.challenge_daily
import merge2048.app.shared.generated.resources.challenge_timer
import merge2048.app.shared.generated.resources.new_game_button_desc
import merge2048.app.shared.generated.resources.new_game_button_text
import merge2048.app.shared.generated.resources.undo_button_desc
import merge2048.app.shared.generated.resources.undo_button_desc_with_count
import merge2048.app.shared.generated.resources.undo_button_text
import merge2048.app.shared.generated.resources.undo_button_text_with_count
import org.jetbrains.compose.resources.stringResource
import com.finley.android.merge2048.ui.theme.GameColors

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
    onClick: () -> Unit,
    completedToday: Boolean = false
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
            text = if (completedToday) "✓ ${stringResource(Res.string.challenge_daily)}"
            else stringResource(Res.string.challenge_daily),
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