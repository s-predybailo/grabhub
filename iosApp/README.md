# GrabHub iOS App

SwiftUI shell for iOS. The shared KMP framework is built by Gradle and linked from Xcode.

## Generate the shared framework

On macOS, from the project root:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

Or build for simulator:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

The framework output is at `shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework`.

## Xcode setup

1. Open `iosApp/GrabHub.xcodeproj` in Xcode.
2. Add a **Run Script** build phase before Compile Sources:

   ```bash
   cd "$SRCROOT/.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```

3. Link `Shared.framework` and set **Framework Search Paths** to:
   `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`

4. Import in Swift: `import Shared`

## SwiftUI shell

See `iosApp/GrabHub/ContentView.swift` for the search screen shell that calls into the shared module via `SearchFacade`.

## Thingiverse token

Set `THINGIVERSE_ACCESS_TOKEN` in the Xcode scheme environment variables or in `iosApp/Config.xcconfig`.
