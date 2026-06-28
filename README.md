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

The debug APK is published as a **workflow artifact** named `grabhub-debug-apk`.

**Where to download:** open a green **Android CI** run (not the job log), scroll to the bottom, section **Artifacts**:

- Latest successful `develop` run: https://github.com/s-predybailo/grabhub/actions/workflows/android-ci.yml

On the run page use the `#artifacts` anchor, for example:

https://github.com/s-predybailo/grabhub/actions/runs/28319825048#artifacts

Failed runs do not publish an APK. After download, install on a device:

```bash
adb install androidApp-debug.apk
```

You can also trigger a build manually: Actions → **Android CI** → **Run workflow**.

## Architecture

```
shared/
  domain/     ModelItem, SourceType, SearchPage
  providers/  PrintablesProvider, ThingiverseProvider
  engine/     SearchEngine (parallel merge + dedup)
  data/       SearchRepository
  network/    Ktor HttpClient
```
