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

### Build

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:iosSimulatorArm64Test   # shared logic tests
```

## Architecture

```
shared/
  domain/     ModelItem, SourceType, SearchPage
  providers/  PrintablesProvider, ThingiverseProvider
  engine/     SearchEngine (parallel merge + dedup)
  data/       SearchRepository
  network/    Ktor HttpClient
```
