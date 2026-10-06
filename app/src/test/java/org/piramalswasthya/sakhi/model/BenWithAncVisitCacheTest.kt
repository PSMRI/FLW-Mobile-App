package org.piramalswasthya.sakhi.model

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import java.util.concurrent.TimeUnit

class BenWithAncVisitCacheTest {

    private val ben = mockk<BenBasicCache>(relaxed = true)

    private fun emptyCache() = BenWithAncVisitCache(
        ben = ben,
        pwr = emptyList(),
        pmsma = emptyList(),
        savedPmsmaRecords = emptyList(),
        savedAncRecords = emptyList()
    )

    private fun pwr(active: Boolean = true, lmpDate: Long = 0L) = PregnantWomanRegistrationCache(
        benId = 1L,
        active = active,
        lmpDate = lmpDate,
        createdBy = "c",
        updatedBy = "u",
        syncState = SyncState.SYNCED
    )

    private fun anc(
        visitNumber: Int = 1,
        isActive: Boolean = true,
        isAborted: Boolean = false,
        ancDate: Long = 0L,
        lmpDate: Long? = null,
        anyHighRisk: Boolean? = null,
        placeOfAncId: Int? = 0,
        syncState: SyncState = SyncState.SYNCED,
        terminationDoneBy: String? = null
    ) = PregnantWomanAncCache(
        benId = 1L,
        visitNumber = visitNumber,
        isActive = isActive,
        ancDate = ancDate,
        lmpDate = lmpDate,
        isAborted = isAborted,
        anyHighRisk = anyHighRisk,
        placeOfAncId = placeOfAncId,
        terminationDoneBy = terminationDoneBy,
        createdBy = "c",
        updatedBy = "u",
        syncState = syncState,
        frontFilePath = null,
        backFilePath = null
    )

    private fun pmsmaCache(
        visitNumber: Int = 1,
        isActive: Boolean = true,
        visitDate: Long? = null,
        syncState: SyncState = SyncState.SYNCED
    ) = PMSMACache(
        benId = 1L,
        visitNumber = visitNumber,
        isActive = isActive,
        visitDate = visitDate,
        createdBy = "c",
        updatedBy = "u",
        syncState = syncState
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

    // ---------------- lmp source / anc filledWeek / ancDate ----------------

    @Test fun `asDomainModel uses active pwr lmp for anc filledWeek`() {
        val now = System.currentTimeMillis()
        val lmp = now - TimeUnit.DAYS.toMillis(70)
        val ancRecord = anc(visitNumber = 1, isActive = true, ancDate = now)
        val cache = emptyCache().copy(
            pwr = listOf(pwr(active = true, lmpDate = lmp)),
            savedAncRecords = listOf(ancRecord)
        )

        val domain = cache.asDomainModel()

        assertEquals(1, domain.anc.size)
        assertEquals(10, domain.anc[0].filledWeek)
        assertEquals(now, domain.ancDate)
    }

    @Test fun `asDomainModel anc filledWeek zero when no lmp source available`() {
        val cache = emptyCache().copy(
            savedAncRecords = listOf(anc(visitNumber = 1, isActive = true))
        )

        val domain = cache.asDomainModel()

        assertEquals(0, domain.anc[0].filledWeek)
    }

    @Test fun `asDomainModel anc list filters out inactive records`() {
        val cache = emptyCache().copy(
            savedAncRecords = listOf(
                anc(visitNumber = 1, isActive = true),
                anc(visitNumber = 2, isActive = false)
            )
        )

        val domain = cache.asDomainModel()

        assertEquals(1, domain.anc.size)
        assertEquals(1, domain.anc[0].visitNumber)
    }

    @Test fun `asDomainModel falls back to abortion record lmp and populates apply-block fields`() {
        val lmp = 1_600_000_000_000L
        val abortionAncDate = 1_602_000_000_000L
        val abortionRecord = anc(visitNumber = 1, isAborted = true, lmpDate = lmp, ancDate = abortionAncDate)
        val cache = emptyCache().copy(savedAncRecords = listOf(abortionRecord))

        val domain = cache.asDomainModel()

        assertEquals(lmp, domain.lmpDate)
        assertEquals(lmp + TimeUnit.DAYS.toMillis(280), domain.eddDate)
        assertNotNull(domain.weekOfPregnancy)
        assertEquals(abortionAncDate, domain.abortionDate)
    }

    @Test fun `asDomainModel prefers active pwr lmp over abortion record and skips apply block`() {
        val lmpFromPwr = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        val lmpFromAbortion = 1_600_000_000_000L
        val abortionRecord = anc(visitNumber = 1, isAborted = true, lmpDate = lmpFromAbortion)
        val cache = emptyCache().copy(
            pwr = listOf(pwr(active = true, lmpDate = lmpFromPwr)),
            savedAncRecords = listOf(abortionRecord)
        )

        val domain = cache.asDomainModel()

        assertNull(domain.lmpDate)
        assertNull(domain.eddDate)
        assertNull(domain.abortionDate)
    }

    // ---------------- pmsma default synthesized status ----------------

    @Test fun `asDomainModel synthesized pmsma filledWeek 1 when no active pmsma and anc visit1 exists`() {
        val cache = emptyCache().copy(
            savedAncRecords = listOf(anc(visitNumber = 1, isActive = true))
        )

        val domain = cache.asDomainModel()

        assertEquals(1, domain.pmsma.size)
        assertEquals(1, domain.pmsma[0].filledWeek)
    }

    @Test fun `asDomainModel synthesized pmsma filledWeek 0 when an active pmsma already exists`() {
        val cache = emptyCache().copy(
            pmsma = listOf(pmsmaCache(isActive = true)),
            savedAncRecords = listOf(anc(visitNumber = 1, isActive = true))
        )

        val domain = cache.asDomainModel()

        assertTrue(domain.hasPmsma)
        assertEquals(1, domain.pmsma.size)
        assertEquals(0, domain.pmsma[0].filledWeek)
    }

    @Test fun `asDomainModel maps existing pmsma records with eligibility when gap at least a week`() {
        val visitDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val cache = emptyCache().copy(
            savedPmsmaRecords = listOf(pmsmaCache(visitNumber = 1, isActive = true, visitDate = visitDate))
        )

        val domain = cache.asDomainModel()

        assertEquals(1, domain.pmsma.size)
        assertEquals(1, domain.pmsma[0].filledWeek)
        assertEquals(1, domain.pmsma[0].visitNumber)
    }

    @Test fun `asDomainModel pmsma eligibility zero when gap under a week`() {
        val visitDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
        val cache = emptyCache().copy(
            savedPmsmaRecords = listOf(pmsmaCache(visitNumber = 1, isActive = true, visitDate = visitDate))
        )

        val domain = cache.asDomainModel()

        assertEquals(0, domain.pmsma[0].filledWeek)
    }

    @Test fun `asDomainModel pmsma eligibility zero when last visit date is null`() {
        val cache = emptyCache().copy(
            savedPmsmaRecords = listOf(pmsmaCache(visitNumber = 1, isActive = true, visitDate = null))
        )

        val domain = cache.asDomainModel()

        assertEquals(0, domain.pmsma[0].filledWeek)
    }

    // ---------------- pmsmaFillable ----------------

    @Test fun `asDomainModel pmsmaFillable true when no active pmsma but an anc visit exists`() {
        val cache = emptyCache().copy(savedAncRecords = listOf(anc(visitNumber = 1)))
        assertTrue(cache.asDomainModel().pmsmaFillable)
    }

    @Test fun `asDomainModel pmsmaFillable false when no active pmsma and anc visitNumber is zero`() {
        val cache = emptyCache().copy(savedAncRecords = listOf(anc(visitNumber = 0)))
        assertFalse(cache.asDomainModel().pmsmaFillable)
    }

    @Test fun `asDomainModel pmsmaFillable true when active pmsma present regardless of anc`() {
        val cache = emptyCache().copy(pmsma = listOf(pmsmaCache(isActive = true)))
        assertTrue(cache.asDomainModel().pmsmaFillable)
    }

    // ---------------- showAddAnc ----------------

    @Test fun `asDomainModel showAddAnc true when active pwr and no anc and enough weeks elapsed`() {
        val lmp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(50)
        val cache = emptyCache().copy(pwr = listOf(pwr(active = true, lmpDate = lmp)))
        assertTrue(cache.asDomainModel().showAddAnc)
    }

    @Test fun `asDomainModel showAddAnc false when active pwr and no anc and not enough weeks elapsed`() {
        val lmp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val cache = emptyCache().copy(pwr = listOf(pwr(active = true, lmpDate = lmp)))
        assertFalse(cache.asDomainModel().showAddAnc)
    }

    @Test fun `asDomainModel showAddAnc true when last anc old enough and visit count below max`() {
        val now = System.currentTimeMillis()
        val lmp = now - TimeUnit.DAYS.toMillis(100)
        val ancDate = now - TimeUnit.DAYS.toMillis(40)
        val cache = emptyCache().copy(
            pwr = listOf(pwr(active = true, lmpDate = lmp)),
            savedAncRecords = listOf(anc(visitNumber = 3, isActive = true, ancDate = ancDate))
        )
        assertTrue(cache.asDomainModel().showAddAnc)
    }

    @Test fun `asDomainModel showAddAnc false when visit count already at max`() {
        val now = System.currentTimeMillis()
        val lmp = now - TimeUnit.DAYS.toMillis(100)
        val ancDate = now - TimeUnit.DAYS.toMillis(40)
        val cache = emptyCache().copy(
            pwr = listOf(pwr(active = true, lmpDate = lmp)),
            savedAncRecords = listOf(anc(visitNumber = 8, isActive = true, ancDate = ancDate))
        )
        assertFalse(cache.asDomainModel().showAddAnc)
    }

    @Test fun `asDomainModel showAddAnc false when last anc is too recent`() {
        val now = System.currentTimeMillis()
        val lmp = now - TimeUnit.DAYS.toMillis(100)
        val ancDate = now - TimeUnit.DAYS.toMillis(5)
        val cache = emptyCache().copy(
            pwr = listOf(pwr(active = true, lmpDate = lmp)),
            savedAncRecords = listOf(anc(visitNumber = 3, isActive = true, ancDate = ancDate))
        )
        assertFalse(cache.asDomainModel().showAddAnc)
    }

    // ---------------- syncState ----------------

    @Test fun `asDomainModel syncState unsynced when active pmsma is unsynced`() {
        val cache = emptyCache().copy(
            pmsma = listOf(pmsmaCache(isActive = true, syncState = SyncState.UNSYNCED))
        )
        assertEquals(SyncState.UNSYNCED, cache.asDomainModel().syncState)
    }

    @Test fun `asDomainModel syncState unsynced when an anc record is unsynced`() {
        val cache = emptyCache().copy(
            savedAncRecords = listOf(anc(visitNumber = 1, syncState = SyncState.UNSYNCED))
        )
        assertEquals(SyncState.UNSYNCED, cache.asDomainModel().syncState)
    }

    @Test fun `asDomainModel syncState synced when active pmsma and anc records are all synced`() {
        val cache = emptyCache().copy(
            pmsma = listOf(pmsmaCache(isActive = true, syncState = SyncState.SYNCED)),
            savedAncRecords = listOf(anc(visitNumber = 1, syncState = SyncState.SYNCED))
        )
        assertEquals(SyncState.SYNCED, cache.asDomainModel().syncState)
    }
}
