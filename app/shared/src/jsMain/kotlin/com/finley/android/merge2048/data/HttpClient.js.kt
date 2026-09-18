package com.finley.android.merge2048.data

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.Promise

actual object HttpClient {
    actual suspend fun get(url: String, headers: Map<String, String>): String =
        request("GET", url, null, headers)

    actual suspend fun post(url: String, body: String, headers: Map<String, String>): String =
        request("POST", url, body, headers)

    private suspend fun request(method: String, url: String, body: String?, headers: Map<String, String>): String {
        val init: dynamic = js("({})")
        init.method = method
        init.body = body
        val headerObj: dynamic = js("({})")
        headers.forEach { (k, v) -> headerObj[k] = v }
        init.headers = headerObj

        val promise = window.asDynamic().fetch(url, init)
        val response = (promise as Promise<dynamic>).await()
        val text = (response.text() as Promise<dynamic>).await()
        return text.toString()
    }
}
