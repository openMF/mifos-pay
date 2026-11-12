import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Platform bridges only Swift can create (e.g. a StoreKit 2 shim) go in this list; the fork's
        // onForkIosConfigure (cmp-shared kpt/shared/fork) receives them after Koin starts.
        ViewControllerKt.viewController(platformBridges: [])
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(edges: .all)
                .ignoresSafeArea(.keyboard) // .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}



