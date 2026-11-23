package org.piramalswasthya.sakhi.helpers

import android.content.Context
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
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class DatabaseKeyManagerTest {

    private fun mockEncryptedPrefsWithPassphrase(passphrase: String): SharedPreferences {
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString("room_db_password", null) } returns passphrase
        mockkStatic(MasterKeys::class)
        mockkStatic(EncryptedSharedPreferences::class)
        every { MasterKeys.getOrCreate(any()) } returns "alias"
        return prefs
    }

    @Test
    fun getDatabasePassphrase_keepsSharedMasterKeyWhenRetryAfterDeletingOwnFileSucceeds() {
        val context: Context = mockk(relaxed = true)
        val prefs = mockEncryptedPrefsWithPassphrase("secret")
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keyset") andThen prefs

        val result = DatabaseKeyManager.getDatabasePassphrase(context)

        assertEquals("secret", String(result))
        verify(exactly = 1) { context.deleteSharedPreferences("room_db_encryption_pref") }
        verify(exactly = 2) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getDatabasePassphrase_resetsMasterKeyOnlyWhenRetryWithExistingKeyAlsoFails() {
        val context: Context = mockk(relaxed = true)
        val prefs = mockEncryptedPrefsWithPassphrase("secret")
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keyset") andThenThrows IOException("master key unusable") andThen prefs

        val result = DatabaseKeyManager.getDatabasePassphrase(context)

        assertEquals("secret", String(result))
        verify(exactly = 2) { context.deleteSharedPreferences("room_db_encryption_pref") }
        verify(exactly = 3) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getDatabasePassphrase_throwsWhenAllRecoveryAttemptsFail() {
        val context: Context = mockk(relaxed = true)
        mockEncryptedPrefsWithPassphrase("secret")
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("still corrupted")

        val error = assertThrows(IOException::class.java) {
            DatabaseKeyManager.getDatabasePassphrase(context)
        }

        assertEquals("still corrupted", error.message)
        verify(exactly = 3) { EncryptedSharedPreferences.create(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getDatabasePassphrase_recoversEvenWhenDeletingPrefsFileFails() {
        val context: Context = mockk(relaxed = true)
        every { context.deleteSharedPreferences(any()) } throws RuntimeException("cannot delete prefs")
        val prefs = mockEncryptedPrefsWithPassphrase("secret")
        every {
            EncryptedSharedPreferences.create(any(), any(), any(), any(), any())
        } throws IOException("corrupted keyset") andThen prefs

        val result = DatabaseKeyManager.getDatabasePassphrase(context)

        assertEquals("secret", String(result))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getDatabasePassphrase_throwsWhenEncryptedPreferencesCannotBeCreated() {
        val context: Context = mockk(relaxed = true)

        assertThrows(Throwable::class.java) {
            DatabaseKeyManager.getDatabasePassphrase(context)
        }
    }

    @Test
    fun getDatabasePassphrase_throwsEvenWhenFilesDirIsUnavailable() {
        val context: Context = mockk(relaxed = true)
        every { context.filesDir } returns null

        assertThrows(Throwable::class.java) {
            DatabaseKeyManager.getDatabasePassphrase(context)
        }
    }
}
