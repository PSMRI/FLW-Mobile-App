package org.piramalswasthya.sakhi.model

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BenWithAncVisitCacheTest {

    private val ben = mockk<BenBasicCache>(relaxed = true)

    private fun emptyCache() = BenWithAncVisitCache(
        ben = ben,
        pwr = emptyList(),
        pmsma = emptyList(),
        savedPmsmaRecords = emptyList(),
        savedAncRecords = emptyList()
    )

    // ---------------- accessors ----------------

    @Test fun `holds ben reference`() {
        assertSame(ben, emptyCache().ben)
    }

    @Test fun `default lists are empty`() {
        val cache = emptyCache()
        assertTrue(cache.pwr.isEmpty())
        assertTrue(cache.pmsma.isEmpty())
        assertTrue(cache.savedPmsmaRecords.isEmpty())
        assertTrue(cache.savedAncRecords.isEmpty())
    }

    @Test fun `copy replaces pwr list`() {
        val pwr = listOf(mockk<PregnantWomanRegistrationCache>(relaxed = true))
        val copy = emptyCache().copy(pwr = pwr)
        assertEquals(1, copy.pwr.size)
        assertTrue(copy.pmsma.isEmpty())
    }

    @Test fun `equal caches are equal`() {
        assertEquals(emptyCache(), emptyCache())
    }

    @Test fun `caches with different anc records not equal`() {
        val a = emptyCache()
        val b = emptyCache().copy(
            savedAncRecords = listOf(mockk<PregnantWomanAncCache>(relaxed = true))
        )
        assertFalse(a == b)
    }

    // ---------------- asDomainModel (empty-list path) ----------------

    @Test fun `asDomainModel returns non-null domain for empty records`() {
        assertNotNull(emptyCache().asDomainModel())
    }

    @Test fun `asDomainModel has empty anc list when no anc records`() {
        assertTrue(emptyCache().asDomainModel().anc.isEmpty())
    }

    @Test fun `asDomainModel hasPmsma false with no active pmsma`() {
        assertFalse(emptyCache().asDomainModel().hasPmsma)
    }

    @Test fun `asDomainModel showAddAnc false with no active pwr`() {
        assertFalse(emptyCache().asDomainModel().showAddAnc)
    }

    @Test fun `asDomainModel pmsmaFillable false with no anc and no active pmsma`() {
        assertFalse(emptyCache().asDomainModel().pmsmaFillable)
    }

    @Test fun `asDomainModel synthesizes single default pmsma status`() {
        assertEquals(1, emptyCache().asDomainModel().pmsma.size)
    }

    @Test fun `asDomainModel showViewAnc true when anc empty`() {
        assertTrue(emptyCache().asDomainModel().showViewAnc)
    }

    @Test fun `asDomainModel syncState null when no pmsma and no anc`() {
        assertNull(emptyCache().asDomainModel().syncState)
    }

    @Test fun `asDomainModel savedAncRecords passed through`() {
        assertTrue(emptyCache().asDomainModel().savedAncRecords.isEmpty())
    }

    @Test fun `asDomainModel ancDate defaults to zero with no records`() {
        assertEquals(0L, emptyCache().asDomainModel().ancDate)
    }
}
