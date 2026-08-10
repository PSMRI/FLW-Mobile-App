package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in PNC.kt:
 *  - PNCVisitCache.asDomainModel()
 *  - PNCVisitCache.asNetworkModel()
 *  - PNCNetwork.asCacheModel()
 *
 * Skipped: BenWithDoAndPncCache.asBasicDomainModelForPNC() (requires
 * BenBasicCache + related DeliveryOutcome graph).
 */
class PncMappingTest {

    private fun cache(
        benId: Long = 1L,
        pncPeriod: Int = 1,
        isActive: Boolean = true,
        pncDate: Long = 1_600_000_000_000L
    ) = PNCVisitCache(
        benId = benId,
        pncPeriod = pncPeriod,
        isActive = isActive,
        pncDate = pncDate,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.UNSYNCED
    )

    // ---------------------------------------------------------------
    // PNCVisitCache.asDomainModel()
    // ---------------------------------------------------------------

    @Test
    fun `asDomainModel maps benId visitNumber and syncState`() {
        val domain = cache(benId = 9L, pncPeriod = 7).asDomainModel()

        assertEquals(9L, domain.benId)
        assertEquals(7, domain.visitNumber)
        assertEquals(SyncState.UNSYNCED, domain.syncState)
    }

    // ---------------------------------------------------------------
    // PNCVisitCache.asNetworkModel()
    // ---------------------------------------------------------------

    @Test
    fun `asNetworkModel maps core fields`() {
        val net = cache(benId = 12L, pncPeriod = 3, isActive = true).copy(
            id = 5L,
            ifaTabsGiven = 30,
            contraceptionMethod = "IUCD",
            referralFacility = "PHC",
            motherDeath = false
        ).asNetworkModel()

        assertEquals(5L, net.id)
        assertEquals(12L, net.benId)
        assertEquals(3, net.pncPeriod)
        assertTrue(net.isActive)
        assertEquals(30, net.ifaTabsGiven)
        assertEquals("IUCD", net.contraceptionMethod)
        assertEquals("PHC", net.referralFacility)
        assertNotNull(net.pncDate)
        assertNotNull(net.createdDate)
    }

    // ---------------------------------------------------------------
    // PNCNetwork.asCacheModel()
    // ---------------------------------------------------------------

    @Test
    fun `PNCNetwork asCacheModel maps core fields and sets synced`() {
        val net = PNCNetwork(
            id = 1L,
            benId = 77L,
            pncPeriod = 6,
            isActive = true,
            pncDate = "2024-01-15",
            ifaTabsGiven = 45,
            anyContraceptionMethod = true,
            contraceptionMethod = "Condom",
            sterilisationDate = "2024-01-10",
            otherPpcMethod = null,
            anyDangerSign = null,
            motherDangerSign = null,
            otherDangerSign = null,
            referralFacility = "CHC",
            motherDeath = false,
            deathDate = null,
            causeOfDeath = null,
            otherDeathCause = null,
            placeOfDeath = null,
            otherPlaceOfDeath = null,
            remarks = "ok",
            createdBy = "c",
            createdDate = "2024-01-15",
            updatedBy = "u",
            updatedDate = "2024-01-16"
        )

        val cache = net.asCacheModel()

        assertEquals(77L, cache.benId)
        assertEquals(6, cache.pncPeriod)
        assertTrue(cache.isActive)
        assertEquals(45, cache.ifaTabsGiven)
        assertEquals("Condom", cache.contraceptionMethod)
        assertEquals("CHC", cache.referralFacility)
        assertEquals("ok", cache.remarks)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------------------------------------------------------
    // Branch-variant coverage: mother-death path, no-contraception path,
    // and differing pncPeriod / isActive on PNCNetwork.asCacheModel.
    // ---------------------------------------------------------------

    @Test
    fun `asNetworkModel mother-death path`() {
        val net = cache(benId = 21L, pncPeriod = 42).copy(
            id = 9L,
            motherDeath = true,
            referralFacility = "DH"
        ).asNetworkModel()
        assertEquals(9L, net.id)
        assertEquals(21L, net.benId)
        assertEquals(42, net.pncPeriod)
        assertNotNull(net.pncDate)
    }

    @Test
    fun `asDomainModel inactive`() {
        val domain = cache(benId = 3L, pncPeriod = 21, isActive = false).asDomainModel()
        assertEquals(3L, domain.benId)
        assertEquals(21, domain.visitNumber)
    }

    @Test
    fun `PNCNetwork asCacheModel no-contraception mother-death`() {
        val net = PNCNetwork(
            id = 2L,
            benId = 78L,
            pncPeriod = 1,
            isActive = false,
            pncDate = "2024-02-15",
            ifaTabsGiven = 0,
            anyContraceptionMethod = false,
            contraceptionMethod = null,
            sterilisationDate = "",
            otherPpcMethod = null,
            anyDangerSign = "Yes",
            motherDangerSign = "Fever",
            otherDangerSign = null,
            referralFacility = "DH",
            motherDeath = true,
            deathDate = "2024-02-20",
            causeOfDeath = "Sepsis",
            otherDeathCause = null,
            placeOfDeath = "Hospital",
            otherPlaceOfDeath = null,
            remarks = "critical",
            createdBy = "c",
            createdDate = "2024-02-15",
            updatedBy = "u",
            updatedDate = "2024-02-16"
        )
        val cache = net.asCacheModel()
        assertEquals(78L, cache.benId)
        assertEquals(1, cache.pncPeriod)
        assertEquals("critical", cache.remarks)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }
}
