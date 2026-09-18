package com.finley.android.merge2048.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

actual object HttpClient {
    actual suspend fun get(url: String, headers: Map<String, String>): String =
        request("GET", url, null, headers)

    actual suspend fun post(url: String, body: String, headers: Map<String, String>): String =
        request("POST", url, body, headers)

    private suspend fun request(method: String, url: String, body: String?, headers: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
                if (method == "POST") {
                    connection.doOutput = true
                    if (body != null) {
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                    }
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = if (stream == null) "" else ByteArrayOutputStream().use { out ->
                    stream.use { ins -> ins.copyTo(out) }
                    out.toString("UTF-8")
                }
                if (code in 200..299) response
                else throw HttpClientException("HTTP $code: $response")
            } finally {
                connection.disconnect()
            }
        }
}
