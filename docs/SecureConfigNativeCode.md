## Secure Configuration with Native Code 🚀

This documentation provides a comprehensive guide to the `native-lib.cpp`, `CMakeLists.txt`, and `KeyUtils.kt` files in the FLW Mobile App repository. These files work together to securely handle sensitive configuration values by leveraging native code.

### 🛠️ Native Library (`native-lib.cpp`)

The `native-lib.cpp` file contains the native C++ code for interacting with JNI (Java Native Interface) to expose various utility methods to an Android application.

#### Included Headers 📁
```c++
#include <jni.h>
#include <string>
#include <android/log.h>
```

#### Macros 🏷️
```c++
#define LOG_TAG "KeyUtils"
```

#### Namespace 🌐
```c++
using namespace std;
```

### JNI Functions 🔧

#### `encryptedPassKey` 🔑
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_encryptedPassKey(JNIEnv *env, jobject thiz) {
    std::string encryptedPassKey = ENCRYPTED_PASS_KEY;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Encrypted Password Key: %s",
                        encryptedPassKey.c_str());
    return env->NewStringUTF(encryptedPassKey.c_str());
}
```

#### `abhaClientSecret` 🕵️‍♂️
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaClientSecret(JNIEnv *env, jobject thiz) {
    std::string abhaClientSecret = ABHA_CLIENT_SECRET;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "ABHA Client Secret: %s",
                        abhaClientSecret.c_str());
    return env->NewStringUTF(abhaClientSecret.c_str());
}
```

#### `abhaClientID` 🆔
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaClientID(JNIEnv *env, jobject thiz) {
    std::string abhaClientID = ABHA_CLIENT_ID;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "ABHA Client ID: %s", abhaClientID.c_str());
    return env->NewStringUTF(abhaClientID.c_str());
}
```

#### `baseTMCUrl` 🌐
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseTMCUrl(JNIEnv *env, jobject thiz) {
    std::string baseTMCUrl = BASE_TMC_URL;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Base TMC URL: %s", baseTMCUrl.c_str());
    return env->NewStringUTF(baseTMCUrl.c_str());
}
```

#### `baseAbhaUrl` 🌐
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseAbhaUrl(JNIEnv *env, jobject thiz) {
    std::string baseAbhaUrl = BASE_ABHA_URL;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Base ABHA URL: %s", baseAbhaUrl.c_str());
    return env->NewStringUTF(baseAbhaUrl.c_str());
}
```

#### `abhaTokenUrl` 🔒
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaTokenUrl(JNIEnv *env, jobject thiz) {
    std::string abhaTokenUrl = ABHA_TOKEN_URL;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "ABHA Token URL: %s", abhaTokenUrl.c_str());
    return env->NewStringUTF(abhaTokenUrl.c_str());
}
```

#### `abhaAuthUrl` 🔐
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaAuthUrl(JNIEnv *env, jobject thiz) {
    std::string abhaAuthUrl = ABHA_AUTH_URL;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "ABHA Auth URL: %s", abhaAuthUrl.c_str());
    return env->NewStringUTF(abhaAuthUrl.c_str());
}
```

#### `chatUrl` 💬
```c++
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_chatUrl(JNIEnv *env, jobject thiz) {
    std::string chatUrl = CHAT_URL;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "CHAT URL: %s", chatUrl.c_str());
    return env->NewStringUTF(chatUrl.c_str());
}
```

### Logging 📝
Each JNI function logs its respective key or URL using the `__android_log_print` function, which helps in debugging and ensures that the correct values are being accessed.

---

### ⚙️ CMake Configuration (`CMakeLists.txt`)

The `CMakeLists.txt` file configures the CMake build system for the Sakhi project. It sets up environment variables, passes them to the compiler, and defines the build targets and link libraries.

#### Minimum CMake Version 📅
```cmake
cmake_minimum_required(VERSION 3.11)
```

#### Project Definition 📋
```cmake
project(Sakhi LANGUAGES CXX)
```

#### Fetch Environment Variables 🌎
```cmake
set(ENCRYPTED_PASS_KEY "$ENV{ENCRYPTED_PASS_KEY}")
set(ABHA_CLIENT_SECRET "$ENV{ABHA_CLIENT_SECRET}")
set(ABHA_CLIENT_ID "$ENV{ABHA_CLIENT_ID}")
set(BASE_TMC_URL "$ENV{BASE_TMC_URL}")
set(BASE_ABHA_URL "$ENV{BASE_ABHA_URL}")
set(ABHA_TOKEN_URL "$ENV{ABHA_TOKEN_URL}")
set(ABHA_AUTH_URL "$ENV{ABHA_AUTH_URL}")
set(CHAT_URL "$ENV{CHAT_URL}")
```

#### Pass Values to the Compiler 🚀
```cmake
add_definitions(
    -DENCRYPTED_PASS_KEY=\"${ENCRYPTED_PASS_KEY}\"
    -DABHA_CLIENT_SECRET=\"${ABHA_CLIENT_SECRET}\"
    -DABHA_CLIENT_ID=\"${ABHA_CLIENT_ID}\"
    -DBASE_TMC_URL=\"${BASE_TMC_URL}\"
    -DBASE_ABHA_URL=\"${BASE_ABHA_URL}\"
    -DABHA_TOKEN_URL=\"${ABHA_TOKEN_URL}\"
    -DABHA_AUTH_URL=\"${ABHA_AUTH_URL}\"
    -DCHAT_URL=\"${CHAT_URL}\"
)
```

#### Define Library Name 📚
```cmake
set(LIBRARY_NAME "sakhi")
```

#### Add Source File for Shared Library 🛠️
```cmake
add_library(
    ${LIBRARY_NAME}
    SHARED
    native-lib.cpp
)
```

#### Find Log Library 🔍
```cmake
find_library(log-lib log)
```

#### Link Libraries 🔗
```cmake
target_link_libraries(
    ${LIBRARY_NAME}
    ${log-lib}
)
```

### Guidelines for Adding New Environment Variables 📝

To add new environment variables for use in `native-lib.cpp` or other native functions, follow these steps:

1. **Define the Environment Variable:**
   ```cmake
   set(NEW_VARIABLE_NAME "$ENV{NEW_VARIABLE_NAME}")
   ```

2. **Pass the Value to the Compiler:**
   ```cmake
   add_definitions(
       -DNEW_VARIABLE_NAME=\"${NEW_VARIABLE_NAME}\"
   )
   ```

3. **Use the Variable in `native-lib.cpp`:**
   ```c++
   std::string newVariable = NEW_VARIABLE_NAME;
   __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "New Variable: %s", newVariable.c_str());
   return env->NewStringUTF(newVariable.c_str());
   ```

4. **Ensure the Environment Variable is Set:**
   ```bash
   export NEW_VARIABLE_NAME="your_value"
   ```

---

### 🔒 Secure Configuration with `KeyUtils.kt`

The `KeyUtils.kt` file is a Kotlin class that provides a secure interface to access sensitive information such as keys, secrets, and URLs by leveraging native code.

#### Overview 🌐
The `KeyUtils` object securely handles sensitive configuration values. It relies on a native library called `sakhi` to retrieve these values, ensuring they are not exposed in the Kotlin code.

#### Initialization 🚀
```kotlin
private const val NATIVE_JNI_LIB_NAME = "sakhi"

init {
    try {
        System.loadLibrary(NATIVE_JNI_LIB_NAME)
        Timber.tag("KeyUtils").d(encryptedPassKey())
        Timber.tag("KeyUtils").d(abhaClientSecret())
        Timber.tag("KeyUtils").d(abhaClientID())
    } catch (e: UnsatisfiedLinkError) {
        Timber.tag("KeyUtils").e(e, "Failed to load native library")
        throw RuntimeException("Failed to load native library: $NATIVE_JNI_LIB_NAME")
    }
}
```

#### Native Methods 🔧
The `KeyUtils` object declares several external functions that are implemented in native code.

```kotlin
external fun encryptedPassKey(): String
external fun abhaClientSecret(): String
external fun abhaClientID(): String
external fun baseTMCUrl(): String
external fun baseAbhaUrl(): String
external fun abhaTokenUrl(): String
external fun abhaAuthUrl(): String
external fun chatUrl(): String
```

### Guidelines for Adding New Native Methods 📝

1. **Define the Native Method:**
   ```kotlin
   external fun newSensitiveData(): String
   ```

2. **Implement the Native Method:**
   ```c++
   extern "C" JNIEXPORT jstring JNICALL
   Java_org_piramalswasthya_sakhi_utils_KeyUtils_newSensitiveData(JNIEnv *env, jobject thiz) {
       std::string newData = NEW_SENSITIVE_DATA;
       __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "New Sensitive Data: %s", newData.c_str());
       return env->NewStringUTF(newData.c_str());
   }
   ```

3. **Update CMakeLists.txt:**
   ```cmake
   set(NEW_SENSITIVE_DATA "$ENV{NEW_SENSITIVE_DATA}")
   add_definitions(-DNEW_SENSITIVE_DATA=\"${NEW_SENSITIVE_DATA}\")
   ```

4. **Ensure the Environment Variable is Set:**
   ```bash
   export NEW_SENSITIVE_DATA="your_value"
   ```
