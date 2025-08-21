#include <jni.h>
#include <string>
#include <android/log.h>


// JNI functions
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_encryptedPassKey(JNIEnv *env, jobject thiz) {
    std::string encryptedPassKey = ENCRYPTED_PASS_KEY;
    return env->NewStringUTF(encryptedPassKey.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaClientSecret(JNIEnv *env, jobject thiz) {
    std::string abhaClientSecret = ABHA_CLIENT_SECRET;
    return env->NewStringUTF(abhaClientSecret.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaClientID(JNIEnv *env, jobject thiz) {
    std::string abhaClientID = ABHA_CLIENT_ID;
    return env->NewStringUTF(abhaClientID.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseTMCUrl(JNIEnv *env, jobject thiz) {
    std::string baseTMCUrl = BASE_TMC_URL;
    return env->NewStringUTF(baseTMCUrl.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseAbhaUrl(JNIEnv *env, jobject thiz) {
    std::string baseAbhaUrl = BASE_ABHA_URL;
    return env->NewStringUTF(baseAbhaUrl.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaTokenUrl(JNIEnv *env, jobject thiz) {
    std::string abhaTokenUrl = ABHA_TOKEN_URL;
    return env->NewStringUTF(abhaTokenUrl.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaAuthUrl(JNIEnv *env, jobject thiz) {
    std::string abhaAuthUrl = ABHA_AUTH_URL;
    return env->NewStringUTF(abhaAuthUrl.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_chatUrl(JNIEnv *env, jobject thiz) {
    std::string chatUrl = CHAT_URL;
    return env->NewStringUTF(chatUrl.c_str());
}
