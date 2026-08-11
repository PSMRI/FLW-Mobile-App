package org.piramalswasthya.sakhi.ui.home_activity.all_ben

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

class AbhaBenFragmentNavigationTest {

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

    private fun args() = AbhaBenFragmentArgs(source = 11)

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
        assertTrue(a.toString().contains("AbhaBenFragmentArgs"))
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
        assertEquals(args(), AbhaBenFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = AbhaBenFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(0, a.source)
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getInt("source") } returns 11
        assertEquals(args(), AbhaBenFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("source") } returns false
        val a = AbhaBenFragmentArgs.fromBundle(bundle)
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
    fun actionAbhaBenFragmentToNewBenRegFragment_buildsDirections() {
        val d = AbhaBenFragmentDirections.actionAbhaBenFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AbhaBenFragmentDirections.actionAbhaBenFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = AbhaBenFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AbhaBenFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
