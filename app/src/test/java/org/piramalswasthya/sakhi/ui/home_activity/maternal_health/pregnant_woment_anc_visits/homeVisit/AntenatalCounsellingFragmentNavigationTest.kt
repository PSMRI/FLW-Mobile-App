package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.homeVisit

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AntenatalCounsellingFragmentNavigationTest {

    private lateinit var bundle: Bundle

    @Before
    fun setUp() {
        bundle = mockk(relaxed = true)
        every { bundle.setClassLoader(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    private fun args() = AntenatalCounsellingFragmentArgs(benId = 10L, visitNumber = 21, viewMode = true, visitDate = "v4", lastItemClick = true)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.benId)
        assertEquals(21, a.visitNumber)
        assertEquals(true, a.viewMode)
        assertEquals("v4", a.visitDate)
        assertEquals(true, a.lastItemClick)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("AntenatalCounsellingFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(21, a.component2())
        assertEquals(true, a.component3())
        assertEquals("v4", a.component4())
        assertEquals(true, a.component5())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(benId = 99L)
        assertEquals(99L, b.benId)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals(10L, handle.get<Long>("benId"))
        assertEquals(21, handle.get<Int>("visitNumber"))
        assertEquals(true, handle.get<Boolean>("viewMode"))
        assertEquals("v4", handle.get<String>("visitDate"))
        assertEquals(true, handle.get<Boolean>("lastItemClick"))
        assertEquals(args(), AntenatalCounsellingFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to 10L, "visitNumber" to 21))
        val a = AntenatalCounsellingFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(false, a.viewMode)
        assertEquals("", a.visitDate)
        assertEquals(false, a.lastItemClick)
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            AntenatalCounsellingFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to null, "visitNumber" to 21, "viewMode" to true, "visitDate" to "v4", "lastItemClick" to true))
        assertThrows(IllegalArgumentException::class.java) {
            AntenatalCounsellingFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getInt("visitNumber") } returns 21
        every { bundle.getBoolean("viewMode") } returns true
        every { bundle.getString("visitDate") } returns "v4"
        every { bundle.getBoolean("lastItemClick") } returns true
        assertEquals(args(), AntenatalCounsellingFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("viewMode") } returns false
        every { bundle.containsKey("visitDate") } returns false
        every { bundle.containsKey("lastItemClick") } returns false
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getInt("visitNumber") } returns 21
        val a = AntenatalCounsellingFragmentArgs.fromBundle(bundle)
        assertEquals(false, a.viewMode)
        assertEquals("", a.visitDate)
        assertEquals(false, a.lastItemClick)
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            AntenatalCounsellingFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun toBundle_putsEveryArgument() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
        assertNotNull(args().toBundle())
    }

    @Before
    fun setUpDirections() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDownDirections() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun actionPwAncCounsellingFormFragmentToPwAncFormFragment_buildsDirections() {
        val d = AntenatalCounsellingFragmentDirections.actionPwAncCounsellingFormFragmentToPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AntenatalCounsellingFragmentDirections.actionPwAncCounsellingFormFragmentToPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionPwAncCounsellingFormFragmentToNcdReferForm_buildsDirections() {
        val d = AntenatalCounsellingFragmentDirections.actionPwAncCounsellingFormFragmentToNcdReferForm(benId = 10L, referral = "v2", referralType = "v3", cbacId = 41)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AntenatalCounsellingFragmentDirections.actionPwAncCounsellingFormFragmentToNcdReferForm(benId = 10L, referral = "v2", referralType = "v3", cbacId = 41)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = AntenatalCounsellingFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AntenatalCounsellingFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun constructor_usesDefaults_whenOptionalArgumentsOmitted() {
        val a = AntenatalCounsellingFragmentArgs(benId = 10L, visitNumber = 21)
        assertFalse(a.viewMode)
        assertEquals("", a.visitDate)
        assertFalse(a.lastItemClick)
    }
}
