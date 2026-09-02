package com.finley.android.merge2048

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Header ----------
            Header(
                score = state.score,
                bestScore = state.bestScore,
                maxTile = state.maxTile,
                moveCount = state.moveCount
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Sub header: hint + new game ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOW TO PLAY:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = GameColors.SubText
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UndoButton(
                        enabled = state.canUndo,
                        onClick = { onIntent(GameIntent.Undo) }
                    )
                    NewGameButton(
                        onClick = { onIntent(GameIntent.NewGame) }
                    )
                }
            }
            Text(
                text = "Swipe to move tiles. Merge same numbers to combine them.",
                fontSize = 12.sp,
                color = GameColors.SubText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- Board + overlays ----------
            BoardAndOverlays(
                board = state.board,
                showWin = state.showWinDialog,
                isGameOver = state.isGameOver,
                score = state.score,
                bestScore = state.bestScore,
                maxTile = state.maxTile,
                onSwipe = { direction -> onIntent(GameIntent.Move(direction)) },
                onNewGame = { onIntent(GameIntent.NewGame) },
                onContinue = { onIntent(GameIntent.ContinueAfterWin) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // ---------- Footer ----------
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Swipe or use arrow keys / WASD \u2022 R to restart \u2022 Undo to go back",
                    fontSize = 11.sp,
                    color = GameColors.SubText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Made for fun \u2022 Merge2048",
                    fontSize = 11.sp,
                    color = GameColors.SubText.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Header(
    score: Int,
    bestScore: Int,
    maxTile: Int,
    moveCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "2048",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText,
                lineHeight = 54.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatPill(
                    label = "MAX",
                    value = if (maxTile == 0) "\u2014" else maxTile.toString(),
                    accent = GameColors.Tile2048
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatPill(
                    label = "MOVES",
                    value = moveCount.toString(),
                    accent = GameColors.SubText
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScoreBlock(
                label = "SCORE",
                value = score
            )
            ScoreBlock(
                label = "BEST",
                value = bestScore
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.width(5.dp))
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(tween(200)) togetherWith fadeOut(tween(100)))
            },
            label = "stat"
        ) { target ->
            Text(
                text = target,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }
    }
}

@Composable
private fun BoardAndOverlays(
    board: List<List<Int>>,
    showWin: Boolean,
    isGameOver: Boolean,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    onSwipe: (Direction) -> Unit,
    onNewGame: () -> Unit,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        SwipeableGameBoard(
            board = board,
            onSwipe = onSwipe,
            onNewGame = onNewGame,
            modifier = Modifier.fillMaxSize()
        )

        // Win overlay
        AnimatedVisibility(
            visible = showWin,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            GameOverlay(
                title = "You win!",
                subtitle = "You made the 2048 tile!",
                score = score,
                bestScore = bestScore,
                maxTile = maxTile,
                primaryLabel = "Play again",
                onPrimary = onNewGame,
                secondaryLabel = "Keep going",
                onSecondary = onContinue,
                highlight = GameColors.Tile2048
            )
        }

        // Game over overlay
        AnimatedVisibility(
            visible = isGameOver,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            GameOverlay(
                title = "Game over",
                subtitle = "Board is full.",
                score = score,
                bestScore = bestScore,
                maxTile = maxTile,
                primaryLabel = "Try again",
                onPrimary = onNewGame,
                secondaryLabel = null,
                onSecondary = null,
                highlight = GameColors.ButtonBackground
            )
        }
    }
}

@Composable
private fun GameOverlay(
    title: String,
    subtitle: String,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?,
    highlight: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCCFAF8EF))
            .clip(RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFFFFF))
                .padding(horizontal = 28.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = GameColors.SubText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground)
                    .padding(horizontal = 32.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GameColors.ScoreLabel
                )
                Text(
                    text = score.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OverlayStat(label = "BEST", value = bestScore.toString())
                OverlayStat(label = "MAX", value = if (maxTile == 0) "\u2014" else maxTile.toString(), accent = GameColors.Tile2048)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = highlight
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = primaryLabel.uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }

            if (secondaryLabel != null && onSecondary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSecondary
                ) {
                    Text(
                        text = secondaryLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.SubText
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayStat(
    label: String,
    value: String,
    accent: Color = GameColors.SubText
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.SubText
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = accent
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
            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.ScoreBlockBackground)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.ScoreLabel
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(tween(200))) togetherWith
                    (fadeOut(tween(100)))
            },
            label = "score"
        ) { target ->
            Text(
                text = target.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun UndoButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground.copy(alpha = if (enabled) 1f else 0.4f),
            disabledContainerColor = GameColors.ButtonBackground.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = "\u21A9 UNDO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White
        )
    }
}

@Composable
private fun NewGameButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = "NEW GAME",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White
        )
    }
}