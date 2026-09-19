package com.finley.android.merge2048.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.finley.android.merge2048.data.ShareService
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.MoveAnimationData
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.ui.ConfettiCelebration
import com.finley.android.merge2048.ui.GameOverSummary
import com.finley.android.merge2048.ui.GameOverlay
import com.finley.android.merge2048.ui.MergePopups
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.dialog_win_end_game
import merge2048.app.shared.generated.resources.dialog_win_keep_going
import merge2048.app.shared.generated.resources.dialog_win_subtitle
import merge2048.app.shared.generated.resources.dialog_win_title
import merge2048.app.shared.generated.resources.pause_overlay_resume
import merge2048.app.shared.generated.resources.pause_overlay_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BoardAndOverlays(
    board: List<List<Int>>,
    showWin: Boolean,
    isGameOver: Boolean,
    isPaused: Boolean,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    onSwipe: (Direction) -> Unit,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onEndGame: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier,
    lastMergePoints: Int = 0,
    lastMergePositions: List<Triple<Int, Int, Int>> = emptyList(),
    comboCount: Int = 0,
    boardSize: Int = 4,
    moveCount: Int = 0,
    totalMerges: Int = 0,
    isNewBest: Boolean = false,
    shareService: ShareService? = null,
    moveAnimationData: MoveAnimationData? = null
) {
    // Flexible region between header and footer. The game board is a square that
    // fits entirely within the remaining width/height, centered when there is slack,
    // so it never overflows on small, tall, or landscape screens.
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val boardDim = minOf(maxWidth, maxHeight)
        val boardModifier = Modifier
            .width(boardDim)
            .height(boardDim)

        Box(modifier = boardModifier) {
            SwipeableGameBoard(
                board = board,
                onSwipe = onSwipe,
                onNewGame = onNewGame,
                modifier = Modifier.fillMaxSize(),
                moveAnimationData = moveAnimationData
            )

            // Merge position popups with combo indicator
            MergePopups(
                mergePositions = lastMergePositions,
                comboCount = comboCount,
                boardSize = boardSize,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            )

            // Win overlay with confetti
            ConfettiCelebration(
                visible = showWin,
                modifier = Modifier.fillMaxSize()
            )

            // Win dialog with confetti behind it. Rendered as a modal Dialog so
            // the rest of the screen (header buttons, etc.) is blocked.
            if (showWin) {
                GameOverlay(
                    title = stringResource(Res.string.dialog_win_title),
                    subtitle = stringResource(Res.string.dialog_win_subtitle, maxTile),
                    score = score,
                    bestScore = bestScore,
                    maxTile = maxTile,
                    primaryLabel = stringResource(Res.string.dialog_win_keep_going),
                    onPrimary = onContinue,
                    secondaryLabel = stringResource(Res.string.dialog_win_end_game),
                    onSecondary = onEndGame,
                    highlight = GameColors.Tile2048,
                    onDismiss = null
                )
            }

            // Game over overlay with enhanced summary
            GameOverSummary(
                visible = isGameOver,
                score = score,
                bestScore = bestScore,
                maxTile = maxTile,
                moveCount = moveCount,
                totalMerges = totalMerges,
                isNewBest = isNewBest,
                onNewGame = onNewGame,
                onDismiss = onNewGame,
                shareService = shareService,
                boardSize = boardSize
            )

            // Pause overlay. Rendered as a modal Dialog so the rest of the screen is blocked.
            if (isPaused && !isGameOver) {
                Dialog(
                    onDismissRequest = onTogglePause,
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = false,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GameColors.OverlayScrim),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.pause_overlay_title),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp,
                                color = GameColors.HeaderText
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onTogglePause,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GameColors.ButtonBackground
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.pause_overlay_resume),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = GameColors.ButtonLabel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}