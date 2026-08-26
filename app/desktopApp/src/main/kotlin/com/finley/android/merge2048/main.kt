package com.finley.android.merge2048

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Merge2048",
    ) {
        App()
    }
}