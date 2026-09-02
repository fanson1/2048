package com.finley.android.merge2048

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    GameContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun GameContent(
    state: GameState,
    onIntent: (GameIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFfaf8ef))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2048",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF776e65)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreBlock(
                    label = "SCORE",
                    value = state.score
                )
                ScoreBlock(
                    label = "BEST",
                    value = state.bestScore
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SwipeableGameBoard(
            board = state.board,
            onSwipe = { direction -> onIntent(GameIntent.Move(direction)) },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onIntent(GameIntent.NewGame) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8f7a66)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "NEW GAME",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Swipe to move tiles. Combine matching numbers to reach 2048!",
            fontSize = 14.sp,
            color = Color(0xFF776e65),
            textAlign = TextAlign.Center
        )
    }

    if (state.showWinDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(GameIntent.DismissWinDialog) },
            title = { Text("Congratulations!") },
            text = { Text("You've reached 2048! Your score: ${state.score}") },
            confirmButton = {
                TextButton(onClick = { onIntent(GameIntent.NewGame) }) {
                    Text("Play Again")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(GameIntent.ContinueAfterWin) }) {
                    Text("Continue")
                }
            }
        )
    }

    if (state.isGameOver) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over!") },
            text = { Text("Final score: ${state.score}, Best: ${state.bestScore}") },
            confirmButton = {
                TextButton(onClick = { onIntent(GameIntent.NewGame) }) {
                    Text("Try Again")
                }
            }
        )
    }
}

@Composable
private fun ScoreBlock(
    label: String,
    value: Int
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFbbada0))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFeee4da)
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(tween(200)) togetherWith fadeOut(tween(100)))
            },
            label = "score"
        ) { target ->
            Text(
                text = target.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}