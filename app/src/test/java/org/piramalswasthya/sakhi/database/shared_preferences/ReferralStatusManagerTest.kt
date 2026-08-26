package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReferralStatusManagerTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var manager: ReferralStatusManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences("referral_status", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } just Runs

        manager = ReferralStatusManager(context)
    }

    @Test
    fun `isReferred returns true when a matching flag is stored`() {
        every { prefs.getBoolean("42_HWC", false) } returns true

        assertTrue(manager.isReferred(42L, "HWC"))
    }

    @Test
    fun `isReferred returns false when nothing is stored for that ben and type`() {
        every { prefs.getBoolean("42_HWC", false) } returns false

        assertFalse(manager.isReferred(42L, "HWC"))
    }

    @Test
    fun `markAsReferred writes a true flag keyed by benId and referral type`() {
        manager.markAsReferred(7L, "PHC")

        verify { editor.putBoolean("7_PHC", true) }
        verify { editor.apply() }
    }

    @Test
    fun `clearReferralStatus removes the flag keyed by benId and referral type`() {
        manager.clearReferralStatus(7L, "PHC")

        verify { editor.remove("7_PHC") }
        verify { editor.apply() }
    }

    @Test
    fun `different referral types for the same beneficiary use distinct keys`() {
        manager.markAsReferred(1L, "HWC")
        manager.markAsReferred(1L, "PHC")

        verify { editor.putBoolean("1_HWC", true) }
        verify { editor.putBoolean("1_PHC", true) }
    }
}
