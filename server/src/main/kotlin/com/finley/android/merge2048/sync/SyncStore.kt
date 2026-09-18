package com.finley.android.merge2048.sync

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Offline-first sync store, file-backed, persisted to a single committed
 * project document under `server/data/merge2048-db.json`.
 *
 * Ask 1 — the DB file lives in-project and is COMMITTED, so moving to a
 * brand-new computer keeps the existing test data untouched.
 *
 * Ask 2 — register auto-associates: this device's local records merge under
 * the userId and the full pre-existing set is pulled back and returned.
 *
 * Ask 3 — offline-first LWW sync: merge by per-[(recordKey)] last-write-wins,
 * never dropping a locally kept save.
 */
class SyncStore(private val file: File, private val json: Json = Json { prettyPrint = true }) {
    private val lock = ReentrantReadWriteLock()
    private val userRecords = LinkedHashMap<String, LinkedHashMap<String, RecordEnvelope>>()

    init {
        if (file.parentFile != null) file.parentFile!!.mkdirs()
        load()
    }

    private fun load() {
        if (!file.exists()) return
        val text = file.readText()
        val list = runCatching {
            json.decodeFromString(ListSerializer(RecordEnvelope.serializer()), text)
        }.getOrNull() ?: return
        for (rec in list) {
            userRecords.getOrPut(rec.userId) { LinkedHashMap() }[rec.recordKey] = rec
        }
    }

    private fun persist() {
        val all = userRecords.values.flatMap { it.values }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(json.encodeToString(ListSerializer(RecordEnvelope.serializer()), all))
        Files.move(tmp.toPath(), file.toPath(),
            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    /** Ask 2 — register auto-associates: [local] merges into userId's store
     *  and the full (already-present + merged) set is returned. */
    fun register(userId: String, deviceId: String, local: List<RecordEnvelope>): List<RecordEnvelope> =
        lock.write { merge(userId, deviceId, local) }

    /** Ask 3 — offline-first: LWW per (userId, recordKey); a locally kept
     *  save is never dropped; returns the full merged set. */
    fun sync(userId: String, deviceId: String, incoming: List<RecordEnvelope>): List<RecordEnvelope> =
        lock.write { merge(userId, deviceId, incoming) }

    private fun merge(userId: String, deviceId: String, incoming: List<RecordEnvelope>): List<RecordEnvelope> {
        val user = userRecords.getOrPut(userId) { LinkedHashMap() }
        for (rec in incoming) {
            val owned = if (rec.userId == userId) rec else rec.copy(userId = userId, deviceId = deviceId)
            val cur = user[owned.recordKey]
            if (cur == null || owned.updatedAtMs >= cur.updatedAtMs) {
                user[owned.recordKey] = owned
            }
        }
        persist()
        return user.values.toList()
    }
}
