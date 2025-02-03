# FLW Mobile App 🚀

Welcome to the **FLW Mobile App** repository! This project is designed for healthcare programs and
consultation services provided by ASHAs for pregnant women, mothers, and newborns in India. Our goal
is to replace pen-and-paper methods with a digital, accurate, and user-friendly system.

---

## Table of Contents

- [Overview](#overview)
- [CI/CD Pipeline for Android Build and Distribute](#cicd-pipeline-for-android-build-and-distribute)
    - [Workflows Overview](#workflows-overview)
    - [Workflow: `android.yml`](#workflow-androidyml)
    - [Workflow: `build-distribute.yml`](#workflow-build-distributeyml)
    - [Guidelines for Environments & GitHub Secrets](#guidelines-for-environments--github-secrets)
    - [Example Usage](#example-usage)
- [Android `build.gradle` File Documentation](#android-buildgradle-file-documentation)
    - [Splits Configuration](#splits-configuration)
    - [External Native Build Configuration](#external-native-build-configuration)
    - [Version Management](#version-management)
- [Firebase App Distribution Configurations](#firebase-app-distribution-configurations)

---

## Overview

The FLW Mobile App is built for modernizing healthcare processes by enabling ASHAs to provide
efficient services to their communities. The app simplifies data collection and management, ensuring
higher accuracy and ease of use.

---

## CI/CD Pipeline for Android Build and Distribute ⚙️

Our CI/CD pipeline uses **GitHub Actions** to automate the build and distribution process of the
Android application. The key workflow configuration files are:

- **[android.yml](./.github/workflows/android.yml)**
- **[build-distribute.yml](./.github/workflows/build-distribute.yml)**

### Workflows Overview

The pipeline consists of two primary workflow files:

1. **`android.yml`**: Handles triggering events, setting up a build matrix for various environments,
   and invoking the distribution workflow.
2. **`build-distribute.yml`**: Contains the detailed steps to build, sign, and distribute the app
   through Firebase and GitHub Releases.

---

### Workflow: `android.yml`

Triggered by:

- **Manual Runs** via `workflow_dispatch` 🔄
- **Push Events** on the `develop` branch
- **Pull Request Events** targeting `develop`

#### Matrix Configuration

This file uses a matrix strategy to build different configurations. The environments and build types
include:

- **SAKSHAM_STAG**
    - *Variant*: `sakshamStag`
    - *Build Type*: `debug`
- **SAKSHAM_UAT**
    - *Variant*: `sakshamUat`
    - *Build Type*: `debug`
- **NIRAMAY_PRODUCTION**
    - *Variant*: `niramay`
    - *Build Type*: `release`
- **XUSHRUKHA_PRODUCTION**
    - *Variant*: `xushrukha`
    - *Build Type*: `release`

#### Job Details

- **Job Name**: `build_and_distribute`
- **Strategy Matrix**: Provides environment-specific configurations.
- **Uses**: Invokes the workflow defined in `./.github/workflows/build-distribute.yml`
- **Inputs Passed**:
    - `environment`
    - `variant`
    - `build_type`
- **Secrets**: Inherits all repository secrets.

---

### Workflow: `build-distribute.yml`

Triggered by a **workflow_call**, this file accepts inputs and runs the build process on
`ubuntu-latest`.

#### Steps Overview

1. **Set Environment**  
   Sets the job's environment using the provided input.

2. **Checkout Code**  
   Uses `actions/checkout@v4` to retrieve the code.

3. **Set Up JDK**  
   Configures JDK 17 (Zulu distribution) via `actions/setup-java@v4`.

4. **Set Up Android SDK & NDK**
    - Android SDK: `android-actions/setup-android@v2`
    - Android NDK: `nttld/setup-ndk@v1.5.0` (version `r27c`)

5. **Install CMake**  
   Utilizes `jwlawson/actions-setup-cmake@v1` (version `3.31.1`).

6. **Set Up Ruby Environment**  
   Uses `ruby/setup-ruby@v1` (Ruby version `2.7.2`) with Bundler caching enabled.

7. **Generate AES Key and IV** 🔑  
   Creates a 32-byte AES key and a 16-byte IV, encodes them to Base64, and masks them in logs.

8. **Decode Configuration Files**
    - **google-services.json**: Decodes based on environment (`NIRAMAY_PRODUCTION`,
      `XUSHRUKHA_PRODUCTION`, or generic).
    - **Firebase Credentials**: Decodes based on the capitalized variant.
    - **Google Play JSON Key**: Decodes for release builds.
    - **Keystore**: Decodes and sets the file path.

9. **Configure Local Properties**  
   Generates a `local.properties` file with the Android SDK directory.

10. **Retrieve & Verify Version**  
    Extracts the version from `version/version.properties` and verifies it.

11. **Build and Distribute**  
    Sets environment variables for app URLs, signing credentials, and Firebase tokens.  
    Runs `fastlane` for:
    - `build_and_distribute_debug` (debug builds)
    - `build_and_distribute_release` (release builds)

12. **Verify & Upload Artifacts**  
    Checks the output folder and uploads the generated APKs or AABs using
    `actions/upload-artifact@v4`.

13. **Push Release Artifacts**  
    Uses `ncipollo/release-action@v1` to push artifacts to GitHub Releases.

---

### Guidelines for Environments & GitHub Secrets 📝

#### **Updating/Adding/Deleting Environments**

1. **Update an Environment**:
    - Open `.github/workflows/android.yml`
    - Locate `jobs.build_and_distribute.strategy.matrix.config`
    - Update the necessary details (e.g., `environment`, `variant`, `build_type`).

2. **Add a New Environment**:
    - Open `.github/workflows/android.yml`
    - Add a new entry under `jobs.build_and_distribute.strategy.matrix.config` with the desired
      parameters.

3. **Delete an Environment**:
    - Open `.github/workflows/android.yml`
    - Remove the entry for the environment you want to delete.

#### **Updating/Adding/Deleting GitHub Secrets**

1. **Update a Secret**:
    - Navigate to **Settings > Secrets and variables > Actions** in your repository.
    - Click the secret you want to update and modify its value.

2. **Add a New Secret**:
    - Go to **Settings > Secrets and variables > Actions**.
    - Click on **New repository secret**.
    - Enter the secret name and value, then click **Add secret**.

3. **Delete a Secret**:
    - Navigate to **Settings > Secrets and variables > Actions**.
    - Locate the secret and click **Delete**.

---

### Example Usage

To **manually trigger** the workflow:

1. Open the **Actions** tab in the repository.
2. Select the **Android Build and Distribute** workflow.
3. Click the **Run workflow** button.

For more details on GitHub Actions, check out
the [GitHub Actions Documentation](https://docs.github.com/en/actions).

---

## Android `build.gradle` File Documentation 📜

This section explains key configurations in your `build.gradle` file, focusing on **splits** and *
*externalNativeBuild**.

### Splits Configuration

The **splits** block configures APK generation for different ABIs (Application Binary Interfaces).
This helps create smaller, architecture-specific APKs.

```gradle
splits {
    abi {
        enable true
        reset()
        include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
        universalApk true
    }
}
