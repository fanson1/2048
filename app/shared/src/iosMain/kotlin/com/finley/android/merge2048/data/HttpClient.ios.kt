package com.finley.android.merge2048.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSHTTPURLResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual object HttpClient {
    actual suspend fun get(url: String, headers: Map<String, String>): String =
        request("GET", url, null, headers)

    actual suspend fun post(url: String, body: String, headers: Map<String, String>): String =
        request("POST", url, body, headers)

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun request(method: String, url: String, body: String?, headers: Map<String, String>): String =
        suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL.URLWithString(url) ?: run {
                continuation.resumeWithException(IllegalArgumentException("Invalid URL: $url"))
                return@suspendCancellableCoroutine
            }
            val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
                setHTTPMethod(method)
                headers.forEach { (k, v) -> setValue(v, forHTTPHeaderField = k) }
                if (body != null) {
                    setHTTPBody(body.encodeToByteArray().toNSData())
                }
            }
            val task: NSURLSessionDataTask = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
                if (error != null) {
                    continuation.resumeWithException(error.toNSError())
                    return@dataTaskWithRequest
                }
                val http = response as? NSHTTPURLResponse
                val code = http?.statusCode ?: 200
                val text = data?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding) as String? } ?: ""
                if (code in 200..299) continuation.resume(text)
                else continuation.resumeWithException(HttpClientException("HTTP $code: $text"))
            }
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    val bytes = this
    return bytes.usePinned { pinned ->
        if (bytes.isEmpty()) NSData.create()
        else NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
}
