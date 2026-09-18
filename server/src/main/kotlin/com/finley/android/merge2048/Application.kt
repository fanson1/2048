
package com.finley.android.merge2048

import com.finley.android.merge2048.sync.RecordEnvelope
import com.finley.android.merge2048.sync.RegisterRequest
import com.finley.android.merge2048.sync.SyncRequest
import com.finley.android.merge2048.sync.SyncResponse
import com.finley.android.merge2048.sync.SyncStore
import com.finley.android.merge2048.sync.RegisterResponse
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

val syncJson = Json { ignoreUnknownKeys = true }


private fun defaultDbFile(): File {
    // Anchor to the REPO ROOT (the dir holding settings.gradle.kts), no
    // matter what working directory Gradle runs tests from — keeps the
    // committed in-project DB (Ask 1) at server/data/… consistently.
    var dir = File(System.getProperty("user.dir")).absoluteFile
    while (dir != null && !File(dir, "settings.gradle.kts").isFile) dir = dir.parentFile
    val root = dir ?: File(System.getProperty("user.dir"))
    return File(root, "server/data/merge2048-db.json")
}

fun Application.module(store: SyncStore = SyncStore(defaultDbFile())) {
    routing {
        get("/") {
            call.respondText("Hello, Ktor!")
        }

        // Ask 2 — register auto-associates existing local data and pulls
        // back everything the server already had for this user.
        post("/api/register") {
            val req = syncJson.decodeFromString<RegisterRequest>(call.receiveText())
            val merged = store.register(req.userId, req.deviceId, req.localRecords)
            call.respondText(
                syncJson.encodeToString(RegisterResponse.serializer(),
                    RegisterResponse(req.userId, merged)),
                ContentType.Application.Json
            )
        }

        // Ask 3 — offline-first sync, last-write-wins per recordKey, local
        // saves never dropped.
        post("/api/sync") {
            val req = syncJson.decodeFromString<SyncRequest>(call.receiveText())
            val merged = store.sync(req.userId, req.deviceId, req.clientRecords)
            call.respondText(
                syncJson.encodeToString(SyncResponse.serializer(), SyncResponse(merged)),
                ContentType.Application.Json
            )
        }
    }
}
