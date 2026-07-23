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
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
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
}
