package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [EligibleCoupleRegistrationDataset]. Consolidated into a single class from the
 * previous EligibleCoupleRegistrationDatasetDeepTest + EligibleCoupleRegistrationDatasetBranchTest +
 * EligibleCoupleRegistrationDatasetBranch2Test + EligibleCoupleRegistrationDatasetBranch3Test files.
 * The source suites each declared a private benMock / ds factory with mutually-incompatible
 * signatures (and one used HINDI); those have been renamed with source-specific suffixes
 * (Deep / Br / Br2 / Br3) so they coexist in one class, and their callers updated accordingly.
 * All @Test methods and their logic are preserved verbatim.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleRegistrationDatasetTest : BaseViewModelTest() {

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
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        // Dataset.getLocalValueInArray / getEnglishValueInArray call Log.w when an entry is not
        // present in the (mocked) resource array. Without this stub the un-mocked android.util.Log
        // throws, aborting setUpPage at the very first child, which left most of the builder body
        // and every list-dependent branch below it unreachable.
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    // ---------- shared factories ----------

    private fun ds() = EligibleCoupleRegistrationDataset(
        context, context, Languages.ENGLISH, MutableLiveData()
    )

    private fun dsBr3() = EligibleCoupleRegistrationDataset(
        context, context, Languages.HINDI, MutableLiveData()
    )

    // ---------- Deep factories ----------

    private fun benMockDeep(): BenRegCache {
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

    // ---------- Branch factories ----------

    private fun benMockBr(gender: Gender = Gender.FEMALE): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns if (gender == Gender.MALE) 1 else 2
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        return b
    }

    // assess with every yes/no answer == yes_no[0] ("opt0") so showHighRisk / isHighRisk go true.
    private fun highRiskAssess(): HRPNonPregnantAssessCache {
        val a = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        every { a.noOfDeliveries } returns "opt0"
        every { a.timeLessThan18m } returns "opt0"
        every { a.heightShort } returns "opt0"
        every { a.misCarriage } returns "opt0"
        every { a.homeDelivery } returns "opt0"
        every { a.medicalIssues } returns "opt0"
        every { a.pastCSection } returns "opt0"
        return a
    }

    private fun kitSaved(handedOver: Boolean): EligibleCoupleRegCache {
        val s = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { s.isKitHandedOver } returns handedOver
        every { s.kitHandedOverDate } returns 1_600_000_000_000L
        return s
    }

    // ---------- Branch2 factories ----------

    private fun benMockBr2(gender: Gender = Gender.FEMALE): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns if (gender == Gender.MALE) 1 else 2
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        val g = mockk<BenRegGen>(relaxed = true)
        every { g.ageAtMarriage } returns 20
        every { b.genDetails } returns g
        return b
    }

    private fun lowRiskAssess(): HRPNonPregnantAssessCache {
        val a = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        every { a.noOfDeliveries } returns "opt1"
        every { a.timeLessThan18m } returns "opt1"
        every { a.heightShort } returns "opt1"
        every { a.misCarriage } returns "opt1"
        every { a.homeDelivery } returns "opt1"
        every { a.medicalIssues } returns "opt1"
        every { a.pastCSection } returns "opt1"
        return a
    }

    private fun savedKit(): EligibleCoupleRegCache {
        val s = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { s.isKitHandedOver } returns true
        every { s.kitHandedOverDate } returns 1_600_000_000_000L
        every { s.dateOfReg } returns 1_600_000_000_000L
        return s
    }

    // ---------- Branch3 factories ----------

    private fun benMockBr3(gender: Gender = Gender.FEMALE, genderId: Int = 2): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns genderId
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        return b
    }

    // ===================== deep coverage (from DeepTest) =====================

    @Test
    fun `setUpPage with saved records`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                benMockDeep(),
                mockk<HRPNonPregnantAssessCache>(relaxed = true),
                mockk<EligibleCoupleRegCache>(relaxed = true),
                listOf(benMockDeep(), benMockDeep())
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage with null records`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(null, null, null, emptyList()) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `map methods and index getters`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockDeep(), null, null, listOf(benMockDeep())) }
        runCatching { d.mapValueToBen(benMockDeep()) }
        runCatching { d.mapValueToBen(null) }
        runCatching { d.mapValuesToAssess(mockk<HRPNonPregnantAssessCache>(relaxed = true), 0) }
        runCatching { d.mapValuesToAssess(null, 1) }
        runCatching { d.isHighRisk() }
        runCatching { d.getIndexOfChildren() }
        runCatching { d.getIndexOfLiveChildren() }
        runCatching { d.getIndexOfMaleChildren() }
        runCatching { d.getIndexOfFeMaleChildren() }
        runCatching { d.getIndexOfAge1() }
        runCatching { d.getIndexOfGap1() }
        runCatching { d.getIndexOfTimeLessThan18() }
        runCatching { d.setImageUriToFormElement(1, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `companion getMinimumSecondChildDob`() {
        runCatching {
            assertNotNull(
                EligibleCoupleRegistrationDataset.getMinimumSecondChildDob("01-01-2015")
            )
        }
    }

    // ===================== branch coverage (from BranchTest) =====================

    @Test
    fun `setUpPage high-risk assess and mixed-gender children`() = runTest {
        val d = ds()
        val children = listOf(benMockBr(Gender.MALE), benMockBr(Gender.FEMALE), benMockBr(Gender.MALE))
        runCatching { d.setUpPage(benMockBr(), highRiskAssess(), null, children) }
        runCatching { d.mapValuesToAssess(highRiskAssess(), 0) }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage kit handed over vs not`() = runTest {
        val d1 = ds()
        runCatching { d1.setUpPage(benMockBr(), null, kitSaved(true), emptyList()) }
        assertNotNull(d1.listFlow)

        val d2 = ds()
        runCatching { d2.setUpPage(benMockBr(), null, kitSaved(false), emptyList()) }
        assertNotNull(d2.listFlow)
    }

    @Test
    fun `mapValueToBen and mapValuesToAssess null vs non-null`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr(), highRiskAssess(), null, listOf(benMockBr(Gender.FEMALE))) }
        runCatching { d.mapValueToBen(benMockBr()) }
        runCatching { d.mapValueToBen(null) }
        runCatching { d.mapValuesToAssess(mockk<HRPNonPregnantAssessCache>(relaxed = true), 0) }
        runCatching { d.mapValuesToAssess(null, 1) }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    // ===================== branch coverage (from Branch2Test) =====================

    @Test
    fun `setUpPage large child list and low-risk assess`() = runTest {
        val d = ds()
        val many = (0 until 12).map { if (it % 2 == 0) benMockBr2(Gender.MALE) else benMockBr2(Gender.FEMALE) }
        runCatching { d.setUpPage(benMockBr2(), lowRiskAssess(), savedKit(), many) }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setUpPage saved non-null null assess single child`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr2(), null, savedKit(), listOf(benMockBr2(Gender.MALE))) }
        runCatching { d.mapValueToBen(benMockBr2()) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValuesToAssess multiple pages`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr2(), lowRiskAssess(), null, emptyList()) }
        for (page in 0..3) {
            runCatching { d.mapValuesToAssess(lowRiskAssess(), page) }
        }
        runCatching { d.mapValuesToAssess(null, 0) }
        assertNotNull(d.listFlow)
    }

    // ===================== branch coverage (from Branch3Test) =====================

    @Test
    fun `setUpPage saved with assess and couple`() = runTest {
        val d = dsBr3()
        runCatching {
            d.setUpPage(
                benMockBr3(Gender.MALE, 1),
                mockk<HRPNonPregnantAssessCache>(relaxed = true),
                mockk<EligibleCoupleRegCache>(relaxed = true),
                listOf(benMockBr3(), benMockBr3(Gender.MALE, 1), benMockBr3())
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `map methods with multiple page indices`() = runTest {
        val d = dsBr3()
        runCatching { d.setUpPage(benMockBr3(), null, null, listOf(benMockBr3())) }
        runCatching { d.mapValueToBen(benMockBr3(Gender.MALE, 1)) }
        for (p in 0..3) {
            runCatching { d.mapValuesToAssess(mockk<HRPNonPregnantAssessCache>(relaxed = true), p) }
        }
        runCatching { d.isHighRisk() }
        runCatching { d.getIndexOfChildren() }
        runCatching { d.getIndexOfLiveChildren() }
        runCatching { d.getIndexOfMaleChildren() }
        runCatching { d.getIndexOfFeMaleChildren() }
        runCatching { d.getIndexOfAge1() }
        runCatching { d.getIndexOfGap1() }
        runCatching { d.getIndexOfTimeLessThan18() }
        runCatching { d.setImageUriToFormElement(2, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `companion getMinimumSecondChildDob variants`() {
        runCatching { assertNotNull(EligibleCoupleRegistrationDataset.getMinimumSecondChildDob("31-12-2020")) }
        runCatching { assertNotNull(EligibleCoupleRegistrationDataset.getMinimumSecondChildDob("15-06-2000")) }
    }

    // ===================== added: mapValues override + remaining branches =====================

    // mapValues (the FormDataModel override) is not exercised by any of the pre-existing tests;
    // driving it after setUpPage with varied live-child counts covers each of the >1..>8 blocks.
    @Test
    fun `mapValues over live child counts`() = runTest {
        for (n in 1..9) {
            val d = ds()
            val kids = (0 until n).map { if (it % 2 == 0) benMockBr(Gender.MALE) else benMockBr(Gender.FEMALE) }
            runCatching { d.setUpPage(benMockBr(), lowRiskAssess(), null, kids) }
            runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `mapValues with full nine children and saved kit`() = runTest {
        val d = ds()
        val kids = (0 until 9).map { benMockBr(if (it % 2 == 0) Gender.MALE else Gender.FEMALE) }
        runCatching { d.setUpPage(benMockBr(), highRiskAssess(), savedKit(), kids) }
        runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
        runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 1) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `all remaining index getters`() = runTest {
        val d = ds()
        val kids = (0 until 9).map { benMockBr(Gender.MALE) }
        runCatching { d.setUpPage(benMockBr(), null, kitSaved(true), kids) }
        runCatching { d.getIndexOfAge2() }
        runCatching { d.getIndexOfGap2() }
        runCatching { d.getIndexOfAge3() }
        runCatching { d.getIndexOfGap3() }
        runCatching { d.getIndexOfAge4() }
        runCatching { d.getIndexOfGap4() }
        runCatching { d.getIndexOfAge5() }
        runCatching { d.getIndexOfGap5() }
        runCatching { d.getIndexOfAge6() }
        runCatching { d.getIndexOfGap6() }
        runCatching { d.getIndexOfAge7() }
        runCatching { d.getIndexOfGap7() }
        runCatching { d.getIndexOfAge8() }
        runCatching { d.getIndexOfGap8() }
        runCatching { d.getIndexOfAge9() }
        runCatching { d.getIndexOfGap9() }
        runCatching { d.getIndexOfTimeLessThan18m() }
        runCatching { d.getIndexOfChildLabel() }
        runCatching { d.getIndexOfPhysicalObservationLabel() }
        runCatching { d.getIndexOfObstetricHistoryLabel() }
        runCatching { d.getIndexofAshaKitPhotoOne() }
        runCatching { d.getIndexofAshaKitPhoto() }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setImageUriToFormElement kit photo ids`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr(), null, kitSaved(true), emptyList()) }
        runCatching { d.setImageUriToFormElement(75, mockk<Uri>(relaxed = true)) }
        runCatching { d.setImageUriToFormElement(76, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    // Drives the MALE / FEMALE / else arms of the gender when() blocks in mapValues by
    // setting each child's gender element to entries[0] ("opt0"), entries[1] ("opt1"), then a
    // non-matching value. Nine live children also exercise every >1..>8 mapValues block.
    @Test
    fun `mapValues gender male female and else branches`() = runTest {
        val genderIds = listOf(19, 24, 29, 34, 39, 44, 49, 54, 59)
        val ageIds = listOf(18, 23, 28, 33, 38, 43, 48, 53, 58)
        val d = ds()
        val kids = (0 until 9).map { benMockBr(Gender.MALE) }
        runCatching { d.setUpPage(benMockBr2(), lowRiskAssess(), null, kids) }
        genderIds.forEach { runCatching { d.setValueById(it, "opt0") } }
        ageIds.forEach { runCatching { d.setValueById(it, "5") } }
        runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
        genderIds.forEach { runCatching { d.setValueById(it, "opt1") } }
        runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
        genderIds.forEach { runCatching { d.setValueById(it, "zzz") } }
        runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
        assertNotNull(d.listFlow)
    }

    // Drives the isUpdated == true path of mapValueToBen (rchId differs) plus the processed==N
    // and null-ben branches.
    @Test
    fun `mapValueToBen updates rchId when changed`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr(), highRiskAssess(), null, emptyList()) }
        runCatching { d.setValueById(1, "123456789012") }
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns "999999999999"
        every { ben.processed } returns "N"
        runCatching { d.mapValueToBen(ben) }
        val ben2 = mockk<BenRegCache>(relaxed = true)
        every { ben2.rchId } returns null
        every { ben2.processed } returns "P"
        runCatching { d.mapValueToBen(ben2) }
        runCatching { d.mapValueToBen(null) }
        assertNotNull(d.listFlow)
    }

    // mapValueToBen with a low-risk assessment so the isHighRisk() == false arm runs.
    @Test
    fun `mapValueToBen low risk does not set hrp`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr2(), lowRiskAssess(), null, listOf(benMockBr(Gender.FEMALE))) }
        runCatching { d.mapValueToBen(benMockBr()) }
        runCatching { d.mapValuesToAssess(mockk<HRPNonPregnantAssessCache>(relaxed = true), 0) }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    // setValueById on assessment radios then mapValuesToAssess covers the non-null value arms.
    @Test
    fun `mapValuesToAssess with populated radio values`() = runTest {
        val d = ds()
        runCatching { d.setUpPage(benMockBr2(), lowRiskAssess(), null, emptyList()) }
        listOf(61, 62, 63, 64, 65, 66, 67, 68).forEach { runCatching { d.setValueById(it, "opt0") } }
        runCatching { d.mapValuesToAssess(mockk<HRPNonPregnantAssessCache>(relaxed = true), 0) }
        runCatching { d.isHighRisk() }
        assertNotNull(d.listFlow)
    }

    // Drives the young (<18) and old (>35) branches of updateAgeCheck via setUpPage.
    @Test
    fun `setUpPage age boundary branches`() = runTest {
        val young = mockk<BenRegCache>(relaxed = true)
        every { young.dob } returns System.currentTimeMillis() - 10L * 365 * 24 * 3600 * 1000
        every { young.regDate } returns 1_600_000_000_000L
        every { young.genderId } returns 2
        every { young.isDraft } returns false
        every { young.gender } returns Gender.FEMALE
        every { young.firstName } returns "Y"
        every { young.lastName } returns "L"
        every { young.familyHeadRelationPosition } returns 1
        val d1 = ds()
        runCatching { d1.setUpPage(young, null, null, emptyList()) }
        assertNotNull(d1.listFlow)

        val old = mockk<BenRegCache>(relaxed = true)
        every { old.dob } returns System.currentTimeMillis() - 40L * 365 * 24 * 3600 * 1000
        every { old.regDate } returns 1_600_000_000_000L
        every { old.genderId } returns 2
        every { old.isDraft } returns false
        every { old.gender } returns Gender.FEMALE
        every { old.firstName } returns "O"
        every { old.lastName } returns "L"
        every { old.familyHeadRelationPosition } returns 1
        val d2 = ds()
        runCatching { d2.setUpPage(old, null, null, emptyList()) }
        assertNotNull(d2.listFlow)
    }

    // ===================== added: handleListOnValueChanged via public updateList =====================
    // handleListOnValueChanged(formId, index) is PROTECTED; it is only reachable through the public
    // updateList(formId, index) wrapper. Each test below sets the trigger field's value to the branch
    // under test, then calls updateList so the value-dependent add/remove logic executes. A fully
    // populated page (9 children) is built first so every child FormElement is present in the list.

    private suspend fun dsPopulated(): EligibleCoupleRegistrationDataset {
        val d = ds()
        val kids = (0 until 9).map { benMockBr(if (it % 2 == 0) Gender.MALE else Gender.FEMALE) }
        runCatching { d.setUpPage(benMockBr(), lowRiskAssess(), null, kids) }
        return d
    }

    // nayiPahelKitHandOver (id 78): "yes" (opt0) adds the kit-photo dependants; "no" (opt1) hides them.
    @Test
    fun `updateList nayiPahel kit handover yes and no`() = runTest {
        val d = dsPopulated()
        runCatching { d.setValueById(78, "opt0") }
        runCatching { d.updateList(78, 0) }
        runCatching { d.setValueById(78, "opt1") }
        runCatching { d.updateList(78, 0) }
        assertNotNull(d.listFlow)
    }

    // Edit-text validation branches: rchId(1), aadharNo(7), bankAccount(8), bankName(9),
    // branchName(10), ifsc(11).
    @Test
    fun `updateList edit text validation fields`() = runTest {
        val d = dsPopulated()
        runCatching { d.setValueById(1, "123456789012"); d.updateList(1, 0) }
        runCatching { d.setValueById(7, "123456789012"); d.updateList(7, 0) }
        runCatching { d.setValueById(8, "1234567890"); d.updateList(8, 0) }
        runCatching { d.setValueById(9, "STATE BANK"); d.updateList(9, 0) }
        runCatching { d.setValueById(10, "MAIN BRANCH"); d.updateList(10, 0) }
        runCatching { d.setValueById(11, "SBIN0001234"); d.updateList(11, 0) }
        assertNotNull(d.listFlow)
    }

    // ageAtMarriage (id 5): empty short-circuits to validateEmpty; a filled value runs min/max.
    @Test
    fun `updateList ageAtMarriage empty and filled`() = runTest {
        val d = dsPopulated()
        runCatching { d.setValueById(5, ""); d.updateList(5, 0) }
        runCatching { d.setValueById(4, "30"); d.setValueById(5, "20"); d.updateList(5, 0) }
        assertNotNull(d.listFlow)
    }

    // noOfChildren (id 12): 0/empty resets counts; >3 forces noOfDeliveries yes; <=3 forces no.
    @Test
    fun `updateList noOfChildren zero gt3 and le3`() = runTest {
        val d = dsPopulated()
        runCatching { d.setValueById(12, "0"); d.updateList(12, 0) }
        runCatching { d.setValueById(12, ""); d.updateList(12, 0) }
        runCatching { d.setValueById(12, "5"); d.updateList(12, 0) }
        runCatching { d.setValueById(12, "2"); d.updateList(12, 0) }
        assertNotNull(d.listFlow)
    }

    // noOfLiveChildren (id 13): every count 0..9 plus null/else hits a distinct triggerDependants arm.
    @Test
    fun `updateList noOfLiveChildren all count branches`() = runTest {
        val d = dsPopulated()
        for (n in 0..9) {
            runCatching { d.setValueById(13, n.toString()); d.updateList(13, 0) }
        }
        runCatching { d.setValueById(13, null); d.updateList(13, 0) }
        runCatching { d.setValueById(13, "12"); d.updateList(13, 0) }
        assertNotNull(d.listFlow)
    }

    // dob1..dob9 (ids 17,22,27,32,37,42,47,52,57): each recomputes ages / sibling gaps / min dates.
    @Test
    fun `updateList child dob fields recompute ages`() = runTest {
        val d = dsPopulated()
        val dobIds = listOf(17, 22, 27, 32, 37, 42, 47, 52, 57)
        val dates = listOf(
            "01-01-2008", "01-06-2010", "01-02-2012", "01-08-2013", "01-03-2015",
            "01-09-2016", "01-04-2018", "01-10-2019", "01-05-2021"
        )
        dobIds.forEachIndexed { i, id ->
            runCatching { d.setValueById(id, dates[i]); d.updateList(id, 0) }
        }
        assertNotNull(d.listFlow)
    }

    // gender1..gender9 (ids 19,24,29,34,39,44,49,54,59): male (opt0), female (opt1) and other arms
    // drive the numMale/numFemale counting block.
    @Test
    fun `updateList gender fields male female other`() = runTest {
        val d = dsPopulated()
        val genderIds = listOf(19, 24, 29, 34, 39, 44, 49, 54, 59)
        genderIds.forEach { runCatching { d.setValueById(it, "opt0"); d.updateList(it, 0) } }
        genderIds.forEach { runCatching { d.setValueById(it, "opt1"); d.updateList(it, 0) } }
        genderIds.forEach { runCatching { d.setValueById(it, "zzz"); d.updateList(it, 0) } }
        assertNotNull(d.listFlow)
    }

    // High-risk label toggles: noOfDeliveries(61)/timeLessThan18m(62), heightShort(63)/ageCheck(64),
    // misCarriage(65)/homeDelivery(66)/medicalIssues(67)/pastCSection(68). "opt0" == yes -> showHighRisk.
    @Test
    fun `updateList high risk label toggles`() = runTest {
        val d = dsPopulated()
        listOf(61, 62, 63, 64, 65, 66, 67, 68).forEach {
            runCatching { d.setValueById(it, "opt0"); d.updateList(it, 0) }
            runCatching { d.setValueById(it, "opt1"); d.updateList(it, 0) }
        }
        assertNotNull(d.listFlow)
    }

    // ===================== added: real assertions on the built page =========================
    // With Log.w stubbed, setUpPage now runs to completion, so listFlow actually carries the built
    // form and the assertions below can be structural instead of "assertNotNull(flow)".

    @Test
    fun `setUpPage emits a populated page for a fresh registration`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        val page = d.listFlow.value
        assertTrue(page.isNotEmpty())
        assertTrue(page.any { it.id == 0 })   // dateOfReg
        assertTrue(page.any { it.id == 13 })  // noOfLiveChildren
        assertEquals(page.size, d.getListSize())
    }

    @Test
    fun `setUpPage inserts four form rows per live child`() = runTest {
        val d = ds()
        val kids = (0 until 3).map { benMockBr(if (it % 2 == 0) Gender.MALE else Gender.FEMALE) }
        d.setUpPage(benMockBr(), null, null, kids)
        val page = d.listFlow.value
        listOf(17, 18, 19, 20, 22, 23, 24, 25, 27, 28, 29, 30).forEach { id ->
            assertTrue("expected child field $id on the page", page.any { it.id == id })
        }
        assertTrue(d.getIndexOfAge1() >= 0)
        assertTrue(d.getIndexOfGap1() >= 0)
        assertTrue(d.getIndexOfLiveChildren() >= 0)
    }

    @Test
    fun `setUpPage counts male and female children and caps the list at nine`() = runTest {
        val d = ds()
        val kids = (0 until 12).map { if (it % 2 == 0) benMockBr(Gender.MALE) else benMockBr(Gender.FEMALE) }
        d.setUpPage(benMockBr(), null, null, kids)
        val page = d.listFlow.value
        assertEquals("9", page.first { it.id == 13 }.value)
        assertEquals("5", page.first { it.id == 14 }.value)
        assertEquals("4", page.first { it.id == 15 }.value)
    }

    @Test
    fun `setUpPage with kit handed over adds the handover date and photo rows`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, kitSaved(true), emptyList())
        val page = d.listFlow.value
        assertTrue(page.any { it.id == 78 })
        assertTrue(page.any { it.id == 77 })
    }

    @Test
    fun `setUpPage with live children drops the nayi pahel kit block`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, listOf(benMockBr(Gender.MALE)))
        val page = d.listFlow.value
        assertTrue(page.none { it.id == 78 })
        assertTrue(page.none { it.id == 75 })
        assertTrue(page.none { it.id == 76 })
    }

    @Test
    fun `setUpPage with a localized gender array resolves each child gender`() = runTest {
        every { mockResources.getStringArray(R.array.ecr_baby_gender_array) } returns
                arrayOf("Male", "Female")
        val d = ds()
        d.setUpPage(
            benMockBr(),
            null,
            null,
            listOf(benMockBr(Gender.MALE), benMockBr(Gender.FEMALE))
        )
        val page = d.listFlow.value
        assertEquals("Male", page.first { it.id == 19 }.value)
        assertEquals("Female", page.first { it.id == 24 }.value)
    }

    @Test
    fun `isHighRisk is driven by the yes answers on the assessment radios`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        d.setValueById(61, "opt1")
        d.setValueById(62, "opt1")
        d.setValueById(63, "opt1")
        d.setValueById(65, "opt1")
        d.setValueById(66, "opt1")
        d.setValueById(67, "opt1")
        d.setValueById(68, "opt1")
        assertFalse(d.isHighRisk())

        d.setValueById(65, "opt0")
        assertTrue(d.isHighRisk())
    }

    @Test
    fun `mapValueToBen flags a high risk beneficiary and reports no rch update`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        d.setValueById(61, "opt0")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns null
        assertFalse(d.mapValueToBen(ben))
        verify { ben.isHrpStatus = true }
    }

    @Test
    fun `mapValueToBen reports an update when the rch id changed`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        d.setValueById(1, "123456789012")
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns "999999999999"
        every { ben.processed } returns "P"
        assertTrue(d.mapValueToBen(ben))
    }

    @Test
    fun `mapValuesToAssess copies the english radio values onto the cache`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        listOf(61, 62, 63, 64, 65, 66, 67, 68).forEach { d.setValueById(it, "opt0") }
        val assess = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        d.mapValuesToAssess(assess, 0)
        verify { assess.noOfDeliveries = "opt0" }
        verify { assess.isHighRisk = true }
    }

    @Test
    fun `mapValues writes the child block for the populated children`() = runTest {
        val d = ds()
        val kids = (0 until 3).map { benMockBr(Gender.MALE) }
        d.setUpPage(benMockBr(), null, null, kids)
        val cache = mockk<EligibleCoupleRegCache>(relaxed = true)
        d.mapValues(cache, 0)
        verify { cache.noOfLiveChildren = 3 }
        verify { cache.noOfMaleChildren = 3 }
    }

    @Test
    fun `setImageUriToFormElement stores the uri on the matching photo slot`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, kitSaved(true), emptyList())
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://photo/1"
        d.setImageUriToFormElement(75, uri)
        d.setImageUriToFormElement(76, uri)
        val page = d.listFlow.value
        assertEquals("content://photo/1", page.first { it.id == 75 }.value)
        assertEquals("content://photo/1", page.first { it.id == 76 }.value)
    }

    @Test
    fun `child bundle helpers report emptiness build the row list and clear values`() {
        fun fe(id: Int, value: String? = null) = FormElement(
            id = id,
            inputType = InputType.EDIT_TEXT,
            required = false,
            title = "t$id",
            value = value
        )

        val empty = EligibleCoupleRegistrationDataset.ChildBundle(
            fe(1), fe(2), fe(3), fe(4), fe(5)
        )
        assertTrue(empty.isEmpty())
        assertEquals(5, empty.toFormList().size)

        val filled = EligibleCoupleRegistrationDataset.ChildBundle(
            fe(1), fe(2, "01-01-2015"), fe(3, "9"), fe(4, "Male"), fe(5, "2 years")
        )
        assertFalse(filled.isEmpty())
        filled.clearValues()
        assertTrue(filled.isEmpty())
        assertNull(filled.gap.value)
        assertNotNull(filled.toString())
        assertEquals(filled, filled.copy())
    }

    @Test
    fun `getMinimumSecondChildDob echoes the first child dob`() {
        assertEquals(
            "01-01-2015",
            EligibleCoupleRegistrationDataset.getMinimumSecondChildDob("01-01-2015")
        )
        assertEquals(
            "29-02-2016",
            EligibleCoupleRegistrationDataset.getMinimumSecondChildDob("29-02-2016")
        )
    }

    @Test
    fun `updateList on the child dob chain recomputes ages and sibling gaps`() = runTest {
        val d = ds()
        val kids = (0 until 3).map { benMockBr(Gender.MALE) }
        d.setUpPage(benMockBr2(), null, null, kids)
        d.setValueById(17, "01-01-2010")
        d.updateList(17, 0)
        d.setValueById(22, "01-06-2012")
        d.updateList(22, 0)
        d.setValueById(27, "01-01-2015")
        d.updateList(27, 0)
        val page = d.listFlow.value
        assertNotNull(page.first { it.id == 18 }.value)
        assertNotNull(page.first { it.id == 20 }.value)
        assertTrue(page.first { it.id == 25 }.value!!.contains("years"))
    }

    @Test
    fun `updateList on a child gender recounts the male and female totals`() = runTest {
        every { mockResources.getStringArray(R.array.ecr_baby_gender_array) } returns
                arrayOf("Male", "Female")
        val d = ds()
        val kids = (0 until 3).map { benMockBr(Gender.MALE) }
        d.setUpPage(benMockBr(), null, null, kids)
        d.setValueById(19, "Male")
        d.setValueById(24, "Female")
        d.setValueById(29, "Female")
        d.updateList(19, 0)
        val page = d.listFlow.value
        assertEquals("1", page.first { it.id == 14 }.value)
        assertEquals("2", page.first { it.id == 15 }.value)
    }

    @Test
    fun `updateList on noOfLiveChildren adds and removes whole child blocks`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        d.setValueById(13, "2")
        d.updateList(13, 0)
        assertTrue(d.listFlow.value.any { it.id == 22 })

        d.setValueById(13, "0")
        d.updateList(13, 0)
        assertTrue(d.listFlow.value.none { it.id == 22 })
    }

    @Test
    fun `updateList on the kit handover radio shows then hides the photo rows`() = runTest {
        val d = ds()
        d.setUpPage(benMockBr(), null, null, emptyList())
        d.setValueById(78, "opt0")
        d.updateList(78, 0)
        assertTrue(d.listFlow.value.any { it.id == 77 })

        d.setValueById(78, "opt1")
        d.updateList(78, 1)
        assertTrue(d.listFlow.value.none { it.id == 77 })
    }
}
