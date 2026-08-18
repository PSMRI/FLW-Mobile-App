package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class ImmunizationMappingTest {

    // =====================================================
    // String.toVaccineType() Tests
    // =====================================================

    @Test fun `toVaccineType maps BCG`() {
        assertEquals(VaccineType.BCG, "BCG Vaccine".toVaccineType())
    }

    @Test fun `toVaccineType maps OPV-1`() {
        assertEquals(VaccineType.OPV_1, "OPV-1".toVaccineType())
    }

    @Test fun `toVaccineType maps Pentavalent-3`() {
        assertEquals(VaccineType.PENTA_3, "Pentavalent-3".toVaccineType())
    }

    @Test fun `toVaccineType maps PCV-Booster`() {
        assertEquals(VaccineType.PCV_BOOSTER, "PCV-Booster".toVaccineType())
    }

    @Test fun `toVaccineType trims whitespace`() {
        assertEquals(VaccineType.TD, "  Tetanus & adult Diphtheria (Td)  ".toVaccineType())
    }

    @Test fun `toVaccineType unknown string maps to UNKNOWN`() {
        assertEquals(VaccineType.UNKNOWN, "Some Random Vaccine".toVaccineType())
    }

    @Test fun `toVaccineType empty string maps to UNKNOWN`() {
        assertEquals(VaccineType.UNKNOWN, "".toVaccineType())
    }

    // =====================================================
    // ImmunizationCache.asPostModel() Tests
    // =====================================================

    private fun cache() = ImmunizationCache(
        id = 5L,
        beneficiaryId = 10L,
        vaccineId = 3,
        place = "PHC",
        byWho = "Nurse",
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED,
        mcpCardSummary1 = "s1",
        mcpCardSummary2 = "s2"
    )

    @Test fun `asPostModel maps identifiers`() {
        val post = cache().asPostModel()
        assertEquals(5L, post.id)
        assertEquals(10L, post.beneficiaryId)
        assertEquals(3, post.vaccineId)
    }

    @Test fun `asPostModel maps place and byWho`() {
        val post = cache().asPostModel()
        assertEquals("PHC", post.vaccinationreceivedat)
        assertEquals("Nurse", post.vaccinatedBy)
    }

    @Test fun `asPostModel maps createdBy and modifiedBy`() {
        val post = cache().asPostModel()
        assertEquals("creator", post.createdBy)
        assertEquals("modifier", post.modifiedBy)
    }

    @Test fun `asPostModel maps mcp card summaries`() {
        val post = cache().asPostModel()
        assertEquals("s1", post.mcpCardSummary1)
        assertEquals("s2", post.mcpCardSummary2)
    }

    @Test fun `asPostModel null date yields null receivedDate`() {
        val post = cache().copy(date = null).asPostModel()
        assertNull(post.receivedDate)
    }

    @Test fun `asPostModel non-null date yields date string`() {
        val post = cache().copy(date = 1_700_000_000_000L).asPostModel()
        // getDateStrFromLong returns yyyy-MM-dd portion
        assertEquals(10, post.receivedDate?.length)
    }

    // =====================================================
    // ImmunizationPost.toCacheModel() Tests
    // =====================================================

    @Test fun `toCacheModel maps identifiers`() {
        val post = ImmunizationPost(
            id = 7L,
            beneficiaryId = 20L,
            vaccineId = 4,
            receivedDate = "2023-01-15",
            createdBy = "c",
            modifiedBy = "m"
        )
        val cache = post.toCacheModel()
        assertEquals(7L, cache.id)
        assertEquals(20L, cache.beneficiaryId)
        assertEquals(4, cache.vaccineId)
    }

    @Test fun `toCacheModel sets processed P and synced state`() {
        val post = ImmunizationPost(beneficiaryId = 20L, vaccineId = 4, createdBy = "c", modifiedBy = "m")
        val cache = post.toCacheModel()
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `toCacheModel null place and byWho become empty strings`() {
        val post = ImmunizationPost(
            beneficiaryId = 20L, vaccineId = 4, createdBy = "c", modifiedBy = "m",
            vaccinationreceivedat = null, vaccinatedBy = null
        )
        val cache = post.toCacheModel()
        assertEquals("", cache.place)
        assertEquals("", cache.byWho)
    }

    @Test fun `toCacheModel parses received date to long`() {
        val post = ImmunizationPost(
            beneficiaryId = 20L, vaccineId = 4, createdBy = "c", modifiedBy = "m",
            receivedDate = "2023-01-15"
        )
        val cache = post.toCacheModel()
        assert(cache.date != null && cache.date!! > 0L)
    }

    @Test fun `toCacheModel null received date yields zero`() {
        val post = ImmunizationPost(beneficiaryId = 20L, vaccineId = 4, createdBy = "c", modifiedBy = "m")
        val cache = post.toCacheModel()
        assertEquals(0L, cache.date)
    }

    @Test fun `Vaccine omitting optional args falls back to defaults`() {
        val vaccine = Vaccine(
            vaccineId = 1,
            vaccineName = "BCG",
            minAllowedAgeInMillis = 0L,
            maxAllowedAgeInMillis = 1000L,
            category = ImmunizationCategory.CHILD,
            immunizationService = ChildImmunizationCategory.BIRTH
        )
        assertEquals(1000L, vaccine.overdueDurationSinceMinInMillis)
        assertNull(vaccine.dependantVaccineId)
        assertNull(vaccine.dependantCoolDuration)
    }

    @Test fun `VaccineDomain omitting optional args falls back to defaults`() {
        val domain = VaccineDomain(
            vaccineId = 1,
            vaccineName = "BCG",
            vaccineCategory = ChildImmunizationCategory.BIRTH,
            state = VaccineState.PENDING
        )
        assertEquals(false, domain.isSwitchChecked)
        assertEquals("", domain.dueDate)
    }

    @Test fun `VaccineCategoryDomain omitting optional args falls back to defaults`() {
        val domain = VaccineCategoryDomain(
            category = ChildImmunizationCategory.BIRTH,
            vaccineStateList = emptyList()
        )
        assertEquals(ChildImmunizationCategory.BIRTH.name, domain.categoryString)
        assertEquals(false, domain.isBenDeath)
    }

    @Test fun `VaccineCategoryDomain exposes category, vaccineStateList and isBenDeath setter`() {
        val vaccine = VaccineDomain(
            vaccineId = 1,
            vaccineName = "BCG",
            vaccineCategory = ChildImmunizationCategory.BIRTH,
            state = VaccineState.PENDING
        )
        val domain = VaccineCategoryDomain(
            category = ChildImmunizationCategory.BIRTH,
            vaccineStateList = listOf(vaccine)
        )
        assertEquals(ChildImmunizationCategory.BIRTH, domain.category)
        assertEquals(1, domain.vaccineStateList.size)
        domain.isBenDeath = true
        assertEquals(true, domain.isBenDeath)
    }
}
