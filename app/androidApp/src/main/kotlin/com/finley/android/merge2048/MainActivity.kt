package com.finley.android.merge2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.finley.android.merge2048.data.SettingsRepository
import com.finley.android.merge2048.data.setAppLocale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the persisted locale before super.onCreate() so AppCompat
        // inflates the correct resources from the very first frame.
        val prefs = SettingsRepository().snapshot()
        if (prefs.language != "system") {
            setAppLocale(prefs.language)
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
