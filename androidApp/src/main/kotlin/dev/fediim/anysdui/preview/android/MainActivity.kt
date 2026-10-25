package dev.fediim.anysdui.preview.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.fediim.anysdui.preview.PreviewApp
import dev.fediim.anysdui.preview.openLink

/**
 * The Android entry. A share link arrives as the intent's data (the App Link filter in the
 * manifest); it goes to [PreviewApp], which opens it only for the configured server. The link is
 * never logged.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val link = intent?.takeIf { it.action == Intent.ACTION_VIEW }?.data?.toString()
        setContent { PreviewApp(initialLink = link) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) intent.data?.toString()?.let(::openLink)
    }
}
