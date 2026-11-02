package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

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

class PwAncVisitsListFragmentNavigationTest {

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

    private fun args() = PwAncVisitsListFragmentArgs(source = 11)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(11, a.source)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("PwAncVisitsListFragmentArgs"))
        assertEquals(11, a.component1())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(source = 99)
        assertEquals(99, b.source)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals(11, handle.get<Int>("source"))
        assertEquals(args(), PwAncVisitsListFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = PwAncVisitsListFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(0, a.source)
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getInt("source") } returns 11
        assertEquals(args(), PwAncVisitsListFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("source") } returns false
        val a = PwAncVisitsListFragmentArgs.fromBundle(bundle)
        assertEquals(0, a.source)
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
    fun actionPwAncVisitsFragmentToPwAncFormFragment_buildsDirections() {
        val d = PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionPwAncVisitsFragmentToPmsmaFragment_buildsDirections() {
        val d = PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(benId = 10L, hhId = 20L, visitNumber = 31, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(benId = 10L, hhId = 20L, visitNumber = 31, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionPwAncVisitsFragmentToPwAncCounsellingFormFragment_buildsDirections() {
        val d = PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncCounsellingFormFragment(benId = 10L, visitNumber = 21, viewMode = true, visitDate = "v4", lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncCounsellingFormFragment(benId = 10L, visitNumber = 21, viewMode = true, visitDate = "v4", lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = PwAncVisitsListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = PwAncVisitsListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
