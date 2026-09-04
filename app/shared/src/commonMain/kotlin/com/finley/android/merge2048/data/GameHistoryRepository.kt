package com.finley.android.merge2048.data

import com.finley.android.merge2048.domain.GameRecord
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Stores a rolling list of finished-game records (newest first). Capped at
 * [MAX_RECORDS] entries to keep storage bounded — the older entries are
 * dropped as new games finish.
 */
class GameHistoryRepository(
    private val settings: Settings = createSettings(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val _flow = MutableStateFlow(load())
    val flow: StateFlow<List<GameRecord>> = _flow.asStateFlow()

    fun snapshot(): List<GameRecord> = _flow.value

    /** Append a record. Newest first; cap at [MAX_RECORDS]. */
    fun append(record: GameRecord) {
        val list = (listOf(record) + _flow.value).take(MAX_RECORDS)
        save(list)
    }

    fun clear() = save(emptyList())

    private fun load(): List<GameRecord> {
        val raw = settings.getStringOrNull(KEY_HISTORY_JSON) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(GameRecord.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    private fun save(list: List<GameRecord>) {
        if (list.isEmpty()) {
            settings.remove(KEY_HISTORY_JSON)
        } else {
            settings.putString(
                KEY_HISTORY_JSON,
                json.encodeToString(ListSerializer(GameRecord.serializer()), list)
            )
        }
        _flow.value = list
    }

    companion object {
        const val MAX_RECORDS = 100
        private const val KEY_HISTORY_JSON = "game_history_json"
    }
}