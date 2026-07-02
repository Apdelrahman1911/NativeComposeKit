import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The Native UI design-system kit as a standalone KMP library. It does NOT emit its own iOS framework —
// `:composeApp` owns the single `ComposeApp` framework and links this module statically into it (and
// `export(project(":nativecomposekit"))` re-exports the kit's public ObjC symbols into the framework header so the
// native chrome shell can see the NativeChromeSource contract / NativeShellChrome / etc.). Dependency-pure:
// Compose-official artifacts only, no Coil/Ktor (those stay app-side in :composeApp).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.binary.compatibility.validator)
    `maven-publish`
}

// Published coordinates: io.github.apdelrahman1911:nativecomposekit:<version>. These also make Gradle
// composite-build substitution work (`includeBuild(...)` in a consumer maps this module onto the same
// coordinates), and `publishToMavenLocal` produces a consumable artifact set today; Maven Central signing
// and upload are layered on top of this later without changing consumers.
group = "io.github.apdelrahman1911"
version = "0.1.0"

// ABI lock: the public surface is dumped to nativecomposekit/api/*.api (JVM) + *.klib.api (native), and `apiCheck`
// (wired into `check`) fails the build if the public API changes without an intentional `:nativecomposekit:apiDump`.
apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib {
        // Also validate the KLIB ABI (the iOS-facing surface — e.g. NativeNavBridge — that the JVM dump misses).
        enabled = true
    }
}

kotlin {
    // Library ABI hardening: every public/protected declaration must declare its visibility explicitly
    // (and public functions/properties their return types). Catches accidental API exposure at compile time.
    explicitApi()

    androidTarget {
        // Publish the release variant of the Android target (required for the android artifact to be
        // included in the multiplatform publication).
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Same target rationale as :composeApp (iosX64 omitted — CMP 1.11.0 dropped Apple x86_64).
    // No binaries.framework {} here: :composeApp emits the one framework and links this statically.
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // `api`, not `implementation`: these Compose types appear in the kit's PUBLIC component signatures
            // (Modifier, @Composable params, Color, PaddingValues, ImageVector via NativeIcon), so consumers
            // (:composeApp) must see them transitively.
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.materialIconsExtended)
            // NO Coil, NO Ktor — image loading + networking are app concerns (kept in :composeApp).
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        // Compose UI tests on the JVM via Robolectric (no emulator) — test-only, never shipped.
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        androidUnitTest.dependencies {
            implementation(compose.uiTest)
            implementation(libs.robolectric)
            implementation(libs.junit)
        }
    }
}

android {
    // Android namespace is distinct from :composeApp's (io.github.apdelrahman1911.nativecomposekit) to avoid
    // R-class ambiguity — it maps to the R-class package only, and is independent of the Kotlin package
    // (io.github.apdelrahman1911.nativecomposekit.*) and the iOS framework. The kit ships no Android resources.
    namespace = "io.github.apdelrahman1911.nativecomposekit.kit"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true // Robolectric loads themes/manifest for the Compose UI tests
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // The empty ComponentActivity + manifest the Robolectric Compose UI tests host into (debug variant).
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// POM metadata on every publication (the KMP plugin creates one per target + the root module).
publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("NativeComposeKit")
            description.set(
                "Compose Multiplatform UI kit: one shared component API, rendered with Material 3 on " +
                    "Android and real UIKit controls on iOS.",
            )
            url.set("https://github.com/Apdelrahman1911/NativeComposeKit")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("Apdelrahman1911")
                    name.set("Apdelrahman1911")
                    url.set("https://github.com/Apdelrahman1911")
                }
            }
            scm {
                url.set("https://github.com/Apdelrahman1911/NativeComposeKit")
                connection.set("scm:git:https://github.com/Apdelrahman1911/NativeComposeKit.git")
                developerConnection.set("scm:git:https://github.com/Apdelrahman1911/NativeComposeKit.git")
            }
        }
    }
}
