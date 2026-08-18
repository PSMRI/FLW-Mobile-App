package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ECTNetworkTest {

    @Test
    fun `constructor uses default values when omitted`() {
        val network = ECTNetwork(
            benId = 1L,
            visitDate = "2026-08-17",
            isPregnancyTestDone = "N",
            pregnancyTestResult = null,
            isPregnant = "N",
            usingFamilyPlanning = true,
            methodOfContraception = "IUD",
            isActive = true,
            createdBy = "ashaWorker",
            createdDate = "2026-08-17",
            updatedBy = "ashaWorker",
            updatedDate = "2026-08-17",
            lmp_date = 0L
        )

        assertNotNull(network)
        assertNull(network.lmpDate)
        assertNull(network.dateOfSterilisation)
        assertNull(network.mpaFile)
    }
}
