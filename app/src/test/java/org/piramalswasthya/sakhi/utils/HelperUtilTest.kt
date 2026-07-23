package org.piramalswasthya.sakhi.utils

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import java.util.Calendar

class HelperUtilTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        every { context.getString(R.string.year) } returns "Year"
        every { context.getString(R.string.years) } returns "Years"
        every { context.getString(R.string.month) } returns "Month"
        every { context.getString(R.string.months) } returns "Months"
        every { context.getString(R.string.day) } returns "Day "
        every { context.getString(R.string.days) } returns "Days "
    }

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
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
        assertEquals("2 Years, 3 Months", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with 1 year singular`() {
        val age = AgeUnitDTO(years = 1, months = 0, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
        assertEquals("1 Year", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with 1 month singular`() {
        val age = AgeUnitDTO(years = 0, months = 1, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
        assertEquals("1 Month", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with days only`() {
        val age = AgeUnitDTO(years = 0, months = 0, days = 15)
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
        assertEquals("15 Days ", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with 1 day singular`() {
        val age = AgeUnitDTO(years = 0, months = 0, days = 1)
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
        assertEquals("1 Day ", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with years months and days`() {
        val age = AgeUnitDTO(years = 2, months = 3, days = 10)
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
        assertEquals("2 Years, 3 Months, 10 Days ", result)
    }

    @Test
    fun `getAgeStrFromAgeUnit with zero age`() {
        val age = AgeUnitDTO(years = 0, months = 0, days = 0)
        val result = HelperUtil.getAgeStrFromAgeUnit(context, age)
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

    // =====================================================
    // getLongFromDateStr()  (yyyy-MM-dd)
    // =====================================================

    @Test
    fun `getLongFromDateStr parses valid yyyy-MM-dd`() {
        val result = HelperUtil.getLongFromDateStr("2026-03-17")
        // Compare against the same SimpleDateFormat semantics via Calendar.
        val cal = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.MARCH, 17, 0, 0, 0)
        }
        assertEquals(cal.timeInMillis, result)
    }

    @Test
    fun `getLongFromDateStr returns 0 for null`() {
        assertEquals(0L, HelperUtil.getLongFromDateStr(null))
    }

    // =====================================================
    // getLongFromDateMDY()  (MMM dd, yyyy)
    // =====================================================

    @Test
    fun `getLongFromDateMDY parses valid MMM dd yyyy`() {
        val result = HelperUtil.getLongFromDateMDY("Mar 17, 2026")
        val cal = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.MARCH, 17, 0, 0, 0)
        }
        assertEquals(cal.timeInMillis, result)
    }

    @Test
    fun `getLongFromDateMDY returns 0 for null`() {
        assertEquals(0L, HelperUtil.getLongFromDateMDY(null))
    }

    // =====================================================
    // isValidName()
    // =====================================================

    @Test
    fun `isValidName accepts simple name`() {
        assertTrue(HelperUtil.isValidName("John"))
    }

    @Test
    fun `isValidName accepts name with space`() {
        assertTrue(HelperUtil.isValidName("John Doe"))
    }

    @Test
    fun `isValidName accepts name with apostrophe and hyphen`() {
        assertTrue(HelperUtil.isValidName("O'Brien-Smith"))
    }

    @Test
    fun `isValidName trims surrounding whitespace`() {
        assertTrue(HelperUtil.isValidName("  Jane  "))
    }

    @Test
    fun `isValidName rejects name with digits`() {
        assertFalse(HelperUtil.isValidName("John123"))
    }

    @Test
    fun `isValidName rejects empty string`() {
        assertFalse(HelperUtil.isValidName(""))
    }

    @Test
    fun `isValidName rejects single character`() {
        // regex requires at least a start letter and an end letter
        assertFalse(HelperUtil.isValidName("A"))
    }

    // =====================================================
    // getYearRange()
    // =====================================================

    @Test
    fun `getYearRange spans start and end of the year`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 15, 10, 30, 0)
        }
        val (start, end) = HelperUtil.getYearRange(cal.timeInMillis)

        assertTrue("start must precede end", start < end)

        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(2026, startCal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY))

        assertEquals(2026, endCal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, endCal.get(Calendar.HOUR_OF_DAY))
    }

    // =====================================================
    // detectExtAndMime()
    // =====================================================

    @Test
    fun `detectExtAndMime detects PDF`() {
        val bytes = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x00)
        assertEquals("pdf" to "application/pdf", HelperUtil.detectExtAndMime(bytes))
    }

    @Test
    fun `detectExtAndMime detects JPEG`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0x00)
        assertEquals("jpg" to "image/jpeg", HelperUtil.detectExtAndMime(bytes))
    }

    @Test
    fun `detectExtAndMime detects PNG`() {
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D)
        assertEquals("png" to "image/png", HelperUtil.detectExtAndMime(bytes))
    }

    @Test
    fun `detectExtAndMime returns octet-stream for unknown magic`() {
        val bytes = byteArrayOf(0x00, 0x01, 0x02, 0x03)
        assertEquals("bin" to "application/octet-stream", HelperUtil.detectExtAndMime(bytes))
    }

    @Test
    fun `detectExtAndMime returns octet-stream for short input`() {
        val bytes = byteArrayOf(0x25, 0x50)
        assertEquals("bin" to "application/octet-stream", HelperUtil.detectExtAndMime(bytes))
    }

    // =====================================================
    // convertToServerDate()  (dd-MM-yyyy -> yyyy-MM-dd)
    // =====================================================

    @Test
    fun `convertToServerDate flips local date to server order`() {
        assertEquals("2026-03-17", HelperUtil.convertToServerDate("17-03-2026"))
    }

    @Test
    fun `convertToServerDate returns null for null or blank`() {
        assertNull(HelperUtil.convertToServerDate(null))
        assertNull(HelperUtil.convertToServerDate("   "))
    }

    @Test
    fun `convertToServerDate returns input when not three parts`() {
        assertEquals("2026", HelperUtil.convertToServerDate("2026"))
    }

    // =====================================================
    // convertToLocalDate()  (yyyy-MM-dd -> dd-MM-yyyy)
    // =====================================================

    @Test
    fun `convertToLocalDate flips server date to local order`() {
        assertEquals("17-03-2026", HelperUtil.convertToLocalDate("2026-03-17"))
    }

    @Test
    fun `convertToLocalDate returns null for null or blank`() {
        assertNull(HelperUtil.convertToLocalDate(null))
        assertNull(HelperUtil.convertToLocalDate(""))
    }

    @Test
    fun `convertToLocalDate returns input when not three parts`() {
        assertEquals("2026-03", HelperUtil.convertToLocalDate("2026-03"))
    }

    // =====================================================
    // getMaleRelationId()
    // =====================================================

    @Test
    fun `getMaleRelationId maps known relations`() {
        assertEquals(8, HelperUtil.getMaleRelationId(18))
        assertEquals(19, HelperUtil.getMaleRelationId(16))
        assertEquals(12, HelperUtil.getMaleRelationId(14))
        assertEquals(11, HelperUtil.getMaleRelationId(12))
        assertEquals(16, HelperUtil.getMaleRelationId(10))
        assertEquals(1, HelperUtil.getMaleRelationId(1))
        assertEquals(4, HelperUtil.getMaleRelationId(19))
    }

    @Test
    fun `getMaleRelationId returns 19 for unknown`() {
        assertEquals(19, HelperUtil.getMaleRelationId(999))
    }

    // =====================================================
    // getFemaleRelationId()
    // =====================================================

    @Test
    fun `getFemaleRelationId maps known relations`() {
        assertEquals(17, HelperUtil.getFemaleRelationId(9))
        assertEquals(0, HelperUtil.getFemaleRelationId(2))
        assertEquals(17, HelperUtil.getFemaleRelationId(7))
        assertEquals(10, HelperUtil.getFemaleRelationId(17))
        assertEquals(19, HelperUtil.getFemaleRelationId(15))
        assertEquals(13, HelperUtil.getFemaleRelationId(13))
        assertEquals(12, HelperUtil.getFemaleRelationId(11))
        assertEquals(5, HelperUtil.getFemaleRelationId(19))
    }

    @Test
    fun `getFemaleRelationId returns 19 for unknown`() {
        assertEquals(19, HelperUtil.getFemaleRelationId(999))
    }

    // =====================================================
    // getMimeFromUri()
    // =====================================================

    private fun uriReturning(value: String): Uri {
        val uri = mockk<Uri>()
        every { uri.toString() } returns value
        return uri
    }

    @Test
    fun `getMimeFromUri detects jpeg for jpg and jpeg`() {
        assertEquals("image/jpeg", HelperUtil.getMimeFromUri(uriReturning("file:///a/photo.JPG")))
        assertEquals("image/jpeg", HelperUtil.getMimeFromUri(uriReturning("file:///a/photo.jpeg")))
    }

    @Test
    fun `getMimeFromUri detects png`() {
        assertEquals("image/png", HelperUtil.getMimeFromUri(uriReturning("content://x/img.PNG")))
    }

    @Test
    fun `getMimeFromUri detects pdf`() {
        assertEquals("application/pdf", HelperUtil.getMimeFromUri(uriReturning("file:///doc.pdf")))
    }

    @Test
    fun `getMimeFromUri falls back to octet-stream`() {
        assertEquals(
            "application/octet-stream",
            HelperUtil.getMimeFromUri(uriReturning("file:///data.bin"))
        )
    }

    // =====================================================
    // getCurrentYear()
    // =====================================================

    @Test
    fun `getCurrentYear returns four digit current year`() {
        val expected = Calendar.getInstance().get(Calendar.YEAR).toString()
        assertEquals(expected, HelperUtil.getCurrentYear())
    }

    // =====================================================
    // getMinVisitDate() / getMaxVisitDate()
    // =====================================================

    @Test
    fun `getMinVisitDate is roughly one month in the past`() {
        val result = HelperUtil.getMinVisitDate()
        val expected = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time
        val diff = Math.abs(result.time - expected.time)
        assertTrue("Should be within 5 seconds", diff < 5000)
    }

    @Test
    fun `getMaxVisitDate is roughly two months in the future`() {
        val result = HelperUtil.getMaxVisitDate()
        val expected = Calendar.getInstance().apply { add(Calendar.MONTH, 2) }.time
        val diff = Math.abs(result.time - expected.time)
        assertTrue("Should be within 5 seconds", diff < 5000)
    }

    @Test
    fun `getMinVisitDate precedes getMaxVisitDate`() {
        assertTrue(HelperUtil.getMinVisitDate().before(HelperUtil.getMaxVisitDate()))
    }

    // =====================================================
    // formatDate()  (ISO8601 UTC -> "d MMM yyyy")
    // =====================================================

    @Test
    fun `formatDate formats a valid ISO timestamp`() {
        // Noon UTC keeps the calendar date stable across common timezones.
        val result = HelperUtil.formatDate("2026-03-17T12:00:00.000Z")
        assertTrue("Should contain month abbreviation, was: $result", result.contains("Mar"))
        assertTrue("Should contain year, was: $result", result.contains("2026"))
    }

    @Test
    fun `formatDate returns empty for null`() {
        assertEquals("", HelperUtil.formatDate(null))
    }

    @Test
    fun `formatDate returns empty for blank`() {
        assertEquals("", HelperUtil.formatDate("   "))
    }

    @Test
    fun `formatDate returns empty for unparseable input`() {
        assertEquals("", HelperUtil.formatDate("not-a-real-date"))
    }

    // =====================================================
    // formatNumber()
    // =====================================================

    @Test
    fun `formatNumber returns same integer for english locale`() {
        assertEquals(1234, HelperUtil.formatNumber(1234, Languages.ENGLISH))
        assertEquals(0, HelperUtil.formatNumber(0, Languages.ENGLISH))
        assertEquals(7, HelperUtil.formatNumber(7, Languages.ENGLISH))
    }

    // =====================================================
    // Context.getBabyOrder()
    // =====================================================

    @Test
    fun `getBabyOrder returns ordinal baby strings`() {
        val context = mockk<Context>(relaxed = true)
        every { context.getString(R.string.first_baby) } returns "First Baby"
        every { context.getString(R.string.second_baby) } returns "Second Baby"
        every { context.getString(R.string.third_baby) } returns "Third Baby"
        every { context.getString(R.string.nth_baby, 5) } returns "Baby 5"

        with(HelperUtil) {
            assertEquals("First Baby", context.getBabyOrder(0))
            assertEquals("Second Baby", context.getBabyOrder(1))
            assertEquals("Third Baby", context.getBabyOrder(2))
            assertEquals("Baby 5", context.getBabyOrder(4))
        }
    }

    // =====================================================
    // getDateTimeStringFromLong()
    // =====================================================

    @Test
    fun `getDateTimeStringFromLong returns ISO timestamp`() {
        val cal = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 17, 0, 0, 0) }
        val result = HelperUtil.getDateTimeStringFromLong(cal.timeInMillis)
        assertTrue("Should contain date", result!!.startsWith("2026-03-17"))
        assertTrue("Should contain T separator", result.contains("T"))
        assertTrue("Should end with .000Z", result.endsWith(".000Z"))
    }

    @Test
    fun `getDateTimeStringFromLong returns null for null`() {
        assertNull(HelperUtil.getDateTimeStringFromLong(null))
    }

    @Test
    fun `getDateTimeStringFromLong returns null for zero`() {
        assertNull(HelperUtil.getDateTimeStringFromLong(0L))
    }

    // =====================================================
    // getTrackDate()
    // =====================================================

    @Test
    fun `getTrackDate prefixes localized track_on string`() {
        val resources = mockk<Resources>(relaxed = true)
        every { resources.getString(R.string.track_on) } returns "Tracked on "
        val cal = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 17, 0, 0, 0) }
        val result = HelperUtil.getTrackDate(cal.timeInMillis, resources)
        assertTrue("Should start with prefix, was: $result", result!!.startsWith("Tracked on "))
        assertTrue("Should contain year, was: $result", result.contains("2026"))
    }

    @Test
    fun `getTrackDate returns null for null`() {
        val resources = mockk<Resources>(relaxed = true)
        assertNull(HelperUtil.getTrackDate(null, resources))
    }

    // =====================================================
    // MutableMap.hasUploadedFile()
    // =====================================================

    @Test
    fun `hasUploadedFile true when a non-null uri present`() {
        val map: MutableMap<Int, Uri?> = mutableMapOf(1 to null, 2 to mockk<Uri>())
        with(HelperUtil) {
            assertTrue(map.hasUploadedFile())
        }
    }

    @Test
    fun `hasUploadedFile false when all null`() {
        val map: MutableMap<Int, Uri?> = mutableMapOf(1 to null, 2 to null)
        with(HelperUtil) {
            assertFalse(map.hasUploadedFile())
        }
    }

    // =====================================================
    // Long/String.toRequestBody()
    // =====================================================

    @Test
    fun `Long toRequestBody produces text plain body`() {
        with(HelperUtil) {
            val body = 42L.toRequestBody()
            assertEquals("text", body.contentType()?.type)
            assertEquals("plain", body.contentType()?.subtype)
        }
    }

    @Test
    fun `String toRequestBody produces text plain body`() {
        with(HelperUtil) {
            val body = "hello".toRequestBody()
            assertEquals("text", body.contentType()?.type)
            assertEquals("plain", body.contentType()?.subtype)
        }
    }

    // =====================================================
    // checkAndShowMUACAlert() / WeightForHeight / SAMAlert
    // (only the guard / no-dialog branches are JVM-safe)
    // =====================================================

    @Test
    fun `checkAndShowMUACAlert returns false for non-numeric`() {
        assertFalse(HelperUtil.checkAndShowMUACAlert(context, "not-a-number"))
    }

    @Test
    fun `checkAndShowMUACAlert returns false for safe value`() {
        assertFalse(HelperUtil.checkAndShowMUACAlert(context, "15.0"))
    }

    @Test
    fun `checkAndShowWeightForHeightAlert returns false for non-SAM`() {
        assertFalse(HelperUtil.checkAndShowWeightForHeightAlert(context, "NORMAL"))
    }

    @Test
    fun `checkAndShowSAMAlert muac string safe returns false`() {
        assertFalse(HelperUtil.checkAndShowSAMAlert(context, "muac", "15.0"))
    }

    @Test
    fun `checkAndShowSAMAlert muac number safe returns false`() {
        assertFalse(HelperUtil.checkAndShowSAMAlert(context, "muac", 20))
    }

    @Test
    fun `checkAndShowSAMAlert muac null returns false`() {
        assertFalse(HelperUtil.checkAndShowSAMAlert(context, "muac", null))
    }

    @Test
    fun `checkAndShowSAMAlert weight status non-SAM returns false`() {
        assertFalse(HelperUtil.checkAndShowSAMAlert(context, "weight_for_height_status", "NORMAL"))
    }

    @Test
    fun `checkAndShowSAMAlert unknown field returns false`() {
        assertFalse(HelperUtil.checkAndShowSAMAlert(context, "something_else", "value"))
    }

    // =====================================================
    // parseSelections()  (non-JSON branches only)
    // =====================================================

    @Test
    fun `parseSelections returns empty for null`() {
        assertTrue(HelperUtil.parseSelections(null, null).isEmpty())
    }

    @Test
    fun `parseSelections returns empty for blank`() {
        assertTrue(HelperUtil.parseSelections("   ", null).isEmpty())
    }

    @Test
    fun `parseSelections splits on comma`() {
        assertEquals(listOf("a", "b", "c"), HelperUtil.parseSelections("a, b, c", null))
    }

    @Test
    fun `parseSelections splits on pipe`() {
        assertEquals(listOf("x", "y"), HelperUtil.parseSelections("x|y", null))
    }

    @Test
    fun `parseSelections matches entries and sorts by position`() {
        val result = HelperUtil.parseSelections("hello world", arrayOf("world", "hello"))
        assertEquals(listOf("hello", "world"), result)
    }

    @Test
    fun `parseSelections falls back to raw single value`() {
        assertEquals(listOf("xyz"), HelperUtil.parseSelections("xyz", arrayOf("abc")))
    }

    // =====================================================
    // extractFieldValue()  (guard branch only)
    // =====================================================

    @Test
    fun `extractFieldValue returns empty for null`() {
        assertEquals("", HelperUtil.extractFieldValue(null, "key"))
    }

    @Test
    fun `extractFieldValue returns empty for blank`() {
        assertEquals("", HelperUtil.extractFieldValue("   ", "key"))
    }
}
