# Publishing to Maven Central

The release runbook for `:nativecomposekit`. Publishing happens ONLY through the protected GitHub Actions
release flow described in [§4](#4-release-flow-github-actions-only) — never from a developer machine.

## Branch model

| Branch | Role | CI behavior |
|---|---|---|
| `main` | development | `ci.yml` verification only — can never publish, tag, or create releases |
| `testing` | integration / pre-release verification | `ci.yml` verification only — same guarantee |
| `release` | release candidates | `ci.yml` verification **plus** `release.yml`: full verify → **manual approval gate** → signed Maven Central publish → tag `v<version>` → GitHub Release |

The publish step exists solely in `release.yml`, whose every job is pinned to `refs/heads/release`
(both for the `push` trigger and manual `workflow_dispatch`), and whose publish job runs inside the
`maven-central` GitHub Environment — with a required reviewer configured there, an accidental push to
`release` stops at the approval gate before anything is uploaded.

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
  - `./gradlew :nativecomposekit:publishToMavenLocal` — full artifact set into `~/.m2` (local verification;
    this is the only publish-shaped task ever run outside the release workflow).
  - `./gradlew publishAndReleaseToMavenCentral` — signed upload + validation + release. Reserved for the
    protected `release.yml` publish job (policy: no publishing from developer machines); the manual gate is
    the GitHub Environment approval, not the Portal UI button.

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
5. **GitHub Environment + secrets** (what the release workflow uses) — in the repo:
   *Settings → Environments → New environment* named exactly **`maven-central`**, then:
   - **Protection**: add a *Required reviewer* (yourself). This is the manual approval gate — the publish
     job cannot start until the pending deployment is approved.
   - **Environment secrets** (exact names; exposed only to the approved publish job):

     | Secret | Value |
     |---|---|
     | `MAVEN_CENTRAL_USERNAME` | the Portal user-token username |
     | `MAVEN_CENTRAL_PASSWORD` | the Portal user-token password |
     | `SIGNING_KEY_ID` | the 8-hex short id of the signing key |
     | `SIGNING_KEY` | the full armored private key (paste `central-signing.asc` as-is — real newlines are fine in Actions secrets) |
     | `SIGNING_PASSPHRASE` | the key's passphrase |

   The workflow maps these to `ORG_GRADLE_PROJECT_*` Gradle properties; GitHub masks secret values in all
   logs. For **local artifact verification only** the same five values can live in
   `~/.gradle/gradle.properties` (`mavenCentralUsername`, `mavenCentralPassword`, `signingInMemoryKeyId`,
   `signingInMemoryKey` with `\n`-escaped newlines, `signingInMemoryKeyPassword`) — but the Central upload
   itself is reserved for the Actions flow. Details:
   [vanniktech plugin docs](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).

## 3. Verify locally (no upload, no credentials)

```bash
./gradlew :nativecomposekit:publishToMavenLocal
ls -R ~/.m2/repository/io/github/apdelrahman1911/
```

Expect four artifact directories (`nativecomposekit`, `-android`, `-iosarm64`, `-iossimulatorarm64`), each
containing the main artifact (module jar/AAR/klib), `-sources.jar`, `-javadoc.jar`, `.pom`, and `.module`.
CI runs this same task on every push (macOS job, "Smoke-publish the full multiplatform artifact set").

Optionally consume it from a scratch project via `mavenLocal()` to prove resolution end to end.

## 4. Release flow (GitHub Actions only)

1. Pre-flight on `main`: CI green; `CHANGELOG.md` — release date set on the version section; README
   dependency line shows the version being released.
2. Fast-forward the branches: `testing` first if you want a verification-only dry pass, then `release`:
   ```bash
   git push origin main:release
   ```
3. `release.yml` runs on the push: the **verify** job re-runs the full gates plus a **version guard**
   (fails cleanly if `v<version>` is already tagged or `<version>` already exists on Maven Central — and
   the Portal rejects duplicate uploads server-side as an independent backstop).
4. The **publish** job then waits in the `maven-central` environment for **manual approval** (Actions run
   page → *Review deployments* → approve). On approval it signs in-memory, uploads to the Central Portal,
   waits for validation, and releases the deployment.
5. On success the workflow **tags `v<version>`** (lightweight, on the released commit) and creates a
   **GitHub Release** whose notes are that version's `CHANGELOG.md` section.
6. Sync time: minutes to ~1 h until `repo1.maven.org` serves it; search indexing can lag hours.
7. Roll over for the next cycle (on `main`): bump `version` in `nativecomposekit/build.gradle.kts`,
   update README's dependency line, start a new `## [Unreleased]` CHANGELOG section.

A re-run can be triggered without a new commit via *Actions → Release → Run workflow* — **select the
`release` branch**; every job is ref-guarded and becomes a no-op on any other branch.

## 5. Later (optional)

- **Snapshots**: the Portal supports `-SNAPSHOT` publishing once enabled for the namespace
  (`central.sonatype.com` → namespace settings); consumers then add the portal snapshots repository.
