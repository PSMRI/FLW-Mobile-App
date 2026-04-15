package org.piramalswasthya.sakhi.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import java.util.Calendar

class HelperUtilTest {

    // =====================================================
    // getDobFromAge() Tests
    // =====================================================

    @Test
    fun `getDobFromAge for 25 years returns approximately 25 years ago`() {
        val ageUnit = AgeUnitDTO(years = 25, months = 0, days = 0)
        val result = HelperUtil.getDobFromAge(ageUnit)

        val expectedCal = Calendar.getInstance()
        expectedCal.add(Calendar.YEAR, -25)

        // Allow 1 second tolerance
        val diffMs = Math.abs(result - expectedCal.timeInMillis)
        assertTrue("Should be within 1 second", diffMs < 1000)
    }

    @Test
    fun `getDobFromAge for 0 years 6 months returns approximately 6 months ago`() {
        val ageUnit = AgeUnitDTO(years = 0, months = 6, days = 0)
        val result = HelperUtil.getDobFromAge(ageUnit)

        val expectedCal = Calendar.getInstance()
        expectedCal.add(Calendar.MONTH, -6)

        val diffMs = Math.abs(result - expectedCal.timeInMillis)
        assertTrue("Should be within 1 second", diffMs < 1000)
    }

    @Test
    fun `getDobFromAge for 0 years 0 months 15 days`() {
        val ageUnit = AgeUnitDTO(years = 0, months = 0, days = 15)
        val result = HelperUtil.getDobFromAge(ageUnit)

        val expectedCal = Calendar.getInstance()
        expectedCal.add(Calendar.DAY_OF_MONTH, -15)

        val diffMs = Math.abs(result - expectedCal.timeInMillis)
        assertTrue("Should be within 1 second", diffMs < 1000)
    }

    @Test
    fun `getDobFromAge for 2 years 3 months 10 days`() {
        val ageUnit = AgeUnitDTO(years = 2, months = 3, days = 10)
        val result = HelperUtil.getDobFromAge(ageUnit)

        val expectedCal = Calendar.getInstance()
        expectedCal.add(Calendar.YEAR, -2)
        expectedCal.add(Calendar.MONTH, -3)
        expectedCal.add(Calendar.DAY_OF_MONTH, -10)

        val diffMs = Math.abs(result - expectedCal.timeInMillis)
        assertTrue("Should be within 1 second", diffMs < 1000)
    }

    // =====================================================
    // getAgeStrFromAgeUnit() Tests
    // =====================================================

    @Test
    fun `getAgeStrFromAgeUnit with years and months`() {
        val age = AgeUnitDTO(years = 2, months = 3, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("2 Years, 3 Months", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with 1 year singular`() {
        val age = AgeUnitDTO(years = 1, months = 0, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("1 Year", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with 1 month singular`() {
        val age = AgeUnitDTO(years = 0, months = 1, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("1 Month", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with days only`() {
        val age = AgeUnitDTO(years = 0, months = 0, days = 15)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("15 Days ", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with 1 day singular`() {
        val age = AgeUnitDTO(years = 0, months = 0, days = 1)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("1 Day ", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with years months and days`() {
        val age = AgeUnitDTO(years = 2, months = 3, days = 10)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("2 Years, 3 Months, 10 Days ", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with zero age`() {
        val age = AgeUnitDTO(years = 0, months = 0, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(age)
        assertEquals("", result)
    }

    // =====================================================
    // getDiffYears() Tests
    // =====================================================

    @Test
    fun `getDiffYears for 26 year gap`() {
        val a = Calendar.getInstance().apply { set(2000, Calendar.JANUARY, 1) }
        val b = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 17) }
        assertEquals(26, HelperUtil.getDiffYears(a, b))
    }

    @Test
    fun `getDiffYears same year returns 0`() {
        val a = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }
        val b = Calendar.getInstance().apply { set(2026, Calendar.DECEMBER, 31) }
        assertEquals(0, HelperUtil.getDiffYears(a, b))
    }

    @Test
    fun `getDiffYears birthday not yet passed this year decrements`() {
        val a = Calendar.getInstance().apply { set(2000, Calendar.DECEMBER, 31) }
        val b = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }
        // December 31 hasn't been reached in January, so age decrements
        assertEquals(25, HelperUtil.getDiffYears(a, b))
    }

    // =====================================================
    // updateAgeDTO() Tests
    // =====================================================

    @Test
    fun `updateAgeDTO calculates age from birth calendar`() {
        val ageDTO = AgeUnitDTO(0, 0, 0)
        val birthCal = Calendar.getInstance().apply {
            add(Calendar.YEAR, -5)
            add(Calendar.MONTH, -3)
        }

        HelperUtil.updateAgeDTO(ageDTO, birthCal)

        assertEquals(5, ageDTO.years)
        assertEquals(3, ageDTO.months)
    }

    @Test
    fun `updateAgeDTO for newborn`() {
        val ageDTO = AgeUnitDTO(0, 0, 0)
        val birthCal = Calendar.getInstance() // Born today

        HelperUtil.updateAgeDTO(ageDTO, birthCal)

        assertEquals(0, ageDTO.years)
        assertEquals(0, ageDTO.months)
        assertEquals(0, ageDTO.days)
    }

    // =====================================================
    // getCurrentDate() Tests
    // =====================================================

    @Test
    fun `getCurrentDate returns ISO format`() {
        val result = HelperUtil.getCurrentDate()
        // Format: yyyy-MM-ddTHH:mm:ss.000Z
        assertTrue("Should contain T separator", result.contains("T"))
        assertTrue("Should end with .000Z", result.endsWith(".000Z"))
        assertTrue("Date part should be 10 chars", result.indexOf("T") == 10)
    }

    @Test
    fun `getCurrentDate with specific millis returns correct date`() {
        // 2026-03-17T00:00:00.000Z in UTC
        val millis = 1773964800000L
        val result = HelperUtil.getCurrentDate(millis)
        assertTrue("Should contain date part", result.startsWith("2026-03"))
    }

    // =====================================================
    // getDateStrFromLong() Tests
    // =====================================================

    @Test
    fun `getDateStrFromLong returns formatted date`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 17, 0, 0, 0)
        }
        val result = HelperUtil.getDateStrFromLong(cal.timeInMillis)
        assertEquals("2026-03-17", result)
    }

    @Test
    fun `getDateStrFromLong returns null for null input`() {
        assertNull(HelperUtil.getDateStrFromLong(null))
    }

    @Test
    fun `getDateStrFromLong returns null for zero`() {
        assertNull(HelperUtil.getDateStrFromLong(0L))
    }

    // =====================================================
    // parseDateToMillis() Tests
    // =====================================================

    @Test
    fun `parseDateToMillis parses valid date`() {
        val result = HelperUtil.parseDateToMillis("17-03-2026")
        assertTrue("Should return positive millis", result > 0)
    }

    @Test
    fun `parseDateToMillis returns 0 for invalid date`() {
        assertEquals(0L, HelperUtil.parseDateToMillis("invalid"))
    }

    @Test
    fun `parseDateToMillis returns 0 for empty string`() {
        assertEquals(0L, HelperUtil.parseDateToMillis(""))
    }

    @Test
    fun `parseDateToMillis returns 0 for wrong format`() {
        // yyyy-MM-dd format should not parse as dd-MM-yyyy
        assertEquals(0L, HelperUtil.parseDateToMillis("2026-03-17"))
    }

    // =====================================================
    // getDateStringFromLong() Tests
    // =====================================================

    @Test
    fun `getDateStringFromLong returns formatted date`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 17, 0, 0, 0)
        }
        val result = HelperUtil.getDateStringFromLong(cal.timeInMillis)
        assertEquals("2026-03-17", result)
    }

    @Test
    fun `getDateStringFromLong returns null for null`() {
        assertNull(HelperUtil.getDateStringFromLong(null))
    }

    // =====================================================
    // getDateStringFromLongStraight() Tests
    // =====================================================

    @Test
    fun `getDateStringFromLongStraight returns dd-MM-yyyy format`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 17, 0, 0, 0)
        }
        val result = HelperUtil.getDateStringFromLongStraight(cal.timeInMillis)
        assertEquals("17-03-2026", result)
    }

    @Test
    fun `getDateStringFromLongStraight returns null for null`() {
        assertNull(HelperUtil.getDateStringFromLongStraight(null))
    }

    // =====================================================
    // getLongFromDate() Tests
    // =====================================================

    @Test
    fun `getLongFromDate parses dd-MM-yyyy`() {
        val result = HelperUtil.getLongFromDate("17-03-2026")
        assertTrue("Should return positive millis", result > 0)
    }

    @Test
    fun `getLongFromDate throws for invalid date`() {
        try {
            HelperUtil.getLongFromDate("not-a-date")
            assertTrue("Should have thrown", false)
        } catch (e: Exception) {
            // Expected
        }
    }
}
