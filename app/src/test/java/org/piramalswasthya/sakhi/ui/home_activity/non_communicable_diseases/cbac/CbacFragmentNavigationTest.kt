package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.cbac

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

class CbacFragmentNavigationTest {

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

    private fun args() = CbacFragmentArgs(benId = 10L, cbacId = 21, ashaId = 31)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.benId)
        assertEquals(21, a.cbacId)
        assertEquals(31, a.ashaId)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("CbacFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(21, a.component2())
        assertEquals(31, a.component3())
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
        assertEquals(21, handle.get<Int>("cbacId"))
        assertEquals(31, handle.get<Int>("ashaId"))
        assertEquals(args(), CbacFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = CbacFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(0L, a.benId)
        assertEquals(0, a.cbacId)
        assertEquals(0, a.ashaId)
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getInt("cbacId") } returns 21
        every { bundle.getInt("ashaId") } returns 31
        assertEquals(args(), CbacFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("benId") } returns false
        every { bundle.containsKey("cbacId") } returns false
        every { bundle.containsKey("ashaId") } returns false
        val a = CbacFragmentArgs.fromBundle(bundle)
        assertEquals(0L, a.benId)
        assertEquals(0, a.cbacId)
        assertEquals(0, a.ashaId)
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
    fun actionCbacFragmentToNcdReferForm_buildsDirections() {
        val d = CbacFragmentDirections.actionCbacFragmentToNcdReferForm(benId = 10L, referral = "v2", referralType = "v3", cbacId = 41)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = CbacFragmentDirections.actionCbacFragmentToNcdReferForm(benId = 10L, referral = "v2", referralType = "v3", cbacId = 41)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = CbacFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = CbacFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun constructor_usesDefaults_whenOptionalArgumentsOmitted() {
        val a = CbacFragmentArgs()
        assertEquals(0L, a.benId)
        assertEquals(0, a.cbacId)
        assertEquals(0, a.ashaId)
    }
}
