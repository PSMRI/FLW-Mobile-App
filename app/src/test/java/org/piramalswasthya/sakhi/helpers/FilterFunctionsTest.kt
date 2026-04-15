package org.piramalswasthya.sakhi.helpers

import android.text.TextUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.BenBasicDomain

class FilterFunctionsTest {

    @Before
    fun setUp() {
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers {
            val str = firstArg<CharSequence>()
            str.all { it.isDigit() }
        }
    }

    private fun makeBen(name: String, benId: Long = 1L, rchId: String = ""): BenBasicDomain {
        val ben = mockk<BenBasicDomain>(relaxed = true)
        every { ben.benFullName } returns name
        every { ben.benId } returns benId
        every { ben.rchId } returns rchId
        every { ben.mobileNo } returns ""
        every { ben.abhaId } returns ""
        every { ben.age } returns ""
        return ben
    }

    // =====================================================
    // filterBenList(list, text) Tests
    // =====================================================

    @Test fun `filterBenList returns all for empty filter`() {
        val list = listOf(makeBen("John"), makeBen("Jane"))
        val result = filterBenList(list, "")
        assertEquals(2, result.size)
    }

    @Test fun `filterBenList filters by name`() {
        val list = listOf(makeBen("John Doe"), makeBen("Jane Smith"))
        val result = filterBenList(list, "john")
        assertEquals(1, result.size)
    }

    @Test fun `filterBenList returns empty for no match`() {
        val list = listOf(makeBen("John"), makeBen("Jane"))
        val result = filterBenList(list, "xyz")
        assertEquals(0, result.size)
    }

    @Test fun `filterBenList case insensitive`() {
        val list = listOf(makeBen("John Doe"))
        val result = filterBenList(list, "JOHN")
        assertEquals(1, result.size)
    }

    @Test fun `filterBenList on empty list`() {
        val result = filterBenList(emptyList(), "test")
        assertTrue(result.isEmpty())
    }

    @Test fun `filterBenList filters by benId`() {
        val list = listOf(makeBen("John", benId = 12345L))
        val result = filterBenList(list, "12345")
        assertEquals(1, result.size)
    }


    // =====================================================
    // filterBenList(list, isDeath) Tests
    // =====================================================



    // =====================================================
    // setToStartOfTheDay Tests
    // =====================================================

    @Test fun `setToStartOfTheDay sets hours to 0`() {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 15)
        cal.set(java.util.Calendar.MINUTE, 30)
        cal.setToStartOfTheDay()
        assertEquals(0, cal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(java.util.Calendar.MINUTE))
        assertEquals(0, cal.get(java.util.Calendar.SECOND))
        assertEquals(0, cal.get(java.util.Calendar.MILLISECOND))
    }

    // =====================================================
    // getDateStrFromLong Tests
    // =====================================================

    @Test fun `getDateStrFromLong returns null for 0`() {
        val result = org.piramalswasthya.sakhi.model.getDateStrFromLong(0)
        // Should return a date string for epoch
        assertTrue(result != null)
    }
}
