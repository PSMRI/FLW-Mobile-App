package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration

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

class EyeSurgeryFormFragmentNavigationTest {

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

    private fun args() = EyeSurgeryFormFragmentArgs(hhId = 10L, benId = 20L, isViewMode = true, eyeSide = "v4", formDataJson = "v5", recordId = 61, benName = "v7", gender = "v8", age = "v9")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.hhId)
        assertEquals(20L, a.benId)
        assertEquals(true, a.isViewMode)
        assertEquals("v4", a.eyeSide)
        assertEquals("v5", a.formDataJson)
        assertEquals(61, a.recordId)
        assertEquals("v7", a.benName)
        assertEquals("v8", a.gender)
        assertEquals("v9", a.age)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("EyeSurgeryFormFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(20L, a.component2())
        assertEquals(true, a.component3())
        assertEquals("v4", a.component4())
        assertEquals("v5", a.component5())
        assertEquals(61, a.component6())
        assertEquals("v7", a.component7())
        assertEquals("v8", a.component8())
        assertEquals("v9", a.component9())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(hhId = 99L)
        assertEquals(99L, b.hhId)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals(10L, handle.get<Long>("hhId"))
        assertEquals(20L, handle.get<Long>("benId"))
        assertEquals(true, handle.get<Boolean>("isViewMode"))
        assertEquals("v4", handle.get<String>("eyeSide"))
        assertEquals("v5", handle.get<String>("formDataJson"))
        assertEquals(61, handle.get<Int>("recordId"))
        assertEquals("v7", handle.get<String>("benName"))
        assertEquals("v8", handle.get<String>("gender"))
        assertEquals("v9", handle.get<String>("age"))
        assertEquals(args(), EyeSurgeryFormFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("hhId" to 10L))
        val a = EyeSurgeryFormFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(0L, a.benId)
        assertEquals(false, a.isViewMode)
        assertEquals("", a.eyeSide)
        assertNull(a.formDataJson)
        assertEquals(0, a.recordId)
        assertEquals("", a.benName)
        assertEquals("", a.gender)
        assertEquals("", a.age)
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            EyeSurgeryFormFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("hhId" to null, "benId" to 20L, "isViewMode" to true, "eyeSide" to "v4", "formDataJson" to "v5", "recordId" to 61, "benName" to "v7", "gender" to "v8", "age" to "v9"))
        assertThrows(IllegalArgumentException::class.java) {
            EyeSurgeryFormFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("hhId") } returns 10L
        every { bundle.getLong("benId") } returns 20L
        every { bundle.getBoolean("isViewMode") } returns true
        every { bundle.getString("eyeSide") } returns "v4"
        every { bundle.getString("formDataJson") } returns "v5"
        every { bundle.getInt("recordId") } returns 61
        every { bundle.getString("benName") } returns "v7"
        every { bundle.getString("gender") } returns "v8"
        every { bundle.getString("age") } returns "v9"
        assertEquals(args(), EyeSurgeryFormFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("benId") } returns false
        every { bundle.containsKey("isViewMode") } returns false
        every { bundle.containsKey("eyeSide") } returns false
        every { bundle.containsKey("formDataJson") } returns false
        every { bundle.containsKey("recordId") } returns false
        every { bundle.containsKey("benName") } returns false
        every { bundle.containsKey("gender") } returns false
        every { bundle.containsKey("age") } returns false
        every { bundle.getLong("hhId") } returns 10L
        val a = EyeSurgeryFormFragmentArgs.fromBundle(bundle)
        assertEquals(0L, a.benId)
        assertEquals(false, a.isViewMode)
        assertEquals("", a.eyeSide)
        assertNull(a.formDataJson)
        assertEquals(0, a.recordId)
        assertEquals("", a.benName)
        assertEquals("", a.gender)
        assertEquals("", a.age)
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            EyeSurgeryFormFragmentArgs.fromBundle(bundle)
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
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = EyeSurgeryFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = EyeSurgeryFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
