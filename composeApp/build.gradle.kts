import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iosX64 intentionally omitted: Compose Multiplatform 1.11.0 dropped Apple x86_64 support
    // (Kotlin deprecation KT-81596). Apple Silicon Macs use iosSimulatorArm64; physical iPhones
    // use iosArm64.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // Must match `import ComposeApp` + `MainViewControllerKt.MainViewController()` in the
            // Swift host (iosApp/iosApp/ContentView.swift).
            baseName = "ComposeApp"
            isStatic = true
            // Re-export the :brandkit library's public ObjC symbols into ComposeApp-Swift.h so the SwiftUI
            // shell can still see BrandNavBridge / BrandNavCancellable / BrandShellChromeKt (now living in
            // :brandkit). Requires `api(project(":brandkit"))` below (export needs the api configuration).
            export(project(":brandkit"))
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // The design-system kit, extracted to its own module. `api` (not implementation) is required so
            // the iOS framework's `export(project(":brandkit"))` can re-export its public ObjC symbols.
            api(project(":brandkit"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // Material icons (used by the catalog for BrandIcon leading/trailing demos). Pinned to
            // 1.7.3 upstream — the deprecation note is cosmetic; a real app can swap in its own
            // vector assets. Icons are caller-supplied, not baked into the Brand* components.
            implementation(compose.materialIconsExtended)
            // Image loading — APP-LEVEL ONLY (used by the manga screens). The kit's `components/` deliberately
            // has no third-party deps; image loading is an app concern handled by Coil 3 (+ its Ktor-3 network
            // fetcher; platform Ktor engines are added per source set below).
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
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
    namespace = "io.github.apdelrahman1911.nativecomposekit"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.apdelrahman1911.nativecomposekit"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
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
            isIncludeAndroidResources = true // required so Robolectric can load themes/manifest for Compose tests
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Merges the empty ComponentActivity + manifest that Compose UI tests host into (debug variant = what the
    // Robolectric unit tests use). Test-only; never in a release build.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
