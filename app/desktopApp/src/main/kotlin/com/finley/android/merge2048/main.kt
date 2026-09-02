package com.finley.android.merge2048

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Merge2048",
        icon = painterResource("icon.png"),
        state = rememberWindowState(width = 480.dp, height = 760.dp)
    ) {
        App()
    }
}
