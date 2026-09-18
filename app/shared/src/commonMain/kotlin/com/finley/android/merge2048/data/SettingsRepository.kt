package com.finley.android.merge2048.data

import com.finley.android.merge2048.domain.UserAccount
import com.finley.android.merge2048.domain.UserPreferences
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Persists the [UserPreferences] blob and the sync [UserAccount] session
 * using a platform [Settings] store.
 * Exposes a [StateFlow] so the UI / ViewModel can react to changes.
 */
class SettingsRepository(
    private val settings: Settings = createSettings(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val _flow = MutableStateFlow(load())
    val flow: StateFlow<UserPreferences> = _flow.asStateFlow()

    fun snapshot(): UserPreferences = _flow.value

    /** Persist the entire preferences object, replacing any previous state. */
    fun save(prefs: UserPreferences) {
        settings.putString(KEY_PREFS_JSON, json.encodeToString(UserPreferences.serializer(), prefs))
        _flow.value = prefs
    }

    /** Update a single field and re-persist. */
    fun update(transform: (UserPreferences) -> UserPreferences) {
        save(transform(_flow.value))
    }

    private fun load(): UserPreferences {
        val raw = settings.getStringOrNull(KEY_PREFS_JSON) ?: return UserPreferences.Default
        return runCatching { json.decodeFromString(UserPreferences.serializer(), raw) }
            .getOrDefault(UserPreferences.Default)
    }

    /** The synced account session, or null when signed out. */
    fun account(): UserAccount? {
        val raw = settings.getStringOrNull(KEY_ACCOUNT_JSON) ?: return null
        return runCatching { json.decodeFromString(UserAccount.serializer(), raw) }.getOrNull()
    }

    /** Persist (or clear, when null) the synced account session plus the account id in prefs. */
    fun saveAccount(account: UserAccount?) {
        if (account == null) {
            settings.remove(KEY_ACCOUNT_JSON)
            if (_flow.value.accountId != null) {
                update { it.copy(accountId = null) }
            }
        } else {
            settings.putString(KEY_ACCOUNT_JSON, json.encodeToString(UserAccount.serializer(), account))
            if (_flow.value.accountId != account.id) {
                update { it.copy(accountId = account.id) }
            }
        }
    }

    companion object {
        private const val KEY_PREFS_JSON = "user_preferences_json"
        private const val KEY_ACCOUNT_JSON = "sync_account_json"
    }
}
