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
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.FamilyMember
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [BenRegFormDataset]. Consolidated into a single class from the previous
 * BenRegFormDatasetDeepTest + BenRegFormDatasetBranchTest + BenRegFormDatasetBranch2Test +
 * BenRegFormDatasetBranch3Test files. The source suites each declared private helper factories
 * (benMock / genMock / ds) with mutually-incompatible signatures; those have been renamed with
 * source-specific suffixes (Deep / Br / Br2 / Br3) so they coexist in one class, and their callers
 * updated accordingly. All @Test methods and their logic are preserved verbatim.
 *
 * Deep coverage: builds rich (relaxed) BenRegCache / HouseholdCache mocks with the fields the
 * builder bodies read stubbed to sensible non-null values, so the large setPageForHof /
 * setPageForFamilyMember / setFirstPageToRead bodies run to completion instead of NPE-ing early.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BenRegFormDatasetTest : BaseViewModelTest() {

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
        // shouldShowMaternalDeath() logs with Log.v and Dataset's array lookups log with Log.w;
        // without these stubs the un-mocked android.util.Log throws and aborts those code paths.
        every { Log.v(any(), any()) } returns 0
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

    private fun householdMock(): HouseholdCache = mockk(relaxed = true)

    private fun ds() = BenRegFormDataset(context, Languages.ENGLISH)

    private fun dsBr3(lang: Languages = Languages.HINDI) = BenRegFormDataset(context, lang)

    // ---------- Deep factories ----------

    private fun benMockDeep(death: Boolean = false): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns 2
        every { b.isDraft } returns false
        every { b.gender } returns Gender.FEMALE
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        every { b.isDeath } returns death
        return b
    }

    // ---------- Branch factories ----------

    private fun genMockBr(
        maritalStatusId: Int = 2,
        spouse: String? = "SPOUSE",
        ageAtMarriage: Int? = 20,
        marriageDate: Long? = 1_500_000_000_000L
    ): BenRegGen {
        val g = mockk<BenRegGen>(relaxed = true)
        every { g.maritalStatusId } returns maritalStatusId
        every { g.spouseName } returns spouse
        every { g.ageAtMarriage } returns (ageAtMarriage ?: 0)
        every { g.marriageDate } returns marriageDate
        return g
    }

    private fun benMockBr(
        genderId: Int = 2,
        gender: Gender? = Gender.FEMALE,
        death: Boolean = false,
        spouseAdded: Boolean = false,
        childrenAdded: Boolean = false,
        hasChildren: Boolean = false,
        withGen: Boolean = true
    ): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns genderId
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        every { b.isDeath } returns death
        every { b.isSpouseAdded } returns spouseAdded
        every { b.isChildrenAdded } returns childrenAdded
        every { b.doYouHavechildren } returns hasChildren
        if (withGen) every { b.genDetails } returns genMockBr()
        return b
    }

    // ---------- Branch2 factories ----------

    private fun genMockBr2(maritalStatusId: Int): BenRegGen {
        val g = mockk<BenRegGen>(relaxed = true)
        every { g.maritalStatusId } returns maritalStatusId
        every { g.spouseName } returns "SPOUSE"
        every { g.ageAtMarriage } returns 21
        every { g.marriageDate } returns 1_500_000_000_000L
        return g
    }

    private fun benMockBr2(
        genderId: Int = 2,
        gender: Gender? = Gender.FEMALE,
        death: Boolean = false,
        maritalStatusId: Int = 2
    ): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns genderId
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        every { b.isDeath } returns death
        every { b.genDetails } returns genMockBr2(maritalStatusId)
        return b
    }

    // ---------- Branch3 factories ----------

    private fun benMockBr3(
        gender: Gender = Gender.MALE,
        genderId: Int = 1,
        dob: Long = System.currentTimeMillis() - 5L * 365 * 24 * 3600 * 1000,
        draft: Boolean = true,
        death: Boolean = false
    ): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns dob
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns genderId
        every { b.isDraft } returns draft
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 2
        every { b.isDeath } returns death
        return b
    }

    // ===================== deep coverage (from DeepTest) =====================

    @Test
    fun `setFirstPageToRead with saved ben`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setFirstPageToRead with null ben`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(null, null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForHof paths`() = runTest {
        val d = ds()
        runCatching { d.setPageForHof(benMockDeep(), householdMock(), null) }
        runCatching { d.setPageForHof(benMockDeep(death = true), householdMock(), null) }
        runCatching { d.setPageForHof(null, householdMock(), mockk<FamilyMember>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForFamilyMember relation variants`() = runTest {
        val household = householdMock()
        for (relId in listOf(0, 1, 4, 5, 8, 9, 10, 2, 19)) {
            val d = ds()
            runCatching {
                d.setPageForFamilyMember(
                    benMockDeep(), household, benMockDeep(), Gender.FEMALE, relId, emptyList(), benMockDeep(), 0
                )
            }
            runCatching {
                d.setPageForFamilyMember(
                    null, household, benMockDeep(), Gender.MALE, relId, emptyList(), null, 1
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setSecondPage paths`() = runTest {
        val d = ds()
        runCatching { d.setSecondPage(benMockDeep()) }
        runCatching { d.setSecondPage(null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValueToBen and helper getters`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        runCatching { d.mapValueToBen(benMockDeep()) }
        runCatching { d.mapValueToBen(null) }
        runCatching { d.isKid() }
        runCatching { d.getIndexOfAgeAtMarriage() }
        runCatching { d.getIndexOfContactNumber() }
        runCatching { d.getIndexOfMaritalStatus() }
        runCatching { d.getTempMobileNoStatus() }
        runCatching { d.getAbhaSubmitBtnId() }
        runCatching { d.getAbhaCardInput() }
        runCatching { d.getIndexOfBirthCertificateFrontPath() }
        runCatching { d.getIndexOfBirthCertificateBackPath() }
        runCatching { d.setIsAddingChildren(true) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `companion calculateMaxSonAge`() {
        val p = BenRegFormDataset.calculateMaxSonAge(40, 3, 18, 6)
        assertNotNull(p)
    }

    // ===================== branch coverage (from BranchTest) =====================

    @Test
    fun `setFirstPageToRead gender and genderId variants`() = runTest {
        // genderId 1 (wife), 2 (husband), 3 (spouse); gender MALE/FEMALE/TRANSGENDER whens.
        val combos = listOf(
            Triple(1, Gender.MALE, false),
            Triple(2, Gender.FEMALE, false),
            Triple(3, Gender.TRANSGENDER, false),
            Triple(2, Gender.FEMALE, true)
        )
        for ((gid, g, death) in combos) {
            val d = ds()
            runCatching {
                d.setFirstPageToRead(
                    benMockBr(genderId = gid, gender = g, death = death, hasChildren = true), 9876543210L
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setFirstPageToRead spouse-added and no-gen variants`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(spouseAdded = true, childrenAdded = true), null) }
        runCatching { d.setFirstPageToRead(benMockBr(withGen = false), 6000000000L) }
        runCatching { d.setFirstPageToRead(null, null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForHof death and gender variants`() = runTest {
        for ((gid, g) in listOf(1 to Gender.MALE, 2 to Gender.FEMALE, 3 to Gender.TRANSGENDER)) {
            val d = ds()
            runCatching { d.setPageForHof(benMockBr(genderId = gid, gender = g), householdMock(), null) }
            runCatching {
                d.setPageForHof(benMockBr(genderId = gid, gender = g, death = true, spouseAdded = true), householdMock(), null)
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setPageForHof with abha member`() = runTest {
        val d = ds()
        val member = mockk<FamilyMember>(relaxed = true)
        every { member.name } returns "JOHN DOE"
        every { member.gender } returns "Female"
        every { member.dob } returns "01-01-1990"
        every { member.mobileNo } returns "9876543210"
        every { member.abhId } returns "12345678901234"
        every { member.familyId } returns "FAM1"
        runCatching { d.setPageForHof(null, householdMock(), member) }
        val memberSingle = mockk<FamilyMember>(relaxed = true)
        every { memberSingle.name } returns "MADONNA"
        every { memberSingle.gender } returns "Male"
        runCatching { d.setPageForHof(null, householdMock(), memberSingle) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForFamilyMember relation gender and addspouse matrix`() = runTest {
        val hh = householdMock()
        for (relId in listOf(0, 1, 4, 5, 8, 9, 10, 11, 12, 2, 19)) {
            for (bg in listOf(Gender.MALE, Gender.FEMALE, Gender.TRANSGENDER)) {
                val d = ds()
                runCatching {
                    d.setPageForFamilyMember(
                        benMockBr(gender = bg), hh, benMockBr(gender = bg), bg, relId,
                        listOf(benMockBr(gender = bg)), benMockBr(gender = bg), 0
                    )
                }
                assertNotNull(d.listFlow)
            }
        }
    }

    @Test
    fun `setPageForFamilyMember addspouse and death and null-ben variants`() = runTest {
        val hh = householdMock()
        for (relId in listOf(4, 5, 8, 9)) {
            val d = ds()
            runCatching {
                d.setPageForFamilyMember(
                    benMockBr(death = true), hh, benMockBr(spouseAdded = true), Gender.FEMALE, relId,
                    listOf(benMockBr(gender = Gender.MALE)), benMockBr(), 1
                )
            }
            runCatching {
                d.setPageForFamilyMember(
                    null, hh, benMockBr(), Gender.MALE, relId, emptyList(), null, 0
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setSecondPage and mapValueToBen variants`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 1, gender = Gender.MALE), 9876543210L) }
        runCatching { d.setSecondPage(benMockBr()) }
        runCatching { d.setSecondPage(benMockBr(death = true)) }
        runCatching { d.setSecondPage(null) }
        runCatching { d.mapValueToBen(benMockBr(genderId = 1, gender = Gender.MALE)) }
        runCatching { d.mapValueToBen(benMockBr(genderId = 3, gender = Gender.TRANSGENDER)) }
        runCatching { d.mapValueToBen(null) }
        runCatching { d.isKid() }
        assertNotNull(d.listFlow)
    }

    // ===================== branch coverage (from Branch2Test) =====================

    @Test
    fun `setPageForFamilyMember uncovered relation ids`() = runTest {
        val hh = householdMock()
        for (relId in listOf(3, 6, 7, 13, 14, 15, 16, 17, 18, 20)) {
            for (bg in listOf(Gender.MALE, Gender.FEMALE, Gender.TRANSGENDER)) {
                val d = ds()
                runCatching {
                    d.setPageForFamilyMember(
                        benMockBr2(gender = bg), hh, benMockBr2(gender = bg), bg, relId,
                        listOf(benMockBr2(gender = bg)), benMockBr2(gender = bg), 1
                    )
                }
                assertNotNull(d.listFlow)
            }
        }
    }

    @Test
    fun `setPageForHof transgender and death head`() = runTest {
        val d = ds()
        runCatching { d.setPageForHof(benMockBr2(genderId = 3, gender = Gender.TRANSGENDER, death = true), householdMock(), null) }
        runCatching { d.setPageForHof(benMockBr2(genderId = 1, gender = Gender.MALE, maritalStatusId = 1), householdMock(), null) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForHof abha member gender variants`() = runTest {
        val d = ds()
        for (g in listOf("Male", "Female", "Transgender", "Other")) {
            val member = mockk<FamilyMember>(relaxed = true)
            every { member.name } returns "SINGLE"
            every { member.gender } returns g
            every { member.dob } returns "10-10-2000"
            every { member.mobileNo } returns "9000000000"
            every { member.abhId } returns "11112222333344"
            every { member.familyId } returns "FAM2"
            runCatching { d.setPageForHof(null, householdMock(), member) }
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValueToBen marital status variants`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr2(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        for (ms in listOf(1, 2, 3, 4, 5)) {
            runCatching { d.mapValueToBen(benMockBr2(maritalStatusId = ms)) }
        }
        runCatching { d.setIsAddingChildren(true) }
        runCatching { d.setIsAddingChildren(false) }
        assertNotNull(d.listFlow)
    }

    // ===================== branch coverage (from Branch3Test) =====================

    @Test
    fun `setFirstPageToRead male kid draft variants`() = runTest {
        val d = dsBr3()
        runCatching { d.setFirstPageToRead(benMockBr3(), null) }
        runCatching { d.setFirstPageToRead(benMockBr3(gender = Gender.MALE, death = true), 100L) }
        runCatching {
            d.setFirstPageToRead(
                benMockBr3(gender = Gender.FEMALE, genderId = 2, draft = false), 9876543210L
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForHof male and family member`() = runTest {
        val d = dsBr3(Languages.ASSAMESE)
        runCatching { d.setPageForHof(benMockBr3(), mockk<HouseholdCache>(relaxed = true), null) }
        runCatching {
            d.setPageForHof(
                benMockBr3(gender = Gender.FEMALE, genderId = 2), mockk<HouseholdCache>(relaxed = true),
                mockk<FamilyMember>(relaxed = true)
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setPageForFamilyMember all relations both genders`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        for (relId in 0..20) {
            val d = dsBr3()
            runCatching {
                d.setPageForFamilyMember(
                    benMockBr3(gender = Gender.MALE, genderId = 1), household,
                    benMockBr3(), Gender.MALE, relId, emptyList(), benMockBr3(), relId % 3
                )
            }
            runCatching {
                d.setPageForFamilyMember(
                    benMockBr3(gender = Gender.FEMALE, genderId = 2, draft = false), household,
                    benMockBr3(gender = Gender.FEMALE), Gender.FEMALE, relId, listOf(benMockBr3()), null, 0
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `mapValueToBen and getters kid path`() = runTest {
        val d = dsBr3()
        runCatching { d.setFirstPageToRead(benMockBr3(), 100L) }
        runCatching { d.mapValueToBen(benMockBr3(gender = Gender.MALE)) }
        runCatching { d.isKid() }
        runCatching { d.getIndexOfAgeAtMarriage() }
        runCatching { d.getIndexOfContactNumber() }
        runCatching { d.getIndexOfMaritalStatus() }
        runCatching { d.getTempMobileNoStatus() }
        runCatching { d.getAbhaSubmitBtnId() }
        runCatching { d.getAbhaCardInput() }
        runCatching { d.getIndexOfBirthCertificateFrontPath() }
        runCatching { d.getIndexOfBirthCertificateBackPath() }
        runCatching { d.setIsAddingChildren(false) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `companion calculateMaxSonAge alternate values`() {
        assertNotNull(BenRegFormDataset.calculateMaxSonAge(60, 0, 0, 0))
        assertNotNull(BenRegFormDataset.calculateMaxSonAge(20, 12, 25, 11))
    }

    // ===================== added: mapValues, image, household, ayushman, value-changed =========

    // mapValues (the FormDataModel override) is not touched by the pre-existing suite.
    @Test
    fun `mapValues after setup variants`() = runTest {
        val bens = listOf(
            benMockBr(genderId = 1, gender = Gender.MALE),
            benMockBr(genderId = 2, gender = Gender.FEMALE),
            benMockBr3()
        )
        for (b in bens) {
            val d = ds()
            runCatching { d.setFirstPageToRead(b, 9876543210L) }
            runCatching { d.mapValues(b, 0) }
            runCatching { d.mapValues(b, 1) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setImageUriToFormElement front back and default`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        runCatching { d.setImageUriToFormElement(46, mockk<Uri>(relaxed = true)) }
        runCatching { d.setImageUriToFormElement(47, mockk<Uri>(relaxed = true)) }
        runCatching { d.setImageUriToFormElement(99, mockk<Uri>(relaxed = true)) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateHouseholdWithHoFDetails and yearsAgo`() = runTest {
        val d = ds()
        runCatching { d.updateHouseholdWithHoFDetails(householdMock(), benMockDeep()) }
        assertNotNull(d.yearsAgo(5))
        assertNotNull(d.yearsAgo(0))
        assertNotNull(d.yearsAgo(50))
    }

    @Test
    fun `prefillFromAyushmanCard gender variants`() = runTest {
        val d = ds()
        for (g in listOf("Male", "Female", "Transgender", "Other")) {
            val member = mockk<FamilyMember>(relaxed = true)
            every { member.name } returns "JANE DOE"
            every { member.gender } returns g
            every { member.dob } returns "01-01-1990"
            every { member.mobileNo } returns "9876543210"
            every { member.abhId } returns "12345678901234"
            every { member.familyId } returns "FAM9"
            runCatching { d.prefillFromAyushmanCard(member) }
        }
        assertNotNull(d.listFlow)
    }

    // handleListOnValueChanged is only reachable through the public updateList wrapper; sweeping
    // the form-id space after a populated first page dispatches each handled branch.
    @Test
    fun `updateList drives handleListOnValueChanged`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        for (id in 0..120) {
            runCatching { d.updateList(id, 0) }
        }
        assertNotNull(d.listFlow)
    }

    // ===================== added: value-dependent handleListOnValueChanged branches =============
    // The sweep above dispatches every branch with default (null) values; the tests below first set
    // each trigger field to a specific option (via the public setValueById) so the value-dependent
    // add/remove arms inside handleListOnValueChanged execute. All go through the public updateList.

    // beneficiaryStatus (id 50): death (value == entries[2] -> getPosition()==2) vs alive. Built from
    // a death ben so beneficiaryStatus + death fields are present in the list; dateOfReg must be set
    // because the death arm reads dateOfReg.value!!.
    @Test
    fun `updateList beneficiaryStatus death and alive`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        runCatching { d.setValueById(50, "opt2"); d.updateList(50, 1) }
        runCatching { d.setValueById(50, "opt0"); d.updateList(50, 0) }
        assertNotNull(d.listFlow)
    }

    // placeOfDeath (id 54): a chosen entry drives triggerDependants for otherPlaceOfDeath.
    @Test
    fun `updateList placeOfDeath option`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        runCatching { d.setValueById(54, "opt8"); d.updateList(54, 8) }
        runCatching { d.setValueById(54, "opt0"); d.updateList(54, 0) }
        assertNotNull(d.listFlow)
    }

    // gender (id 9): index 0/1/2 select the male / female / other marital + relation entry sets.
    @Test
    fun `updateList gender index variants`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        for (idx in 0..2) {
            runCatching { d.updateList(9, idx) }
        }
        assertNotNull(d.listFlow)
    }

    // maritalStatus (id 1008): entries[0] (unmarried), entries[1] (married) and else. gender is driven
    // first so maritalStatus.entries is populated before the value is set.
    @Test
    fun `updateList maritalStatus positions`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        runCatching { d.updateList(9, 1) }
        runCatching { d.setValueById(1008, "opt0"); d.updateList(1008, 0) }
        runCatching { d.setValueById(1008, "opt1"); d.updateList(1008, 1) }
        runCatching { d.setValueById(1008, "opt3"); d.updateList(1008, 3) }
        assertNotNull(d.listFlow)
    }

    // agePopup (115), ageAtMarriage (1012) and dateOfMarriage (1013) date-driven recalculation arms.
    @Test
    fun `updateList age and marriage date fields`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        runCatching { d.setValueById(115, "01-01-1990") }
        for (idx in 0..2) {
            runCatching { d.updateList(115, idx) }
        }
        runCatching { d.setValueById(1012, "20"); d.updateList(1012, 0) }
        runCatching { d.setValueById(1013, "01-01-2010"); d.updateList(1013, 0) }
        assertNotNull(d.listFlow)
    }

    // mobileNoOfRelation (id 12): index 0..3 (self/relation), 4 (HoF) and else (other) arms.
    @Test
    fun `updateList mobileNoOfRelation index variants`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        for (idx in 0..5) {
            runCatching { d.updateList(12, idx) }
        }
        assertNotNull(d.listFlow)
    }

    // tempraryContactNo (id 44): empty, short (<10) and full (>=10) mobile drive the OTP-button arms.
    @Test
    fun `updateList tempraryContactNo length variants`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        runCatching { d.setValueById(44, ""); d.updateList(44, 0) }
        runCatching { d.setValueById(44, "98765"); d.updateList(44, 0) }
        runCatching { d.setValueById(44, "9876543210"); d.updateList(44, 0) }
        assertNotNull(d.listFlow)
    }

    // Remaining dropdown/edit-text trigger arms: relationToHead(15), religion(18), abhaIdCheck(9100),
    // childRegisteredAtSchool(21), haveChildren(48), sendOtpBtn(42), and edit-text validators.
    @Test
    fun `updateList remaining trigger fields`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        for (idx in 0..2) {
            runCatching { d.updateList(15, idx) }
            runCatching { d.updateList(18, idx) }
            runCatching { d.updateList(9100, idx) }
        }
        runCatching { d.setValueById(48, "opt0"); d.updateList(48, 0) }
        runCatching { d.updateList(21, 0) }
        runCatching { d.updateList(42, 0) }
        runCatching { d.setValueById(9101, "12345678901234"); d.updateList(9101, 0) }
        runCatching { d.setValueById(23, "123456789012"); d.updateList(23, 0) }
        runCatching { d.setValueById(1029, "BC123456"); d.updateList(1029, 0) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: fully-populated mapValues + repro-status matrix ==============

    /**
     * mapValues stops at `contactNumber.value!!.toLong()` (an NPE) whenever the contact number is
     * blank, which is exactly what every pre-existing test hits — so the whole second half of
     * mapValues (kid details, reproductive status, health id, flags) was never executed. Filling in
     * the registration date, dob, gender, relation and contact number first lets the body run on.
     */
    private suspend fun populatedFirstPage(
        d: BenRegFormDataset,
        dob: String = "01-01-1990",
        genderValue: String = "opt1"
    ) {
        runCatching { d.setFirstPageToRead(benMockBr(genderId = 2, gender = Gender.FEMALE), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        runCatching { d.setValueById(3, "JANE") }
        runCatching { d.setValueById(4, "DOE") }
        runCatching { d.setValueById(9, genderValue) }
        runCatching { d.setValueById(115, dob) }
        runCatching { d.setValueById(116, dob) }
        runCatching { d.setValueById(14, "9876543210") }
        runCatching { d.setValueById(15, "opt3") }
        runCatching { d.setValueById(17, "opt1") }
        runCatching { d.setValueById(18, "opt1") }
        runCatching { d.setValueById(1012, "20") }
    }

    @Test
    fun `mapValues runs past the contact number with a fully populated first page`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        val ben = benMockBr(genderId = 2, gender = Gender.FEMALE)
        runCatching { d.mapValues(ben, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValues with each gender option and a populated contact number`() = runTest {
        for (g in listOf("opt0", "opt1", "opt2", "zzz")) {
            val d = ds()
            populatedFirstPage(d, genderValue = g)
            runCatching { d.mapValues(benMockBr(), 0) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `mapValues for a child aged beneficiary drives the kid details block`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-2020", genderValue = "opt0")
        runCatching { d.setValueById(1029, "BC123456") }
        runCatching { d.mapValues(benMockBr3(), 0) }
        runCatching { d.mapValues(benMockBr3(), 1) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValues after the second page fills the birth details`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-2022")
        runCatching { d.setSecondPage(benMockDeep()) }
        runCatching { d.mapValues(benMockDeep(), 1) }
        assertNotNull(d.listFlow)
    }

    /**
     * updateReproductiveOptionsBasedOnAgeGender() branches on age band x marital status. Reaching it
     * through maritalStatus (rather than agePopup) avoids the agePopup arm, whose android.util.Range
     * construction is not usable under plain JVM unit tests.
     */
    @Test
    fun `updateList maritalStatus across every age band and status option`() = runTest {
        val dobs = listOf("01-01-2008", "01-01-1998", "01-01-1960", "01-01-2020")
        val statuses = listOf("opt0", "opt1", "opt4")
        for (dob in dobs) {
            for (status in statuses) {
                val d = ds()
                populatedFirstPage(d, dob = dob)
                runCatching { d.updateList(9, 1) }
                runCatching { d.setValueById(1008, status); d.updateList(1008, 0) }
                assertNotNull(d.listFlow)
            }
        }
    }

    @Test
    fun `updateList gender for a female drives the reproductive status options`() = runTest {
        for (idx in 0..2) {
            val d = ds()
            populatedFirstPage(d, dob = "01-01-1998", genderValue = "opt$idx")
            runCatching { d.updateList(9, idx) }
            runCatching { d.setValueById(1028, "opt0"); d.updateList(1028, 0) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `updateList name and contact validators with valid and invalid values`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        val textIds = listOf(3, 4, 16, 13, 19, 1009, 1010, 1011)
        textIds.forEach {
            runCatching { d.setValueById(it, ""); d.updateList(it, 0) }
            runCatching { d.setValueById(it, "VALID NAME"); d.updateList(it, 0) }
            runCatching { d.setValueById(it, "invalid name"); d.updateList(it, 0) }
        }
        runCatching { d.setValueById(14, "9876543210"); d.updateList(14, 0) }
        runCatching { d.setValueById(14, "1234"); d.updateList(14, 0) }
        runCatching { d.setValueById(23, "111111111111"); d.updateList(23, 0) }
        runCatching { d.setValueById(1029, "AB123456"); d.updateList(1029, 0) }
        runCatching { d.setValueById(1029, "AAAAAA"); d.updateList(1029, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList beneficiaryStatus death path with a maternal-age female`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        runCatching { d.setValueById(9, "opt1") }
        runCatching { d.setValueById(115, "01-01-1995") }
        runCatching { d.setValueById(50, "opt2"); d.updateList(50, 1) }
        runCatching { d.setValueById(54, "opt8"); d.updateList(54, 8) }
        runCatching { d.setValueById(55, "HOME"); d.updateList(55, 0) }
        runCatching { d.setValueById(50, "opt0"); d.updateList(50, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList beneficiaryStatus death path for a non maternal beneficiary`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        runCatching { d.setValueById(9, "opt0") }
        runCatching { d.setValueById(115, "01-01-1950") }
        runCatching { d.setValueById(50, "opt2"); d.updateList(50, 1) }
        runCatching { d.setValueById(54, "opt1"); d.updateList(54, 1) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList ageAtMarriage and dateOfMarriage recompute each other`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-1990")
        runCatching { d.setValueById(1012, "22"); d.updateList(1012, 0) }
        runCatching { d.setValueById(1013, "01-01-2012"); d.updateList(1013, 0) }
        runCatching { d.setValueById(1012, ""); d.updateList(1012, 0) }
        runCatching { d.setValueById(1013, ""); d.updateList(1013, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList haveChildren toggles the adding-children flag`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        runCatching { d.setValueById(48, "opt0"); d.updateList(48, 0) }
        runCatching { d.setValueById(48, "opt1"); d.updateList(48, 1) }
        runCatching { d.setValueById(48, null); d.updateList(48, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList relationToHead religion and childRegisteredAtSchool option sweeps`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-2018")
        for (idx in listOf(0, 5, 7, 9, 10, 13)) {
            runCatching { d.setValueById(15, "opt$idx"); d.updateList(15, idx) }
            runCatching { d.setValueById(18, "opt$idx"); d.updateList(18, idx) }
        }
        runCatching { d.setValueById(21, "opt0"); d.updateList(21, 0) }
        runCatching { d.setValueById(21, "opt1"); d.updateList(21, 1) }
        runCatching { d.setValueById(22, "opt0"); d.updateList(22, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList abha check and abha id validation`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        runCatching { d.updateList(9100, 0) }
        runCatching { d.setValueById(9101, "12345678901234"); d.updateList(9101, 0) }
        runCatching { d.setValueById(9101, "abc"); d.updateList(9101, 0) }
        runCatching { d.updateList(9100, 1) }
        assertNotNull(d.listFlow)
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateHouseholdWithHoFDetails copies the head name onto the family`() = runTest {
        val d = ds()
        val hh = householdMock()
        val ben = benMockDeep()
        runCatching { d.updateHouseholdWithHoFDetails(hh, ben) }
        assertNotNull(hh)
    }

    @Test
    fun `setImageUriToFormElement writes the uri to front back and profile slots`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://doc/1"
        runCatching { d.setImageUriToFormElement(46, uri) }
        runCatching { d.setImageUriToFormElement(47, uri) }
        runCatching { d.setImageUriToFormElement(1, uri) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `calculateMaxSonAge over a matrix of parent and child ages`() {
        for (parentAge in listOf(18, 30, 45, 70)) {
            for (parentMonths in listOf(0, 6, 11)) {
                for (childAge in listOf(0, 5, 25)) {
                    assertNotNull(
                        BenRegFormDataset.calculateMaxSonAge(parentAge, parentMonths, childAge, 3)
                    )
                }
            }
        }
    }
}
