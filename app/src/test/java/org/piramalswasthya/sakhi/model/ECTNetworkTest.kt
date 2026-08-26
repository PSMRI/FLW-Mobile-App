package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    private fun fullNetwork() = ECTNetwork(
        benId = 1L,
        lmpDate = "2026-01-01",
        dateOfSterilisation = "2026-02-01",
        visitDate = "2026-08-17",
        dateOfAntraInjection = "2026-03-01",
        dueDateOfAntraInjection = "2026-06-01",
        mpaFile = "mpa.jpg",
        dischargeSummary1 = "ds1.jpg",
        dischargeSummary2 = "ds2.jpg",
        antraDose = "2",
        isPregnancyTestDone = "Y",
        pregnancyTestResult = "Negative",
        isPregnant = "N",
        usingFamilyPlanning = true,
        methodOfContraception = "IUD",
        isActive = true,
        createdBy = "ashaWorker",
        createdDate = "2026-08-17",
        updatedBy = "ashaWorker",
        updatedDate = "2026-08-17",
        lmp_date = 100L
    )

    @Test fun `holds all field values`() {
        val n = fullNetwork()
        assertEquals(1L, n.benId)
        assertEquals("2026-01-01", n.lmpDate)
        assertEquals("mpa.jpg", n.mpaFile)
        assertEquals("2", n.antraDose)
        assertTrue(n.usingFamilyPlanning!!)
        assertEquals("IUD", n.methodOfContraception)
        assertEquals(100L, n.lmp_date)
    }

    @Test fun `copy and equality`() {
        val a = fullNetwork()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertFalse(a == a.copy(benId = 999L))
        assertTrue(a.toString().contains("ECTNetwork"))
    }
}
