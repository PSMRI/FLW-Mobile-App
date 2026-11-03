# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep line numbers so Crashlytics stack traces stay readable once R8 obfuscates
# (FLW-1211); the source file name is replaced to avoid shipping it.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Libraries (Moshi, OkHttp, Firebase, Play services, AndroidX, Kotlin, coroutines) ship
# their own R8 rules, so they are no longer kept wholesale (FLW-1211).

# Retrofit 2.9.0 ships no R8 full-mode rules, and AGP 8 runs R8 in full mode, which would
# strip the generic signatures Retrofit reads from suspend API methods. These are the rules
# newer Retrofit versions ship.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Activities, services and the Application class are kept by the rules AAPT generates from the
# manifest, and data binding classes are called directly, so neither needs its own keep rule.

# --- Hilt / DI Support ---
# hilt-android and hilt-work ship their own R8 rules for modules and generated components.
-keep @dagger.hilt.EntryPoint interface * { *; }

# --- Annotations ---
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# --- Reflection Safety ---
# App code is shrunk and obfuscated (FLW-1211). Only classes read by reflection are kept:
# Gson/Moshi classes (model/, network/ and the ones below) and JNI.
-keep class org.piramalswasthya.sakhi.repositories.UwinRepo$* { *; }
-keep class org.piramalswasthya.sakhi.ui.asha_supervisor.**.model.** { *; }
-keep class org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryRecordUI { *; }
-keep class org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI { *; }
-keep class org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.model.** { *; }

# SyncLogTree picks sync logs for the Sync Dashboard by the logging class's name.
-keepnames class org.piramalswasthya.sakhi.work.**
-keepnames class org.piramalswasthya.sakhi.repositories.**

-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontnote com.google.gson.**
-keepattributes Signature
-keepattributes *Annotation*

# Application classes serialized/deserialized by Gson and Moshi. Gson reads and writes fields by
# name and needs the constructors; Moshi uses generated adapters. Getters, copy(), equals() etc.
# are left to R8 (FLW-1211).
-keep class org.piramalswasthya.sakhi.model.** { <fields>; <init>(...); }
-keep class org.piramalswasthya.sakhi.network.** { <fields>; <init>(...); }
-dontusemixedcaseclassnames

# Keep coroutines continuation classes and methods
-keepclassmembers class kotlin.coroutines.Continuation {
    *;
}

# Prevent warning for kotlinx.coroutines internal classes
-dontwarn kotlinx.coroutines.internal.**

# Prevent warning for kotlinx.coroutines implementation details
-dontwarn kotlinx.coroutines.**

# Keep all classes in the JNI package
-keep class org.piramalswasthya.sakhi.jni.** { *; }

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Timber calls are NOT stripped — SyncLogTree (planted in all builds)
# intercepts sync-related logs for the Sync Dashboard logs tab.
# DebugTree is only planted in debug builds, so release logs only
# go to SyncLogManager (in-memory), never to logcat.
#
# -assumenosideeffects class timber.log.Timber {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
#     public static *** w(...);
# }

