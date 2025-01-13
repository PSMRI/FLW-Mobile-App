package org.piramalswasthya.sakhi.ui.login_activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.internal.common.CommonUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.MyContextWrapper
import org.piramalswasthya.sakhi.utils.RootedUtil
import java.util.*


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WrapperEntryPoint {
        val preferenceDao: PreferenceDao
    }

    override fun attachBaseContext(newBase: Context) {
        val pref = EntryPointAccessors.fromApplication(
            newBase,
            WrapperEntryPoint::class.java
        ).preferenceDao
        super.attachBaseContext(
            MyContextWrapper.wrap(
                newBase,
                newBase.applicationContext,
                pref.getCurrentLanguage().symbol
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // This will block user to cast app screen
        if (BuildConfig.FLAVOR.equals("sakshamProd", true) ||BuildConfig.FLAVOR.equals("niramayProd", true) || BuildConfig.FLAVOR.equals("xushrukhaProd", true))  {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        createSyncServiceNotificationChannel()
        if (!BuildConfig.DEBUG && isDeviceRootedOrEmulator()) {
            AlertDialog.Builder(this)
                .setTitle("Unsupported Device")
                .setMessage("This app cannot run on rooted devices or emulators.")
                .setCancelable(false)
                .setPositiveButton("Exit") { dialog, id -> finish() }
                .show()
        }
    }

    private fun createSyncServiceNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = resources.getString(R.string.notification_sync_channel_name)
            val descriptionText =
                resources.getString(R.string.notification_sync_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                resources.getString(R.string.notification_sync_channel_id),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun isDeviceRootedOrEmulator(): Boolean {
//      return CommonUtils.isRooted() || CommonUtils.isEmulator() || RootedUtil().isDeviceRooted(applicationContext)
        return CommonUtils.isRooted() || CommonUtils.isEmulator()

    }


}