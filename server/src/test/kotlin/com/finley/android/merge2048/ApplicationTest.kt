package com.finley.android.merge2048

import com.finley.android.merge2048.sync.RecordEnvelope
import com.finley.android.merge2048.sync.RegisterRequest
import com.finley.android.merge2048.sync.RegisterResponse
import com.finley.android.merge2048.sync.SyncRequest
import com.finley.android.merge2048.sync.SyncResponse
import com.finley.android.merge2048.sync.SyncStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import kotlin.test.*

private val testJson = Json { ignoreUnknownKeys = true }

private fun env(key: String, updatedAtMs: Long, snapshot: String): RecordEnvelope =
    RecordEnvelope(
        recordKey = key, updatedAtMs = updatedAtMs,
        snapshotJson = snapshot, userId = "", deviceId = ""
    )

private fun tempStore(): SyncStore {
    val f = File.createTempFile("merge2048-test", ".json")
    f.deleteOnExit()
    return SyncStore(f)
}

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application { module() }
        val r = client.get("/")
        assertEquals(HttpStatusCode.OK, r.status)
        assertEquals("Hello, Ktor!", r.bodyAsText())
    }

    // Ask 2 — register AUTO-ASSOCIATES: existing server-side data for the
    // user is pulled back and merged, nothing pre-existing is wiped.
    @Test
    fun testRegisterAutoAssociates() = testApplication {
        store2 = tempStore()
        application { module(store2) }
        val r = client.post("/api/register") {
            setBody(TextContent(
                testJson.encodeToString(RegisterRequest.serializer(),
                    RegisterRequest("user-1", "phone-a", listOf(env("save-a", 1000, "{}")) )),
                ContentType.Application.Json
            ))
        }
        assertEquals(HttpStatusCode.OK, r.status)
        val resp = testJson.decodeFromString<RegisterResponse>(r.bodyAsText())
        assertNotNull(resp.records.firstOrNull { it.recordKey == "save-a" })
    }

    // Ask 3 — offline-first LWW: a locally kept save is never dropped; newer
    // client update wins per recordKey and the full merged set is returned.
    @Test
    fun testSyncLwwNeverDropsLocal() = testApplication {
        store3 = tempStore()
        application { module(store3) }
        // seed the server through registration: save-a(1000) + save-b(900)
        client.post("/api/register") {
            setBody(TextContent(
                testJson.encodeToString(RegisterRequest.serializer(),
                    RegisterRequest("user-1", "phone-a",
                        listOf(env("save-a", 1000, "first"), env("save-b", 900, "b000")))),
                ContentType.Application.Json
            ))
        }
        // offline client kept save-a with a NEWER edit on the device
        val r = client.post("/api/sync") {
            setBody(TextContent(
                testJson.encodeToString(SyncRequest.serializer(),
                    SyncRequest("user-1", "phone-a",
                        listOf(env("save-a", 99999, "newer-from-phone")))),
                ContentType.Application.Json
            ))
        }
        assertEquals(HttpStatusCode.OK, r.status)
        val resp = testJson.decodeFromString<SyncResponse>(r.bodyAsText())
        assertNotNull(resp.records.firstOrNull { it.recordKey == "save-b" }, "locally kept save-b must survive")
        val a = resp.records.first { it.recordKey == "save-a" }
        assertEquals("newer-from-phone", a.snapshotJson)
        assertTrue(resp.records.map { it.recordKey }.containsAll(listOf("save-a", "save-b")))
    }

    // Ask 1 — the DB file is committed & reopening the store on the SAME path
    // reloads previously kept data (a brand-new computer opening the committed
    // document keeps it exactly).
    @Test
    fun testStorePersistsAcrossReopen() {
        val dir = Files.createTempDirectory("merge2048-reload").toFile()
        val db = File(dir, "merge2048-db.json")
        SyncStore(db).register("user-1", "phone-a", listOf(env("keep-me", 1000, "{}")))
        // reopen the SAME committed path → data must still be there
        val reloaded = SyncStore(db).sync("user-1", "phone-a", emptyList())
        assertNotNull(reloaded.firstOrNull { it.recordKey == "keep-me" })
    }

    companion object {
        lateinit var store2: SyncStore
        lateinit var store3: SyncStore
    }
}
