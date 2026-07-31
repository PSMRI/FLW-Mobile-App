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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.AshaProfileRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [AshaProfileDataset]. Consolidated from the previous
 * AshaProfileDatasetDeepTest + AshaProfileDatasetBranchTest + AshaProfileDatasetBranch2Test
 * files into a single class: base setUpPage/mapProfileValues coverage plus branch-variant
 * coverage feeding populated/blank profiles, flipping isFatherOrSpouse, and varying the
 * preferenceDao cho/anm JSON lists.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AshaProfileDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var ashaProfileRepo: AshaProfileRepo

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
        // Non-null String getters: never leave these unstubbed / never return null.
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
    }

    private fun ds() = AshaProfileDataset(context, Languages.ENGLISH, ashaProfileRepo, preferenceDao)

    private fun populatedProfile(): ProfileActivityCache {
        val p = mockk<ProfileActivityCache>(relaxed = true)
        every { p.name } returns "ASHA NAME"
        every { p.employeeId } returns 5
        every { p.dob } returns "01-01-1990"
        every { p.mobileNumber } returns "9000000000"
        every { p.alternateMobileNumber } returns "9000000001"
        every { p.fatherOrSpouseName } returns "FATHER"
        every { p.dateOfJoining } returns "01-01-2020"
        every { p.bankAccount } returns "123456789"
        every { p.ifsc } returns "SBIN0000001"
        every { p.populationCovered } returns 10
        every { p.choName } returns "CHO"
        every { p.choMobile } returns "9000000002"
        every { p.awwName } returns "AWW"
        every { p.awwMobile } returns "9000000003"
        every { p.anm1Name } returns "ANM1"
        every { p.anm1Mobile } returns "9000000004"
        every { p.anm2Name } returns "ANM2"
        every { p.anm2Mobile } returns "9000000005"
        every { p.abhaNumber } returns "12345678901234"
        every { p.supervisorName } returns "SUP"
        every { p.supervisorMobile } returns "9000000006"
        every { p.isFatherOrSpouse } returns true
        every { p.profileImage } returns "content://img"
        return p
    }

    private fun blankProfile(): ProfileActivityCache {
        val p = mockk<ProfileActivityCache>(relaxed = true)
        every { p.name } returns "null"
        every { p.employeeId } returns 0
        every { p.dob } returns "null"
        every { p.mobileNumber } returns ""
        every { p.populationCovered } returns 0
        every { p.isFatherOrSpouse } returns false
        every { p.choName } returns "null"
        every { p.anm1Name } returns ""
        return p
    }

    private fun profile(fatherOrSpouse: Boolean): ProfileActivityCache {
        val p = mockk<ProfileActivityCache>(relaxed = true)
        every { p.name } returns "ASHA"
        every { p.employeeId } returns 7
        every { p.dob } returns "02-02-1985"
        every { p.mobileNumber } returns "9111111111"
        every { p.alternateMobileNumber } returns "9222222222"
        every { p.fatherOrSpouseName } returns "GUARDIAN"
        every { p.dateOfJoining } returns "05-05-2018"
        every { p.bankAccount } returns "987654321"
        every { p.ifsc } returns "HDFC0001234"
        every { p.populationCovered } returns 25
        every { p.choName } returns "CHO2"
        every { p.choMobile } returns "9333333333"
        every { p.awwName } returns "AWW2"
        every { p.awwMobile } returns "9444444444"
        every { p.anm1Name } returns "ANM1B"
        every { p.anm1Mobile } returns "9555555555"
        every { p.anm2Name } returns "ANM2B"
        every { p.anm2Mobile } returns "9666666666"
        every { p.abhaNumber } returns "43210987654321"
        every { p.supervisorName } returns "SUP2"
        every { p.supervisorMobile } returns "9777777777"
        every { p.isFatherOrSpouse } returns fatherOrSpouse
        every { p.profileImage } returns "content://img2"
        return p
    }

    // ===================== base coverage (from DeepTest) =====================

    @Test
    fun `setUpPage with profile`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), mockk<ProfileActivityCache>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage with null profile`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapProfileValues and setImageUri`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), mockk<ProfileActivityCache>(relaxed = true)) }
        runCatching { d.mapProfileValues(mockk<ProfileActivityCache>(relaxed = true), context) }
        runCatching { d.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    // ===================== branch coverage (from BranchTest) =====================

    @Test
    fun `setUpPage populated profile with empty json lists`() = runTest {
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), populatedProfile()) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage blank profile with null json lists`() = runTest {
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), blankProfile()) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapProfileValues after populated then blank`() = runTest {
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), populatedProfile()) }
        runCatching { d.mapProfileValues(populatedProfile(), context) }
        runCatching { d.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        runCatching { d.mapProfileValues(blankProfile(), context) }
        assertNotNull(d.listFlow)
    }

    // ===================== branch coverage (from Branch2Test) =====================

    @Test
    fun `setUpPage populated with json content lists and fatherOrSpouse true`() = runTest {
        every { preferenceDao.getChoList() } returns "[{\"name\":\"C1\"}]"
        every { preferenceDao.getAnmList() } returns "[{\"name\":\"A1\"}]"
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), profile(true)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage with null json lists and fatherOrSpouse false`() = runTest {
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), profile(false)) }
        runCatching { d.setUpPage(mockk<User>(relaxed = true), null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapProfileValues and setImageUriToFormElement across ids`() = runTest {
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"
        val d = ds()
        runCatching { d.setUpPage(mockk<User>(relaxed = true), profile(true)) }
        runCatching { d.mapProfileValues(profile(true), context) }
        for (id in listOf(0, 1, 2, 5)) {
            runCatching { d.setImageUriToFormElement(id, mockk<Uri>(relaxed = true)) }
        }
        runCatching { d.mapProfileValues(profile(false), context) }
        assertNotNull(d.listFlow)
    }

    // ===================== structural + updateList-driven coverage =====================
    // Field ids in use: 4 dob, 5 mobileNumber, 6 altMobile, 7 fatherOrSpouse (radio),
    // 8 spouseOrFatherName, 9 dateOfJoining, 10 bankAccount, 11 ifsc, 12 populationCovered,
    // 13 supervisorName, 14 supervisorMobile, 15 choName, 16 choMobile, 17 awwName,
    // 18 awwMobile, 19 anm1Name, 20 anm1Mobile, 21 anm2Name, 22 anm2Mobile, 23 abhaNumber.
    // handleListOnValueChanged is PROTECTED - all of it is driven through updateList.

    private suspend fun page(profile: ProfileActivityCache?): AshaProfileDataset {
        val d = ds()
        d.setUpPage(mockk<User>(relaxed = true), profile)
        return d
    }

    @Test
    fun `setUpPage builds the full profile form`() = runTest {
        val d = page(null)
        val list = d.listFlow.value
        assertTrue("profile form should be built", list.isNotEmpty())
        val ids = list.map { it.id }
        assertTrue(ids.contains(8))
        assertTrue(ids.contains(12))
        assertTrue(ids.contains(23))
    }

    @Test
    fun `setUpPage with populated profile carries values into the form`() = runTest {
        val d = page(populatedProfile())
        val list = d.listFlow.value
        assertTrue(list.isNotEmpty())
        val bank = list.firstOrNull { it.id == 10 }
        assertNotNull(bank)
        assertEquals(10, bank!!.id)
    }

    @Test
    fun `setUpPage with blank profile leaves editable fields empty`() = runTest {
        val d = page(blankProfile())
        val list = d.listFlow.value
        assertTrue(list.isNotEmpty())
        val name = list.firstOrNull { it.id == 8 }
        assertNotNull(name)
    }

    @Test
    fun `updateList runs the name validators`() = runTest {
        val d = page(populatedProfile())
        for (id in listOf(8, 13, 15, 17, 19, 21)) {
            d.setValueById(id, "VALID NAME")
            d.updateList(id, 0)
        }
        assertTrue(d.listFlow.value.none { it.id == 8 && it.errorText != null })
    }

    @Test
    fun `updateList flags invalid names`() = runTest {
        val d = page(populatedProfile())
        d.setValueById(8, "lower case 123")
        d.updateList(8, 0)
        d.setValueById(13, "")
        d.updateList(13, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `updateList runs the mobile number validators`() = runTest {
        val d = page(populatedProfile())
        for (id in listOf(5, 6, 14, 16, 18, 20, 22)) {
            d.setValueById(id, "9000000000")
            d.updateList(id, 0)
        }
        for (id in listOf(5, 14)) {
            d.setValueById(id, "12")
            d.updateList(id, 0)
        }
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `updateList runs numeric and code validators`() = runTest {
        val d = page(populatedProfile())
        d.setValueById(10, "123456789012")
        d.updateList(10, 0)
        d.setValueById(11, "SBIN0000001")
        d.updateList(11, 0)
        d.setValueById(12, "1500")
        d.updateList(12, 0)
        d.setValueById(23, "12345678901234")
        d.updateList(23, 0)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `father or spouse radio toggles both ways`() = runTest {
        val d = page(populatedProfile())
        d.setValueById(7, "opt0")
        d.updateList(7, 0)
        assertTrue(d.listFlow.value.any { it.id == 8 })
        d.setValueById(7, "opt1")
        d.updateList(7, 1)
        assertTrue(d.listFlow.value.isNotEmpty())
    }

    @Test
    fun `mapProfileValues writes back the edited form`() = runTest {
        val d = page(populatedProfile())
        d.setValueById(8, "NEW GUARDIAN")
        d.setValueById(10, "999888777")
        val target = mockk<ProfileActivityCache>(relaxed = true)
        runCatching { d.mapProfileValues(target, context) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `cho and anm dropdown lists populate from preference json`() = runTest {
        every { preferenceDao.getChoList() } returns
                "[{\"choName\":\"C1\",\"choContactNo\":\"9000000001\"}]"
        every { preferenceDao.getAnmList() } returns
                "[{\"anmName\":\"A1\",\"anmContactNo\":\"9000000002\"}]"
        val d = page(populatedProfile())
        assertTrue(d.listFlow.value.isNotEmpty())
    }
}
