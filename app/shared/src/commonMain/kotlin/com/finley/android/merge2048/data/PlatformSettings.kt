package com.finley.android.merge2048.data

import com.russhwolf.settings.Settings

/**
 * Hook for the host platform to inject the application context (Android).
 * On other platforms this is a no-op. The app entry point should call it
 * once during composition so [createSettings] can resolve storage.
 */
internal expect fun initPlatformStorage(context: Any)

/**
 * Provides the platform-specific [Settings] instance used by the repositories.
 * Each target supplies an actual that resolves a real key-value store
 * (SharedPreferences on Android, file on JVM/JS/Wasm, NSUserDefaults on iOS).
 */
expect fun createSettings(): Settings