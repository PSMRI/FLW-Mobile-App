#include <jni.h>
#include <string>
#include <android/log.h>
#define LOG_TAG "JNI_KEYS"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


// ================== Development Constants ================== //
 #ifdef IS_DEVELOPMENT
 const char* M_ENCRYPTED_PASS_KEY = "Piramal12Piramal";
 const char* M_ABHA_CLIENT_SECRET = "c3631213-60ea-4a10-8e30-43523d3e0253";
 const char* M_ABHA_CLIENT_ID = "PIRSWASTH_953256";
 const char* M_BASE_TMC_URL = "https://amritdemo.piramalswasthya.org/";
         //"https://amritdemo.piramalswasthya.org/";
         //"https://amritflw.piramalswasthya.org/";
         //"http://devbox.bizbrolly.com:4040/";
         //https://uatamrit.piramalswasthya.org/;
 const char* M_BASE_ABHA_URL = "https://abhasbx.abdm.gov.in/abha/api/";
 const char* M_ABHA_TOKEN_URL = "https://dev.abdm.gov.in/api/hiecm/gateway/v3/sessions";
 const char* M_ABHA_AUTH_URL = "https://abhasbx.abdm.gov.in/abha/api/v3/profile/public/certificate";
 const char* M_CHAT_URL = "https://piramalvoicebot.yugasa.org/";
// ================== Production Constants (from Environment) ================== //
 #else
 const char* M_ENCRYPTED_PASS_KEY = ENCRYPTED_PASS_KEY;
 const char* M_ABHA_CLIENT_SECRET = ABHA_CLIENT_SECRET;
 const char* M_ABHA_CLIENT_ID = ABHA_CLIENT_ID;
 const char* M_BASE_TMC_URL = BASE_TMC_URL;
 const char* M_BASE_ABHA_URL = BASE_ABHA_URL;
 const char* M_ABHA_TOKEN_URL = ABHA_TOKEN_URL;
 const char* M_ABHA_AUTH_URL = ABHA_AUTH_URL;
 const char* M_CHAT_URL = CHAT_URL;
 #endif

// =================================================================== //

// JNI functions
extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_encryptedPassKey(JNIEnv* env, jobject thiz) {
    LOGI("Encrypted Pass Key: %s", M_ENCRYPTED_PASS_KEY);

    return env->NewStringUTF(M_ENCRYPTED_PASS_KEY);
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaClientSecret(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_CLIENT_SECRET);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaClientID(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_CLIENT_ID);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseTMCUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_BASE_TMC_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_baseAbhaUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_BASE_ABHA_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaTokenUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_TOKEN_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_abhaAuthUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_ABHA_AUTH_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_piramalswasthya_sakhi_utils_KeyUtils_chatUrl(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF(M_CHAT_URL);
}
