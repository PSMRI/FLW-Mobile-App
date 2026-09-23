package org.piramalswasthya.sakhi.adapters

import android.os.Looper
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

class ChildImmunizationVaccineAdapterTest {

    private fun vaccine(
        vaccineId: Int = 1,
        state: VaccineState = VaccineState.PENDING,
        isSwitchChecked: Boolean = false
    ) = VaccineDomain(
        vaccineId = vaccineId,
        vaccineName = "BCG",
        vaccineCategory = ChildImmunizationCategory.BIRTH,
        state = state,
        isSwitchChecked = isSwitchChecked
    )

    @Test
    fun areItemsTheSame_comparesByVaccineId() {
        val callback = ChildImmunizationVaccineAdapter.ImmunizationIconDiffCallback
        assertTrue(callback.areItemsTheSame(vaccine(vaccineId = 1), vaccine(vaccineId = 1, isSwitchChecked = true)))
        assertFalse(callback.areItemsTheSame(vaccine(vaccineId = 1), vaccine(vaccineId = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = ChildImmunizationVaccineAdapter.ImmunizationIconDiffCallback
        val old = vaccine(vaccineId = 1, state = VaccineState.PENDING)
        val same = vaccine(vaccineId = 1, state = VaccineState.PENDING)
        val different = vaccine(vaccineId = 1, state = VaccineState.DONE)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClicked_invokesLambdaWithPositionAndVaccine() {
        var capturedPosition: Int? = null
        var capturedVaccine: VaccineDomain? = null
        val listener = ChildImmunizationVaccineAdapter.ImmunizationClickListener { position, vaccine ->
            capturedPosition = position
            capturedVaccine = vaccine
        }
        val item = vaccine(vaccineId = 4)
        listener.onClicked(2, item)
        assertEquals(2, capturedPosition)
        assertEquals(item, capturedVaccine)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ChildImmunizationVaccineAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
