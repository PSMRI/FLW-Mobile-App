package org.piramalswasthya.sakhi.utils


object KeyUtils {

    private const val NATIVE_JNI_LIB_NAME = "sakhi"

    init {
        try {
            System.loadLibrary(NATIVE_JNI_LIB_NAME)
        } catch (e: UnsatisfiedLinkError) {
            throw RuntimeException("Failed to load native library: $NATIVE_JNI_LIB_NAME")
        }

    }


    external fun encryptedPassKey(): String

    external fun abhaClientSecret(): String

    external fun abhaClientID(): String

    external fun baseTMCUrl(): String

    external fun baseAbhaUrl(): String

    external fun abhaTokenUrl(): String

    external fun abhaAuthUrl(): String

    external fun chatUrl(): String


}