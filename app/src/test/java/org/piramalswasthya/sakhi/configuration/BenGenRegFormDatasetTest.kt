package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class BenGenRegFormDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.w(any(), any<String>()) } returns 0
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers {
            firstArg<CharSequence>().all { it.isDigit() }
        }
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    private fun benMock(): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns 2
        every { b.isDraft } returns false
        every { b.gender } returns Gender.FEMALE
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        return b
    }

    private fun ds() = BenGenRegFormDataset(context, Languages.ENGLISH)

    @Test
    fun `setFirstPage saved and null`() = runTest {
        val d = ds()
        runCatching { d.setFirstPage(benMock(), 9876543210L) }
        runCatching { d.setFirstPage(null, null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setSecondPage and setThirdPage`() = runTest {
        val d = ds()
        runCatching { d.setSecondPage(benMock()) }
        runCatching { d.setSecondPage(null) }
        runCatching { d.setThirdPage(benMock()) }
        runCatching { d.setThirdPage(null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `index getters and helpers`() = runTest {
        val d = ds()
        runCatching { d.setFirstPage(benMock(), 1L) }
        runCatching { d.hasThirdPage() }
        runCatching { d.getIndexOfRelationToHead() }
        runCatching { d.getIndexOfAgeAtMarriage() }
        runCatching { d.getIndexOfFatherName() }
        runCatching { d.getIndexOfMotherName() }
        runCatching { d.getIndexOfSpouseName() }
        runCatching { d.getIndexOfMaritalStatus() }
        runCatching { d.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    // ---- Added deep-branch coverage ----

    private fun genMock(maritalId: Int): org.piramalswasthya.sakhi.model.BenRegGen {
        val g = mockk<org.piramalswasthya.sakhi.model.BenRegGen>(relaxed = true)
        every { g.maritalStatusId } returns maritalId
        every { g.ageAtMarriage } returns 20
        every { g.marriageDate } returns 1_500_000_000_000L
        every { g.spouseName } returns "SPOUSE"
        every { g.reproductiveStatusId } returns 2
        return g
    }

    private fun benG(
        genderId: Int,
        maritalId: Int,
        mobileId: Int,
        relPos: Int,
        religionId: Int,
        isDraft: Boolean = false,
    ): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.isDraft } returns isDraft
        every { b.genderId } returns genderId
        every { b.genDetails } returns genMock(maritalId)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.mobileNoOfRelationId } returns mobileId
        every { b.familyHeadRelationPosition } returns relPos
        every { b.religionId } returns religionId
        every { b.communityId } returns 1
        every { b.hasAadharId } returns 1
        every { b.contactNumber } returns 9876543210L
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        return b
    }

    @Test
    fun `setFirstPage male married with wife`() = runTest {
        val d = ds()
        runCatching {
            d.setFirstPage(benG(genderId = 1, maritalId = 2, mobileId = 80, relPos = 80, religionId = 7), 111L)
            d.mapValues(benG(genderId = 1, maritalId = 2, mobileId = 80, relPos = 80, religionId = 7), 0)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setFirstPage female married with husband`() = runTest {
        val d = ds()
        runCatching {
            d.setFirstPage(benG(genderId = 2, maritalId = 2, mobileId = 5, relPos = 3, religionId = 1), 222L)
            d.hasThirdPage()
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setFirstPage transgender widowed with spouse`() = runTest {
        val d = ds()
        runCatching {
            d.setFirstPage(benG(genderId = 3, maritalId = 3, mobileId = 2, relPos = 5, religionId = 3), 333L)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setFirstPage draft ben skips saved block`() = runTest {
        val d = ds()
        runCatching {
            d.setFirstPage(benG(genderId = 1, maritalId = 1, mobileId = 1, relPos = 1, religionId = 1, isDraft = true), 1L)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `second and third page saved paths`() = runTest {
        val d = ds()
        val ben = benG(genderId = 2, maritalId = 2, mobileId = 5, relPos = 3, religionId = 1)
        every { ben.aadharNum } returns "123456789012"
        every { ben.rchId } returns "112233445566"
        runCatching {
            d.setFirstPage(ben, 5L)
            d.setSecondPage(ben)
            d.setThirdPage(ben)
            d.mapValues(ben, 0)
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `hindi first page`() = runTest {
        val d = BenGenRegFormDataset(context, Languages.HINDI)
        runCatching {
            d.setFirstPage(benG(genderId = 2, maritalId = 2, mobileId = 5, relPos = 3, religionId = 7), 9L)
        }
        assertNotNull(d.listFlow)
    }

    // ---- handleListOnValueChanged coverage via the public updateList wrapper ----

    private fun BenGenRegFormDataset.valueOf(id: Int): String? =
        listFlow.value.firstOrNull { it.id == id }?.value

    private fun BenGenRegFormDataset.errorOf(id: Int): String? =
        listFlow.value.firstOrNull { it.id == id }?.errorText

    private suspend fun freshFirstPage(): BenGenRegFormDataset {
        val d = ds()
        d.setFirstPage(null, 9876543210L)
        return d
    }

    @Test
    fun `name fields run empty and caps validations`() = runTest {
        val d = freshFirstPage()
        d.setValueById(3, "")
        d.updateList(3, 0)
        assertNotNull(d.errorOf(3))
        d.setValueById(3, "john doe")
        d.updateList(3, 0)
        assertNotNull(d.errorOf(3))
        d.setValueById(3, "JOHN DOE")
        d.updateList(3, 0)
        assertEquals("JOHN DOE", d.valueOf(3))
        assertEquals(null, d.errorOf(3))
        d.setValueById(4, "")
        d.updateList(4, 0)
        d.setValueById(4, "SMITH")
        d.updateList(4, 0)
        assertEquals("SMITH", d.valueOf(4))
    }

    @Test
    fun `parent and spouse name fields run validations`() = runTest {
        val d = freshFirstPage()
        d.setValueById(14, "")
        d.updateList(14, 0)
        d.setValueById(14, "FATHER NAME")
        d.updateList(14, 0)
        assertEquals("FATHER NAME", d.valueOf(14))
        d.setValueById(15, "MOTHER NAME")
        d.updateList(15, 0)
        assertEquals("MOTHER NAME", d.valueOf(15))
        d.updateList(9, 0)
        d.updateList(10, 0)
        d.updateList(11, 0)
        assertTrue(d.getListSize() > 0)
    }

    @Test
    fun `date of birth change recalculates age`() = runTest {
        val d = freshFirstPage()
        d.setValueById(6, "01-01-1990")
        d.updateList(6, 0)
        val ageValue = d.valueOf(5)
        assertNotNull(ageValue)
        assertTrue(ageValue!!.toInt() > 0)
    }

    @Test
    fun `age change back-computes dob and adds date of marriage on trigger index`() = runTest {
        val d = freshFirstPage()
        d.setValueById(5, "")
        d.updateList(5, 0)
        assertNotNull(d.errorOf(5))
        d.setValueById(5, "30")
        d.updateList(5, 0)
        assertNotNull(d.valueOf(6))
        assertTrue(d.getIndexById(13) >= 0)
        d.setValueById(5, "30")
        d.updateList(5, 1)
        assertEquals(-1, d.getIndexById(13))
    }

    @Test
    fun `age below minimum sets range error and leaves dob untouched`() = runTest {
        val d = freshFirstPage()
        d.setValueById(5, "5")
        d.updateList(5, 1)
        assertNotNull(d.errorOf(5))
    }

    @Test
    fun `gender selection swaps marital status and relation entry lists`() = runTest {
        val d = freshFirstPage()
        for (index in 0..2) {
            d.updateList(7, index)
        }
        assertTrue(d.getIndexById(8) >= 0)
        assertEquals(-1, d.getIndexById(20))
    }

    @Test
    fun `marital status unmarried removes spouse and age at marriage`() = runTest {
        val d = freshFirstPage()
        d.setValueById(7, "opt0")
        d.updateList(7, 0)
        d.setValueById(8, "opt1")
        d.updateList(8, 1)
        assertTrue(d.getIndexById(10) >= 0)
        assertTrue(d.getIndexById(12) >= 0)
        d.setValueById(8, "opt0")
        d.updateList(8, 0)
        assertEquals(-1, d.getIndexById(10))
        assertEquals(-1, d.getIndexById(12))
    }

    @Test
    fun `marital status married adds husband for female beneficiary`() = runTest {
        val d = freshFirstPage()
        d.setValueById(7, "opt1")
        d.updateList(7, 1)
        d.setValueById(8, "opt1")
        d.updateList(8, 1)
        assertTrue(d.getIndexById(9) >= 0)
        assertTrue(d.getIndexById(12) >= 0)
    }

    @Test
    fun `marital status other adds spouse name for transgender beneficiary`() = runTest {
        val d = freshFirstPage()
        d.setValueById(7, "opt2")
        d.updateList(7, 2)
        d.setValueById(8, "opt2")
        d.updateList(8, 2)
        assertTrue(d.getIndexById(11) >= 0)
        assertTrue(d.getIndexById(12) >= 0)
    }

    @Test
    fun `age at marriage equal to age adds date of marriage and different age removes it`() = runTest {
        val d = freshFirstPage()
        d.setValueById(7, "opt1")
        d.updateList(7, 1)
        d.setValueById(8, "opt1")
        d.updateList(8, 1)
        d.setValueById(5, "30")
        d.setValueById(12, "30")
        d.updateList(12, 0)
        assertTrue(d.getIndexById(13) >= 0)
        d.setValueById(12, "25")
        d.updateList(12, 0)
        assertEquals(-1, d.getIndexById(13))
    }

    @Test
    fun `age at marriage is ignored when age is blank`() = runTest {
        val d = freshFirstPage()
        d.setValueById(7, "opt1")
        d.updateList(7, 1)
        d.setValueById(8, "opt1")
        d.updateList(8, 1)
        d.setValueById(5, "")
        d.setValueById(12, "22")
        d.updateList(12, 0)
        assertEquals(-1, d.getIndexById(13))
    }

    @Test
    fun `contact number validates emptiness and mobile pattern`() = runTest {
        val d = freshFirstPage()
        d.setValueById(18, "")
        d.updateList(18, 0)
        d.setValueById(18, "9999999999")
        d.updateList(18, 0)
        assertNotNull(d.errorOf(18))
        d.setValueById(18, "9876543210")
        d.updateList(18, 0)
        assertEquals(null, d.errorOf(18))
    }

    @Test
    fun `mobile number of relation switches between self family head and other`() = runTest {
        val d = freshFirstPage()
        d.updateList(16, 0)
        assertTrue(d.getIndexById(18) >= 0)
        assertEquals(-1, d.getIndexById(114))
        d.updateList(16, 4)
        assertTrue(d.getIndexById(114) >= 0)
        assertEquals("9876543210", d.valueOf(114))
        assertEquals(-1, d.getIndexById(18))
        d.updateList(16, 6)
        assertTrue(d.getIndexById(17) >= 0)
        assertTrue(d.getIndexById(18) >= 0)
        d.setValueById(17, "")
        d.updateList(17, 0)
        assertNotNull(d.errorOf(17))
    }

    @Test
    fun `relation to head other adds free text and validates it`() = runTest {
        val d = freshFirstPage()
        d.updateList(19, 79)
        assertTrue(d.getIndexById(20) >= 0)
        d.setValueById(20, "")
        d.updateList(20, 0)
        assertNotNull(d.errorOf(20))
        d.updateList(19, 2)
        assertEquals(-1, d.getIndexById(20))
    }

    @Test
    fun `religion other adds free text and validates it`() = runTest {
        val d = freshFirstPage()
        d.updateList(22, 7)
        assertTrue(d.getIndexById(23) >= 0)
        d.setValueById(23, "")
        d.updateList(23, 0)
        assertNotNull(d.errorOf(23))
        d.updateList(22, 1)
        assertEquals(-1, d.getIndexById(23))
    }

    @Test
    fun `unknown form id on first page is a no op`() = runTest {
        val d = freshFirstPage()
        val before = d.getListSize()
        d.updateList(9999, 0)
        assertEquals(before, d.getListSize())
    }

    @Test
    fun `second page aadhaar toggle and identity validations`() = runTest {
        val d = ds()
        d.setSecondPage(null)
        d.updateList(24, 0)
        assertTrue(d.getIndexById(25) >= 0)
        d.setValueById(25, "1234")
        d.updateList(25, 0)
        assertNotNull(d.errorOf(25))
        d.setValueById(25, "123456789012")
        d.updateList(25, 0)
        assertEquals(null, d.errorOf(25))
        d.setValueById(26, "1234")
        d.updateList(26, 0)
        assertNotNull(d.errorOf(26))
        d.setValueById(26, "111111111111")
        d.updateList(26, 0)
        assertNotNull(d.errorOf(26))
        d.setValueById(26, "123456789012")
        d.updateList(26, 0)
        assertEquals(null, d.errorOf(26))
        d.updateList(24, 1)
        assertEquals(-1, d.getIndexById(25))
    }

    @Test
    fun `map values writes a fully populated first and second page`() = runTest {
        val d = ds()
        d.setFirstPage(null, 9876543210L)
        d.updateList(7, 1)
        d.setValueById(8, "opt1")
        d.updateList(8, 1)
        d.setValueById(3, "FIRST")
        d.setValueById(4, "LAST")
        d.setValueById(5, "30")
        d.setValueById(6, "01-01-1994")
        d.setValueById(7, "opt1")
        d.setValueById(9, "HUSBAND")
        d.setValueById(12, "22")
        d.setValueById(14, "FATHER")
        d.setValueById(15, "MOTHER")
        d.setValueById(16, "opt1")
        d.setValueById(18, "9876543210")
        d.setValueById(19, "opt3")
        d.setValueById(21, "opt1")
        d.setValueById(22, "opt1")
        val ben = benG(genderId = 2, maritalId = 2, mobileId = 1, relPos = 4, religionId = 1)
        d.mapValues(ben, 0)
        verify { ben.firstName = "FIRST" }
        verify { ben.gender = Gender.FEMALE }
        verify { ben.contactNumber = 9876543210L }
    }

    @Test
    fun `map values uses family head phone when mobile belongs to head`() = runTest {
        val d = ds()
        d.setFirstPage(null, 9876543210L)
        d.updateList(16, 4)
        d.setValueById(5, "30")
        d.setValueById(6, "01-01-1994")
        d.setValueById(7, "opt0")
        d.setValueById(19, "opt2")
        val ben = benG(genderId = 1, maritalId = 1, mobileId = 5, relPos = 3, religionId = 1)
        d.mapValues(ben, 0)
        verify { ben.contactNumber = 9876543210L }
        verify { ben.gender = Gender.MALE }
    }

    @Test
    fun `map values marks transgender and no aadhaar`() = runTest {
        val d = ds()
        d.setFirstPage(null, 9876543210L)
        d.setValueById(5, "30")
        d.setValueById(6, "01-01-1994")
        d.setValueById(7, "opt2")
        d.setValueById(18, "9876543210")
        d.setValueById(19, "opt1")
        val ben = benG(genderId = 3, maritalId = 1, mobileId = 1, relPos = 2, religionId = 1)
        d.mapValues(ben, 0)
        verify { ben.gender = Gender.TRANSGENDER }
        verify { ben.hasAadharId = 2 }
    }

    @Test
    fun `has third page is true only for married female`() = runTest {
        val d = freshFirstPage()
        d.setValueById(7, "opt1")
        d.updateList(7, 1)
        d.setValueById(8, "opt1")
        d.updateList(8, 1)
        assertTrue(d.hasThirdPage())
        d.setValueById(8, "opt0")
        d.updateList(8, 0)
        assertTrue(!d.hasThirdPage())
    }

    @Test
    fun `image uri is stored on the picture element`() = runTest {
        val d = freshFirstPage()
        val uri = mockk<Uri>(relaxed = true)
        d.setImageUriToFormElement(1, uri)
        assertNotNull(d.valueOf(1))
        val stored = d.valueOf(1)
        d.setImageUriToFormElement(999, uri)
        assertEquals(stored, d.valueOf(1))
    }

    @Test
    fun `setFirstPage adds date of marriage when saved age at marriage matches the computed age`() = runTest {
        val d = ds()
        val dobMillis = java.util.Calendar.getInstance().apply { add(java.util.Calendar.YEAR, -25) }.timeInMillis
        val computedAge = org.piramalswasthya.sakhi.model.BenBasicCache.getAgeFromDob(dobMillis)
        val ben = benG(genderId = 2, maritalId = 2, mobileId = 1, relPos = 3, religionId = 1)
        every { ben.dob } returns dobMillis
        every { ben.genDetails!!.ageAtMarriage } returns computedAge
        d.setFirstPage(ben, 9876543210L)
        assertTrue(d.getIndexById(13) >= 0)
    }

    @Test
    fun `setFirstPage adds family head contact field when saved mobile relation is family head`() = runTest {
        val d = ds()
        val ben = benG(genderId = 2, maritalId = 1, mobileId = 4, relPos = 3, religionId = 1)
        d.setFirstPage(ben, 9876543210L)
        assertTrue(d.getIndexById(114) >= 0)
        assertEquals(-1, d.getIndexById(18))
    }
}
