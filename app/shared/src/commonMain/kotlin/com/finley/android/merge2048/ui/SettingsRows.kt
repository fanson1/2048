package com.finley.android.merge2048.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.ui.theme.GameColors
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.icon_check_mark
import merge2048.app.shared.generated.resources.settings_board_size_chip_format
import merge2048.app.shared.generated.resources.settings_board_size_classic
import merge2048.app.shared.generated.resources.settings_board_size_fast
import merge2048.app.shared.generated.resources.settings_board_size_large
import merge2048.app.shared.generated.resources.settings_board_size_massive
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = GameColors.SubText
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
internal fun BoardSizeChip(
    size: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (selected) 6.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = if (selected) GameColors.ButtonBackground.copy(alpha = 0.5f) else Color(0x33000000)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) GameColors.ButtonBackground else GameColors.ScoreBlockBackground)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.settings_board_size_chip_format, size),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (selected) Color.White else GameColors.HeaderText
            )
            Text(
                text = when (size) {
                    3 -> stringResource(Res.string.settings_board_size_fast)
                    4 -> stringResource(Res.string.settings_board_size_classic)
                    5 -> stringResource(Res.string.settings_board_size_large)
                    else -> stringResource(Res.string.settings_board_size_massive)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White.copy(alpha = 0.8f) else GameColors.SubText
            )
        }
    }
}

@Composable
internal fun SelectableRow(
    label: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (!enabled) GameColors.ScoreBlockBackground.copy(alpha = 0.15f)
                else if (selected) GameColors.ButtonBackground.copy(alpha = 0.15f)
                else GameColors.ScoreBlockBackground.copy(alpha = 0.3f)
            )
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) GameColors.ButtonBackground else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) GameColors.ButtonBackground else GameColors.HeaderText
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = GameColors.SubText
            )
        }
        if (selected) {
            Text(
                text = stringResource(Res.string.icon_check_mark),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.ButtonBackground
            )
        }
    }
}