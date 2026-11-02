package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import org.piramalswasthya.sakhi.BuildConfig

class RdServiceHelper(private val context: android.content.Context) {

    companion object {
        private var isDev = true
        private var ABHA_PACKAGE =
            if(BuildConfig.FLAVOR.contains("stag", ignoreCase = true) ||
                BuildConfig.FLAVOR.contains("uat", ignoreCase = true)) {
                isDev = true
                "in.ndhm.phr.debug"
            } else {
                isDev = false
            "in.ndhm.phr"
            }

    }

    fun isAbhaAppInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(ABHA_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    fun redirectToPlayStore(activity: Activity) {
        if (!isDev) {
            val uri = Uri.parse("market://details?id=$ABHA_PACKAGE")
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: ActivityNotFoundException) {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$ABHA_PACKAGE")
                    )
                )
            }
        } else {
            val uri = Uri.parse("https://sandbox.abdm.gov.in/sandbox/v3/new-documentation?doc=hiu")
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}