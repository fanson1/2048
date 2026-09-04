package com.finley.android.merge2048.data

import com.finley.android.merge2048.domain.GameSnapshot
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

/**
 * Persists the in-progress [GameSnapshot] so the player can resume
 * after the app is closed.
 */
class GameRepository(
    private val settings: Settings = createSettings(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun load(): GameSnapshot? {
        val raw = settings.getStringOrNull(KEY_SNAPSHOT_JSON) ?: return null
        return runCatching { json.decodeFromString(GameSnapshot.serializer(), raw) }
            .getOrNull()
    }

    fun save(snapshot: GameSnapshot) {
        settings.putString(KEY_SNAPSHOT_JSON, json.encodeToString(GameSnapshot.serializer(), snapshot))
    }

    fun clear() {
        settings.remove(KEY_SNAPSHOT_JSON)
    }

    companion object {
        private const val KEY_SNAPSHOT_JSON = "game_snapshot_json"
    }
}