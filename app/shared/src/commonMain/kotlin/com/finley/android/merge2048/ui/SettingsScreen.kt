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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.GameColors
import com.finley.android.merge2048.domain.AnimationLevel
import com.finley.android.merge2048.domain.Achievement
import com.finley.android.merge2048.domain.UserPreferences

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2190",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = GameColors.HeaderText,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 12.dp)
            )
            Text(
                text = "Settings",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ---- Board Size ----
        SettingSection("BOARD SIZE") {
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
        SettingSection("THEME") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                com.finley.android.merge2048.domain.GameTheme.all.forEach { theme ->
                    val unlocked = theme.isUnlocked(prefs.gamesPlayed, prefs.bestMaxTile)
                    SelectableRow(
                        label = theme.displayName,
                        subtitle = if (unlocked) theme.unlockRequirement else "Locked: ${theme.unlockRequirement}",
                        selected = prefs.themeId == theme.id,
                        enabled = unlocked,
                        onClick = { if (unlocked) onUpdate(prefs.copy(themeId = theme.id)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Animation Level ----
        SettingSection("ANIMATION") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AnimationLevel.entries.forEach { level ->
                    val label = when (level) {
                        AnimationLevel.FULL -> "Full"
                        AnimationLevel.REDUCED -> "Reduced"
                        AnimationLevel.OFF -> "Off"
                    }
                    val desc = when (level) {
                        AnimationLevel.FULL -> "All animations and effects"
                        AnimationLevel.REDUCED -> "Quick transitions, no long slides"
                        AnimationLevel.OFF -> "Instant, no transitions"
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
        SettingSection("APPEARANCE") {
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
                        text = "Dark mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.HeaderText
                    )
                    Text(
                        text = if (prefs.darkMode) "On" else "Off",
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
        SettingSection("SOUND") {
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
                        text = "Sound effects",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.HeaderText
                    )
                    Text(
                        text = if (prefs.soundEnabled) "On" else "Off",
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
        SettingSection("STATS") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow("Games played", prefs.gamesPlayed.toString())
                StatRow("Best score", prefs.bestScore.toString())
                StatRow("Best max tile", prefs.bestMaxTile.toString())
                val totalMerges = prefs.totalMerges
                StatRow("Total score", prefs.totalScore.toString())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ---- Achievements ----
        SettingSection("ACHIEVEMENTS") {
            val unlockedCount = prefs.unlockedAchievementIds.size
            val totalCount = Achievement.All.size
            Text(
                text = "$unlockedCount / $totalCount unlocked",
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
                text = "${size}x$size",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (selected) Color.White else GameColors.HeaderText
            )
            Text(
                text = if (size == 4) "Classic" else if (size == 3) "Fast" else if (size == 5) "Large" else "Massive",
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
                text = "\u2714",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.ButtonBackground
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
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