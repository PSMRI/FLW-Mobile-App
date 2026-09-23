package org.piramalswasthya.sakhi.utils

import timber.log.Timber


/**
 * Security utility for retrieving sensitive configuration values through native code.
 * This class provides a secure interface to access sensitive information such as keys,
 * secrets, and URLs by leveraging native code to prevent reverse engineering.
 *
 * Note: This class requires the native library 'sakhi' to be properly configured
 * and built using CMake. Ensure the native implementation follows security best
 * practices for handling sensitive data.
 */
object KeyUtils {

    private const val NATIVE_JNI_LIB_NAME = "sakhi"

    init {
        try {
            System.loadLibrary(NATIVE_JNI_LIB_NAME)
        } catch (e: UnsatisfiedLinkError) {
            Timber.tag("KeyUtils").e(e, "Failed to load native library")
        }
    }

    fun encryptedPassKey(): String = "dummy_key"

    fun abhaClientSecret(): String = "dummy_secret"

    fun abhaClientID(): String = "dummy_client"

    fun baseTMCUrl(): String = "https://example.com/"

    fun baseAbhaUrl(): String = "https://example.com/"

    fun abhaTokenUrl(): String = "https://example.com/"

    fun abhaAuthUrl(): String = "https://example.com/"

    fun chatUrl(): String = "https://example.com/"
}