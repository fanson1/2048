package com.finley.android.merge2048.data

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Plays short tones using Android's built-in [ToneGenerator]. The tones are
 * short (50-150 ms) and use a moderate volume so they layer well with other
 * app audio.
 */
class AndroidSoundService : SoundService {
    private var tone: ToneGenerator? = null

    private fun tone(): ToneGenerator {
        val current = tone
        if (current != null) return current
        val created = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        tone = created
        return created
    }

    override fun play(event: SoundEvent) {
        val (toneType, durationMs) = when (event) {
            SoundEvent.Merge -> ToneGenerator.TONE_PROP_BEEP to 60
            SoundEvent.BigMerge -> ToneGenerator.TONE_CDMA_CONFIRM to 140
            SoundEvent.NewGame -> ToneGenerator.TONE_PROP_BEEP2 to 100
            SoundEvent.Undo -> ToneGenerator.TONE_PROP_NACK to 60
            SoundEvent.InvalidMove -> ToneGenerator.TONE_PROP_NACK to 80
            SoundEvent.GameOver -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 280
            SoundEvent.Achievement -> ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE to 200
        }
        try {
            tone().startTone(toneType, durationMs)
        } catch (e: RuntimeException) {
            // ToneGenerator can fail on some devices; ignore and stay silent.
        }
    }

    override fun shutdown() {
        tone?.release()
        tone = null
    }
}

actual fun createSoundService(): SoundService = AndroidSoundService()