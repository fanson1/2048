package com.finley.android.merge2048

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.presentation.GameViewModel
import com.finley.android.merge2048.ui.GameOverlay
import com.finley.android.merge2048.ui.NewGameButton
import com.finley.android.merge2048.ui.ScoreBlock
import com.finley.android.merge2048.ui.StatPill
import com.finley.android.merge2048.ui.UndoButton

/**
 * Screen layer: composes the presentational building blocks ([ui] package)
 * and wires them to the [GameViewModel]. It holds no game logic.
 */
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        // Adapt to available space so the board never overflows, especially on
        // short/landscape screens.
        val compact = maxHeight < 640.dp
        val titleFont = if (compact) 44.sp else 52.sp
        val hintVisible = maxHeight > 520.dp
        val footerVisible = maxHeight > 470.dp
        val headerGap = if (compact) 12.dp else 20.dp
        val boardTopGap = if (compact) 10.dp else 14.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .padding(horizontal = 16.dp, vertical = if (compact) 12.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Header ----------
            Header(
                score = state.score,
                bestScore = state.bestScore,
                maxTile = state.maxTile,
                moveCount = state.moveCount,
                titleFont = titleFont,
                compact = compact
            )

            Spacer(modifier = Modifier.height(headerGap))

            // ---------- Sub header: hint + new game ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hintVisible) {
                    Text(
                        text = "HOW TO PLAY:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = GameColors.SubText
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
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

            Spacer(modifier = Modifier.height(boardTopGap))

            // ---------- Board + overlays (flexible, fits remaining space) ----------
            BoardAndOverlays(
                board = state.board,
                showWin = state.showWinDialog,
                isGameOver = state.isGameOver,
                score = state.score,
                bestScore = state.bestScore,
                maxTile = state.maxTile,
                onSwipe = { direction -> onIntent(GameIntent.Move(direction)) },
                onNewGame = { onIntent(GameIntent.NewGame) },
                onContinue = { onIntent(GameIntent.ContinueAfterWin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // ---------- Footer ----------
            if (footerVisible) {
                Spacer(modifier = Modifier.height(10.dp))
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
}

@Composable
private fun Header(
    score: Int,
    bestScore: Int,
    maxTile: Int,
    moveCount: Int,
    titleFont: TextUnit,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "2048",
                fontSize = titleFont,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText,
                lineHeight = titleFont
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatPill(
                    label = "MAX",
                    value = if (maxTile == 0) "\u2014" else maxTile.toString(),
                    accent = GameColors.Tile2048
                )
                Spacer(modifier = Modifier.width(if (compact) 8.dp else 12.dp))
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
                value = score,
                compact = compact
            )
            ScoreBlock(
                label = "BEST",
                value = bestScore,
                compact = compact
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
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Flexible region between header and footer. The game board is a square that
    // fits entirely within the remaining width/height, centered when there is slack,
    // so it never overflows on small, tall, or landscape screens.
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val boardSize = minOf(maxWidth, maxHeight)
        val boardModifier = Modifier
            .width(boardSize)
            .height(boardSize)

        Box(modifier = boardModifier) {
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
}
