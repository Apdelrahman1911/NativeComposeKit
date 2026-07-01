package io.github.apdelrahman1911.nativecomposekit.app.navigation

/**
 * Lightweight navigation tracing for the sample. Off by default; flip [enabled] (e.g. in a debug build) to print
 * every navigator intent and the resulting per-tab stacks. Lines are tagged `NCK-Nav` so they're easy to filter
 * in logcat / the Xcode console.
 *
 * This exists to diagnose stack desyncs (a push/back landing on the wrong or an empty screen): the log shows the
 * exact stack the source of truth holds after every intent.
 */
object NativeNavLog {
    /** When true, navigation events are printed. Leave false in production. */
    var enabled: Boolean = false

    internal inline fun log(message: () -> String) {
        if (enabled) println("NCK-Nav: ${message()}")
    }
}
