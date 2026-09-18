package com.finley.android.merge2048.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Current wall-clock time in epoch milliseconds (portable stdlib [Clock]). */
@OptIn(ExperimentalTime::class)
fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

@OptIn(ExperimentalUuidApi::class)
internal fun newId(): String = Uuid.random().toString()

/**
 * User account for cloud synchronization.
 * Stored locally and synced with server.
 */
@Serializable
data class UserAccount(
    val id: String,                    // UUID
    val email: String,
    val displayName: String,
    val createdAt: Long = nowEpochMillis(),  // epoch milliseconds
    val lastSyncAt: Long? = null,      // epoch milliseconds
    val accessToken: String? = null,   // JWT or opaque token
    val refreshToken: String? = null
) {
    companion object {
        fun create(email: String, displayName: String): UserAccount {
            return UserAccount(
                id = newId(),
                email = email.lowercase(),
                displayName = displayName
            )
        }
    }
}

/**
 * Complete sync payload containing all user data.
 * Sent to server on push, received on pull.
 */
@Serializable
data class SyncPayload(
    val accountId: String,
    val timestamp: Long = nowEpochMillis(),  // epoch milliseconds
    val preferences: UserPreferences,
    val gameRecords: List<GameRecord>,
    val gameSnapshot: GameSnapshot?,
    val dailyChallengeResults: Map<Int, DailyChallengeResult>,
    val deviceId: String,              // To detect conflicts across devices
    val schemaVersion: Int = 1         // For future migrations
)

/**
 * Server response for sync operations.
 */
@Serializable
sealed class SyncResult {
    @Serializable
    data class Success(
        val payload: SyncPayload,
        val serverTimestamp: Long
    ) : SyncResult()

    @Serializable
    data class Conflict(
        val serverPayload: SyncPayload,
        val clientPayload: SyncPayload
    ) : SyncResult()

    @Serializable
    data class Error(
        val code: String,
        val message: String
    ) : SyncResult()
}

/**
 * Authentication request/response.
 */
@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val deviceId: String,
    val deviceName: String
)

@Serializable
data class AuthResponse(
    val account: UserAccount,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val deviceId: String,
    val deviceName: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)