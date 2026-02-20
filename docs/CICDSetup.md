# CI/CD Setup

CI/CD pipeline for the FLW Mobile App using GitHub Actions and Fastlane. Builds are triggered manually via `workflow_dispatch` and distributed to Firebase App Distribution (debug) or Google Play Store internal track (release).

---

## Table of Contents

- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Project Configuration Map](#project-configuration-map)
- [Workflow Files](#workflow-files)
  - [flw-android-build.yml](#flw-android-buildyml)
  - [build-distribute.yml](#build-distributeyml)
- [Fastlane](#fastlane)
- [GitHub Environments and Secrets](#github-environments-and-secrets)
  - [Environment Setup](#environment-setup)
  - [Required Secrets by Build Type](#required-secrets-by-build-type)
  - [Managing Secrets](#managing-secrets)
- [Version Management](#version-management)
- [Native Build (C++ / JNI)](#native-build-c--jni)
- [Build Configuration (build.gradle)](#build-configuration-buildgradle)
- [Firebase App Distribution Config](#firebase-app-distribution-config)

---

## Quick Start

1. Go to **Actions** tab in the GitHub repository.
2. Select **FLW Android Build** workflow.
3. Click **Run workflow**.
4. Fill in:
   - **Project**: Select from the dropdown (e.g., `sakshamUat`).
   - **Branch**: The git branch to build (default: `main`).
   - **Version**: Version name for the build (e.g., `2.5.0`).
   - **Version Code**: Integer version code for the build (e.g., `16`).
5. Click **Run workflow** to start.

The pipeline automatically resolves the Gradle flavor, GitHub environment, and build type based on the selected project. Debug builds go to Firebase App Distribution; release builds go to Google Play Store internal track.

---

## Architecture

```text
flw-android-build.yml (caller)
  |
  +-- resolve-config job
  |     Maps project input -> flavor, environment, build_type
  |
  +-- flw-build job (calls build-distribute.yml)
        Uses: secrets: inherit + environment scoping
        |
        +-- Setup (JDK, SDK, NDK, CMake, Ruby)
        +-- Decode secrets (conditional on build type)
        +-- Fastlane build and distribute
```

---

## Project Configuration Map

| Project Input  | Gradle Flavor  | GitHub Environment     | Build Type | Distribution          |
|----------------|----------------|------------------------|------------|-----------------------|
| `sakshamStag`  | `SakshamStag`  | `SAKSHAM_STAG`         | debug      | Firebase App Dist     |
| `sakshamUat`   | `SakshamUat`   | `SAKSHAM_UAT`          | debug      | Firebase App Dist     |
| `saksham`      | `Saksham`      | `SAKSHAM_PRODUCTION`   | release    | Play Store (internal) |
| `xushrukha`    | `Xushrukha`    | `XUSHRUKHA_PRODUCTION` | release    | Play Store (internal) |
| `niramay`      | `Niramay`      | `NIRAMAY_PRODUCTION`   | release    | Play Store (internal) |
| `mitaninStag`  | `MitaninStag`  | `MITANIN_STAG`         | debug      | Firebase App Dist     |
| `mitaninUat`   | `MitaninUat`   | `MITANIN_UAT`          | debug      | Firebase App Dist     |
| `mitanin`      | `Mitanin`      | `MITANIN_PRODUCTION`   | release    | Play Store (internal) |

---

## Workflow Files

### `flw-android-build.yml`

**Path**: `.github/workflows/flw-android-build.yml`

The caller workflow, triggered manually via `workflow_dispatch`. Accepts 3 inputs:

| Input         | Type   | Required | Description                              |
|---------------|--------|----------|------------------------------------------|
| `project`     | choice | yes      | Dropdown with all 8 project options      |
| `branch`      | string | yes      | Git branch to build (default: `main`)    |
| `version`     | string | yes      | Version name (e.g., `2.5.0`)            |
| `versionCode` | string | yes      | Version code integer (e.g., `16`)        |

**Jobs**:

1. **`resolve-config`**: Maps the `project` input to `flavor`, `environment`, and `build_type` using a shell `case` statement. Outputs these values for the next job.
2. **`flw-build`**: Calls `build-distribute.yml` with the resolved configuration. Uses `secrets: inherit` so both repo-level and environment-scoped secrets are available.

### `build-distribute.yml`

**Path**: `.github/workflows/build-distribute.yml`

A reusable workflow (`workflow_call`) that performs the actual build. Accepts 6 inputs: `branch`, `environment`, `flavor`, `build_type`, `version`, `version_code`.

**Key steps**:

1. **Checkout code** from the specified branch.
2. **Setup toolchain**: JDK 17 (Zulu), Android SDK, NDK r29, CMake 3.31.1, Ruby 3.1.4.
3. **Decode secrets** (conditional):
   - `google-services.json` - always (from `GOOGLE_SERVICES_JSON_GENERIC`).
   - `firebase_credentials.json` - debug builds only (from `FIREBASE_CREDENTIALS_JSON`).
   - `google_play_service_account.json` - release builds only (from `GOOGLE_PLAY_JSON_KEY`).
   - `keystore.jks` - release builds only (from `KEYSTORE_FILE`).
4. **Export env vars** for native C++ build (8 variables matching `CMakeLists.txt`).
5. **Run Fastlane**:
   - Debug: `bundle exec fastlane build_and_distribute_debug flavor:<Flavor> version_name:<version>`
   - Release: `bundle exec fastlane build_and_distribute_release flavor:<Flavor> version_name:<version>`

**Note**: Secrets are not explicitly declared in the `workflow_call` block. They are inherited from the caller (`secrets: inherit`) and scoped by the `environment:` field on the build job.

---

## Fastlane

**Path**: `fastlane/Fastfile`

### Lanes

**`build_and_distribute_debug`**:
1. Uses the manually provided version code from workflow input.
2. Writes `version/version.properties` with the provided version name and version code.
3. Runs `gradle clean assemble<Flavor>Debug`.
4. Distributes APK to Firebase App Distribution using groups and release notes from `FirebaseAppDistributionConfig/`.

**`build_and_distribute_release`**:
1. Uses the manually provided version code from workflow input.
2. Validates that the version code is greater than the current highest on Play Store (fails early if not).
3. Writes `version/version.properties` with the provided version name and version code.
4. Runs `gradle clean bundle<Flavor>Release` with keystore signing properties.
5. Uploads AAB to Google Play Store internal track as a draft.

### Helper Functions

- **`get_package_name(flavor)`**: Maps a Gradle flavor name to its full package name (`org.piramalswasthya.sakhi.<suffix>`). Supports all 8 flavors.
- **`latest_googleplay_version_code(package_name)`**: Scans production, beta, alpha, and internal tracks for the highest version code. Used in the release lane to validate that the provided version code is greater than the current highest. Returns 0 if none found.

### Options

Both lanes accept:
- `flavor:` - The Gradle flavor name (e.g., `SakshamUat`).
- `version_name:` - The version name string (e.g., `2.5.0`).
- `version_code:` - The integer version code (e.g., `16`).

---

## GitHub Environments and Secrets

### Environment Setup

The pipeline requires one GitHub environment per project configuration. Each environment scopes its secrets so they are only available to builds targeting that environment.

**Required environments**:

| Environment            | Used By         | Type       |
|------------------------|-----------------|------------|
| `SAKSHAM_STAG`         | `sakshamStag`   | Debug      |
| `SAKSHAM_UAT`          | `sakshamUat`    | Debug      |
| `SAKSHAM_PRODUCTION`   | `saksham`       | Production |
| `XUSHRUKHA_PRODUCTION` | `xushrukha`     | Production |
| `NIRAMAY_PRODUCTION`   | `niramay`       | Production |
| `MITANIN_STAG`         | `mitaninStag`   | Debug      |
| `MITANIN_UAT`          | `mitaninUat`    | Debug      |
| `MITANIN_PRODUCTION`   | `mitanin`       | Production |

To create or manage environments: **Settings > Environments** in the GitHub repository.

### Required Secrets by Build Type

**Repo-level secrets** (shared across all builds):

| Secret                       | Description                              |
|------------------------------|------------------------------------------|
| `ENCRYPTED_PASS_KEY`         | Encryption key for native C++ build      |
| `CHAT_URL`                   | Chat service URL for native build        |
| `GOOGLE_SERVICES_JSON_GENERIC` | Base64-encoded google-services.json    |

**Shared environment secrets** (needed by both debug and production):

| Secret                     | Description                                |
|----------------------------|--------------------------------------------|
| `ABHA_CLIENT_SECRET`       | ABHA API client secret                     |
| `ABHA_CLIENT_ID`           | ABHA API client ID                         |
| `BASE_TMC_URL`             | TMC base URL                               |
| `BASE_ABHA_URL`            | ABHA base URL                              |
| `ABHA_TOKEN_URL`           | ABHA token endpoint URL                    |
| `ABHA_AUTH_URL`            | ABHA auth endpoint URL                     |

**Debug-only environment secrets** (STAG/UAT - used for Firebase App Distribution):

| Secret                     | Description                                |
|----------------------------|--------------------------------------------|
| `FIREBASE_APP_ID`          | Firebase App ID for distribution           |
| `FIREBASE_CREDENTIALS_JSON`| Base64-encoded Firebase service account key|

**Production-only environment secrets** (used for Play Store signing and upload):

| Secret                     | Description                                |
|----------------------------|--------------------------------------------|
| `GOOGLE_PLAY_JSON_KEY`     | Base64-encoded Google Play service account |
| `KEYSTORE_FILE`            | Base64-encoded release keystore (.jks)     |
| `KEYSTORE_PASSWORD`        | Keystore password                          |
| `KEY_ALIAS`                | Signing key alias                          |
| `KEY_PASSWORD`             | Signing key password                       |

### Managing Secrets

**Add/Update a secret**:
1. Go to **Settings > Environments > [environment name]**.
2. Under **Environment secrets**, click **Add secret** or update an existing one.
3. Enter the name and Base64-encoded value (where applicable).

**Add a new environment**:
1. Go to **Settings > Environments > New environment**.
2. Name it matching the convention (e.g., `NEWPROJECT_PRODUCTION`).
3. Add all required secrets for its build type (debug or production).
4. Add the project to the `case` statement in `flw-android-build.yml`.
5. Add the flavor to `get_package_name()` in `fastlane/Fastfile`.

---

## Version Management

Version is managed through `version/version.properties`:

```properties
VERSION=2.5.0
VERSIONCODE=27
```

- **VERSION**: The human-readable version name (e.g., `2.5.0`).
- **VERSIONCODE**: The integer version code used by Android and app stores.

During CI builds, Fastlane overwrites this file with the version name from the workflow input and an auto-incremented version code (from Firebase or Play Store). The `versioning.gradle` file reads these values and applies them to the build.

For local development, edit `version/version.properties` directly.

---

## Native Build (C++ / JNI)

The app uses a native C++ library built via CMake (`app/src/main/cpp/CMakeLists.txt`). The following 8 environment variables must be set at build time:

| Variable              | Source                |
|-----------------------|-----------------------|
| `ENCRYPTED_PASS_KEY`  | Repo-level secret     |
| `ABHA_CLIENT_SECRET`  | Environment secret    |
| `ABHA_CLIENT_ID`      | Environment secret    |
| `BASE_TMC_URL`        | Environment secret    |
| `BASE_ABHA_URL`       | Environment secret    |
| `ABHA_TOKEN_URL`      | Environment secret    |
| `ABHA_AUTH_URL`       | Environment secret    |
| `CHAT_URL`            | Repo-level secret     |

These are exported as shell environment variables in the `build-distribute.yml` workflow before invoking Fastlane/Gradle. CMake reads them via `$ENV{VAR_NAME}` and compiles them into the native library.

---

## Build Configuration (build.gradle)

Key settings in `app/build.gradle`:

- **compileSdk**: 35
- **minSdk**: 25
- **targetSdk**: 35
- **NDK**: r29
- **8 product flavors** in a single `project` dimension
- **ABI splits**: armeabi-v7a, arm64-v8a, x86, x86_64 + universal APK
- **Data binding and view binding**: Enabled

---

## Firebase App Distribution Config

**Path**: `FirebaseAppDistributionConfig/`

- **`groups.txt`**: Comma-separated list of tester groups (e.g., `trusted-testers`).
- **`release_notes.txt`**: Release notes included with each Firebase distribution.

Update these files before triggering a debug build to customize distribution targets and release notes.
