package com.finley.android.merge2048.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.access_back
import merge2048.app.shared.generated.resources.icon_back_arrow
import merge2048.app.shared.generated.resources.icon_check_mark
import merge2048.app.shared.generated.resources.screen_settings_title
import merge2048.app.shared.generated.resources.settings_achievements_progress_format
import merge2048.app.shared.generated.resources.settings_animation_full
import merge2048.app.shared.generated.resources.settings_animation_full_desc
import merge2048.app.shared.generated.resources.settings_animation_off
import merge2048.app.shared.generated.resources.settings_animation_off_desc
import merge2048.app.shared.generated.resources.settings_animation_reduced
import merge2048.app.shared.generated.resources.settings_animation_reduced_desc
import merge2048.app.shared.generated.resources.settings_board_size_chip_format
import merge2048.app.shared.generated.resources.settings_board_size_classic
import merge2048.app.shared.generated.resources.settings_board_size_fast
import merge2048.app.shared.generated.resources.settings_board_size_large
import merge2048.app.shared.generated.resources.settings_board_size_massive
import merge2048.app.shared.generated.resources.settings_dark_mode_label
import merge2048.app.shared.generated.resources.settings_section_achievements
import merge2048.app.shared.generated.resources.settings_section_animation
import merge2048.app.shared.generated.resources.settings_section_appearance
import merge2048.app.shared.generated.resources.settings_section_board_size
import merge2048.app.shared.generated.resources.settings_section_sound
import merge2048.app.shared.generated.resources.settings_section_stats
import merge2048.app.shared.generated.resources.settings_section_theme
import merge2048.app.shared.generated.resources.settings_sound_effects_label
import merge2048.app.shared.generated.resources.settings_switch_off
import merge2048.app.shared.generated.resources.settings_switch_on
import merge2048.app.shared.generated.resources.settings_theme_locked_format
import merge2048.app.shared.generated.resources.stat_label_best_max_tile
import merge2048.app.shared.generated.resources.stat_label_best_score_value
import merge2048.app.shared.generated.resources.stat_label_games_played
import merge2048.app.shared.generated.resources.stat_label_total_score
import com.finley.android.merge2048.GameColors
import com.finley.android.merge2048.domain.AnimationLevel
import com.finley.android.merge2048.domain.Achievement
import com.finley.android.merge2048.domain.UserPreferences
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    prefs: UserPreferences,
    onUpdate: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ---- Header ----
        val backDesc = stringResource(Res.string.access_back)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.icon_back_arrow),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = GameColors.HeaderText,
                modifier = Modifier
                    .clickable { onBack() }
                    .semantics { contentDescription = backDesc }
                    .padding(end = 12.dp)
            )
            Text(
                text = stringResource(Res.string.screen_settings_title),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ---- Board Size ----
        SettingSection(stringResource(Res.string.settings_section_board_size)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(3, 4, 5, 6).forEach { size ->
                    BoardSizeChip(
                        size = size,
                        selected = prefs.boardSize == size,
                        onClick = { onUpdate(prefs.copy(boardSize = size)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Theme ----
        SettingSection(stringResource(Res.string.settings_section_theme)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                com.finley.android.merge2048.domain.GameTheme.all.forEach { theme ->
                    val unlocked = theme.isUnlocked(prefs.gamesPlayed, prefs.bestMaxTile)
                    val displayName = stringResource(theme.displayName)
                    val unlockRequirement = stringResource(theme.unlockRequirement)
                    SelectableRow(
                        label = displayName,
                        subtitle = if (unlocked) unlockRequirement
                        else stringResource(Res.string.settings_theme_locked_format, unlockRequirement),
                        selected = prefs.themeId == theme.id,
                        enabled = unlocked,
                        onClick = { if (unlocked) onUpdate(prefs.copy(themeId = theme.id)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Animation Level ----
        SettingSection(stringResource(Res.string.settings_section_animation)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AnimationLevel.entries.forEach { level ->
                    val label = when (level) {
                        AnimationLevel.FULL -> stringResource(Res.string.settings_animation_full)
                        AnimationLevel.REDUCED -> stringResource(Res.string.settings_animation_reduced)
                        AnimationLevel.OFF -> stringResource(Res.string.settings_animation_off)
                    }
                    val desc = when (level) {
                        AnimationLevel.FULL -> stringResource(Res.string.settings_animation_full_desc)
                        AnimationLevel.REDUCED -> stringResource(Res.string.settings_animation_reduced_desc)
                        AnimationLevel.OFF -> stringResource(Res.string.settings_animation_off_desc)
                    }
                    SelectableRow(
                        label = label,
                        subtitle = desc,
                        selected = prefs.animationLevel == level,
                        onClick = { onUpdate(prefs.copy(animationLevel = level)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Dark Mode ----
        SettingSection(stringResource(Res.string.settings_section_appearance)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground.copy(alpha = 0.3f))
                    .clickable { onUpdate(prefs.copy(darkMode = !prefs.darkMode)) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.settings_dark_mode_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.HeaderText
                    )
                    Text(
                        text = if (prefs.darkMode) stringResource(Res.string.settings_switch_on)
                        else stringResource(Res.string.settings_switch_off),
                        fontSize = 13.sp,
                        color = GameColors.SubText
                    )
                }
                Switch(
                    checked = prefs.darkMode,
                    onCheckedChange = { onUpdate(prefs.copy(darkMode = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GameColors.ButtonBackground,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = GameColors.TileEmpty
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Sound ----
        SettingSection(stringResource(Res.string.settings_section_sound)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground.copy(alpha = 0.3f))
                    .clickable { onUpdate(prefs.copy(soundEnabled = !prefs.soundEnabled)) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.settings_sound_effects_label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.HeaderText
                    )
                    Text(
                        text = if (prefs.soundEnabled) stringResource(Res.string.settings_switch_on)
                        else stringResource(Res.string.settings_switch_off),
                        fontSize = 13.sp,
                        color = GameColors.SubText
                    )
                }
                Switch(
                    checked = prefs.soundEnabled,
                    onCheckedChange = { onUpdate(prefs.copy(soundEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GameColors.ButtonBackground,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = GameColors.TileEmpty
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ---- Stats ----
        SettingSection(stringResource(Res.string.settings_section_stats)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow(stringResource(Res.string.stat_label_games_played), prefs.gamesPlayed.toString())
                StatRow(stringResource(Res.string.stat_label_best_score_value), prefs.bestScore.toString())
                StatRow(stringResource(Res.string.stat_label_best_max_tile), prefs.bestMaxTile.toString())
                StatRow(stringResource(Res.string.stat_label_total_score), prefs.totalScore.toString())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ---- Achievements ----
        SettingSection(stringResource(Res.string.settings_section_achievements)) {
            val unlockedCount = prefs.unlockedAchievementIds.size
            val totalCount = Achievement.All.size
            Text(
                text = stringResource(
                    Res.string.settings_achievements_progress_format,
                    unlockedCount,
                    totalCount
                ),
                fontSize = 14.sp,
                color = GameColors.SubText
            )
            Spacer(modifier = Modifier.height(10.dp))
            AchievementWall(
                unlockedIds = prefs.unlockedAchievementIds,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable () -> Unit) {
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
private fun BoardSizeChip(
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
private fun SelectableRow(
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
