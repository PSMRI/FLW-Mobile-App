package org.piramalswasthya.sakhi.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CampaignDateUtilTest {

    // --- parseDateToLocalDate ---

    @Test
    fun `parse yyyy-MM-dd format`() {
        val result = CampaignDateUtil.parseDateToLocalDate("2026-03-17")
        assertNotNull(result)
        assertEquals(2026, result!!.year)
        assertEquals(3, result.monthValue)
        assertEquals(17, result.dayOfMonth)
    }

    @Test
    fun `parse dd-MM-yyyy format`() {
        val result = CampaignDateUtil.parseDateToLocalDate("17-03-2026")
        assertNotNull(result)
        assertEquals(2026, result!!.year)
        assertEquals(3, result.monthValue)
        assertEquals(17, result.dayOfMonth)
    }

    @Test
    fun `parse dd slash MM slash yyyy format`() {
        val result = CampaignDateUtil.parseDateToLocalDate("17/03/2026")
        assertNotNull(result)
        assertEquals(2026, result!!.year)
    }

    @Test
    fun `parse yyyy slash MM slash dd format`() {
        val result = CampaignDateUtil.parseDateToLocalDate("2026/03/17")
        assertNotNull(result)
        assertEquals(17, result!!.dayOfMonth)
    }

    @Test
    fun `parse invalid date returns null`() {
        assertNull(CampaignDateUtil.parseDateToLocalDate("not-a-date"))
    }

    @Test
    fun `parse empty string returns null`() {
        assertNull(CampaignDateUtil.parseDateToLocalDate(""))
    }

    @Test
    fun `parse partial date returns null`() {
        assertNull(CampaignDateUtil.parseDateToLocalDate("2026-03"))
    }

    // --- getYearFromDate ---

    @Test
    fun `getYearFromDate with yyyy-MM-dd`() {
        assertEquals(2026, CampaignDateUtil.getYearFromDate("2026-03-17"))
    }

    @Test
    fun `getYearFromDate with dd-MM-yyyy`() {
        assertEquals(2026, CampaignDateUtil.getYearFromDate("17-03-2026"))
    }

    @Test
    fun `getYearFromDate with invalid string returns 0`() {
        assertEquals(0, CampaignDateUtil.getYearFromDate("invalid"))
    }

    @Test
    fun `getYearFromDate with empty string returns 0`() {
        assertEquals(0, CampaignDateUtil.getYearFromDate(""))
    }

    // --- isMonthsCompleted ---

    @Test
    fun `isMonthsCompleted returns true when enough months passed`() {
        val sixMonthsAgo = LocalDate.now().minusMonths(6).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        assertTrue(CampaignDateUtil.isMonthsCompleted(sixMonthsAgo, 5))
    }

    @Test
    fun `isMonthsCompleted returns false when not enough months passed`() {
        val twoMonthsAgo = LocalDate.now().minusMonths(2).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        assertFalse(CampaignDateUtil.isMonthsCompleted(twoMonthsAgo, 5))
    }

    @Test
    fun `isMonthsCompleted returns true on exact boundary`() {
        val exactMonthsAgo = LocalDate.now().minusMonths(3).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        assertTrue(CampaignDateUtil.isMonthsCompleted(exactMonthsAgo, 3))
    }

    @Test
    fun `isMonthsCompleted returns false for future date`() {
        val futureDate = LocalDate.now().plusMonths(3).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        assertFalse(CampaignDateUtil.isMonthsCompleted(futureDate, 1))
    }

    @Test
    fun `isMonthsCompleted with invalid date returns false`() {
        assertFalse(CampaignDateUtil.isMonthsCompleted("not-a-date", 3))
    }

    @Test
    fun `isMonthsCompleted with zero months and past date returns true`() {
        val yesterday = LocalDate.now().minusDays(1).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        assertTrue(CampaignDateUtil.isMonthsCompleted(yesterday, 0))
    }

    @Test
    fun `isMonthsCompleted with dd-MM-yyyy format works`() {
        val sixMonthsAgo = LocalDate.now().minusMonths(6).format(
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )
        assertTrue(CampaignDateUtil.isMonthsCompleted(sixMonthsAgo, 5))
    }
}
