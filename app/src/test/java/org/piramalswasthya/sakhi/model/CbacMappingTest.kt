package org.piramalswasthya.sakhi.model

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class CbacMappingTest {

    private fun resources(): Resources {
        val res = mockk<Resources>(relaxed = true)
        every { res.getStringArray(any()) } returns arrayOf("o0", "o1", "o2", "o3", "o4")
        every { res.getString(any()) } returns "label"
        return res
    }

    private fun baseCache() = CbacCache(
        benId = 1L,
        ashaId = 1,
        syncState = SyncState.UNSYNCED
    )

    // ---------------- String?.toMillisOrNull ----------------

    @Test
    fun `toMillisOrNull returns null for null and blank`() {
        assertNull((null as String?).toMillisOrNull())
        assertNull("   ".toMillisOrNull())
    }

    @Test
    fun `toMillisOrNull returns null for unparseable input`() {
        assertNull("not a date".toMillisOrNull())
    }

    @Test
    fun `toMillisOrNull parses a valid default-pattern date`() {
        assertNotNull("Jan 01, 2026, 10:30:00 AM".toMillisOrNull())
    }

    @Test
    fun `toMillisOrNull parses with a custom pattern`() {
        assertNotNull("2026-01-01".toMillisOrNull("yyyy-MM-dd"))
    }

    // ---------------- CbacCache.asDomainModel ----------------

    @Test
    fun `asDomainModel carries id and syncState and builds a date label`() {
        val cache = baseCache().copy(id = 9, fillDate = 1_700_000_000_000L)

        val domain = cache.asDomainModel(resources())

        assertEquals(9, domain.cbacId)
        assertEquals(SyncState.UNSYNCED, domain.syncState)
        assertNotNull(domain.date)
    }

    // ---------------- CbacCache.asPostModel ----------------

    @Test
    fun `asPostModel maps male waist and yes answers for position one`() {
        val cache = baseCache().apply {
            cbac_age_posi = 1
            cbac_smoke_posi = 1
            cbac_alcohol_posi = 1
            cbac_waist_posi = 1
            cbac_pa_posi = 1
            cbac_familyhistory_posi = 1
            cbac_fuel_used_posi = 1
            cbac_occupational_exposure_posi = 1
            cbac_little_interest_posi = 1
            cbac_feeling_down_posi = 1
            // yes/no fields at position 1 => "Yes"
            cbac_sortnesofbirth_pos = 1
            cbac_coughing_pos = 1
            total_score = 7
        }

        val post = cache.asPostModel(hhId = 5L, benGender = Gender.MALE, resources = resources())

        assertEquals("Yes", post.cbacShortnessBreath)
        assertEquals("Yes", post.cbacCough2weeks)
        assertEquals(1, post.cbacAgeScore)
        assertEquals(7, post.totalScore)
        // Male branch populated, female branch null.
        assertNotNull(post.cbacWaistMale)
        assertNull(post.cbacWaistFemale)
    }

    @Test
    fun `asPostModel maps female waist and no answers for position two`() {
        val cache = baseCache().apply {
            cbac_age_posi = 2
            cbac_smoke_posi = 2
            cbac_alcohol_posi = 2
            cbac_waist_posi = 2
            cbac_pa_posi = 2
            cbac_familyhistory_posi = 2
            cbac_sortnesofbirth_pos = 2
            cbac_coughing_pos = 2
        }

        val post = cache.asPostModel(hhId = 5L, benGender = Gender.FEMALE, resources = resources())

        assertEquals("No", post.cbacShortnessBreath)
        assertEquals("No", post.cbacCough2weeks)
        assertNotNull(post.cbacWaistFemale)
        assertNull(post.cbacWaistMale)
    }
}
