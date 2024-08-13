package org.piramalswasthya.sakhi.utils

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class RootedUtil {
    fun isDeviceRooted(applicationContext: Context): Boolean {
        return checkSUBinary() || checkRootManagement(applicationContext) || checkProps() || checkBusyBox()
    }

    // Method 1: Check for the presence of su binary
    private fun checkSUBinary(): Boolean {
        val paths = arrayOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkRootManagement(applicationContext: Context): Boolean {
        val packages = arrayOf(
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "com.yellowes.su"
        )
        for (packageName in packages) {
            try {
                val pm = applicationContext.packageManager
                pm.getPackageInfo(packageName, 0)
                return true
            } catch (e: Exception) {
                // Package not found
            }
        }
        return false
    }

    private fun checkProps(): Boolean {
        var reader: BufferedReader? = null
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            reader = BufferedReader(InputStreamReader(process.inputStream))
            val prop: String = reader.readLine()
            while (prop.isNotEmpty()) {
                if (prop.contains("[ro.debuggable]=[1]") || prop.contains("[ro.secure]=[0]")) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        } finally {
            reader?.close()
        }
    }

    private fun checkBusyBox(): Boolean {
        return try {
            Runtime.getRuntime().exec("busybox").destroy()
            true
        } catch (e: Exception) {
            false
        }
    }
}
