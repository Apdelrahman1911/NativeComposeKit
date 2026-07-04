import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The Native UI design-system kit as a standalone KMP library. It does NOT emit its own iOS framework —
// `:composeApp` owns the single `ComposeApp` framework and links this module statically into it (and
// `export(project(":nativecomposekit"))` re-exports the kit's public ObjC symbols into the framework header so the
// native chrome shell can see the NativeChromeSource contract / NativeShellChrome / etc.). Dependency-pure:
// Compose-official artifacts only, no Coil/Ktor (those stay app-side in :composeApp). The publishing
// plugins below are build-time only and add nothing to the runtime dependency graph.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.binary.compatibility.validator)
    // Renders the API docs the javadoc jar packages (Maven Central requires one per module).
    alias(libs.plugins.dokka)
    // Applies maven-publish itself and adds: the KMP publication set, the javadoc/sources jars, in-memory
    // GPG signing, and the Central Portal upload tasks (publishToMavenCentral / publishAndReleaseToMavenCentral).
    alias(libs.plugins.vanniktech.maven.publish)
}

// Published coordinates: io.github.apdelrahman1911:nativecomposekit:<version> — the group MUST stay
// `io.github.apdelrahman1911` verbatim: Central Portal namespace ownership is verified against the GitHub
// username (github.com/Apdelrahman1911), lowercased. These coordinates also make Gradle composite-build
// substitution work (`includeBuild(...)` in a consumer maps this module onto them), and
// `publishToMavenLocal` produces the full consumable artifact set locally.
group = "io.github.apdelrahman1911"
version = "0.2.0"

// ABI lock: the public surface is dumped to nativecomposekit/api/*.api (JVM) + *.klib.api (native), and `apiCheck`
// (wired into `check`) fails the build if the public API changes without an intentional `:nativecomposekit:apiDump`.
apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib {
        // Also validate the KLIB ABI (the iOS-facing surface — e.g. NativeChromeSource — that the JVM dump misses).
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
            // (Modifier, @Composable params, Color, PaddingValues, ImageVector), so consumers (:composeApp)
            // must see them transitively.
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            // Internal-only: default status/rating/list glyphs. ImageVector itself lives in compose-ui and
            // nothing from this artifact reaches a public signature, so consumers don't inherit the large
            // extended-icons dependency.
            implementation(compose.materialIconsExtended)
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

// Maven Central publishing (docs/publishing.md is the full runbook). The KMP publication set is the root
// module + one artifact per target (android release AAR, iosArm64/iosSimulatorArm64 klibs), each with a
// sources jar and the Dokka-rendered javadoc jar Central requires.
mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        ),
    )

    // Upload goes to the Central Portal (central.sonatype.com — the OSSRH staging flow is retired).
    // Credentials come from ~/.gradle/gradle.properties or ORG_GRADLE_PROJECT_* env vars, never this repo.
    publishToMavenCentral()

    // Sign only when a key is configured, so local/CI `publishToMavenLocal` smoke publishes stay unsigned
    // and keyless. A Central upload without signatures fails portal validation — the runbook covers keys.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) signAllPublications()

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
