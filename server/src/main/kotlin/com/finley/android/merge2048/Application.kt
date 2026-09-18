package com.finley.android.merge2048

import com.finley.android.merge2048.sync.SyncStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File

private val syncJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun defaultDbFile(): File = File("server/data/merge2048-db.json")
fun defaultStore(): SyncStore = SyncStore(defaultDbFile())

fun Application.module(store: SyncStore = defaultStore()) {
    routing {
        get("/") {
            call.respondText("""{"status":"ok","service":"merge2048-sync"}""", ContentType.Application.Json)
        }

        // Ask 2 — register auto-associates existing local data & pulls it back.
        post("/api/register") {
            val req = syncJson.decodeFromString<com.finley.android.merge2048.sync.RegisterRequest>(call.receiveText())
            val records = store.register(req.userId, req.deviceId, req.localRecords)
            call.respondText(
                syncJson.encodeToString(com.finley.android.merge2048.sync.RegisterResponse.serializer(), com.finley.android.merge2048.sync.RegisterResponse(req.userId, records)),
                ContentType.Application.Json
            )
        }

        // Ask 3 — offline-first sync: LWW merge, local saves never dropped.
        post("/api/sync") {
            val req = syncJson.decodeFromString<com.finley.android.merge2048.sync.SyncRequest>(call.receiveText())
            val records = store.sync(req.userId, req.deviceId, req.clientRecords)
            call.respondText(
                syncJson.encodeToString(com.finley.android.merge2048.sync.SyncResponse.serializer(), com.finley.android.merge2048.sync.SyncResponse(records)),
                ContentType.Application.Json
            )
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
