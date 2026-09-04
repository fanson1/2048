package com.finley.android.merge2048.data

/**
 * Lightweight cross-platform sound service. Plays short tones for the major
 * game events. The default implementation is a no-op; platforms with native
 * audio (currently Android) provide a real implementation.
 */
interface SoundService {
    fun play(event: SoundEvent)

    fun shutdown() {
        // default no-op
    }
}

enum class SoundEvent {
    Merge,
    BigMerge,   // 128+ merges
    NewGame,
    Undo,
    InvalidMove,
    GameOver,
    Achievement
}

expect fun createSoundService(): SoundService

/** Default no-op implementation for platforms without an audio backend. */
class NoOpSoundService : SoundService {
    override fun play(event: SoundEvent) = Unit
}