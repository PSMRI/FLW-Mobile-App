package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import org.piramalswasthya.sakhi.utils.Log

class RdServiceHelper(private val context: android.content.Context) {

    companion object {
        const val FACE_RD_PACKAGE = "in.gov.uidai.facerd"
        // Confirm this action string against UIDAI's Face RD integration spec before
        // relying on it — verified fingerprint RD services use
        // "in.gov.uidai.rdservice.fp.CAPTURE"; face RD's action may differ and needs
        // its own confirmation.
        const val ACTION_FACE_CAPTURE = "in.gov.uidai.rdservice.face.CAPTURE"
        const val REQUEST_CODE_FACE_CAPTURE = 9001
    }

    fun isRdAppInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(FACE_RD_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    fun redirectToPlayStore(activity: Activity) {
        val uri = Uri.parse("market://details?id=$FACE_RD_PACKAGE")
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$FACE_RD_PACKAGE")
                )
            )
        }
    }

    fun buildPidOptionsXml(
        wadh: String,
        timeout: Int = 10000
    ): String {
        return """<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="1" fType="2" iCount="0" iType="0" pCount="0" pType="0" format="0" pidVer="2.0" timeout="$timeout" posh="UNKNOWN" env="P" wadh="$wadh"/><CustOpts><Param name="mantra.pid.face.reqScore" value="0"/></CustOpts></PidOptions>"""
    }
    fun launchFaceCapture(activity: Activity) {

        val wadh = WadhGenerator.generateWadh(
            ver = "2.5",
            ra = "P",   // face
            rc = "Y",   // consent
            lr = "N",
            de = "N",
            pfr = "N"
        )
        val pidXml = buildPidOptionsXml(wadh, 10000)
        Log.d("FaceRD", "PID_OPTIONS XML = $pidXml")
        val intent = Intent(ACTION_FACE_CAPTURE).apply {
            putExtra("PID_OPTIONS", buildPidOptionsXml(wadh,10000))
            setPackage(FACE_RD_PACKAGE)
        }
        activity.startActivityForResult(intent, REQUEST_CODE_FACE_CAPTURE)
    }

    fun parseCaptureResult(resultCode: Int, data: Intent?): Result<String> {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return Result.failure(Exception("Capture cancelled or failed"))
        }
        val pidData = data.getStringExtra("PID_DATA")
        return if (pidData != null) Result.success(pidData)
        else Result.failure(Exception("No PID_DATA in response"))
    }
}