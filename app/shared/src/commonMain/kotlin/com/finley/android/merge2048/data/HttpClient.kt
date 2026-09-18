package com.finley.android.merge2048.data

/**
 * Minimal cross-platform HTTP client used by [SyncRepository].
 * Returns the response body as a UTF-8 string; throws on any transport or non-2xx response.
 */
expect object HttpClient {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): String
}

/** Thrown by platform [HttpClient] implementations for transport or non-2xx failures. */
class HttpClientException(message: String) : RuntimeException(message)