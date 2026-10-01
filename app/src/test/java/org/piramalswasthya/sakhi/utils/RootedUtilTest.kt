package org.piramalswasthya.sakhi.utils

import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class RootedUtilTest {

    private val rootedUtil = RootedUtil()

    private val rootManagementPackages = listOf(
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "com.yellowes.su"
    )

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun contextWithNoRootManagerAndNoSuFile(): Context {
        val packageManager = mockk<PackageManager>()
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns packageManager
        rootManagementPackages.forEach {
            every { packageManager.getPackageInfo(it, 0) } throws RuntimeException("not installed")
        }
        return context
    }

    @Test
    fun isDeviceRooted_reportsRooted_whenARootManagerPackageIsInstalled() {
        val packageManager = mockk<PackageManager>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns packageManager

        assertTrue(rootedUtil.isDeviceRooted(context))
    }

    @Test
    fun isDeviceRooted_stopsAtTheFirstRootManagerFound() {
        val packageManager = mockk<PackageManager>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns packageManager

        rootedUtil.isDeviceRooted(context)

        verify(exactly = 1) { packageManager.getPackageInfo(rootManagementPackages[0], 0) }
        verify(exactly = 0) { packageManager.getPackageInfo(rootManagementPackages[1], 0) }
    }

    @Test
    fun isDeviceRooted_probesEveryRootManagerPackage_whenNoneAreInstalled() {
        val packageManager = mockk<PackageManager>()
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns packageManager
        rootManagementPackages.forEach {
            every { packageManager.getPackageInfo(it, 0) } throws RuntimeException("not installed")
        }

        rootedUtil.isDeviceRooted(context)

        rootManagementPackages.forEach {
            verify(exactly = 1) { packageManager.getPackageInfo(it, 0) }
        }
    }

    @Test
    fun isDeviceRooted_swallowsAFailureToReachThePackageManager() {
        val context = mockk<Context>()
        every { context.packageManager } throws IllegalStateException("detached")

        rootedUtil.isDeviceRooted(context)

        verify(exactly = rootManagementPackages.size) { context.packageManager }
    }

    private val suBinaryPaths = listOf(
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    private fun deleteUpwardWhileEmpty(directory: File?) {
        var dir = directory
        while (dir != null && dir.isDirectory && dir.list()?.isEmpty() == true) {
            val parent = dir.parentFile
            if (!dir.delete()) break
            dir = parent
        }
    }

    private fun createRealSuBinaryFile(): File? {
        for (path in suBinaryPaths) {
            val candidate = File(path)
            try {
                candidate.parentFile?.mkdirs()
                if (candidate.createNewFile() || candidate.exists()) {
                    return candidate
                }
            } catch (e: Exception) {
            }
        }
        return null
    }

    @Test
    fun isDeviceRooted_returnsTrue_whenSuBinaryFileExists() {
        val suBinaryFile = createRealSuBinaryFile()
        assumeTrue(
            "No su binary path is writable on this machine; skipping real-file check",
            suBinaryFile != null
        )
        val context = mockk<Context>(relaxed = true)

        try {
            assertTrue(rootedUtil.isDeviceRooted(context))
            verify(exactly = 0) { context.packageManager }
        } finally {
            suBinaryFile?.delete()
            deleteUpwardWhileEmpty(suBinaryFile?.parentFile)
        }
    }

    private fun isRealBusyBoxOnPath(): Boolean {
        return try {
            Runtime.getRuntime().exec("busybox").destroy()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isPosix() = File.separatorChar == '/'

    private fun findWritablePathDir(): File? {
        val pathDirs = System.getenv("PATH")?.split(File.pathSeparator).orEmpty()
        for (dir in pathDirs) {
            val d = File(dir)
            if (d.isDirectory && d.canWrite()) return d
        }
        return null
    }

    private fun installFakeExecutable(dir: File, name: String, scriptBody: String): File {
        val script = File(dir, name)
        script.writeText("#!/bin/sh\n$scriptBody")
        script.setExecutable(true)
        return script
    }

    private fun withFakeGetpropAndBusybox(
        getpropOutput: String,
        installBusybox: Boolean,
        block: () -> Unit
    ) {
        assumeTrue(
            "Faking the getprop/busybox executables only works on POSIX filesystems",
            isPosix()
        )
        val dir = findWritablePathDir()
        assumeTrue("No writable PATH directory found to install a fake executable", dir != null)
        val installed = mutableListOf<File>()
        try {
            installed += installFakeExecutable(
                dir!!,
                "getprop",
                if (getpropOutput.isEmpty()) "" else "printf '%s\\n' \"$getpropOutput\"\n"
            )
            if (installBusybox) {
                installed += installFakeExecutable(dir!!, "busybox", "exit 0\n")
            }
            block()
        } finally {
            installed.forEach { it.delete() }
        }
    }

    @Test
    fun isDeviceRooted_returnsTrue_whenGetpropReportsDebuggableBuild() {
        withFakeGetpropAndBusybox(getpropOutput = "[ro.debuggable]=[1]", installBusybox = false) {
            val context = contextWithNoRootManagerAndNoSuFile()

            assertTrue(rootedUtil.isDeviceRooted(context))
        }
    }

    @Test
    fun isDeviceRooted_returnsTrue_whenGetpropReportsInsecureBuild() {
        withFakeGetpropAndBusybox(getpropOutput = "[ro.secure]=[0]", installBusybox = false) {
            val context = contextWithNoRootManagerAndNoSuFile()

            assertTrue(rootedUtil.isDeviceRooted(context))
        }
    }

    @Test
    fun isDeviceRooted_returnsFalse_whenGetpropStreamIsEmptyAndBusyBoxIsMissing() {
        assumeTrue(
            "A real busybox binary is present on PATH on this machine; skipping",
            !isRealBusyBoxOnPath()
        )
        val context = contextWithNoRootManagerAndNoSuFile()

        assertFalse(rootedUtil.isDeviceRooted(context))
    }

    @Test
    fun isDeviceRooted_returnsFalse_whenGetpropExecThrowsAndBusyBoxIsMissing() {
        assumeTrue(
            "A real busybox binary is present on PATH on this machine; skipping",
            !isRealBusyBoxOnPath()
        )
        val context = contextWithNoRootManagerAndNoSuFile()

        assertFalse(rootedUtil.isDeviceRooted(context))
    }

    @Test
    fun isDeviceRooted_returnsTrue_whenBusyBoxIsPresent() {
        withFakeGetpropAndBusybox(getpropOutput = "", installBusybox = true) {
            val context = contextWithNoRootManagerAndNoSuFile()

            assertTrue(rootedUtil.isDeviceRooted(context))
        }
    }
}
