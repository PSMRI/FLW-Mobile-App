package org.piramalswasthya.sakhi.model

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class RemainingAsDomainMappersTest {

    private fun benDomain() = BenBasicDomain(
        benId = 1L,
        hhId = 1L,
        reproductiveStatusId = 0,
        regDate = "01-01-2024",
        benName = "Test",
        gender = "Female",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Test",
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun benCacheMock(): BenBasicCache {
        val ben = mockk<BenBasicCache>(relaxed = true)
        every { ben.asBasicDomainModel() } returns benDomain()
        return ben
    }

    @Test
    fun `BenWithABHAGeneratedCache maps to domain and carries abha`() {
        val abha = ABHAModel(
            beneficiaryID = 1L,
            beneficiaryRegID = 2L,
            benName = "Test",
            createdBy = "asha",
            message = "ok",
            txnId = "txn1",
            providerServiceMapId = 5
        )
        val cache = BenWithABHAGeneratedCache(ben = benCacheMock(), abha = abha)

        val domain = cache.asBenWithABHAGeneratedDomainModel()

        assertEquals(benDomain(), domain.ben)
        assertEquals(abha, domain.abha)
    }

    @Test
    fun `BenWithABHAGeneratedCache tolerates a null abha`() {
        val cache = BenWithABHAGeneratedCache(ben = benCacheMock(), abha = null)

        val domain = cache.asBenWithABHAGeneratedDomainModel()

        assertNull(domain.abha)
    }

    @Test
    fun `BenWithScreeningRound maps round list to domain`() {
        val rounds = listOf(IRSRoundScreening(id = 1, rounds = 2, householdId = 10L), null)
        val cache = BenWithScreeningRound(ben = benCacheMock(), round = rounds)

        val domain = cache.asScreeningRoundDomainModel()

        assertEquals(rounds, domain.round)
    }

    @Test
    fun `BenWithScreeningRound tolerates an empty round list`() {
        val cache = BenWithScreeningRound(ben = benCacheMock(), round = emptyList())

        val domain = cache.asScreeningRoundDomainModel()

        assertTrue(domain.round.isEmpty())
    }

    @Test
    fun `BenWithNCDReferalCache maps to domain and carries refcache`() {
        val referal = ReferalCache(benId = 1L, syncState = SyncState.SYNCED)
        val cache = BenWithNCDReferalCache(ben = benCacheMock(), refcache = referal)

        val domain = cache.ncdreferalDomainModel()

        assertEquals(benDomain(), domain.ben)
        assertEquals(referal, domain.refcache)
    }

    @Test
    fun `BenWithNCDReferalCache tolerates a null refcache`() {
        val cache = BenWithNCDReferalCache(ben = benCacheMock(), refcache = null)

        val domain = cache.ncdreferalDomainModel()

        assertNull(domain.refcache)
    }
}
