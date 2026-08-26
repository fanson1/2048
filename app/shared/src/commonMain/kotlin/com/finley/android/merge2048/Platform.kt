package com.finley.android.merge2048

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform