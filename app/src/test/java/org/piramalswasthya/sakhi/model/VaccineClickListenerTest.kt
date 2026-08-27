package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VaccineClickListenerTest {

    private fun vaccine(vaccineId: Int = 7) = VaccineDomain(
        vaccineId = vaccineId,
        vaccineName = "BCG",
        vaccineCategory = ChildImmunizationCategory.BIRTH,
        state = VaccineState.PENDING
    )

    @Test fun `onClick invokes listener with benId and vaccineId`() {
        var capturedBenId: Long? = null
        var capturedVaccineId: Int? = null
        val listener = VaccineClickListener { benId, vaccineId ->
            capturedBenId = benId
            capturedVaccineId = vaccineId
        }

        listener.onClick(42L, vaccine(vaccineId = 9))

        assertEquals(42L, capturedBenId)
        assertEquals(9, capturedVaccineId)
    }

    @Test fun `onClick extracts vaccineId from vaccine domain not the listener`() {
        var invocationCount = 0
        val listener = VaccineClickListener { _, _ -> invocationCount++ }

        listener.onClick(1L, vaccine(vaccineId = 1))
        listener.onClick(2L, vaccine(vaccineId = 2))

        assertEquals(2, invocationCount)
    }
}
