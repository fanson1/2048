package com.finley.android.merge2048.data

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.toJsString
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response

/**
 * Minimal typed HTTP client for the wasmJs (Kotlin/Wasm) target.
 *
 * Uses the modern typed interop: [window.fetch] returns a [Promise] of [Response];
 * [Response.text] returns a [Promise] of [JsString]. Both are resolved with the
 * kotlinx.coroutines `await()` extension for wasmJs (`kotlin.js.Promise.await`).
 */
actual object HttpClient {
    actual suspend fun get(url: String, headers: Map<String, String>): String =
        request("GET", url, null, headers)

    actual suspend fun post(url: String, body: String, headers: Map<String, String>): String =
        request("POST", url, body, headers)

    private suspend fun request(method: String, url: String, body: String?, headers: Map<String, String>): String {
        val requestHeaders = Headers()
        headers.forEach { (k, v) -> requestHeaders.append(k, v) }
        val init = RequestInit(
            method = method,
            headers = requestHeaders,
            body = body?.toJsString()
        )
        val response: Promise<Response> = window.fetch(url, init)
        val resolved: Response = response.await<Response>()
        val text: JsString = resolved.text().await<JsString>()
        return text.toString()
    }
}
