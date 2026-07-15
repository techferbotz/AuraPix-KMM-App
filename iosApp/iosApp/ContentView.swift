import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            // Universal Link opened while the app was not running.
            .onOpenURL { url in
                _ = DeepLinks.shared.handleUrl(url: url.absoluteString)
            }
            // Universal Link handed off via a browsing user activity.
            .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                if let url = activity.webpageURL {
                    _ = DeepLinks.shared.handleUrl(url: url.absoluteString)
                }
            }
    }
}