# Merge2048

A cross-platform **2048** puzzle game built with Kotlin Multiplatform + Compose Multiplatform (Material 3). Designed to feel and look like a polished, commercial-grade casual game. Fully localized in **English** and **Simplified Chinese** with in-app language switching.

## Platform support

| Platform | Status |
|----------|--------|
| Android | ✅ |
| iOS | ✅ |
| Desktop (JVM) | ✅ |
| Web (Wasm + JS) | ✅ |
| Server (Ktor) | ✅ |

## Features

**Gameplay**
- Classic 4x4 2048 rules with a full MVI state machine
- Custom board sizes: **3x3**, **4x4**, **5x5**, **6x6**
- **Undo** — step back one full move (`UNDO` button, only active when available)
- Score + **Best score** tracking with per-board-size bests (persisted between sessions)
- **Daily Challenge** — same seeded board for everyone every day
- Win / game-over overlays with a full round summary (Score, Best, Max, Moves, Merges, Efficiency, Avg Merge) and motivational messages
- Full keyboard support on desktop/web: **arrow keys / WASD** to move, **R** to restart

**Localization**
- **English** and **Simplified Chinese** (230 strings each, incl. game text, settings, achievements)
- **In-app language switching** — System default / English / 简体中文, persisted and applied at the framework level on Android

**Customization & settings**
- **Themes**: Classic, Dark, and Neon (Dark unlocks after 5 games, Neon at max tile 1024+) with fully recolored palettes
- **Dark mode** toggle (with system-follow fallback)
- **Animation level**: Full / Reduced / Off for accessibility
- **Sound effects** toggle
- Best-score and lifetime stats, achievement progress wall

**Achievements & progression**
- 20+ unlockable achievements (first N tile, merge chains, big boards, games/moves milestones)
- Achievement toasts in-game, celebration **confetti** on unlocks, and a full wall in Settings
- Goals and unlock requirements fully localized

**History & stats**
- Game history screen with **win rate**, lifetime aggregate stats, and a per-game **score sparkline**

**Visual & interaction polish**
- Warm, classic 2048 color palette with premium gradients
- Tile pop-in + merge pop animations, direction-aware board slide animation
- Floating score popups on merges, golden glow on high-value tiles (256+)
- First-launch **tutorial overlay**, animated score cards
- Responsive layout: content capped at 480dp, auto-adapts to landscape/short screens (compact header, progressive UI degradation), board scales to fit available space without overflow

**App icon**
- Custom-designed icon on every platform: Android adaptive + legacy mipmaps, iOS AppIcon, Desktop window/package icon, Web favicon

## Architecture

Strict layered MVI split into clean packages, with a pure-Kotlin domain core that is fully JVM-testable (zero Compose/Android deps):

```
com.finley.android.merge2048
├── domain/             # Pure Kotlin logic — zero Compose/Android deps, JVM-testable
│   ├── Direction.kt        # move direction enum
│   ├── GameState.kt        # immutable game snapshot
│   ├── GameIntent.kt       # user actions (sealed class)
│   ├── GameEngine.kt       # game rules (board, moves, merges, undo, win/game-over)
│   ├── GameReducer.kt      # ★ MVI heart: Intent → State reducer (owns best/win bookkeeping)
│   ├── GameSnapshot.kt     # serializable per-game record payload
│   ├── Achievement.kt      # achievement catalog (title/desc/emoji, localized)
│   ├── AchievementEngine.kt# achievement detection & unlocking
│   ├── DailyChallenge.kt   # date-seeded board generation
│   ├── GameTheme.kt        # classic/dark/neon color themes + unlock rules
│   ├── UserPreferences.kt  # persisted settings (incl. language tag)
│   └── ...                 # GameRecord, TileMovement, LifetimeStats, AnimationLevel
├── data/               # persistence & platform services (expect/actual)
│   ├── SettingsRepository.kt   # JSON UserPreferences via multiplatform-settings
│   ├── GameHistoryRepository.kt# serialized game records
│   ├── GameRepository.kt
│   ├── PlatformSettings.kt     # initPlatformStorage / createSettings (expect/actual)
│   ├── LocaleHelper.kt         # setAppLocale(tag) (expect/actual)
│   ├── SoundService.kt         # sound effects (expect/actual)
│   └── ShareService.kt         # share results (expect/actual)
├── presentation/
│   ├── GameViewModel.kt    # thin shell: holds StateFlow, forwards intents to the reducer
│   └── GameViewModelFactory.kt # expect/actual ViewModel retrieval (Android real ViewModel, others construct directly)
├── ui/
│   ├── navigation/AppNavigation.kt # sealed-class navigation (Game / Settings / History)
│   ├── screen/GameScreen.kt        # screen assembly layer (no business logic)
│   ├── screen/SwipeableGameBoard.kt# board grid + gesture/keyboard handling
│   ├── theme/GameTheme.kt          # GameColors palette + font/score helpers
│   ├── GameComponents.kt           # reusable design-system composables
│   ├── GameOverSummary.kt          # win / game-over dialog & summaries
│   ├── SettingsScreen.kt           # settings screen
│   ├── HistoryScreen.kt            # history + stats
│   ├── AchievementComponents.kt    # achievement wall / toasts
│   ├── ConfettiCelebration.kt      # unlock celebration
│   ├── FloatingScore.kt, SparkLine.kt, TileProgressBar.kt, TutorialOverlay.kt
└── App.kt               # root composable: platform context + navigation wiring
```

### Why a reducer?

All game behavior (move scoring, best-score tracking, win-dialog gating, achievement detection,
undo bookkeeping, state derivation) lives in the pure `domain/GameReducer` (plus
`AchievementEngine`). The `GameViewModel` becomes a passive shell, and every state
transition is unit-tested on the JVM from `commonTest` without Android or Compose
dependencies.

## Persistence & platform services

- **Settings & records** persist via `multiplatform-settings` (SharedPreferences on
  Android, NSUserDefaults on iOS, file on JVM/JS/Wasm), serialized with kotlinx.serialization.
- **Language switching** uses `AppCompatDelegate.setApplicationLocales()` on Android
  (BCP 47 tags, `system`/`en`/`zh`); other platforms are no-ops that follow the OS locale.

## Project layout

- `app/androidApp/` — Android entry point + launcher resources + ProGuard rules
- `app/iosApp/` — iOS entry point + AppIcon assets
- `app/desktopApp/` — Desktop (JVM) entry point + window/package icon
- `app/webApp/` — Web entry point + favicon
- `app/shared/` — shared Compose Multiplatform UI, domain, and data code
- `core/` — code shared by all targets
- `server/` — Ktor server application

## Running the apps

- Android: `./gradlew :app:androidApp:assembleDebug`
- Desktop:
  - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
  - Standard run: `./gradlew :app:desktopApp:run`
- Web:
  - Wasm (faster, modern browsers): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
  - JS (older browser support): `./gradlew :app:webApp:jsBrowserDevelopmentRun`
- iOS: open `app/iosApp` in Xcode and run from there
- Server: `./gradlew :server:run`

## Running tests

- Shared JVM tests: `./gradlew :app:shared:jvmTest`
- Android host tests: `./gradlew :app:shared:testAndroidHostTest`
- Web: `./gradlew :app:shared:jsTest` / `./gradlew :app:shared:wasmJsTest`
- iOS: `./gradlew :app:shared:iosSimulatorArm64Test`
- Server: `./gradlew :server:test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform), and
[Kotlin/Wasm](https://kotl.in/wasm/).