# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AMRIT FLW (Field Level Worker) Mobile App -- a native Android application (Kotlin) for field-level healthcare workers to register beneficiaries, conduct health screenings, and sync data with the AMRIT backend. Part of the AMRIT healthcare EHR platform by Piramal Swasthya. Licensed under GPL v3.

## Common Commands

```bash
./gradlew assembleDebug                    # Debug build
./gradlew assembleRelease                  # Release build (requires signing config)
./gradlew test                             # Run unit tests
./gradlew connectedAndroidTest             # Run instrumented tests
./gradlew clean                            # Clean build
```

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 23 (Android 6.0) / **Target SDK:** 33 / **Compile SDK:** 34
- **Build:** Gradle 8.0 with Android Gradle Plugin
- **DI:** Dagger Hilt
- **Navigation:** Jetpack Navigation (SafeArgs)
- **Firebase:** Crashlytics, Google Services
- **Maps:** Google Maps SDK (Secrets Gradle Plugin for API keys)

## Product Flavors

Multiple flavors under `default` dimension:
- `sakshamStag` -- Staging
- `sakshamUat` -- UAT
- `saksham` -- Production
- `xushrukha`, `niramay` -- Variant deployments

Application ID: `org.piramalswasthya.sakhi` (with flavor-specific suffixes).

## Key Directory Layout

```
app/src/main/java/org/piramalswasthya/sakhi/
  SakhiApplication.kt          # Application class
  activity_contracts/           # RD Service (biometric) contracts
  adapters/                     # RecyclerView adapters
  configuration/                # App configuration
  contracts/                    # Activity result contracts
  crypt/                        # Encryption utilities
  custom_views/                 # Custom Android views
  database/                     # Room database, DAOs, entities
  di/                           # Hilt dependency injection modules
  helpers/                      # Utility helpers
  model/                        # Data models
  network/                      # Retrofit API services, network layer
  repositories/                 # Data repositories (offline-first pattern)
  ui/                           # UI layer
    home_activity/              # Main home screen
    login_activity/             # Login flow
    abha_id_activity/           # ABHA ID integration
    service_location_activity/  # Service location selection
  utils/                        # Utility classes
  work/                         # WorkManager background tasks
```

### Architecture

- **MVVM** with Repository pattern
- **Offline-first:** Room database for local storage, WorkManager for background sync
- **Navigation:** Single-activity with Jetpack Navigation component (SafeArgs for type-safe arguments)
- **i18n:** Supports English (`en`), Hindi (`hi`), Assamese (`as`) via Android resource qualifiers

### Build Notes

- Release builds have minification (`minifyEnabled true`) and resource shrinking enabled
- APK splits enabled for: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64` plus universal
- Version managed via `versioning.gradle` (auto-generates `versionCode` and `versionName`)
- Firebase App Distribution configured for testing via `fastlane`
