package org.piramalswasthya.sakhi.utils

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.TypedValue
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.getTodayMillis
import org.piramalswasthya.sakhi.model.AgeUnitDTO
import org.piramalswasthya.sakhi.model.BenWithAncVisitCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    @After
    fun tearDown() {
        unmockkAll()
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

    // =====================================================
    // parseSelections()  (JSON array branches)
    // =====================================================

    @Test
    fun `parseSelections parses a JSON array of strings`() {
        val result = HelperUtil.parseSelections("[\"Fever\",\"Cough\"]", null)
        assertEquals(listOf("Fever", "Cough"), result)
    }

    @Test
    fun `parseSelections skips blank entries inside a JSON array`() {
        val result = HelperUtil.parseSelections("[\"Fever\",\"\",\"  \",\"Cough\"]", null)
        assertEquals(listOf("Fever", "Cough"), result)
    }

    @Test
    fun `parseSelections falls through when the JSON array is empty`() {
        // "[]" parses fine but yields no items, so entry-matching takes over
        // and nothing matches -> the raw value is returned as a single item.
        assertEquals(listOf("[]"), HelperUtil.parseSelections("[]", arrayOf("abc")))
    }

    @Test
    fun `parseSelections swallows malformed JSON and falls back to raw`() {
        val raw = "[ {\"a\" ]"
        assertEquals(listOf(raw), HelperUtil.parseSelections(raw, arrayOf("zzz")))
    }

    @Test
    fun `parseSelections prefers comma split over entry matching`() {
        val result = HelperUtil.parseSelections("Fever, , Cough", arrayOf("Fever", "Cough"))
        assertEquals(listOf("Fever", "Cough"), result)
    }

    // =====================================================
    // extractFieldValue()  (JSON branches)
    // =====================================================

    @Test
    fun `extractFieldValue reads a value out of the fields object`() {
        assertEquals(
            "42",
            HelperUtil.extractFieldValue("{\"fields\":{\"weight\":\"42\"}}", "weight")
        )
    }

    @Test
    fun `extractFieldValue returns empty when the fields object is absent`() {
        assertEquals("", HelperUtil.extractFieldValue("{\"other\":1}", "weight"))
    }

    @Test
    fun `extractFieldValue returns empty when the key is absent`() {
        assertEquals("", HelperUtil.extractFieldValue("{\"fields\":{\"height\":\"10\"}}", "weight"))
    }

    @Test
    fun `extractFieldValue returns empty for malformed json`() {
        assertEquals("", HelperUtil.extractFieldValue("not-json-at-all", "weight"))
    }

    // =====================================================
    // checkAndShowSAMAlert() remaining guard
    // =====================================================

    @Test
    fun `checkAndShowSAMAlert weight status null returns false`() {
        assertFalse(HelperUtil.checkAndShowSAMAlert(context, "weight_for_height_status", null))
    }

    // =====================================================
    // Context.findFragmentActivity()
    // =====================================================

    @Test
    fun `findFragmentActivity returns null for a plain context`() {
        val plain = mockk<Context>()
        with(HelperUtil) {
            assertNull(plain.findFragmentActivity())
        }
    }

    @Test
    fun `findFragmentActivity walks the wrapper chain and gives up`() {
        val plain = mockk<Context>()
        val inner = mockk<ContextWrapper>()
        val outer = mockk<ContextWrapper>()
        every { inner.baseContext } returns plain
        every { outer.baseContext } returns inner
        with(HelperUtil) {
            assertNull(outer.findFragmentActivity())
        }
    }

    // =====================================================
    // Context.getLocalizedDewormingLocation()
    // =====================================================

    private fun contextWithArray(arrayResId: Int, values: Array<String>): Context {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getStringArray(arrayResId) } returns values
        return ctx
    }

    @Test
    fun `getLocalizedDewormingLocation maps english values to the localized array`() {
        val ctx = contextWithArray(
            R.array.deworming_location_options,
            arrayOf("L-School", "L-Anganwadi", "L-Community", "L-HomeVisit")
        )
        with(HelperUtil) {
            assertEquals("L-School", ctx.getLocalizedDewormingLocation("School"))
            assertEquals("L-Anganwadi", ctx.getLocalizedDewormingLocation("Anganwadi Centre"))
            assertEquals("L-Community", ctx.getLocalizedDewormingLocation("Community center"))
            assertEquals("L-HomeVisit", ctx.getLocalizedDewormingLocation("Home Visit"))
        }
    }

    @Test
    fun `getLocalizedDewormingLocation returns N-A for null and blank`() {
        val ctx = contextWithArray(R.array.deworming_location_options, arrayOf("L-School"))
        with(HelperUtil) {
            assertEquals("N/A", ctx.getLocalizedDewormingLocation(null))
            assertEquals("N/A", ctx.getLocalizedDewormingLocation("   "))
        }
    }

    @Test
    fun `getLocalizedDewormingLocation returns the raw value for an unknown option`() {
        val ctx = contextWithArray(
            R.array.deworming_location_options,
            arrayOf("L-School", "L-Anganwadi", "L-Community", "L-HomeVisit")
        )
        with(HelperUtil) {
            assertEquals("Clinic", ctx.getLocalizedDewormingLocation("Clinic"))
        }
    }

    @Test
    fun `getLocalizedDewormingLocation returns the raw value when the localized array is short`() {
        // "Home Visit" is index 3 but the localized array only has 2 entries
        val ctx = contextWithArray(
            R.array.deworming_location_options,
            arrayOf("L-School", "L-Anganwadi")
        )
        with(HelperUtil) {
            assertEquals("Home Visit", ctx.getLocalizedDewormingLocation("Home Visit"))
        }
    }

    // =====================================================
    // Context.getahd()
    // =====================================================

    @Test
    fun `getahd maps english values to the localized ahd array`() {
        val ctx = contextWithArray(
            R.array.ahd_place_options,
            arrayOf("A-School", "A-Anganwadi", "A-Community")
        )
        with(HelperUtil) {
            assertEquals("A-School", ctx.getahd("School"))
            assertEquals("A-Anganwadi", ctx.getahd("Anganwadi Centre"))
            assertEquals("A-Community", ctx.getahd("Community center"))
            assertEquals("HWC", ctx.getahd("HWC"))
            assertEquals("N/A", ctx.getahd(null))
            assertEquals("N/A", ctx.getahd(""))
        }
    }

    // =====================================================
    // Context.getVHND()
    // =====================================================

    @Test
    fun `getVHND maps english values to the localized vhsnc array`() {
        val ctx = contextWithArray(
            R.array.place_of_vhsnc,
            arrayOf("V-Anganwadi", "V-HWC", "V-School", "V-Community")
        )
        with(HelperUtil) {
            assertEquals("V-Anganwadi", ctx.getVHND("Anganwadi Centre"))
            assertEquals("V-HWC", ctx.getVHND("HWC"))
            assertEquals("V-School", ctx.getVHND("School"))
            assertEquals("V-Community", ctx.getVHND("Community center"))
            assertEquals("Sub Centre", ctx.getVHND("Sub Centre"))
            assertEquals("N/A", ctx.getVHND(null))
            assertEquals("N/A", ctx.getVHND("  "))
        }
    }

    // =====================================================
    // Context.getSaasBahuSamalonLocalization()
    // =====================================================

    @Test
    fun `getSaasBahuSamalonLocalization maps english values to the place array`() {
        val ctx = contextWithArray(
            R.array.place_array,
            arrayOf("S-HWC", "S-Anganwadi", "S-Community")
        )
        with(HelperUtil) {
            assertEquals("S-HWC", ctx.getSaasBahuSamalonLocalization("HWC"))
            assertEquals("S-Anganwadi", ctx.getSaasBahuSamalonLocalization("Anganwadi Centre"))
            assertEquals("S-Community", ctx.getSaasBahuSamalonLocalization("Community center"))
            assertEquals("School", ctx.getSaasBahuSamalonLocalization("School"))
            assertEquals("N/A", ctx.getSaasBahuSamalonLocalization(null))
            assertEquals("N/A", ctx.getSaasBahuSamalonLocalization("   "))
        }
    }

    // =====================================================
    // Context.getUWINLocalization()
    // =====================================================

    @Test
    fun `getUWINLocalization maps english values to the place of delivery array`() {
        val ctx = contextWithArray(
            R.array.place_of_delivery_options,
            arrayOf("U-HWC", "U-School", "U-Anganwadi", "U-Community")
        )
        with(HelperUtil) {
            assertEquals("U-HWC", ctx.getUWINLocalization("HWC"))
            assertEquals("U-School", ctx.getUWINLocalization("School"))
            assertEquals("U-Anganwadi", ctx.getUWINLocalization("Anganwadi Centre"))
            assertEquals("U-Community", ctx.getUWINLocalization("Community center"))
            assertEquals("Home", ctx.getUWINLocalization("Home"))
            assertEquals("N/A", ctx.getUWINLocalization(null))
            assertEquals("N/A", ctx.getUWINLocalization("   "))
        }
    }

    // =====================================================
    // Downloads log writers (error path only - no real file IO)
    // =====================================================

    private fun stubUnavailableDownloadsDir() {
        mockkStatic(Environment::class)
        mockkStatic(android.util.Log::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns
                File("flw_missing_downloads_dir_for_tests")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
    }

    @Test
    fun `saveApiResponseToDownloads logs an error when the file cannot be written`() {
        stubUnavailableDownloadsDir()
        HelperUtil.saveApiResponseToDownloads(context, "api_response.txt", "payload")
        verify { android.util.Log.e("SAVE_FILE", any()) }
    }

    @Test
    fun `deliveryOutcomeDBLogMethod logs an error when the file cannot be written`() {
        stubUnavailableDownloadsDir()
        HelperUtil.deliveryOutcomeDBLogMethod(context, "db_log.txt", "payload")
        verify { android.util.Log.e("SAVE_FILE", any()) }
    }

    @Test
    fun `deliveryOutcomeUpdatePNCWorkerMethod logs an error when the file cannot be written`() {
        stubUnavailableDownloadsDir()
        HelperUtil.deliveryOutcomeUpdatePNCWorkerMethod(context, "pnc_worker.txt", "payload")
        verify { android.util.Log.e("SAVE_FILE", any()) }
    }

    @Test
    fun `deliveryOutcomeRepoMethod logs an error when the file cannot be written`() {
        stubUnavailableDownloadsDir()
        HelperUtil.deliveryOutcomeRepoMethod(context, "repo.txt", "payload")
        verify { android.util.Log.e("SAVE_FILE", any()) }
    }

    @Test
    fun `log StringBuilders start out empty`() {
        assertEquals("", HelperUtil.allPagesContent.toString())
        assertEquals("", HelperUtil.deliveryOutcomeDBLog.toString())
        assertEquals("", HelperUtil.deliveryOutcomeUpdatePNCWorker.toString())
        assertEquals("", HelperUtil.deliveryOutcomeRepo.toString())
    }

    // =====================================================
    // Toast helpers
    // =====================================================

    private fun stubToast(): Toast {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast
        return toast
    }

    @Test
    fun `showToast shows a long toast with the given message`() {
        val toast = stubToast()
        with(HelperUtil) {
            context.showToast("Saved")
        }
        verify { Toast.makeText(context, "Saved", Toast.LENGTH_LONG) }
        verify { toast.show() }
    }

    @Test
    fun `showImageLoadedMessage shows the image uploaded string`() {
        val toast = stubToast()
        every { context.resources.getString(R.string.image_uploaded) } returns "Image uploaded"
        HelperUtil.showImageLoadedMessage(context)
        verify { Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT) }
        verify { toast.show() }
    }

    @Test
    fun `showFileLoadedMessage shows the file uploaded string`() {
        val toast = stubToast()
        every { context.resources.getString(R.string.file_uploaded) } returns "File uploaded"
        HelperUtil.showFileLoadedMessage(context)
        verify { Toast.makeText(context, "File uploaded", Toast.LENGTH_SHORT) }
        verify { toast.show() }
    }

    // =====================================================
    // getFileName() / getFilesName()
    // =====================================================

    private fun contentUri(): Uri {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "content"
        return uri
    }

    private fun contextWithCursor(uri: Uri, cursor: Cursor?): Context {
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.query(uri, any(), any(), any(), any()) } returns cursor
        return ctx
    }

    private fun displayNameCursor(name: String?, columnIndex: Int = 0, moveToFirst: Boolean = true): Cursor {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns columnIndex
        every { cursor.moveToFirst() } returns moveToFirst
        every { cursor.getString(columnIndex) } returns name
        return cursor
    }

    @Test
    fun `getFileName reads the display name from a content uri`() {
        val uri = contentUri()
        val ctx = contextWithCursor(uri, displayNameCursor("report.pdf"))
        assertEquals("report.pdf", HelperUtil.getFileName(uri, ctx))
    }

    @Test
    fun `getFileName returns null when the display name column is missing`() {
        val uri = contentUri()
        val ctx = contextWithCursor(uri, displayNameCursor("report.pdf", columnIndex = -1))
        assertNull(HelperUtil.getFileName(uri, ctx))
    }

    @Test
    fun `getFileName returns null when the cursor is empty`() {
        val uri = contentUri()
        val ctx = contextWithCursor(uri, displayNameCursor("report.pdf", moveToFirst = false))
        assertNull(HelperUtil.getFileName(uri, ctx))
    }

    @Test
    fun `getFileName returns null when the resolver has no cursor`() {
        val uri = contentUri()
        val ctx = contextWithCursor(uri, null)
        assertNull(HelperUtil.getFileName(uri, ctx))
    }

    @Test
    fun `getFileName falls back to the last path segment for a file uri`() {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns "/storage/emulated/0/Download/scan.png"
        val ctx = mockk<Context>(relaxed = true)
        assertEquals("scan.png", HelperUtil.getFileName(uri, ctx))
    }

    @Test
    fun `getFileName returns null for a file uri without a path`() {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns null
        val ctx = mockk<Context>(relaxed = true)
        assertNull(HelperUtil.getFileName(uri, ctx))
    }

    @Test
    fun `getFilesName reads the display name from a content uri`() {
        val uri = contentUri()
        val ctx = contextWithCursor(uri, displayNameCursor("prescription.jpg"))
        assertEquals("prescription.jpg", HelperUtil.getFilesName(uri, ctx))
    }

    @Test
    fun `getFilesName falls back to the path tail when the cursor has no column`() {
        val uri = contentUri()
        every { uri.path } returns "/documents/abc/photo.jpg"
        val ctx = contextWithCursor(uri, displayNameCursor(null, columnIndex = -1))
        assertEquals("photo.jpg", HelperUtil.getFilesName(uri, ctx))
    }

    @Test
    fun `getFilesName strips directories from a non content uri path`() {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns "/a/b/c/report.pdf"
        val ctx = mockk<Context>(relaxed = true)
        assertEquals("report.pdf", HelperUtil.getFilesName(uri, ctx))
    }

    @Test
    fun `getFilesName returns the whole path when there is no separator`() {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns "report.pdf"
        val ctx = mockk<Context>(relaxed = true)
        assertEquals("report.pdf", HelperUtil.getFilesName(uri, ctx))
    }

    @Test
    fun `getFilesName returns null when the uri has no path at all`() {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns null
        val ctx = mockk<Context>(relaxed = true)
        assertNull(HelperUtil.getFilesName(uri, ctx))
    }

    // =====================================================
    // Context.getFileSizeInMB()
    // =====================================================

    private fun contextWithFileDescriptor(uri: Uri, sizeInBytes: Long): Context {
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        val pfd = mockk<ParcelFileDescriptor>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openFileDescriptor(uri, "r") } returns pfd
        every { pfd.statSize } returns sizeInBytes
        return ctx
    }

    @Test
    fun `getFileSizeInMB converts bytes to megabytes`() {
        val uri = mockk<Uri>(relaxed = true)
        val ctx = contextWithFileDescriptor(uri, 2L * 1024 * 1024)
        with(HelperUtil) {
            assertEquals(2.0, ctx.getFileSizeInMB(uri)!!, 0.000001)
        }
    }

    @Test
    fun `getFileSizeInMB handles a fractional size`() {
        val uri = mockk<Uri>(relaxed = true)
        val ctx = contextWithFileDescriptor(uri, 512L * 1024)
        with(HelperUtil) {
            assertEquals(0.5, ctx.getFileSizeInMB(uri)!!, 0.000001)
        }
    }

    @Test
    fun `getFileSizeInMB returns null for an empty file`() {
        val uri = mockk<Uri>(relaxed = true)
        val ctx = contextWithFileDescriptor(uri, 0L)
        with(HelperUtil) {
            assertNull(ctx.getFileSizeInMB(uri))
        }
    }

    @Test
    fun `getFileSizeInMB returns null when the descriptor cannot be opened`() {
        val uri = mockk<Uri>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openFileDescriptor(uri, "r") } throws SecurityException("denied")
        with(HelperUtil) {
            assertNull(ctx.getFileSizeInMB(uri))
        }
    }

    // =====================================================
    // copyToTemp() / compressImageToTemp() / base64ToTempFile()
    // failure paths only - no real file IO
    // =====================================================

    private val missingCacheDir = File("flw_missing_cache_dir_for_tests")

    @Test
    fun `copyToTemp returns null when the temp file cannot be created`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns missingCacheDir
        assertNull(HelperUtil.copyToTemp(mockk(relaxed = true), "doc.pdf", ctx))
    }

    @Test
    fun `copyToTemp returns null when the name hint has no extension`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns missingCacheDir
        assertNull(HelperUtil.copyToTemp(mockk(relaxed = true), "noextension", ctx))
    }

    @Test
    fun `compressImageToTemp returns null when the image cannot be decoded`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns missingCacheDir
        assertNull(HelperUtil.compressImageToTemp(mockk(relaxed = true), "img.jpg", ctx))
    }

    @Test
    fun `base64ToTempFile returns null when the payload cannot be decoded`() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } throws IllegalArgumentException("bad base64")
        val ctx = mockk<Context>(relaxed = true)
        assertNull(HelperUtil.base64ToTempFile("###", missingCacheDir, ctx))
    }

    // =====================================================
    // isAncDue()
    // =====================================================

    private fun ancRecord(
        visitNumber: Int,
        ancDate: Long,
        maternalDeath: Boolean? = false,
        delivered: Boolean? = false
    ): PregnantWomanAncCache {
        val anc = mockk<PregnantWomanAncCache>(relaxed = true)
        every { anc.visitNumber } returns visitNumber
        every { anc.ancDate } returns ancDate
        every { anc.maternalDeath } returns maternalDeath
        every { anc.pregnantWomanDelivered } returns delivered
        return anc
    }

    private fun pwrRecord(active: Boolean, lmpDate: Long): PregnantWomanRegistrationCache {
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { pwr.active } returns active
        every { pwr.lmpDate } returns lmpDate
        return pwr
    }

    private fun benWithAnc(
        pwr: List<PregnantWomanRegistrationCache>,
        ancRecords: List<PregnantWomanAncCache>
    ): BenWithAncVisitCache {
        val ben = mockk<BenWithAncVisitCache>(relaxed = true)
        every { ben.pwr } returns pwr
        every { ben.savedAncRecords } returns ancRecords
        return ben
    }

    private fun daysAgo(days: Long) = getTodayMillis() - TimeUnit.DAYS.toMillis(days)

    @Test
    fun `isAncDue is false after a maternal death`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(100))),
            ancRecords = listOf(ancRecord(1, daysAgo(60), maternalDeath = true))
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is false once the woman has delivered`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(100))),
            ancRecords = listOf(ancRecord(1, daysAgo(60), delivered = true))
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is false when there is no active pregnancy record`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = false, lmpDate = daysAgo(100))),
            ancRecords = emptyList()
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is true for a first visit once the minimum weeks have passed`() {
        // minAnc1Week is 5 weeks == 35 days; 60 days is past that
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(60))),
            ancRecords = emptyList()
        )
        assertTrue(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is false for a first visit before the minimum weeks`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(10))),
            ancRecords = emptyList()
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is true when the last visit is more than 28 days old`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(100))),
            ancRecords = listOf(ancRecord(1, daysAgo(60)), ancRecord(2, daysAgo(60)))
        )
        assertTrue(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is false after the fourth visit`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(100))),
            ancRecords = listOf(ancRecord(4, daysAgo(60)))
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is false when the last visit is recent`() {
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(100))),
            ancRecords = listOf(ancRecord(1, daysAgo(5)))
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    @Test
    fun `isAncDue is false once the pregnancy window has closed`() {
        // lmp + 280 days is already behind lastAncDate + 28 days
        val ben = benWithAnc(
            pwr = listOf(pwrRecord(active = true, lmpDate = daysAgo(400))),
            ancRecords = listOf(ancRecord(1, daysAgo(60)))
        )
        assertFalse(HelperUtil.isAncDue(ben))
    }

    // =====================================================
    // updateAgeDTO() - day/month borrow branches
    // =====================================================

    private fun expectedAge(calNow: Calendar, birthCal: Calendar): Triple<Int, Int, Int> {
        var years = calNow.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        var months = calNow.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
        if (months < 0) {
            years -= 1
            months += 12
        }
        var days = calNow.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)
        if (days < 0) {
            if (months == 0) {
                years -= 1
                months += 11
                days += 30
            } else {
                months -= 1
                days += 30
            }
        }
        return Triple(years, months, days)
    }

    @Test
    fun `updateAgeDTO borrows a day and rolls the month from zero to eleven`() {
        val calNow: Calendar = Calendar.getInstance()
        val birthCal = (calNow.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.YEAR, -5)
        }

        val ageDTO = AgeUnitDTO(0, 0, 0)
        HelperUtil.updateAgeDTO(ageDTO, birthCal)

        val (expYears, expMonths, expDays) = expectedAge(calNow, birthCal)
        assertEquals(expYears, ageDTO.years)
        assertEquals(expMonths, ageDTO.months)
        assertEquals(expDays, ageDTO.days)
    }

    @Test
    fun `updateAgeDTO borrows a day when the month remainder is non zero`() {
        val calNow: Calendar = Calendar.getInstance()
        val birthCal = (calNow.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -3)
            add(Calendar.YEAR, -3)
        }

        val ageDTO = AgeUnitDTO(0, 0, 0)
        HelperUtil.updateAgeDTO(ageDTO, birthCal)

        val (expYears, expMonths, expDays) = expectedAge(calNow, birthCal)
        assertEquals(expYears, ageDTO.years)
        assertEquals(expMonths, ageDTO.months)
        assertEquals(expDays, ageDTO.days)
    }

    @Test
    fun `updateAgeDTO borrows a year when the birth month is later than the current month`() {
        val calNow: Calendar = Calendar.getInstance()
        val birthCal = (calNow.clone() as Calendar).apply {
            add(Calendar.MONTH, 2)
            add(Calendar.YEAR, -3)
        }

        val ageDTO = AgeUnitDTO(0, 0, 0)
        HelperUtil.updateAgeDTO(ageDTO, birthCal)

        val (expYears, expMonths, expDays) = expectedAge(calNow, birthCal)
        assertEquals(expYears, ageDTO.years)
        assertEquals(expMonths, ageDTO.months)
        assertEquals(expDays, ageDTO.days)
    }

    // =====================================================
    // getDiffYears() - equal day boundary
    // =====================================================

    @Test
    fun `getDiffYears does not decrement when the day matches exactly`() {
        val a = Calendar.getInstance().apply { set(2000, Calendar.JANUARY, 15) }
        val b = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 15) }
        assertEquals(26, HelperUtil.getDiffYears(a, b))
    }

    // =====================================================
    // isValidName() - additional regex edge cases
    // =====================================================

    @Test
    fun `isValidName rejects name starting with a hyphen`() {
        assertFalse(HelperUtil.isValidName("-John"))
    }

    @Test
    fun `isValidName rejects name ending with a hyphen`() {
        assertFalse(HelperUtil.isValidName("John-"))
    }

    @Test
    fun `isValidName accepts name with multiple internal spaces`() {
        assertTrue(HelperUtil.isValidName("Mary  Jane"))
    }

    @Test
    fun `isValidName rejects whitespace only input`() {
        assertFalse(HelperUtil.isValidName("   "))
    }

    @Test
    fun `isValidName accepts a two letter name`() {
        assertTrue(HelperUtil.isValidName("Al"))
    }

    // =====================================================
    // parseDateToMillis() - invalid calendar dates
    // =====================================================

    @Test
    fun `parseDateToMillis returns 0 for a non existent february day`() {
        assertEquals(0L, HelperUtil.parseDateToMillis("30-02-2026"))
    }

    @Test
    fun `parseDateToMillis returns 0 for a day that does not exist in april`() {
        assertEquals(0L, HelperUtil.parseDateToMillis("31-04-2026"))
    }

    // =====================================================
    // parseSelections() - null entries fallback path
    // =====================================================

    @Test
    fun `parseSelections throws when there is no delimiter and no entries to match against`() {
        try {
            HelperUtil.parseSelections("justtext", null)
            assertTrue("Expected an NPE for the null entries fallback path", false)
        } catch (e: NullPointerException) {
            // documents the current behavior of the entries!! fallback
        }
    }

    // =====================================================
    // fileToBase64()
    // =====================================================

    @Test
    fun `fileToBase64 encodes file bytes using Base64 NO_WRAP`() {
        val tempFile = File.createTempFile("helper_util_test_", ".bin")
        try {
            tempFile.writeBytes(byteArrayOf(1, 2, 3, 4))
            mockkStatic(Base64::class)
            every { Base64.encodeToString(byteArrayOf(1, 2, 3, 4), Base64.NO_WRAP) } returns "AQIDBA=="

            val result = HelperUtil.fileToBase64(tempFile)

            assertEquals("AQIDBA==", result)
        } finally {
            tempFile.delete()
        }
    }

    // =====================================================
    // convertDpToPixel()
    // =====================================================

    @Test
    fun `convertDpToPixel delegates to TypedValue applyDimension`() {
        mockkStatic(TypedValue::class)
        val metrics = mockk<android.util.DisplayMetrics>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.displayMetrics } returns metrics
        every {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, metrics)
        } returns 48f

        val result = HelperUtil.convertDpToPixel(16f, ctx)

        assertEquals(48f, result, 0.0001f)
    }

    // =====================================================
    // getLocalizedResources() / getLocalizedContext()
    // =====================================================

    private fun contextForLocalization(): Context {
        val configuration = mockk<Configuration>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.resources } returns resources
        every { resources.configuration } returns configuration
        return ctx
    }

    @Test
    fun `getLocalizedResources returns the resources of the localized context`() {
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().setLocale(any()) } just Runs
        val ctx = contextForLocalization()
        val localizedContext = mockk<Context>(relaxed = true)
        val localizedResources = mockk<Resources>(relaxed = true)
        every { ctx.createConfigurationContext(any()) } returns localizedContext
        every { localizedContext.resources } returns localizedResources

        val result = HelperUtil.getLocalizedResources(ctx, Languages.HINDI)

        assertEquals(localizedResources, result)
        verify { ctx.createConfigurationContext(any()) }
    }

    @Test
    fun `getLocalizedContext returns the created configuration context`() {
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().setLocale(any()) } just Runs
        val ctx = contextForLocalization()
        val localizedContext = mockk<Context>(relaxed = true)
        every { ctx.createConfigurationContext(any()) } returns localizedContext

        val result = HelperUtil.getLocalizedContext(ctx, Languages.BANGLA)

        assertEquals(localizedContext, result)
    }

    // =====================================================
    // setEnLocaleForDatePicker() / setOriginalLocaleForDatePicker()
    // =====================================================

    private fun activityForDatePicker(): Pair<FragmentActivity, Resources> {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val resources = mockk<Resources>(relaxed = true)
        val configuration = mockk<Configuration>(relaxed = true)
        every { activity.resources } returns resources
        every { resources.configuration } returns configuration
        every { resources.displayMetrics } returns mockk(relaxed = true)
        return activity to resources
    }

    @Test
    fun `setEnLocaleForDatePicker updates the activity resources to English`() {
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().setLocale(any()) } just Runs
        val (activity, resources) = activityForDatePicker()

        HelperUtil.setEnLocaleForDatePicker(activity)

        verify { resources.updateConfiguration(any(), any()) }
        assertEquals("en", Locale.getDefault().language)
    }

    @Test
    fun `setOriginalLocaleForDatePicker restores the given locale`() {
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().setLocale(any()) } just Runs
        val (activity, resources) = activityForDatePicker()

        HelperUtil.setOriginalLocaleForDatePicker(activity, Locale("hi"))

        verify { resources.updateConfiguration(any(), any()) }
        assertEquals("hi", Locale.getDefault().language)
    }

    // =====================================================
    // Context.createTempImageUri()
    // =====================================================

    @Test
    fun `createTempImageUri writes a temp file and returns a FileProvider uri`() {
        mockkStatic(FileProvider::class)
        val tempDir = Files.createTempDirectory("helper_util_pics").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES) } returns tempDir
        every { ctx.packageName } returns "org.piramalswasthya.sakhi"
        val expectedUri = mockk<Uri>()
        every {
            FileProvider.getUriForFile(ctx, "org.piramalswasthya.sakhi.provider", any())
        } returns expectedUri

        val result = with(HelperUtil) { ctx.createTempImageUri() }

        assertEquals(expectedUri, result)
    }

    // =====================================================
    // findFragmentActivity() - the FragmentActivity-found branch
    // =====================================================

    @Test
    fun `findFragmentActivity returns the FragmentActivity found in the wrapper chain`() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val wrapper = mockk<ContextWrapper>()
        every { wrapper.baseContext } returns activity
        with(HelperUtil) {
            assertEquals(activity, wrapper.findFragmentActivity())
        }
    }

    // =====================================================
    // detectExtAndMime() - remaining short-circuit combinations
    // =====================================================

    @Test
    fun `detectExtAndMime does not match pdf when the second byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0x25, 0x00, 0x44, 0x46))
        )
    }

    @Test
    fun `detectExtAndMime does not match pdf when the third byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0x25, 0x50, 0x00, 0x46))
        )
    }

    @Test
    fun `detectExtAndMime does not match pdf when the fourth byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0x25, 0x50, 0x44, 0x00))
        )
    }

    @Test
    fun `detectExtAndMime does not match jpeg when the second byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0xFF.toByte(), 0x00, 0xFF.toByte(), 0x00))
        )
    }

    @Test
    fun `detectExtAndMime does not match jpeg when the third byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0x00, 0x00))
        )
    }

    @Test
    fun `detectExtAndMime does not match png when the second byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0x89.toByte(), 0x00, 0x4E, 0x47))
        )
    }

    @Test
    fun `detectExtAndMime does not match png when the third byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0x89.toByte(), 0x50, 0x00, 0x47))
        )
    }

    @Test
    fun `detectExtAndMime does not match png when the fourth byte differs`() {
        assertEquals(
            "bin" to "application/octet-stream",
            HelperUtil.detectExtAndMime(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x00))
        )
    }

    // =====================================================
    // getahd() / getVHND() / getSaasBahuSamalonLocalization() / getUWINLocalization()
    // - the localized-array-too-short fallback branch
    // =====================================================

    @Test
    fun `getahd returns the raw value when the localized array is short`() {
        val ctx = contextWithArray(R.array.ahd_place_options, arrayOf("A-School"))
        with(HelperUtil) {
            assertEquals("Community center", ctx.getahd("Community center"))
        }
    }

    @Test
    fun `getVHND returns the raw value when the localized array is short`() {
        val ctx = contextWithArray(R.array.place_of_vhsnc, arrayOf("V-Anganwadi"))
        with(HelperUtil) {
            assertEquals("Community center", ctx.getVHND("Community center"))
        }
    }

    @Test
    fun `getSaasBahuSamalonLocalization returns the raw value when the localized array is short`() {
        val ctx = contextWithArray(R.array.place_array, arrayOf("S-HWC"))
        with(HelperUtil) {
            assertEquals("Community center", ctx.getSaasBahuSamalonLocalization("Community center"))
        }
    }

    @Test
    fun `getUWINLocalization returns the raw value when the localized array is short`() {
        val ctx = contextWithArray(R.array.place_of_delivery_options, arrayOf("U-HWC"))
        with(HelperUtil) {
            assertEquals("Community center", ctx.getUWINLocalization("Community center"))
        }
    }

    // =====================================================
    // parseSelections() - remaining branch gaps
    // =====================================================

    @Test
    fun `parseSelections skips JSON parsing when the closing bracket is missing`() {
        assertEquals(listOf("abc"), HelperUtil.parseSelections("[abc", arrayOf("abc")))
    }

    @Test
    fun `parseSelections filters blank segments when splitting on pipe`() {
        assertEquals(listOf("a", "b"), HelperUtil.parseSelections("a| |b", null))
    }

    // =====================================================
    // copyToTemp() - success paths
    // =====================================================

    @Test
    fun `copyToTemp copies the input stream content into a suffixed temp file`() {
        val tempDir = Files.createTempDirectory("helper_util_copy").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns tempDir
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(any()) } returns "hello".byteInputStream()

        val result = HelperUtil.copyToTemp(mockk(relaxed = true), "doc.pdf", ctx)

        assertNotNull(result)
        assertEquals("hello", result!!.readText())
        result.delete()
    }

    @Test
    fun `copyToTemp copies the input stream content into a suffixless temp file`() {
        val tempDir = Files.createTempDirectory("helper_util_copy_noext").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns tempDir
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(any()) } returns "world".byteInputStream()

        val result = HelperUtil.copyToTemp(mockk(relaxed = true), "noextension", ctx)

        assertNotNull(result)
        assertEquals("world", result!!.readText())
        result.delete()
    }

    @Test
    fun `copyToTemp returns an empty temp file when the input stream is unavailable`() {
        val tempDir = Files.createTempDirectory("helper_util_copy_null").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns tempDir
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(any()) } returns null

        val result = HelperUtil.copyToTemp(mockk(relaxed = true), "doc.pdf", ctx)

        assertNotNull(result)
        assertEquals(0L, result!!.length())
        result.delete()
    }

    // =====================================================
    // Downloads log writers - success paths
    // =====================================================

    private fun stubAvailableDownloadsDir(dir: File) {
        mockkStatic(Environment::class)
        mockkStatic(android.util.Log::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns dir
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
    }

    @Test
    fun `saveApiResponseToDownloads writes the payload and logs success`() {
        val tempDir = Files.createTempDirectory("helper_util_downloads1").toFile()
        tempDir.deleteOnExit()
        stubAvailableDownloadsDir(tempDir)

        HelperUtil.saveApiResponseToDownloads(context, "api_response_success.txt", "payload")

        verify { android.util.Log.d("SAVE_FILE", any()) }
        assertEquals("payload", File(tempDir, "api_response_success.txt").readText())
    }

    @Test
    fun `deliveryOutcomeDBLogMethod writes the payload and logs success`() {
        val tempDir = Files.createTempDirectory("helper_util_downloads2").toFile()
        tempDir.deleteOnExit()
        stubAvailableDownloadsDir(tempDir)

        HelperUtil.deliveryOutcomeDBLogMethod(context, "db_log_success.txt", "payload")

        verify { android.util.Log.d("SAVE_FILE", any()) }
        assertEquals("payload", File(tempDir, "db_log_success.txt").readText())
    }

    @Test
    fun `deliveryOutcomeUpdatePNCWorkerMethod writes the payload and logs success`() {
        val tempDir = Files.createTempDirectory("helper_util_downloads3").toFile()
        tempDir.deleteOnExit()
        stubAvailableDownloadsDir(tempDir)

        HelperUtil.deliveryOutcomeUpdatePNCWorkerMethod(context, "pnc_worker_success.txt", "payload")

        verify { android.util.Log.d("SAVE_FILE", any()) }
        assertEquals("payload", File(tempDir, "pnc_worker_success.txt").readText())
    }

    @Test
    fun `deliveryOutcomeRepoMethod writes the payload and logs success`() {
        val tempDir = Files.createTempDirectory("helper_util_downloads4").toFile()
        tempDir.deleteOnExit()
        stubAvailableDownloadsDir(tempDir)

        HelperUtil.deliveryOutcomeRepoMethod(context, "repo_success.txt", "payload")

        verify { android.util.Log.d("SAVE_FILE", any()) }
        assertEquals("payload", File(tempDir, "repo_success.txt").readText())
    }

    // =====================================================
    // getFilesName() - remaining content-scheme branches
    // =====================================================

    @Test
    fun `getFilesName falls back to path when the resolver returns no cursor`() {
        val uri = contentUri()
        every { uri.path } returns "/documents/xyz/file.jpg"
        val ctx = contextWithCursor(uri, null)
        assertEquals("file.jpg", HelperUtil.getFilesName(uri, ctx))
    }

    @Test
    fun `getFilesName falls back to path when the cursor has no first row`() {
        val uri = contentUri()
        every { uri.path } returns "/documents/xyz/file.jpg"
        val ctx = contextWithCursor(uri, displayNameCursor("ignored.jpg", moveToFirst = false))
        assertEquals("file.jpg", HelperUtil.getFilesName(uri, ctx))
    }

    // =====================================================
    // checkAndShowMUACAlert() / checkAndShowWeightForHeightAlert()
    // - the SAM-detected alert branches
    // =====================================================

    private fun stubAlertDialogBuilder() {
        mockkConstructor(AlertDialog.Builder::class)
        every { anyConstructed<AlertDialog.Builder>().setTitle(any<CharSequence>()) } answers { self as AlertDialog.Builder }
        every { anyConstructed<AlertDialog.Builder>().setMessage(any<CharSequence>()) } answers { self as AlertDialog.Builder }
        every {
            anyConstructed<AlertDialog.Builder>().setPositiveButton(any<CharSequence>(), any())
        } answers { self as AlertDialog.Builder }
        every { anyConstructed<AlertDialog.Builder>().show() } returns mockk<AlertDialog>(relaxed = true)
    }

    @Test
    fun `checkAndShowMUACAlert shows an alert dialog and returns true for a SAM value`() {
        stubAlertDialogBuilder()
        assertTrue(HelperUtil.checkAndShowMUACAlert(context, "10.0"))
    }

    @Test
    fun `checkAndShowWeightForHeightAlert shows an alert dialog and returns true for SAM status`() {
        stubAlertDialogBuilder()
        assertTrue(HelperUtil.checkAndShowWeightForHeightAlert(context, "SAM"))
    }

    // =====================================================
    // HelperUtil Tests (extended)
    // =====================================================

    @Test fun `HelperUtil exists`() {
        assertNotNull(HelperUtil)
    }


    @Test
    fun `getFileSizeInMB returns null when the descriptor is null`() {
        val uri = mockk<Uri>(relaxed = true)
        val ctx = mockk<Context>(relaxed = true)
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openFileDescriptor(uri, "r") } returns null
        with(HelperUtil) {
            assertNull(ctx.getFileSizeInMB(uri))
        }
    }

    @Test
    fun `compressImageToTemp resizes and compresses a large image`() {
        mockkStatic(BitmapFactory::class)
        every {
            BitmapFactory.decodeStream(any(), any(), any())
        } answers {
            val opts = thirdArg<BitmapFactory.Options>()
            opts.outWidth = 2560
            opts.outHeight = 1920
            mockk<Bitmap>(relaxed = true)
        }
        val tempDir = Files.createTempDirectory("helper_util_compress").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns tempDir
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(any()) } returns "x".byteInputStream()

        val result = HelperUtil.compressImageToTemp(mockk(relaxed = true), "img.jpg", ctx)

        assertNotNull(result)
        result!!.delete()
    }

    @Test
    fun `compressImageToTemp falls back to copyToTemp when the decoded bounds are invalid`() {
        mockkStatic(BitmapFactory::class)
        every {
            BitmapFactory.decodeStream(any(), any(), any())
        } answers {
            val opts = thirdArg<BitmapFactory.Options>()
            opts.outWidth = -1
            opts.outHeight = -1
            null
        }
        val tempDir = Files.createTempDirectory("helper_util_compress_invalid").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.cacheDir } returns tempDir
        val resolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { ctx.contentResolver } returns resolver
        every { resolver.openInputStream(any()) } returns "y".byteInputStream()

        val result = HelperUtil.compressImageToTemp(mockk(relaxed = true), "img.jpg", ctx)

        assertNotNull(result)
        result!!.delete()
    }

    @Test
    fun `base64ToTempFile decodes and writes bytes to a temp file`() {
        mockkStatic(Base64::class)
        mockkStatic(FileProvider::class)
        val bytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        every { Base64.decode(any<String>(), any()) } returns bytes
        val tempDir = Files.createTempDirectory("helper_util_b64").toFile()
        tempDir.deleteOnExit()
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.packageName } returns "org.piramalswasthya.sakhi"
        val expectedUri = mockk<Uri>()
        every {
            FileProvider.getUriForFile(ctx, "org.piramalswasthya.sakhi.provider", any())
        } returns expectedUri

        val result = HelperUtil.base64ToTempFile("data:application/pdf;base64,JVBERi0=", tempDir, ctx)

        assertEquals(expectedUri, result)
    }
}
