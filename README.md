# GrabHub

Cross-platform 3D model search aggregator built with Kotlin Multiplatform.

## Platforms

- **Android** — Jetpack Compose
- **iOS** — SwiftUI + shared KMP framework
- **Shared** — domain, providers, search engine

## Phase 1 (MVP)

- Unified search across [Printables](https://www.printables.com) and [Thingiverse](https://www.thingiverse.com)
- API-based providers (GraphQL / REST), no HTML parsing
- Paginated provider interface (UI shows first page in MVP)

## Phase 2

- [MakerWorld](https://makerworld.com) provider (Bambu Lab search API)
- [Creality Cloud](https://www.crealitycloud.com) provider (smart search API)
- SQLDelight search/model cache (15 min TTL)
- Improved deduplication (normalized title + author)
- Android detail screen with navigation

## Phase 3

- Search filters: free / paid, popularity sort, license (commercial / non-commercial)
- Favorites (SQLDelight, toggle on detail screen)
- Search history (last 50 queries, tap to re-run)
- Bottom navigation: Search, Favorites, History

## Releases

| Tag | Contents |
|-----|----------|
| v0.1.0 | Phase 1 MVP |
| v0.3.0 | Phase 2 + Phase 3 (4 providers, cache, detail, filters, favorites, history) |
| v0.7.9 | UI overhaul: home screen with shortcuts and discover feed, browse feeds (Popular/Latest/Trending), floating glass bottom nav, detail screen polish, carousel deduplication fix |

## Gitflow

| Branch | Purpose |
|--------|---------|
| `main` | Production releases |
| `develop` | Integration branch |
| `feature/*` | Feature development |
| `release/*` | Release preparation |
| `hotfix/*` | Production hotfixes |

## Setup

### Requirements

- JDK 17+
- Android SDK (for Android app)
- Xcode (for iOS app, macOS only)

### Thingiverse API token

Thingiverse requires an access token. Register an app at https://www.thingiverse.com/developers and set:

```properties
# androidApp/local.properties or environment variable
THINGIVERSE_ACCESS_TOKEN=your_token_here
```

Printables works without authentication.

### Thingiverse API token

Thingiverse requires an access token. Register an app at https://www.thingiverse.com/developers and set:

```properties
# androidApp/local.properties or environment variable
THINGIVERSE_ACCESS_TOKEN=your_token_here
```

For CI, add the same value as a GitHub repository secret named `THINGIVERSE_ACCESS_TOKEN`
(Settings → Secrets and variables → Actions).

### Build

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:iosSimulatorArm64Test   # shared logic tests
```

### CI (GitHub Actions)

Every push to `main` / `develop` and every PR runs:

1. `:shared:jvmTest`
2. `:androidApp:assembleDebug`

#### Recommended: GitHub Release (easiest download)

Each successful build on `main` or `develop` updates the pre-release **`debug-latest`**:

https://github.com/s-predybailo/grabhub/releases/tag/debug-latest

Download `androidApp-debug.apk` directly from the **Assets** section — no Actions UI needed.

#### Alternative: workflow artifact

The same APK is also stored as artifact `grabhub-debug-apk` on the workflow run page (section **Artifacts** at the bottom). You must be signed in to GitHub; click **Download** on the artifact row, not the `sha256:` digest.

CLI:

```bash
gh release download debug-latest -p '*.apk' -R s-predybailo/grabhub
# or
gh run download -R s-predybailo/grabhub -n grabhub-debug-apk
```

Install on device:

```bash
adb install androidApp-debug.apk
```

You can also trigger a build manually: Actions → **Android CI** → **Run workflow**.

### iOS CI (GitHub Actions)

Every push to `main` / `develop` and every PR also runs **iOS CI** on `macos-15`:

1. `:shared:iosSimulatorArm64Test`
2. Xcode build of `GrabHub.app` for the iOS Simulator

Download artifacts from the workflow run page:

| Artifact | Contents |
|----------|----------|
| `grabhub-ios-simulator-debug` | Zip with `GrabHub.app` for Simulator |
| `grabhub-shared-ios-simulator-framework` | Kotlin `Shared.framework` used by the app |

Install on a booted simulator:

```bash
unzip GrabHub-ios-simulator-debug.zip
xcrun simctl install booted GrabHub.app
xcrun simctl launch booted com.grabhub.ios
```

You can trigger a build manually: Actions → **iOS CI** → **Run workflow**.

Note: CI produces **Simulator** builds only (no device `.ipa` / TestFlight) because code signing is not configured in the workflow.

## Architecture

```
shared/
  domain/     ModelItem, SourceType, SearchPage
  providers/  PrintablesProvider, ThingiverseProvider
  engine/     SearchEngine (parallel merge + dedup)
  data/       SearchRepository
  network/    Ktor HttpClient
```
