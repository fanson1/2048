package com.finley.android.merge2048.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.ui.theme.GameColors

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