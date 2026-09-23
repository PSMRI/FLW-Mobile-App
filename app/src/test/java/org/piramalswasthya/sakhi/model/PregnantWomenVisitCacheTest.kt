package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class PregnantWomenVisitCacheTest {

    private fun cache(
        rchId: String? = "RCH123",
        familyHeadName: String? = "Head",
    ) = PregnantWomenVisitCache(
        benId = 1L,
        name = "Asha",
        dob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365L * 25),
        mobileNo = 9990001111L,
        rchId = rchId,
        familyHeadName = familyHeadName,
        spouseName = "Spouse",
        lmp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(70),
    )

    @Test
    fun `asDomainModel maps present fields through`() {
        val domain = cache().asDomainModel()

        assertEquals(1L, domain.benId)
        assertEquals("Asha", domain.name)
        assertEquals("Spouse", domain.spouseName)
        assertEquals("9990001111", domain.mobileNo)
        assertEquals("RCH123", domain.rchId)
        assertEquals("Head", domain.familyHeadName)
    }

    @Test
    fun `asDomainModel substitutes fallbacks for blank rchId and null family head`() {
        val domain = cache(rchId = "  ", familyHeadName = null).asDomainModel()

        assertEquals("Not Available", domain.rchId)
        assertEquals("Not Available", domain.familyHeadName)
    }
}
