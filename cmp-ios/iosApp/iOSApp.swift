import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        // The app links FirebaseCore + FirebaseAnalytics, but Firebase does nothing until configured.
        // Configure only when the fork bundled its GoogleService-Info.plist (cmp-ios/iosApp/firebase/, fork-owned,
        // copied by scripts/copy-compose-resources.sh) — a fork without Firebase on iOS still runs.
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
