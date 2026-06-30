import SwiftUI
import ComposeApp // Kotlin framework — baseName "ComposeApp" from composeApp/build.gradle.kts

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // let the Compose layer manage keyboard insets
    }
}
