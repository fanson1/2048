package com.finley.android.merge2048.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.finley.android.merge2048.domain.Achievement
import kotlinx.coroutines.delay

@Composable
fun AchievementWall(
    unlockedIds: Set<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (row in Achievement.All.chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (ach in row) {
                    val unlocked = ach.id in unlockedIds
                    AchievementBadge(
                        achievement = ach,
                        unlocked = unlocked,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: Achievement,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (unlocked) 4.dp else 0.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (unlocked) GameColors.ButtonBackground.copy(alpha = 0.4f) else Color(0x22000000)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (unlocked)
                    GameColors.ScoreBlockBackground.copy(alpha = 0.5f)
                else
                    GameColors.TileEmpty.copy(alpha = 0.3f)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = achievement.emoji,
                fontSize = if (unlocked) 28.sp else 24.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = achievement.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) GameColors.HeaderText else GameColors.SubText.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Text(
                text = achievement.description,
                fontSize = 9.sp,
                color = GameColors.SubText.copy(alpha = if (unlocked) 1f else 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * A banner that pops in from the top when an achievement is unlocked,
 * then auto-dismisses after 3 seconds.
 */
@Composable
fun AchievementToast(
    achievementId: String?,
    onConsume: () -> Unit,
    modifier: Modifier = Modifier
) {
    val achievement = achievementId?.let { Achievement.byId(it) }

    if (achievement != null) {
        LaunchedEffect(achievementId) {
            delay(3000)
            onConsume()
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameColors.ButtonBackground)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${achievement.emoji} Achievement Unlocked!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = achievement.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}