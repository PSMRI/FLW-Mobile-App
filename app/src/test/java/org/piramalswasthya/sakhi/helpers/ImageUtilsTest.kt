package org.piramalswasthya.sakhi.helpers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class ImageUtilsTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun contextWithDirs(filesDir: File? = null, cacheDir: File? = null): Context {
        val ctx = mockk<Context>(relaxed = true)
        if (filesDir != null) every { ctx.filesDir } returns filesDir
        if (cacheDir != null) every { ctx.cacheDir } returns cacheDir
        return ctx
    }

    // ===================================================
    // removeAllBenImages() / removeAllStoredBenImages() / removeAllTemporaryBenImages()
    // ===================================================

    @Test
    fun `removeAllBenImages does not delete stored images because the digits-only filter never matches the jpeg extension`() {
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers { firstArg<String>().all { it.isDigit() } }
        val filesDir = Files.createTempDirectory("imageutils-files").toFile().apply { deleteOnExit() }
        val cacheDir = Files.createTempDirectory("imageutils-cache").toFile().apply { deleteOnExit() }
        val storedImage = File(filesDir, "12345.jpeg").apply { writeText("data") }
        val context = contextWithDirs(filesDir, cacheDir)

        ImageUtils.removeAllBenImages(context)

        assertTrue(
            "Stored ben image should remain since isDigitsOnly() rejects the .jpeg extension",
            storedImage.exists()
        )
    }

    @Test
    fun `removeAllBenImages deletes temporary ben images matching the prefix and keeps unrelated files`() {
        val filesDir = Files.createTempDirectory("imageutils-files").toFile().apply { deleteOnExit() }
        val cacheDir = Files.createTempDirectory("imageutils-cache").toFile().apply { deleteOnExit() }
        val tempImage = File(cacheDir, "${Konstants.tempBenImagePrefix}_1.jpeg").apply { writeText("data") }
        val unrelated = File(cacheDir, "other_file.jpeg").apply { writeText("data") }
        val context = contextWithDirs(filesDir, cacheDir)

        ImageUtils.removeAllBenImages(context)

        assertFalse("Temp ben image matching the prefix should be deleted", tempImage.exists())
        assertTrue("Unrelated file should remain untouched", unrelated.exists())
    }

    @Test
    fun `removeAllBenImages does not throw when the target directories do not exist`() {
        val missingFilesDir = File(
            Files.createTempDirectory("imageutils-missing-files").toFile(),
            "does-not-exist"
        )
        val missingCacheDir = File(
            Files.createTempDirectory("imageutils-missing-cache").toFile(),
            "does-not-exist"
        )
        val context = contextWithDirs(missingFilesDir, missingCacheDir)

        ImageUtils.removeAllBenImages(context)
    }

    // ===================================================
    // getEncodedStringForBenImage()
    // ===================================================

    @Test
    fun `getEncodedStringForBenImage returns null when the file does not exist`() {
        val filesDir = Files.createTempDirectory("imageutils-enc-missing").toFile().apply { deleteOnExit() }
        val context = contextWithDirs(filesDir = filesDir)

        val result = ImageUtils.getEncodedStringForBenImage(context, 999L)

        assertNull(result)
    }

    @Test
    fun `getEncodedStringForBenImage returns base64 of the file contents when the file exists`() {
        val filesDir = Files.createTempDirectory("imageutils-enc-present").toFile().apply { deleteOnExit() }
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        File(filesDir, "42.jpeg").apply { writeBytes(bytes) }
        val context = contextWithDirs(filesDir = filesDir)

        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }

        val result = ImageUtils.getEncodedStringForBenImage(context, 42L)

        assertEquals(java.util.Base64.getEncoder().encodeToString(bytes), result)
    }

    // ===================================================
    // renameImage()
    // ===================================================

    @Test
    fun `renameImage returns null when the original file does not exist`() {
        val filesDir = Files.createTempDirectory("imageutils-rename-missing").toFile().apply { deleteOnExit() }
        val context = contextWithDirs(filesDir = filesDir)

        val result = ImageUtils.renameImage(context, 1L, 2L)

        assertNull(result)
    }

    @Test
    fun `renameImage renames the existing file and returns its uri`() {
        val filesDir = Files.createTempDirectory("imageutils-rename-present").toFile().apply { deleteOnExit() }
        val originalFile = File(filesDir, "10.jpeg").apply { writeText("data") }
        val context = contextWithDirs(filesDir = filesDir)

        mockkStatic(Uri::class)
        val fakeUri = mockk<Uri>()
        every { fakeUri.toString() } returns "file://renamed"
        every { Uri.fromFile(any()) } returns fakeUri

        val result = ImageUtils.renameImage(context, 10L, 20L)

        assertEquals("file://renamed", result)
        assertFalse("Original file should have been renamed away", originalFile.exists())
        assertTrue("Renamed file should exist under the new ben id", File(filesDir, "20.jpeg").exists())
    }

    // ===================================================
    // saveBenImageFromCameraToStorage() - guard/exception branches only
    // (real Bitmap decoding and Compressor.compress are out of scope for JVM tests)
    // ===================================================

    @Test
    fun `saveBenImageFromCameraToStorage returns null when the input stream is null`() = runTest {
        mockkStatic(Uri::class)
        val uri = mockk<Uri>()
        every { Uri.parse(any()) } returns uri
        val resolver = mockk<ContentResolver>(relaxed = true)
        every { resolver.openInputStream(uri) } returns null
        val context = mockk<Context>(relaxed = true)
        every { context.contentResolver } returns resolver

        val result = ImageUtils.saveBenImageFromCameraToStorage(context, "content://fake", 1L)

        assertNull(result)
    }

    @Test
    fun `saveBenImageFromCameraToStorage returns null when an exception is thrown while resolving the uri`() = runTest {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } throws IllegalArgumentException("bad uri")
        val context = mockk<Context>(relaxed = true)

        val result = ImageUtils.saveBenImageFromCameraToStorage(context, "bad-uri", 1L)

        assertNull(result)
    }

    // ===================================================
    // saveBenImageFromServerToStorage() - guard/exception branch only
    // (Compressor.compress requires real Bitmap decoding, out of scope for JVM tests)
    // ===================================================

    @Test
    fun `saveBenImageFromServerToStorage returns null when the base64 payload is invalid`() = runTest {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } throws IllegalArgumentException("bad base64")
        val context = mockk<Context>(relaxed = true)

        val result = ImageUtils.saveBenImageFromServerToStorage(context, "###", 1L)

        assertNull(result)
    }
}
