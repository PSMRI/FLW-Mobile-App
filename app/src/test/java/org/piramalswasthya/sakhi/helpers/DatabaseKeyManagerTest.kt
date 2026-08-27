package org.piramalswasthya.sakhi.helpers

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Test

class DatabaseKeyManagerTest {

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
