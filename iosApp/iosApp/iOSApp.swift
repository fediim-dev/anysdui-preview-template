import SwiftUI
import Shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // A universal link arrives here as a URL (SwiftUI passes it directly). Kotlin
                // opens it only if it is a share link of the configured server; it is not logged.
                .onOpenURL { url in
                    PreviewLinksKt.openLink(url: url.absoluteString)
                }
        }
    }
}
