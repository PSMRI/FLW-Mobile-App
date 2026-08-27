package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.VaccineState

class ImmunizationVaccineAdapterTest {

    private fun vaccine(vaccineId: Int = 1, isSwitchChecked: Boolean = false) = VaccineDomain(
        vaccineId = vaccineId,
        vaccineName = "BCG",
        vaccineCategory = ChildImmunizationCategory.BIRTH,
        state = VaccineState.PENDING,
        isSwitchChecked = isSwitchChecked
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<VaccineDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ImmunizationVaccineAdapter\$ImmunizationIconDiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<VaccineDomain>
    }

    @Test
    fun areItemsTheSame_comparesByVaccineId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(vaccine(vaccineId = 1), vaccine(vaccineId = 1, isSwitchChecked = true)))
        assertFalse(callback.areItemsTheSame(vaccine(vaccineId = 1), vaccine(vaccineId = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(vaccine(vaccineId = 1, isSwitchChecked = true), vaccine(vaccineId = 1, isSwitchChecked = true)))
        assertFalse(callback.areContentsTheSame(vaccine(vaccineId = 1, isSwitchChecked = false), vaccine(vaccineId = 1, isSwitchChecked = true)))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ImmunizationVaccineAdapter(isDeath = false)
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }

    @Test
    fun isDeath_isMutableAfterConstruction() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ImmunizationVaccineAdapter(isDeath = false)
            adapter.isDeath = true
            assertTrue(adapter.isDeath)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
