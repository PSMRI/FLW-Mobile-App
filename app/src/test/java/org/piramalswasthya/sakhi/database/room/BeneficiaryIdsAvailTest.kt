package org.piramalswasthya.sakhi.database.room

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeneficiaryIdsAvailTest {

    @Test fun `BeneficiaryIdsAvail defaults id to zero and benRegId to zero`() {
        val entity = BeneficiaryIdsAvail(userId = 1, benId = 10L)
        assertEquals(0, entity.id)
        assertEquals(0L, entity.benRegId)
    }

    @Test fun `BeneficiaryIdsAvail exposes constructor values`() {
        val entity = BeneficiaryIdsAvail(id = 5, userId = 1, benId = 10L, benRegId = 20L)
        assertEquals(5, entity.id)
        assertEquals(1, entity.userId)
        assertEquals(10L, entity.benId)
        assertEquals(20L, entity.benRegId)
    }

    @Test fun `BeneficiaryIdsAvail id and benId var fields are mutable`() {
        val entity = BeneficiaryIdsAvail(userId = 1, benId = 10L)
        entity.id = 99
        entity.benId = 200L
        entity.benRegId = 300L
        assertEquals(99, entity.id)
        assertEquals(200L, entity.benId)
        assertEquals(300L, entity.benRegId)
    }

    @Test fun `BeneficiaryIdsAvail equals and copy`() {
        val a = BeneficiaryIdsAvail(id = 1, userId = 1, benId = 10L, benRegId = 20L)
        val b = BeneficiaryIdsAvail(id = 1, userId = 1, benId = 10L, benRegId = 20L)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(benId = 99L))
        assertTrue(a.toString().contains("BeneficiaryIdsAvail"))
    }
}
