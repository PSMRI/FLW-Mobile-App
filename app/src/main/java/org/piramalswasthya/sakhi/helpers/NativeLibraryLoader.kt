package org.piramalswasthya.sakhi.helpers

import android.content.Context
import com.getkeepsafe.relinker.ReLinker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Single entry point for loading the app's native libraries (`libsqlcipher`, `libsakhi`).
 *
 * `System.loadLibrary` throws `UnsatisfiedLinkError: library "x.so" not found` when the
 * PackageManager failed to extract the library at install/update time (common on low-storage
 * devices), or when the app was side-loaded as a bare `base.apk` without its ABI split APK.
 * ReLinker recovers the first case by extracting the library from the installed APK(s) itself.
 * The second case is unrecoverable; [areRequiredLibrariesAvailable] lets callers detect it and
 * ask the user to reinstall instead of crashing.
 */
object NativeLibraryLoader {

    const val SQLCIPHER = "sqlcipher"
    const val SAKHI = "sakhi"

    private val loaded = ConcurrentHashMap.newKeySet<String>()

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var requiredLibsAvailable: Boolean? = null

    fun init(context: Context) {
        appContext = context.applicationContext ?: context
    }

    /** Loads [name] once. Throws [UnsatisfiedLinkError] if it cannot be loaded. */
    fun load(name: String) {
        if (name in loaded) return
        synchronized(this) {
            if (name in loaded) return
            val context = appContext
            if (context == null) {
                System.loadLibrary(name)
            } else {
                try {
                    ReLinker.log { Timber.tag("ReLinker").d(it) }.loadLibrary(context, name)
                } catch (e: UnsatisfiedLinkError) {
                    throw e
                } catch (e: RuntimeException) {
                    // ReLinker's MissingLibraryException: the .so is in none of the installed APKs
                    throw UnsatisfiedLinkError("Native library $name missing from APK: ${e.message}")
                        .apply { initCause(e) }
                }
            }
            loaded += name
        }
    }

    /**
     * Returns false (and reports a non-fatal) when a required native library is missing, which
     * means the install is incomplete and only a reinstall from Play Store can fix it.
     * The result is cached for the lifetime of the process.
     */
    fun areRequiredLibrariesAvailable(context: Context): Boolean {
        requiredLibsAvailable?.let { return it }
        synchronized(this) {
            requiredLibsAvailable?.let { return it }
            if (appContext == null) init(context)
            val available = try {
                load(SQLCIPHER)
                load(SAKHI)
                true
            } catch (e: UnsatisfiedLinkError) {
                Timber.e(e, "Required native library missing, install is incomplete")
                runCatching { FirebaseCrashlytics.getInstance().recordException(e) }
                false
            }
            requiredLibsAvailable = available
            return available
        }
    }
}
