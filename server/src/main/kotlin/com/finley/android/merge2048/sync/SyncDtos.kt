package com.finley.android.merge2048.sync

import kotlinx.serialization.Serializable

/** One synced save's envelope. [snapshotJson] is the client's OWN opaque
 *  GameSnapshot JSON — stored verbatim; the server never parses it. */
@Serializable
data class RecordEnvelope(
    val recordKey: String,
    /** Client-local saved-at time (ms epoch) — the LWW merge key. */
    val updatedAtMs: Long,
    /** Opaque client GameSnapshot JSON. */
    val snapshotJson: String,
    /** Server-assigned on register/sync; "" until the server first sees it. */
    val userId: String = "",
    /** Server-assigned on register/sync. */
    val deviceId: String = ""
)

@Serializable
data class RegisterRequest(
    val userId: String,
    val deviceId: String,
    /** Records this device already keeps locally (auto-associated). */
    val localRecords: List<RecordEnvelope> = emptyList()
)

@Serializable
data class RegisterResponse(
    val userId: String,
    /** Full set now associated with [userId] (incl. pre-existing). */
    val records: List<RecordEnvelope>
)

@Serializable
data class SyncRequest(
    val userId: String,
    val deviceId: String,
    /** Records this device already keeps locally (offline-first LWW). */
    val clientRecords: List<RecordEnvelope> = emptyList()
)

@Serializable
data class SyncResponse(
    val records: List<RecordEnvelope>
)
