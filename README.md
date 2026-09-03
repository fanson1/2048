# Merge2048

A cross-platform **2048** puzzle game built with Kotlin Multiplatform + Compose Multiplatform (Material 3). Designed to feel and look like a polished, commercial-grade casual game.

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
- **Undo** — step back one full move (`UNDO` button, only active when available)
- Score + **Best score** tracking (kept across games in the same session)
- **MAX** tile and **MOVES** live stats
- Win / Game-over overlays with full round summary (Score, Best, Max)
- Full keyboard support on desktop/web: **arrow keys / WASD** to move, **R** to restart

**Visual & interaction polish**
- Warm, classic 2048 color palette with premium gradients
- Tile pop-in + merge pop animations
- Direction-aware board slide animation on each move
- Golden glow on high-value tiles (256+)
- Animated score cards
- Responsive layout: content capped at 480dp, auto-adapts to landscape/short screens (compact header, progressive UI degradation), board scales to fit available space without overflow

**App icon**
- Custom-designed icon on every platform: Android adaptive + legacy mipmaps, iOS AppIcon, Desktop window/package icon, Web favicon

## Architecture

Strict layered MVI split into clean packages:

```
com.finley.android.merge2048
├── domain/             # Pure Kotlin game logic — zero Compose/Android deps, JVM-testable
│   ├── Direction.kt        # move direction enum
│   ├── GameState.kt        # immutable game snapshot
│   ├── GameIntent.kt       # user actions (sealed class)
│   ├── GameEngine.kt       # game rules (board, moves, merges, undo, win/game-over)
│   └── GameReducer.kt      # ★ MVI heart: Intent → State reducer (owns best/win bookkeeping)
├── presentation/
│   ├── GameViewModel.kt    # thin shell: holds StateFlow, forwards intents to the reducer
│   └── GameViewModelFactory.kt # expect/actual ViewModel retrieval (Android real ViewModel, others construct directly)
├── ui/
│   └── GameComponents.kt   # reusable design-system composables (score, stats, buttons, overlays)
├── GameScreen.kt           # screen assembly layer (no business logic)
├── SwipeableGameBoard.kt   # board grid + gesture/keyboard handling
├── GameTheme.kt            # color & typography tokens
└── App.kt
```

### Why a reducer?

All game behavior (move scoring, best-score tracking, win-dialog gating, undo bookkeeping,
state derivation) lives in the pure `domain/GameReducer`. The `GameViewModel` becomes a
passive shell, and every state transition can be unit-tested on the JVM without Android
or Compose dependencies.

## Project layout

- `app/androidApp/` — Android entry point + launcher resources
- `app/iosApp/` — iOS entry point + AppIcon assets
- `app/desktopApp/` — Desktop (JVM) entry point + window/package icon
- `app/webApp/` — Web entry point + favicon
- `app/shared/` — shared Compose Multiplatform UI and domain code
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

- Shared tests (JVM): `./gradlew :app:shared:jvmTest`
- Android host tests: `./gradlew :app:shared:testAndroidHostTest`
- Web: `./gradlew :app:shared:jsTest` / `./gradlew :app:shared:wasmJsTest`
- iOS: `./gradlew :app:shared:iosSimulatorArm64Test`
- Server: `./gradlew :server:test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform), and
[Kotlin/Wasm](https://kotl.in/wasm/).
