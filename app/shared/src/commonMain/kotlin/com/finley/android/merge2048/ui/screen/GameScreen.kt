package com.finley.android.merge2048.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.access_open_history
import merge2048.app.shared.generated.resources.access_open_settings
import merge2048.app.shared.generated.resources.game_footer_controls_hint
import merge2048.app.shared.generated.resources.game_footer_signature
import merge2048.app.shared.generated.resources.game_how_to_play_label
import merge2048.app.shared.generated.resources.game_title_2048
import merge2048.app.shared.generated.resources.icon_chart
import merge2048.app.shared.generated.resources.icon_settings
import merge2048.app.shared.generated.resources.placeholder_em_dash
import merge2048.app.shared.generated.resources.stat_label_best
import merge2048.app.shared.generated.resources.stat_label_max
import merge2048.app.shared.generated.resources.stat_label_moves
import merge2048.app.shared.generated.resources.stat_label_score
import com.finley.android.merge2048.data.createShareService
import com.finley.android.merge2048.domain.GameIntent
import com.finley.android.merge2048.domain.GameState
import com.finley.android.merge2048.presentation.GameViewModel
import com.finley.android.merge2048.presentation.rememberGameViewModel
import com.finley.android.merge2048.ui.theme.GameColors
import com.finley.android.merge2048.ui.ComboBadge
import com.finley.android.merge2048.ui.BoardSizeSelector
import com.finley.android.merge2048.ui.ConfirmOverlay
import com.finley.android.merge2048.ui.DailyChallengeButton
import com.finley.android.merge2048.ui.NewGameButton
import com.finley.android.merge2048.ui.PauseButton
import com.finley.android.merge2048.ui.ScoreBlock
import com.finley.android.merge2048.ui.StatPill
import com.finley.android.merge2048.ui.TileProgressBar
import com.finley.android.merge2048.ui.TimedChallengeButton
import com.finley.android.merge2048.ui.TimedModeBanner
import com.finley.android.merge2048.ui.UndoButton
import org.jetbrains.compose.resources.stringResource

/**
 * Screen layer: composes the presentational building blocks ([ui] package)
 * and wires them to the [GameViewModel]. It holds no game logic.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = rememberGameViewModel(),
    onOpenSettings: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onDismissAchievement: (String) -> Unit = {},
    dailyChallengeSeed: Int = 0,
    todayDayNumber: Int = 0
) {
    val state by viewModel.state.collectAsState()

    GameContent(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenSettings = onOpenSettings,
        onOpenHistory = onOpenHistory,
        onDismissAchievement = onDismissAchievement,
        dailyChallengeSeed = dailyChallengeSeed,
        todayDayNumber = todayDayNumber
    )
}

@Composable
internal fun GameContent(
    state: GameState,
    onIntent: (GameIntent) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onDismissAchievement: (String) -> Unit = {},
    dailyChallengeSeed: Int = 0,
    todayDayNumber: Int = 0
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        val shareService = createShareService()
        // Adapt to available space so the board never overflows, especially on
        // short/landscape screens.
        val compact = maxHeight < 640.dp
        val titleFont = if (compact) 44.sp else 52.sp
        val hintVisible = maxHeight > 520.dp
        val footerVisible = maxHeight > 470.dp
        val headerGap = if (compact) 8.dp else 12.dp
        val boardTopGap = if (compact) 10.dp else 14.dp

        // Guard against accidentally losing an in-progress game: destructive
        // actions (new game, board-size change, challenge start) only run after
        // the player confirms when there is progress on the line.
        var pendingConfirm by remember { mutableStateOf<GameIntent?>(null) }
        val hasProgress = state.score > 0 && state.moveCount > 0 && !state.isGameOver
        fun confirmOrRun(intent: GameIntent) {
            if (hasProgress) pendingConfirm = intent else onIntent(intent)
        }

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
                comboCount = state.comboCount,
                comboMultiplier = state.comboMultiplier,
                titleFont = titleFont,
                compact = compact,
                newBestThisSession = state.score > state.bestAtSessionStart && state.score > 0
            )

            Spacer(modifier = Modifier.height(headerGap))

            // ---------- Sub header: hint + new game ----------
            // Compact single row: small hint text + tight button group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hintVisible) {
                    Text(
                        text = stringResource(Res.string.game_how_to_play_label),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GameColors.SubText
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.horizontalScroll(
                        state = rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardSizeSelector(
                        currentSize = state.boardSize,
                        onSelect = { size -> confirmOrRun(GameIntent.ChangeBoardSize(size)) }
                    )
                    ChartIcon(onClick = onOpenHistory)
                    SettingsIcon(onClick = onOpenSettings)
                    UndoButton(
                        enabled = state.canUndo,
                        undoCount = state.undoCount,
                        onClick = { onIntent(GameIntent.Undo) }
                    )
                    NewGameButton(
                        onClick = { confirmOrRun(GameIntent.NewGame) }
                    )
                    PauseButton(
                        paused = state.isPaused,
                        onClick = { onIntent(GameIntent.TogglePause) }
                    )
                    DailyChallengeButton(
                        onClick = { confirmOrRun(GameIntent.StartDailyChallenge(seed = dailyChallengeSeed)) },
                        completedToday = state.user.dailyChallengeResults.containsKey(todayDayNumber)
                    )
                    TimedChallengeButton(
                        onClick = { confirmOrRun(GameIntent.StartTimedChallenge(durationSeconds = 60)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(boardTopGap))

            if (state.isTimedMode) {
                TimedModeBanner(
                    remainingSeconds = state.timedRemainingSeconds,
                    bestScore = state.timedBestScore
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---------- Board + overlays (flexible, fits remaining space) ----------
            BoardAndOverlays(
                board = state.board,
                showWin = state.showWinDialog,
                isGameOver = state.isGameOver,
                isPaused = state.isPaused,
                score = state.score,
                bestScore = state.bestScore,
                maxTile = state.maxTile,
                onSwipe = { direction -> onIntent(GameIntent.Move(direction)) },
                onNewGame = { onIntent(GameIntent.NewGame) },
                onContinue = { onIntent(GameIntent.ContinueAfterWin) },
                onDismissWin = { onIntent(GameIntent.DismissWinDialog) },
                onTogglePause = { onIntent(GameIntent.TogglePause) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                lastMergePoints = state.lastMergePoints,
                lastMergePositions = state.lastMergePositions,
                comboCount = state.comboCount,
                boardSize = state.boardSize,
                moveCount = state.moveCount,
                totalMerges = state.totalMerges,
                isNewBest = state.score > state.bestAtSessionStart && state.score > 0,
                shareService = shareService,
                moveAnimationData = state.moveAnimationData
            )

            // ---------- Footer ----------
            if (footerVisible) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.game_footer_controls_hint),
                        fontSize = 11.sp,
                        color = GameColors.SubText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.game_footer_signature),
                        fontSize = 11.sp,
                        color = GameColors.SubText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Confirmation overlay when a destructive action needs the player's OK.
        if (pendingConfirm != null) {
            ConfirmOverlay(
                onConfirm = {
                    val intent = pendingConfirm
                    pendingConfirm = null
                    if (intent != null) onIntent(intent)
                },
                onDismiss = { pendingConfirm = null }
            )
        }
    }
}

@Composable
private fun Header(
    score: Int,
    bestScore: Int,
    maxTile: Int,
    moveCount: Int,
    comboCount: Int,
    comboMultiplier: Float,
    titleFont: TextUnit,
    compact: Boolean,
    newBestThisSession: Boolean = false
) {
    val emDash = stringResource(Res.string.placeholder_em_dash)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(Res.string.game_title_2048),
                fontSize = titleFont,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText,
                lineHeight = titleFont
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatPill(
                    label = stringResource(Res.string.stat_label_max),
                    value = if (maxTile == 0) emDash else maxTile.toString(),
                    accent = GameColors.Tile2048
                )
                Spacer(modifier = Modifier.width(if (compact) 8.dp else 12.dp))
                StatPill(
                    label = stringResource(Res.string.stat_label_moves),
                    value = moveCount.toString(),
                    accent = GameColors.SubText
                )
                if (comboCount > 1) {
                    Spacer(modifier = Modifier.width(if (compact) 8.dp else 12.dp))
                    ComboBadge(count = comboCount, multiplier = comboMultiplier)
                }
            }

            // Progress bar to next tile
            if (!compact && maxTile > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                TileProgressBar(
                    maxTile = maxTile,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScoreBlock(
                label = stringResource(Res.string.stat_label_score),
                value = score,
                compact = compact
            )
            ScoreBlock(
                label = stringResource(Res.string.stat_label_best),
                value = bestScore,
                compact = compact,
                highlight = newBestThisSession
            )
        }
    }
}

@Composable
private fun ChartIcon(onClick: () -> Unit) {
    val desc = stringResource(Res.string.access_open_history)
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .semantics { contentDescription = desc }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.icon_chart),
            fontSize = 16.sp,
            color = GameColors.SubText
        )
    }
}

@Composable
private fun SettingsIcon(onClick: () -> Unit) {
    val desc = stringResource(Res.string.access_open_settings)
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .semantics { contentDescription = desc }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.icon_settings),
            fontSize = 18.sp,
            color = GameColors.SubText
        )
    }
}