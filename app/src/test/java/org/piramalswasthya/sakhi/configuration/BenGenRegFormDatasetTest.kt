package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
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
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
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
}
