package com.finley.android.merge2048

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF776e65)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SCORE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFbbada0)
                )
                Text(
                    text = state.score.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
            text = { Text("Final score: ${state.score}") },
            confirmButton = {
                TextButton(onClick = { onIntent(GameIntent.NewGame) }) {
                    Text("Try Again")
                }
            }
        )
    }
}