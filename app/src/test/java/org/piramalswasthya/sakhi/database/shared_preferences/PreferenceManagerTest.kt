package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.content.res.Resources
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import java.io.File
import java.io.IOException

class PreferenceManagerTest {

    private lateinit var context: Context
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resetInstance()
        context = mockk(relaxed = true)
        resources = mockk(relaxed = true)

        every { context.resources } returns resources
        every { resources.getString(R.string.PREF_NAME) } returns "sakhi_prefs"
        every { context.filesDir } returns File("/data/data/org.piramalswasthya.sakhi/files")

        mockkStatic(MasterKeys::class)
        mockkStatic(EncryptedSharedPreferences::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
        resetInstance()
    }

    private fun resetInstance() {
        val field = PreferenceManager::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, null)
    }

    private fun setCachedInstance(prefs: SharedPreferences) {
        val field = PreferenceManager::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        field.set(null, prefs)
    }

    @Test
    fun `getInstance creates encrypted preferences on the first call`() {
        val encryptedPrefs = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) } returns "alias"
        every {
            EncryptedSharedPreferences.create(
                "sakhi_prefs",
                "alias",
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } returns encryptedPrefs

        val result = PreferenceManager.getInstance(context)

        assertSame(encryptedPrefs, result)
    }

    @Test
    fun `getInstance caches the instance so it is created only once`() {
        val encryptedPrefs = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) } returns "alias"
        every { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) } returns encryptedPrefs

        val first = PreferenceManager.getInstance(context)
        val second = PreferenceManager.getInstance(context)

        assertSame(first, second)
        verify(exactly = 1) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getInstance returns the already cached instance without touching MasterKeys`() {
        val cached = mockk<SharedPreferences>()
        setCachedInstance(cached)

        val result = PreferenceManager.getInstance(context)

        assertSame(cached, result)
        verify(exactly = 0) { MasterKeys.getOrCreate(any()) }
    }

    @Test
    fun `getInstance recovers from a corrupted keystore and retries once`() {
        val recovered = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(any()) } returns "alias"
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keystore") andThen recovered

        val result = PreferenceManager.getInstance(context)

        assertSame(recovered, result)
        verify(exactly = 2) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getInstance keeps the shared master key when the retry after deleting only its own file succeeds`() {
        val recovered = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(any()) } returns "alias"
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keyset") andThen recovered

        val result = PreferenceManager.getInstance(context)

        assertSame(recovered, result)
        verify(exactly = 1) { context.deleteSharedPreferences("sakhi_prefs") }
        verify(exactly = 2) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getInstance resets the master key only when the retry with the existing key also fails`() {
        val recovered = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(any()) } returns "alias"
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keyset") andThenThrows IOException("master key unusable") andThen recovered

        val result = PreferenceManager.getInstance(context)

        assertSame(recovered, result)
        verify(exactly = 2) { context.deleteSharedPreferences("sakhi_prefs") }
        verify(exactly = 3) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getInstance propagates the failure when the retry after recovery also fails`() {
        every { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) } returns "alias"
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("still corrupted")

        try {
            PreferenceManager.getInstance(context)
            fail("expected the final failure to propagate")
        } catch (e: IOException) {
            assertEquals("still corrupted", e.message)
        }
        verify(exactly = 3) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getInstance recovers even when deleting the stale prefs file also fails`() {
        every { context.deleteSharedPreferences(any()) } throws RuntimeException("cannot delete prefs")

        val recovered = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) } returns "alias"
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keystore") andThen recovered

        val result = PreferenceManager.getInstance(context)

        assertSame(recovered, result)
    }

    @Test
    fun `getInstance returns the same instance across many sequential calls`() {
        val encryptedPrefs = mockk<SharedPreferences>()
        every { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) } returns "alias"
        every { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) } returns encryptedPrefs

        val results = (1..5).map { PreferenceManager.getInstance(context) }

        assertTrue(results.all { it === encryptedPrefs })
        verify(exactly = 1) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }
}
