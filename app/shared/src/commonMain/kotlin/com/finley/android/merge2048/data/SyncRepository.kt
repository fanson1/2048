package com.finley.android.merge2048.data

import com.finley.android.merge2048.domain.AuthRequest
import com.finley.android.merge2048.domain.AuthResponse
import com.finley.android.merge2048.domain.RefreshTokenRequest
import com.finley.android.merge2048.domain.RegisterRequest
import com.finley.android.merge2048.domain.SyncPayload
import com.finley.android.merge2048.domain.SyncResult
import com.finley.android.merge2048.domain.UserAccount
import com.finley.android.merge2048.domain.nowEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

sealed interface AuthState {
    data object Uninitialized : AuthState
    data class Authenticated(val account: UserAccount) : AuthState
    data object Unauthenticated : AuthState
}

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(val timestamp: Long) : SyncStatus
    data class Error(val message: String) : SyncStatus
    data class Conflict(val serverPayload: SyncPayload, val clientPayload: SyncPayload) : SyncStatus
}

enum class ConflictChoice { UseServer, UseClient }

/**
 * Cloud synchronization repository.
 *
 * Handles authentication (register / login / logout / token refresh) and the
 * push/pull lifecycle against the Merge2048 sync server. User data (preferences,
 * game records, snapshot, daily results) is wrapped in a [SyncPayload]; when the
 * server timestamp is newer than the client's, the server returns a [SyncResult.Conflict]
 * and the registered [conflictResolver] decides which side wins.
 */
class SyncRepository(
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository,
    private val historyRepository: GameHistoryRepository,
    private val scope: CoroutineScope,
    private val apiBaseUrl: String = "http://10.0.2.2:8080", // Android emulator alias for host
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Uninitialized)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var syncJob: Job? = null
    private var conflictResolver: ((SyncPayload, SyncPayload) -> ConflictChoice)? = null

    // ---- Lifecycle ----

    fun initialize() {
        val account = settingsRepository.account()
        if (account?.accessToken != null) {
            _authState.value = AuthState.Authenticated(account)
            startPeriodicSync()
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun setConflictResolver(resolver: (SyncPayload, SyncPayload) -> ConflictChoice) {
        conflictResolver = resolver
    }

    // ---- Auth ----

    suspend fun register(email: String, password: String, displayName: String, deviceName: String): Result<UserAccount> =
        try {
            val response = HttpClient.post(
                "$apiBaseUrl/auth/register",
                json.encodeToString(
                    RegisterRequest.serializer(),
                    RegisterRequest(
                        email = email,
                        password = password,
                        displayName = displayName,
                        deviceId = settingsRepository.snapshot().deviceId,
                        deviceName = deviceName
                    )
                )
            )
            val auth = json.decodeFromString(AuthResponse.serializer(), response)
            saveSession(auth)
            Result.success(auth.account)
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun login(email: String, password: String, deviceName: String): Result<UserAccount> =
        try {
            val response = HttpClient.post(
                "$apiBaseUrl/auth/login",
                json.encodeToString(
                    AuthRequest.serializer(),
                    AuthRequest(
                        email = email,
                        password = password,
                        deviceId = settingsRepository.snapshot().deviceId,
                        deviceName = deviceName
                    )
                )
            )
            val auth = json.decodeFromString(AuthResponse.serializer(), response)
            saveSession(auth)
            Result.success(auth.account)
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun logout() {
        val account = _authState.value as? AuthState.Authenticated ?: return
        account.account.refreshToken?.let { token ->
            try {
                HttpClient.post(
                    "$apiBaseUrl/auth/logout",
                    json.encodeToString(RefreshTokenRequest.serializer(), RefreshTokenRequest(token)),
                    authHeaders()
                )
            } catch (_: Exception) {
                // Server errors on logout are non-fatal
            }
        }
        clearSession()
    }

    // ---- Sync ----

    suspend fun pull(): Result<SyncPayload> {
        _syncStatus.value = SyncStatus.Syncing
        return try {
            val body = HttpClient.get("$apiBaseUrl/sync/pull", authHeaders())
            when (val result = json.decodeFromString(SyncResult.serializer(), body)) {
                is SyncResult.Success -> {
                    applyPayload(result.payload)
                    _syncStatus.value = SyncStatus.Success(result.serverTimestamp)
                    Result.success(result.payload)
                }
                is SyncResult.Conflict -> {
                    _syncStatus.value = SyncStatus.Conflict(result.serverPayload, result.clientPayload)
                    Result.failure(HttpClientException("Conflict on pull"))
                }
                is SyncResult.Error ->
                    Result.failure(HttpClientException("${result.code}: ${result.message}"))
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: e.toString())
            Result.failure(e)
        }
    }

    suspend fun push(): Result<SyncPayload> {
        _syncStatus.value = SyncStatus.Syncing
        val payload = buildSyncPayload()
        return pushPayload(payload)
    }

    suspend fun pushNow(payload: SyncPayload): Result<SyncPayload> = pushPayload(payload)

    private suspend fun pushPayload(payload: SyncPayload): Result<SyncPayload> {
        return try {
            val body = HttpClient.post(
                "$apiBaseUrl/sync/push",
                json.encodeToString(SyncPayload.serializer(), payload),
                authHeaders()
            )
            when (val result = json.decodeFromString(SyncResult.serializer(), body)) {
                is SyncResult.Success -> {
                    _syncStatus.value = SyncStatus.Success(result.serverTimestamp)
                    Result.success(result.payload)
                }
                is SyncResult.Conflict -> {
                    _syncStatus.value = SyncStatus.Conflict(result.serverPayload, result.clientPayload)
                    val choice = conflictResolver?.invoke(result.serverPayload, result.clientPayload)
                        ?: ConflictChoice.UseClient
                    if (choice == ConflictChoice.UseServer) {
                        pull()
                    } else {
                        // Force: retry with a newer timestamp so the server accepts it.
                        val forced = payload.copy(timestamp = nowEpochMillis() + 1)
                        HttpClient.post(
                            "$apiBaseUrl/sync/push",
                            json.encodeToString(SyncPayload.serializer(), forced),
                            authHeaders()
                        )
                        _syncStatus.value = SyncStatus.Success(forced.timestamp)
                        Result.success(forced)
                    }
                }
                is SyncResult.Error ->
                    Result.failure(HttpClientException("${result.code}: ${result.message}"))
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: e.toString())
            Result.failure(e)
        }
    }

    // ---- Internal helpers ----

    private fun saveSession(auth: AuthResponse) {
        settingsRepository.saveAccount(auth.account)
        _authState.value = AuthState.Authenticated(auth.account)
        startPeriodicSync()
    }

    private fun clearSession() {
        settingsRepository.saveAccount(null)
        _authState.value = AuthState.Unauthenticated
        stopPeriodicSync()
    }

    private fun buildSyncPayload(): SyncPayload {
        val prefs = settingsRepository.snapshot()
        return SyncPayload(
            accountId = prefs.accountId!!,
            preferences = prefs,
            gameRecords = historyRepository.snapshot().toList(),
            gameSnapshot = gameRepository.load(),
            dailyChallengeResults = prefs.dailyChallengeResults,
            deviceId = prefs.deviceId,
            timestamp = nowEpochMillis()
        )
    }

    private fun applyPayload(payload: SyncPayload) {
        val local = settingsRepository.snapshot()
        settingsRepository.save(
            local.copy(
                unlockedAchievementIds = (local.unlockedAchievementIds + payload.preferences.unlockedAchievementIds),
                themeId = payload.preferences.themeId,
                mergeRuleId = payload.preferences.mergeRuleId,
                language = payload.preferences.language,
                darkMode = payload.preferences.darkMode,
                boardSize = payload.preferences.boardSize,
                soundEnabled = payload.preferences.soundEnabled,
                animationLevel = payload.preferences.animationLevel,
                dailyChallengeResults = payload.dailyChallengeResults,
                accountId = payload.accountId,
                deviceId = local.deviceId
            )
        )

        val knownIds = historyRepository.snapshot().mapTo(mutableSetOf()) { it.finishedAtMs }
        payload.gameRecords.forEach { record ->
            if (!knownIds.contains(record.finishedAtMs)) {
                historyRepository.append(record)
                knownIds.add(record.finishedAtMs)
            }
        }

        val snapshot = payload.gameSnapshot
        if (snapshot != null) gameRepository.save(snapshot)
    }

    private fun authHeaders(): Map<String, String> {
        val account = (settingsRepository.account()) ?: return emptyMap()
        val token = account.accessToken ?: return emptyMap()
        return mapOf("Authorization" to "Bearer $token")
    }

    private fun startPeriodicSync() {
        stopPeriodicSync()
        syncJob = scope.launch {
            delay(SYNC_INTERVAL_MS)
            while (isActive) {
                if (_authState.value is AuthState.Authenticated) {
                    push()
                }
                delay(SYNC_INTERVAL_MS)
            }
        }
    }

    private fun stopPeriodicSync() {
        syncJob?.cancel()
        syncJob = null
    }

    private companion object {
        const val SYNC_INTERVAL_MS = 30_000L
    }
}