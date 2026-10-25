package dev.fediim.anysdui.preview

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * The root view controller of the iOS app. A universal link that launches or reaches the app goes
 * to [openLink].
 */
fun MainViewController(): UIViewController = ComposeUIViewController { PreviewApp(null) }
