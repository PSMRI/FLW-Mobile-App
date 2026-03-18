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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class androidx.legacy.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }
-keep class androidx.work.** { *; }
-keep class androidx.startup.** { *; }
-keep class org.bouncycastle.** { *; }
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
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Keep all members in kotlinx.coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep all methods in kotlinx.coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Kotlin standard library functions
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

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

