package org.piramalswasthya.sakhi

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import org.piramalswasthya.sakhi.helpers.CrashHandler
import org.piramalswasthya.sakhi.utils.KeyUtils
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class SakhiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        Timber.plant(Timber.DebugTree())
        KeyUtils.encryptedPassKey()
        KeyUtils.baseAbhaUrl()
        KeyUtils.baseTMCUrl()
        KeyUtils.abhaAuthUrl()
        KeyUtils.abhaClientID()
        KeyUtils.abhaClientSecret()
        KeyUtils.abhaTokenUrl()
        FirebaseApp.initializeApp(this)

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(applicationContext))
    }

}