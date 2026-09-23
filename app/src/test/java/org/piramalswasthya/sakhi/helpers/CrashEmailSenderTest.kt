package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import java.io.File
import java.nio.file.Files

class CrashEmailSenderTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `sendCrashReport does nothing when the crash file does not exist`() {
        val context: Context = mockk(relaxed = true)
        CrashEmailSender.pref = mockk(relaxed = true)
        val missingFile = File(Files.createTempDirectory("crash-email-missing").toFile(), "missing.txt")

        CrashEmailSender.sendCrashReport(context, missingFile)

        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun `sendCrashReport builds and launches an email chooser intent when file exists`() {
        val dir = Files.createTempDirectory("crash-email-exists").toFile()
        dir.deleteOnExit()
        val crashFile = File(dir, "crash-123.txt")
        crashFile.writeText("stacktrace")

        val context: Context = mockk(relaxed = true)
        every { context.packageName } returns "org.piramalswasthya.sakhi"

        val pref: PreferenceDao = mockk(relaxed = true)
        val village = LocationEntity(id = 1, name = "Test Village")
        val locationRecord = LocationRecord(
            country = LocationEntity(0, "India"),
            state = LocationEntity(0, "State"),
            district = LocationEntity(0, "District"),
            block = LocationEntity(0, "Block"),
            village = village
        )
        every { pref.getLocationRecord() } returns locationRecord
        every { pref.getRememberedUserName() } returns "asha1"
        CrashEmailSender.pref = pref

        mockkStatic(FileProvider::class)
        val expectedUri = mockk<Uri>()
        every { FileProvider.getUriForFile(context, "org.piramalswasthya.sakhi.provider", crashFile) } returns expectedUri

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setType(any()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Array<String>>()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<String>()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Uri>()) } answers { self as Intent }
        every { anyConstructed<Intent>().addFlags(any()) } answers { self as Intent }

        mockkStatic(Intent::class)
        val chooserIntent: Intent = mockk(relaxed = true)
        val chooserTitleSlot = slot<String>()
        every { Intent.createChooser(any(), capture(chooserTitleSlot)) } returns chooserIntent

        CrashEmailSender.sendCrashReport(context, crashFile)

        verify { anyConstructed<Intent>().setType("message/rfc822") }
        verify { anyConstructed<Intent>().putExtra(Intent.EXTRA_EMAIL, arrayOf("android.developer@piramalswasthya.org")) }
        verify { anyConstructed<Intent>().putExtra(Intent.EXTRA_STREAM, expectedUri) }
        verify { anyConstructed<Intent>().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        verify { context.startActivity(chooserIntent) }
        assertFalse(chooserTitleSlot.captured.isEmpty())
    }
}
