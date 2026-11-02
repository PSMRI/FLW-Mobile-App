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

-keep class java.util.Locale { *; }
# Prevent removal of resources used via reflection or dynamic access
-keepclassmembers class ** {
    public static <fields>;
}
# Keep all string resource IDs and names
-keepclassmembers class **.R$string {
    *;
}

# --- ContextWrapper ---
-keep class org.piramalswasthya.sakhi.** extends android.content.ContextWrapper { *; }

# --- Assemblers ---
-keep class org.piramalswasthya.sakhi.**Assembler { *; }
-keepnames class org.piramalswasthya.sakhi.**Assembler

# --- Hilt / DI Support ---
-keep @dagger.hilt.EntryPoint interface * { *; }
-keep class dagger.hilt.EntryPoints { *; }
-keep class dagger.hilt.EntryPoint { *; }
-keep class dagger.hilt.android.EntryPointAccessors { *; }
-keep class **_HiltModules* { *; }
-keep class *Module* { *; }

# --- Annotations ---
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# --- Reflection Safety ---
-keep class org.piramalswasthya.sakhi.** { *; }
-keepnames class org.piramalswasthya.sakhi.**

-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontnote com.google.gson.**
-keepattributes Signature
-keepattributes *Annotation*

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keepclassmembers class org.piramalswasthya.**.databinding.** {
    public <methods>;
}
-keep class org.piramalswasthya.sakhi.model.** {*;}
-keep class org.piramalswasthya.sakhi.network.** {*;}
-keep class org.piramalswasthya.sakhi.repositories.** {*;}
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

