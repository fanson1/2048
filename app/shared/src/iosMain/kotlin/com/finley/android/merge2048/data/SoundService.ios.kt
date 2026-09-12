package com.finley.android.merge2048.data

import platform.AudioToolbox.AudioServicesPlaySystemSound

/**
 * Plays short system sounds on iOS via AudioToolbox. Each [SoundEvent] maps to a
 * distinct built-in system sound id so the feedback feels layered without shipping
 * bundled audio assets.
 */
class IosSoundService : SoundService {
    override fun play(event: SoundEvent) {
        val soundId: UInt = when (event) {
            SoundEvent.Merge -> 1104u
            SoundEvent.BigMerge -> 1105u
            SoundEvent.NewGame -> 1103u
            SoundEvent.Undo -> 1057u
            SoundEvent.InvalidMove -> 1073u
            SoundEvent.GameOver -> 1000u
            SoundEvent.Achievement -> 1026u
        }
        AudioServicesPlaySystemSound(soundId)
    }
}

actual fun createSoundService(): SoundService = IosSoundService()