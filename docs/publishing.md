# Publishing to Maven Central

The release runbook for `:nativecomposekit`. Repo-side wiring is done; the one-time account/key steps in
[§2](#2-one-time-setup-account-namespace-key) can only be performed by the project owner.

## Coordinates

```
io.github.apdelrahman1911 : nativecomposekit : <version>
```

- **Group `io.github.apdelrahman1911` is load-bearing** — Central Portal verifies `io.github.*` namespaces
  against the GitHub account, so it must equal the repo owner's GitHub username
  ([github.com/Apdelrahman1911](https://github.com/Apdelrahman1911)), lowercased. Do not "fix" the spelling.
- Artifact id = the Gradle module name (`nativecomposekit`). The KMP plugin derives per-target artifacts
  from it (`nativecomposekit-android`, `nativecomposekit-iosarm64`, `nativecomposekit-iossimulatorarm64`).
- Version lives in `nativecomposekit/build.gradle.kts` (`version = "0.1.0"`).

## 1. What is wired in the repo

- **Plugins** (`gradle/libs.versions.toml`, build-time only): `com.vanniktech.maven.publish` 0.37.0
  (publication set, signing, Central Portal upload) + `org.jetbrains.dokka` 2.2.0 (the API-docs javadoc
  jar Central requires; v2 plugin, K2 analysis).
- **Publication set** per version: root KMP module + android release AAR + the two iOS klib targets, each
  with `-sources.jar` and `-javadoc.jar`, full POM (name/description/url/Apache-2.0/developer/scm) and
  Gradle module metadata.
- **Signing** is gated on `signingInMemoryKey` being present: `publishToMavenLocal` works unsigned and
  keyless (CI smoke-publishes it on every push); a Central upload signs everything.
- **Tasks**:
  - `./gradlew :nativecomposekit:publishToMavenLocal` — full artifact set into `~/.m2` (local verification).
  - `./gradlew publishToMavenCentral` — signed upload to a Portal deployment that waits for the **Publish**
    button in the Portal UI (recommended for the first release).
  - `./gradlew publishAndReleaseToMavenCentral` — same, then releases automatically once validation passes.

## 2. One-time setup (account, namespace, key)

Owner-only steps; nothing here touches the repo.

1. **Central Portal account** — sign up at [central.sonatype.com](https://central.sonatype.com), ideally
   **Sign in with GitHub** (as the `Apdelrahman1911` account).
2. **Namespace** — add namespace `io.github.apdelrahman1911`. With GitHub sign-in it verifies
   automatically; otherwise the Portal shows a verification key and you create a public GitHub repo named
   exactly that key to prove ownership. Wait for the namespace to show **Verified**.
3. **Publishing token** — Portal → account (top right) → *View Account* → *Generate User Token*. This
   yields a `username` + `password` pair (NOT your login credentials).
4. **GPG key** —
   ```bash
   gpg --full-generate-key            # RSA and RSA, 4096 bit, your name + the GitHub-noreply or real email
   gpg --list-secret-keys --keyid-format short   # note the short KEY_ID (8 hex chars)
   gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID   # publish the PUBLIC key (validation needs it)
   gpg --export-secret-keys --armor KEY_ID > central-signing.asc  # keep PRIVATE; never commit
   ```
5. **Credentials on the publishing machine** — in `~/.gradle/gradle.properties` (never the repo):
   ```properties
   mavenCentralUsername=<token username>
   mavenCentralPassword=<token password>
   signingInMemoryKeyId=<KEY_ID, the 8-hex short id>
   signingInMemoryKeyPassword=<key passphrase>
   signingInMemoryKey=<contents of central-signing.asc with every newline replaced by \n>
   ```
   The `\n`-escaping is a `gradle.properties` limitation; alternatively export the same five values as
   `ORG_GRADLE_PROJECT_mavenCentralUsername=…` etc. environment variables, where real newlines are fine.
   Details: [vanniktech plugin docs](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).

## 3. Verify locally (no upload, no credentials)

```bash
./gradlew :nativecomposekit:publishToMavenLocal
ls -R ~/.m2/repository/io/github/apdelrahman1911/
```

Expect four artifact directories (`nativecomposekit`, `-android`, `-iosarm64`, `-iossimulatorarm64`), each
containing the main artifact (module jar/AAR/klib), `-sources.jar`, `-javadoc.jar`, `.pom`, and `.module`.
CI runs this same task on every push (macOS job, "Smoke-publish the full multiplatform artifact set").

Optionally consume it from a scratch project via `mavenLocal()` to prove resolution end to end.

## 4. Release flow (per version)

1. Pre-flight: CI green on `main`; `CHANGELOG.md` — set the release date on the version section; README
   coordinates show the version being released; `./gradlew :nativecomposekit:apiCheck` clean.
2. Upload: `./gradlew publishToMavenCentral` (signed; requires §2 credentials).
3. In the Portal → *Deployments*: wait for validation to pass, review the component list, press
   **Publish**. (Or use `publishAndReleaseToMavenCentral` to skip the button.)
4. Sync time: minutes to ~1 h until `repo1.maven.org` serves it; search indexing can lag hours.
5. Tag and record:
   ```bash
   git tag v0.1.0 && git push origin v0.1.0
   ```
   Then bump `version` in `nativecomposekit/build.gradle.kts` for the next cycle (e.g. `0.2.0`), update
   README's dependency line, start a new `## [Unreleased]` CHANGELOG section, commit.

## 5. Later (optional)

- **CI publishing**: move the five §2 values into GitHub Actions secrets (`ORG_GRADLE_PROJECT_*` env) and
  add a tag-triggered workflow that runs `publishAndReleaseToMavenCentral`.
- **Snapshots**: the Portal supports `-SNAPSHOT` publishing once enabled for the namespace
  (`central.sonatype.com` → namespace settings); consumers then add the portal snapshots repository.
