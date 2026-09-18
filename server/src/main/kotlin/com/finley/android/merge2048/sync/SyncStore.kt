package com.finley.android.merge2048.sync

import com.finley.android.merge2048.sync.RecordEnvelope
import kotlinx.serialization.builtins.ListSerializer
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
 * Ask 1 — the DB file lives inside the project and is COMMITTED, so a
 * brand-new computer keeps the existing test data exactly as it was.
 *
 * Ask 2 — register AUTO-ASSOCIATES: [register] merges this device's local
 * records in under the userId and pulls back every record the server already
 * had for that user (nothing pre-existing is lost or wiped by registration).
 *
 * Ask 3 — offline-first LWW sync: [sync] merges by per-[RecordEnvelope.recordKey]
 * last-write-wins (updatedAtMs), never dropping a locally kept save.
 */
class SyncStore(private val dbFile: File, private val json: Json = Json { prettyPrint = true }) {
    private val lock = ReentrantReadWriteLock()
    private lateinit var db: File
    private val store = LinkedHashMap<String, LinkedHashMap<String, RecordEnvelope>>()

    init {
        db = dbFile
        if (db.parentFile != null) db.parentFile!!.mkdirs()
        load()
    }

    private fun load() {
        if (!db.exists()) return
        val text = runCatching { db.readText() }.getOrNull() ?: return
        val recs = runCatching { json.decodeFromString<ListSerializer(RecordEnvelope.serializer()), text) }
            .getOrNull() ?: emptyList()
        for (rec in recs) store.getOrPut(rec.userId) { LinkedHashMap() }[rec.recordKey] = rec
    }

    private fun persist() {
        db.parentFile?.mkdirs()
        val all = store.values.flatMap { it.values }
        val tmp = File(db.parentFile, db.name + ".tmp")
        tmp.writeText(json.encodeToString(ListSerializer(RecordEnvelope.serializer()), all))
        Files.move(tmp.toPath(), db.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    /** Ask 2 — register auto-associates: [local] merges into the userId's
     *  store; the full (already-present + merged) set is returned. */
    fun register(userId: String, deviceId: String, local: List<RecordEnvelope>): List<RecordEnvelope> =
        lock.write { merge(userId, deviceId, local) }

    /** Ask 3 — offline-first: LWW per key; a locally kept save is never
     *  dropped; returns the full merged set. */
    fun sync(userId: String, deviceId: String, incoming: List<RecordEnvelope>): List<RecordEnvelope> =
        lock.write { merge(userId, deviceId, incoming) }

    private fun merge(userId: String, deviceId: String, incoming: List<RecordEnvelope>): List<RecordEnvelope> {
        val user = store.getOrPut(userId) { LinkedHashMap() }
        for (rec in incoming) {
            val owned = rec.copy(userId = userId, deviceId = deviceId)
            val cur = user[owned.recordKey]
            if (cur == null || owned.updatedAtMs >= cur.updatedAtMs) user[owned.recordKey] = owned
        }
        persist()
        return user.values.toList()
    }
}
