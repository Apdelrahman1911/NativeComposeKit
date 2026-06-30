package io.github.apdelrahman1911.nativecomposekit.navigation

/**
 * Lightweight navigation tracing. Off by default; flip [enabled] (e.g. in a debug build) to print every
 * navigator intent, the resulting per-tab stacks, and — on iOS — every route-id resolution the SwiftUI shell
 * asks for. Lines are tagged `NCK-Nav` so they're easy to filter in logcat / the Xcode console.
 *
 * This exists to diagnose stack desyncs (a push/back landing on the wrong or an empty screen): the log shows the
 * exact stack the source of truth holds versus the id the renderer requested.
 */
public object NativeNavLog {
    /** When true, navigation events are printed. Leave false in production. */
    public var enabled: Boolean = false

    internal inline fun log(message: () -> String) {
        if (enabled) println("NCK-Nav: ${message()}")
    }
}
