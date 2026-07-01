import SwiftUI
import UIKit
import ComposeApp

/// A **dumb native chrome shell**: a real `UINavigationBar` (top) + the Compose content view controller (middle,
/// which renders the nav stack) + a real `UITabBar` (bottom). It renders `NativeNavChrome` state one-way and
/// forwards taps as intents (`backRequested`/`tabSelected`/`actionTapped`). It owns NO navigation stack — the
/// Kotlin `NativeNavigator` is the sole owner; nothing here reads, mirrors, or reconciles the stack.
final class NativeShellViewController: UIViewController, UITabBarDelegate {
    private let root: NativeNavRoot
    private let navBar = UINavigationBar()
    private let tabBar = UITabBar()
    private var cancellable: NativeChromeCancellable?
    private var tabItemsById: [String: UITabBarItem] = [:]
    private var actionIdByTag: [Int: String] = [:]
    private var presentedSheet: UIViewController?

    init(root: NativeNavRoot) {
        self.root = root
        super.init(nibName: nil, bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("init(coder:) not used") }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor { traits in
            NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
        }

        let content = root.contentViewController
        addChild(content)
        content.view.translatesAutoresizingMaskIntoConstraints = false
        navBar.translatesAutoresizingMaskIntoConstraints = false
        tabBar.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(content.view)
        view.addSubview(navBar)
        view.addSubview(tabBar)
        content.didMove(toParent: self)
        tabBar.delegate = self

        let safe = view.safeAreaLayoutGuide
        NSLayoutConstraint.activate([
            navBar.topAnchor.constraint(equalTo: safe.topAnchor),
            navBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            navBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),

            tabBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tabBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            tabBar.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            content.view.topAnchor.constraint(equalTo: navBar.bottomAnchor),
            content.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            content.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            content.view.bottomAnchor.constraint(equalTo: tabBar.topAnchor),
        ])

        render(root.chrome.currentState())
        cancellable = root.chrome.observe { [weak self] state in
            DispatchQueue.main.async { self?.render(state) }
        }
    }

    deinit { cancellable?.cancel() }

    /// One-way: render the chrome projection onto the native bars. Never reads native state back.
    private func render(_ state: NativeChromeState) {
        // Top bar: a single item with the current title, an optional native back chevron, and any actions.
        let item = UINavigationItem(title: state.title)
        if state.canGoBack {
            item.leftBarButtonItem = UIBarButtonItem(
                image: UIImage(systemName: "chevron.backward"),
                style: .plain, target: self, action: #selector(backTapped))
        }
        actionIdByTag.removeAll()
        item.rightBarButtonItems = state.actions.enumerated().map { (i, action) in
            let b = UIBarButtonItem(
                image: UIImage(systemName: action.sfSymbol),
                style: .plain, target: self, action: #selector(actionTapped(_:)))
            b.tag = i
            actionIdByTag[i] = action.id
            return b
        }
        navBar.items = [item]

        // Bottom bar: build items once, then reflect the selected tab.
        if tabBar.items == nil {
            var built: [UITabBarItem] = []
            for (i, t) in state.tabs.enumerated() {
                let ti = UITabBarItem(title: t.title, image: UIImage(systemName: t.sfSymbol), tag: i)
                tabItemsById[t.id] = ti
                built.append(ti)
            }
            tabBar.setItems(built, animated: false)
        }
        tabBar.selectedItem = tabItemsById[state.selectedTabId]

        syncSheet(state.sheetId)
    }

    /// Present / dismiss the sheet natively, mirroring the navigator's sheet STATE only. The content is Compose
    /// (supplied by the bridge); presentation + the interactive swipe-to-dismiss are native; the navigator owns
    /// the state. No stack is involved.
    private func syncSheet(_ sheetId: String?) {
        if sheetId != nil {
            guard presentedSheet == nil, let sheet = root.chrome.sheetViewController() else { return }
            sheet.modalPresentationStyle = .pageSheet
            sheet.view.backgroundColor = .clear
            if let spc = sheet.sheetPresentationController {
                spc.detents = [.medium(), .large()]
                spc.prefersGrabberVisible = true
                spc.delegate = self
            }
            presentedSheet = sheet
            present(sheet, animated: true)
        } else if let sheet = presentedSheet {
            presentedSheet = nil
            sheet.dismiss(animated: true)
        }
    }

    @objc private func backTapped() { root.chrome.backRequested() }

    @objc private func actionTapped(_ sender: UIBarButtonItem) {
        if let id = actionIdByTag[sender.tag] { root.chrome.actionTapped(actionId: id) }
    }

    // A tab tap is an INTENT, not a stack mutation — the navigator decides.
    func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
        if let id = tabItemsById.first(where: { $0.value === item })?.key {
            root.chrome.tabSelected(tabId: id)
        }
    }
}

extension NativeShellViewController: UISheetPresentationControllerDelegate {
    // A native interactive dismissal (swipe / tap-outside) is reported back to the source of truth — the shell
    // never decides navigation, it only tells Kotlin the user closed the sheet. Idempotent.
    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        presentedSheet = nil
        root.chrome.dismissSheet()
    }
}

/// Hosts the native-chrome shell inside the SwiftUI `App`.
struct NativeNavShell: UIViewControllerRepresentable {
    let root: NativeNavRoot
    func makeUIViewController(context: Context) -> UIViewController { NativeShellViewController(root: root) }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
