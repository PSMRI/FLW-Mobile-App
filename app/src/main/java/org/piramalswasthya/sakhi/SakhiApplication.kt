package org.piramalswasthya.sakhi

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.helpers.CrashHandler
import org.piramalswasthya.sakhi.utils.KeyUtils
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class SakhiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: InAppDb

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        KeyUtils.encryptedPassKey()
        KeyUtils.baseAbhaUrl()
        KeyUtils.baseTMCUrl()
        KeyUtils.abhaAuthUrl()
        KeyUtils.abhaClientID()
        KeyUtils.abhaClientSecret()
        KeyUtils.abhaTokenUrl()
        FirebaseApp.initializeApp(this)

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(applicationContext))

        // Recover any records orphaned in SYNCING state from a previous crash/kill
        CoroutineScope(Dispatchers.IO).launch {
            try {
                recoverOrphanedSyncStates()
            } catch (e: Exception) {
                Timber.e(e, "Failed to recover orphaned SYNCING states")
            }
        }
    }

    private suspend fun recoverOrphanedSyncStates() {
        // Reset all records stuck in SYNCING (ordinal 1) back to UNSYNCED (ordinal 0).
        // No sync can be in progress at app startup, so any SYNCING record is orphaned.
        database.benDao.resetSyncingToUnsynced()
        database.cbacDao.resetSyncingToUnsynced()
        database.pmsmaDao.resetSyncingToUnsynced()
        database.deliveryOutcomeDao.resetSyncingToUnsynced()
        database.pncDao.resetSyncingToUnsynced()
        database.infantRegDao.resetSyncingToUnsynced()
        database.vaccineDao.resetSyncingToUnsynced()
        database.aesDao.resetSyncingToUnsynced()
        database.adolescentHealthDao.resetSyncingToUnsynced()
        database.filariaDao.resetSyncingToUnsynced()
        database.kalaAzarDao.resetSyncingToUnsynced()
        database.maaMeetingDao.resetSyncingToUnsynced()
        database.saasBahuSammelanDao.resetSyncingToUnsynced()
        database.uwinDao.resetSyncingToUnsynced()

        // Multi-table DAOs
        database.ecrDao.resetRegSyncingToUnsynced()
        database.ecrDao.resetTrackingSyncingToUnsynced()
        database.maternalHealthDao.resetPwrSyncingToUnsynced()
        database.maternalHealthDao.resetAncSyncingToUnsynced()
        database.leprosyDao.resetScreeningSyncingToUnsynced()
        database.leprosyDao.resetFollowUpSyncingToUnsynced()
        database.malariaDao.resetScreeningSyncingToUnsynced()
        database.malariaDao.resetConfirmedSyncingToUnsynced()
        database.tbDao.resetScreeningSyncingToUnsynced()
        database.tbDao.resetSuspectedSyncingToUnsynced()
        database.tbDao.resetConfirmedSyncingToUnsynced()
        database.hrpDao.resetPregnantAssessSyncingToUnsynced()
        database.hrpDao.resetPregnantTrackSyncingToUnsynced()
        database.hrpDao.resetNonPregnantAssessSyncingToUnsynced()
        database.hrpDao.resetNonPregnantTrackSyncingToUnsynced()
        database.hrpDao.resetMicroBirthPlanSyncingToUnsynced()
        database.vlfDao.resetVhndSyncingToUnsynced()
        database.vlfDao.resetVhncSyncingToUnsynced()
        database.vlfDao.resetPhcSyncingToUnsynced()
        database.vlfDao.resetAhdSyncingToUnsynced()
        database.vlfDao.resetDewormingSyncingToUnsynced()
        database.vlfDao.resetPulsePolioSyncingToUnsynced()
        database.vlfDao.resetOrsSyncingToUnsynced()
        database.vlfDao.resetFilariaMdaSyncingToUnsynced()

        Timber.d("Recovered orphaned SYNCING states at startup")
    }
}