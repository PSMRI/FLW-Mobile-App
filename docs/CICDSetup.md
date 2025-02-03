# CI-CD Setup 🚀

Our Continuous Integration and Continuous Deployment (CI-CD) pipeline is designed to streamline the development process for the FLW Mobile App. Leveraging **GitHub Actions**, the pipeline automates the entire lifecycle of the Android application—from building and testing to signing and distributing the app. This ensures that every change is validated and delivered quickly and reliably, supporting our mission to provide ASHAs with a modern, digital tool to enhance healthcare services for pregnant women, mothers, and newborns in India.

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
  - [Splits Configuration](#splits)
  - [External Native Build Configuration](#external-native-build-configuration)
  - [Version Management](#version-management)
- [Firebase App Distribution Configurations](#firebase-app-distribution-configurations)

---

<a id="overview"></a>
## Overview

The FLW Mobile App is designed for healthcare programs and consultation services rendered by ASHAs’ to serve pregnant women, mothers, and newborns in India. This eliminates pen and paperwork by ASHAs and allows them to enter beneficiaries data in a digital process with increased ease and accuracy of data. We named it as SAKHI for Bihar State and Utprerona for Assam State.

---

<a id="ci-cd-pipeline-for-android-build-and-distribute"></a>
## CI/CD Pipeline for Android Build and Distribute ⚙️

Our CI/CD pipeline uses **GitHub Actions** to automate the build and distribution process of the Android application. The key workflow configuration files are:

- **[android.yml](./.github/workflows/android.yml)**
- **[build-distribute.yml](./.github/workflows/build-distribute.yml)**

### Workflows Overview

The pipeline consists of two primary workflow files:

1. **`android.yml`**: Handles triggering events, setting up a build matrix for various environments, and invoking the distribution workflow.
2. **`build-distribute.yml`**: Contains the detailed steps to build, sign, and distribute the app through Firebase and GitHub Releases.

---

### Workflow: `android.yml`

Triggered by:

- **Manual Runs** via `workflow_dispatch` 🔄
- **Push Events** on the `develop` branch
- **Pull Request Events** targeting `develop`

#### Matrix Configuration

This file uses a matrix strategy to build different configurations. The environments and build types include:

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

Triggered by a **workflow_call**, this file accepts inputs and runs the build process on `ubuntu-latest`.

#### Steps Overview

1. **Set Environment**  
   Sets the job's environment using the provided input.

2. **Checkout Code**  
   Uses `actions/checkout@v4` to retrieve the code.

3. **Set Up JDK**  
   Configures JDK 17 (Zulu distribution) via `actions/setup-java@v4`.

4. **Set Up Android SDK & NDK**
    - **Android SDK**: `android-actions/setup-android@v2`
    - **Android NDK**: `nttld/setup-ndk@v1.5.0` (version `r27c`)

5. **Install CMake**  
   Utilizes `jwlawson/actions-setup-cmake@v1` (version `3.31.1`).

6. **Set Up Ruby Environment**  
   Uses `ruby/setup-ruby@v1` (Ruby version `2.7.2`) with Bundler caching enabled.

7. **Generate AES Key and IV** 🔑  
   Creates a 32-byte AES key and a 16-byte IV, encodes them to Base64, and masks them in logs.

8. **Decode Configuration Files**
    - **google-services.json**: Decodes based on environment (`NIRAMAY_PRODUCTION`, `XUSHRUKHA_PRODUCTION`, or generic).
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
    Checks the output folder and uploads the generated APKs or AABs using `actions/upload-artifact@v4`.

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
    - Add a new entry under `jobs.build_and_distribute.strategy.matrix.config` with the desired parameters.

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

For more details on GitHub Actions, check out the [GitHub Actions Documentation](https://docs.github.com/en/actions).

---

<a id="android-buildgradle-file-documentation"></a>
## Android `build.gradle` File Documentation 📜

### Overview

The `build.gradle` file is used to configure the Android application module. It includes settings for the Android build system, dependencies, and additional build configurations.

### Key Sections

#### `splits`

The `splits` block is used to configure APK generation for different ABIs (Application Binary Interfaces). This allows the creation of separate APKs for different device architectures, which can help reduce the size of the APK.

```gradle
splits {
    abi {
        enable true          // Enables ABI splits, allowing the build system to create separate APKs for each architecture.
        reset()              // Clears any previous ABI configurations, ensuring a clean configuration.
        include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'  // Specifies the ABIs for which APKs should be generated:
        // armeabi-v7a: ARM 32-bit architecture
        // arm64-v8a: ARM 64-bit architecture
        // x86: Intel 32-bit architecture
        // x86_64: Intel 64-bit architecture
        universalApk true    // Generates a universal APK that includes all the specified ABIs.
    }
}
```
- **`enable true`**: Enables ABI splits, allowing the build system to create separate APKs for each architecture.
- **`reset()`**: Resets any previous ABI configurations, ensuring a clean configuration.
- **`include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'`**: Specifies the ABIs for which APKs should be generated. This includes:
  - `armeabi-v7a`: ARM 32-bit architecture
  - `arm64-v8a`: ARM 64-bit architecture
  - `x86`: Intel 32-bit architecture
  - `x86_64`: Intel 64-bit architecture
- **`universalApk true`**: Generates a universal APK that includes all the specified ABIs. This APK can run on any device but will be larger in size compared to the individual ABI-specific APKs.

#### `externalNativeBuild`

The `externalNativeBuild` block is used to configure the build system for native code. It specifies the use of CMake and the path to the CMakeLists.txt file, which contains the build instructions for the native code.

```gradle
externalNativeBuild {
    cmake {
        path file("src/main/cpp/CMakeLists.txt")
    }
}
ndkVersion "27.2.12479018"
```

- **`cmake`**: Specifies that CMake is used for the native build.
  - **`path file("src/main/cpp/CMakeLists.txt")`**: Sets the path to the CMakeLists.txt file, which contains the configuration and build instructions for the native code.
- **`ndkVersion "27.2.12479018"`**: Specifies the version of the Android NDK (Native Development Kit) to be used. This ensures compatibility between the NDK version and the build configurations.

### Additional Information

- **`namespace`**: Defines the package namespace for the application.
- **`compileSdk 34`**: Specifies the SDK version used to compile the application.
- **`defaultConfig`**: Contains default settings for the application, including application ID, minimum and target SDK versions, version code, and version name.
- **`buildTypes`**: Defines different build types, such as `release` with settings for minification and resource shrinking.
- **`flavorDimensions` and `productFlavors`**: Used to define product flavors for different environments like `sakshamStag`, `sakshamUat`, `saksham`, `xushrukha`, and `niramay`.
- **`compileOptions` and `kotlinOptions`**: Configure Java and Kotlin compilation settings.
- **`dataBinding` and `viewBinding`**: Enable data binding and view binding features.

### Guidelines for Changing the Version

The version management is handled by the `versioning.gradle` file, which reads the version from the `version/version.properties` file. To change the version:

versioning.gradle
The versioning.gradle file includes logic to manage and retrieve the version information for the application. Here is the content of the file:

```gradle
ext {
    buildVersionCode = {
        def versionName = buildVersionName()
        def (major, minor, patch) = versionName.toLowerCase().tokenize('.')
        major.toInteger()
    }
    buildVersionName = {
        def props = new Properties()
        rootProject.file("version/version.properties").withInputStream { props.load(it) }
        return props.getProperty("VERSION")
    }
}
```

1. **Open the `version/version.properties` File**:
   - Navigate to `version/version.properties`.

2. **Update the Version**:
   - Modify the `VERSION` property to the new version. For example, to increment from `5.0.0` to `6.0.0`:
     ```ini
     VERSION=6.0.0
     ```

3. **Save the File**:
   - Save the `version.properties` file with the updated version.


## Firebase App Distribution Configurations

groups.txt
-	Purpose: Defines the groups of testers for Firebase App Distribution. Groups should be comma-separated (e.g., `group-1, group-2`).
-	Content: `trusted-testers`: A group named "trusted-testers" that is used to manage testers who are trusted to receive app distributions.

release_notes.txt
-	Purpose: Contains the release notes for the app distribution.
-	Content:  For example, `In this version, we improved the user experience and fixed some bugs.`: A brief note describing improvements and bug fixes in the current version.

