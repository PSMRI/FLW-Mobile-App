package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MonthlyRecapCacheTest {

    private fun cache(
        status: String = RecapStatus.NOT_STARTED.name,
        language: String? = null,
        progressScene: Int = 0,
        totalScenes: Int? = null,
    ) = MonthlyRecapCache(
        id = 1,
        userId = 7,
        recapYearMonth = 202606,
        windowStartMillis = 0,
        windowEndMillis = 1,
        status = status,
        language = language,
        variantSeed = 42L,
        progressScene = progressScene,
        totalScenes = totalScenes,
        createdAt = 0,
        updatedAt = 0,
    )

    @Test
    fun `corrupted status falls back to NOT_STARTED`() {
        assertEquals(RecapStatus.NOT_STARTED, cache(status = "garbage").recapStatus())
        assertEquals(RecapStatus.NOT_STARTED, RecapStatus.fromToken(null))
        assertEquals(RecapStatus.IN_PROGRESS, cache(status = "IN_PROGRESS").recapStatus())
        assertEquals(RecapStatus.COMPLETED, cache(status = "COMPLETED").recapStatus())
    }

    @Test
    fun `language tokens round-trip and unknown is null`() {
        assertEquals(MonthlyRecapLanguage.HINDI, cache(language = "HI").recapLanguage())
        assertEquals(MonthlyRecapLanguage.ASSAMESE, cache(language = "AS").recapLanguage())
        assertNull(cache(language = null).recapLanguage())
        assertNull(cache(language = "EN").recapLanguage())
    }

    @Test
    fun `corrupted progress is coerced into safe bounds`() {
        assertEquals(0, cache(progressScene = -4).safeProgressScene())
        assertEquals(3, cache(progressScene = 3, totalScenes = 10).safeProgressScene())
        assertEquals(10, cache(progressScene = 99, totalScenes = 10).safeProgressScene())
        // Unknown total: only the lower bound applies.
        assertEquals(99, cache(progressScene = 99, totalScenes = null).safeProgressScene())
    }
}
