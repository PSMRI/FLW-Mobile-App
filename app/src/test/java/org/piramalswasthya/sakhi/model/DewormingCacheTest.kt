package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Exercises [DewormingCache.toDTO], the cache -> DTO direction (the reverse
 * DTO.toCache is covered elsewhere; toDewormingCache is covered separately).
 */
class DewormingCacheTest {

    private fun cache() = DewormingCache(
        id = 12,
        dewormingDone = "Yes",
        dewormingDate = "01-01-2024",
        dewormingLocation = "School",
        ageGroup = 3,
        image1 = "img1",
        image2 = "img2",
        regDate = "01-01-2024"
    )

    @Test
    fun `toDTO maps identifiers and status`() {
        val dto = cache().toDTO()
        assertEquals(12, dto.id)
        assertEquals("Yes", dto.dewormingDone)
        assertEquals("01-01-2024", dto.dewormingDate)
        assertEquals("School", dto.dewormingLocation)
    }

    @Test
    fun `toDTO maps age group and images`() {
        val dto = cache().toDTO()
        assertEquals(3, dto.ageGroup)
        assertEquals("img1", dto.image1)
        assertEquals("img2", dto.image2)
    }

    @Test
    fun `toDTO stamps a current regDate string`() {
        val dto = cache().toDTO()
        assertNotNull(dto.regDate)
    }

    @Test
    fun `toDTO on defaults yields nulls`() {
        val dto = DewormingCache().toDTO()
        assertEquals(0, dto.id)
        assertEquals(null, dto.dewormingDone)
        assertEquals(null, dto.ageGroup)
    }
}
