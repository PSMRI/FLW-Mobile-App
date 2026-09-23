package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChildImmunizationListFragmentNavigationTest {

    private lateinit var bundle: Bundle
    private val argsClass = ChildImmunizationListFragmentArgs::class.java
    private val showDueOnlyField = argsClass.declaredFields
        .firstOrNull { it.name == "showDueOnly" }
        ?.apply { isAccessible = true }

    @Before
    fun setUp() {
        bundle = mockk(relaxed = true)
        every { bundle.setClassLoader(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    private fun newArgs(value: Boolean = true): ChildImmunizationListFragmentArgs {
        val ctor = argsClass.declaredConstructors
            .filterNot { c -> c.parameterTypes.any { it.simpleName == "DefaultConstructorMarker" } }
            .maxByOrNull { it.parameterCount }!!
        ctor.isAccessible = true
        val values = ctor.parameterTypes.map { type ->
            if (type == java.lang.Boolean.TYPE || type == java.lang.Boolean::class.java) value else null
        }.toTypedArray()
        return ctor.newInstance(*values) as ChildImmunizationListFragmentArgs
    }

    private fun showDueOnlyOf(a: ChildImmunizationListFragmentArgs): Boolean =
        (showDueOnlyField?.get(a) as? Boolean) ?: false

    @Test
    fun constructor_exposesEveryArgument() {
        val a = newArgs(true)
        assertEquals(true, showDueOnlyOf(a))
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = newArgs(true)
        val same = newArgs(true)
        assertEquals(a, same)
        assertEquals(a.hashCode(), same.hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("ChildImmunizationListFragmentArgs"))
    }

    @Test
    fun differentValues_areNotEqual() {
        val a = newArgs(true)
        val b = newArgs(false)
        if (showDueOnlyField != null) {
            assertNotEquals(a, b)
        } else {
            assertEquals(a, b)
        }
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val a = newArgs(true)
        val handle = a.toSavedStateHandle()
        val roundTripped = ChildImmunizationListFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(showDueOnlyOf(a), showDueOnlyOf(roundTripped))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = ChildImmunizationListFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(false, showDueOnlyOf(a))
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getBoolean("showDueOnly") } returns true
        val a = ChildImmunizationListFragmentArgs.fromBundle(bundle)
        assertEquals(true, showDueOnlyOf(a))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("showDueOnly") } returns false
        val a = ChildImmunizationListFragmentArgs.fromBundle(bundle)
        assertEquals(false, showDueOnlyOf(a))
    }

    @Test
    fun toBundle_putsEveryArgument() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
        assertNotNull(newArgs().toBundle())
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
    fun actionChildImmunizationListFragmentToImmunizationFormFragment_buildsDirections() {
        val d = ChildImmunizationListFragmentDirections.actionChildImmunizationListFragmentToImmunizationFormFragment(vaccineId = 11, benId = 20L, category = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = ChildImmunizationListFragmentDirections.actionChildImmunizationListFragmentToImmunizationFormFragment(vaccineId = 11, benId = 20L, category = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = ChildImmunizationListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = ChildImmunizationListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
