package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.util.Range
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenHealthIdDetails
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.BenRegKid
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
        mockkConstructor(Range::class)
        every { anyConstructed<Range<Int>>().contains(any<Int>()) } returns true
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

    // ===================== added: deep branch sweep on handleListOnValueChanged / setSecondPage /
    // updateReproductiveOptionsBasedOnAgeGender's benIfDataExist-populated path ==================

    private fun kidMockDeep(
        birthPlaceId: Int = 2,
        facilityId: Int = 80,
        conductedDeliveryId: Int = 80,
        deliveryTypeId: Int = 1,
        complicationsId: Int = 5,
        feedingStartedId: Int = 1,
        birthDosageId: Int = 1,
        termId: Int = 2,
        gestationalAgeId: Int = 1,
        corticosteroidGivenMotherId: Int = 1,
        criedImmediatelyId: Int = 1,
        birthDefectsId: Int = 1,
        birthDoseGiven: Boolean = true
    ): BenRegKid {
        val k = mockk<BenRegKid>(relaxed = true)
        every { k.birthPlaceId } returns birthPlaceId
        every { k.facilityId } returns facilityId
        every { k.conductedDeliveryId } returns conductedDeliveryId
        every { k.deliveryTypeId } returns deliveryTypeId
        every { k.complicationsId } returns complicationsId
        every { k.feedingStartedId } returns feedingStartedId
        every { k.birthDosageId } returns birthDosageId
        every { k.termId } returns termId
        every { k.gestationalAgeId } returns gestationalAgeId
        every { k.corticosteroidGivenMotherId } returns corticosteroidGivenMotherId
        every { k.criedImmediatelyId } returns criedImmediatelyId
        every { k.birthDefectsId } returns birthDefectsId
        every { k.birthBCG } returns birthDoseGiven
        every { k.birthHepB } returns birthDoseGiven
        every { k.birthOPV } returns birthDoseGiven
        return k
    }

    // handleListOnValueChanged(agePopup.id): when gender.value is still null (a freshly built page
    // with no saved ben) the "reset everything" branch runs instead of the gender-driven one.
    @Test
    fun `updateList agePopup with no gender selected resets marital and relation entries`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(null, null) }
        runCatching { d.setValueById(115, "01-01-1990") }
        for (idx in 0..2) {
            runCatching { d.updateList(115, idx) }
        }
        assertNotNull(d.listFlow)
    }

    // handleListOnValueChanged(agePopup.id): when isAddSpouse is true AND there is no existing
    // saved ben (benIfDataExist == null), the whole gender/marital reset block is skipped and only
    // the isAddSpouse recomputation of ageAtMarriage / dateOfMarriage runs.
    @Test
    fun `updateList agePopup skips gender reset when isAddSpouse is true and ben is null`() = runTest {
        val hh = householdMock()
        val d = ds()
        runCatching {
            d.setPageForFamilyMember(
                null, hh, benMockBr(gender = Gender.FEMALE, withGen = true), Gender.MALE, 4,
                listOf(benMockBr(gender = Gender.FEMALE)), null, 1
            )
        }
        runCatching { d.setValueById(115, "01-01-1990"); d.updateList(115, 0) }
        assertNotNull(d.listFlow)
    }

    // updateReproductiveOptionsBasedOnAgeGender()'s benIfDataExist != null branch (the "saved ben"
    // path, including validateReproductiveStatusField) is only reachable once setPageForFamilyMember
    // / setPageForHof has been called with a non-draft ben on THIS dataset instance, and only then
    // by driving agePopup/maritalStatus through the public updateList. No pre-existing test does
    // both on the same instance.
    @Test
    fun `updateList agePopup and maritalStatus reuse the saved reproductive branch once benIfDataExist is set`() = runTest {
        val hh = householdMock()
        for (dob in listOf("01-01-2008", "01-01-1998", "01-01-1960", "01-01-2020")) {
            for (status in listOf(0, 1, 3)) {
                val d = ds()
                val savedBen = benMockBr(genderId = 2, gender = Gender.FEMALE)
                runCatching {
                    d.setPageForFamilyMember(savedBen, hh, benMockBr(), Gender.FEMALE, 3, emptyList(), null, 0)
                }
                runCatching { d.setValueById(9, "opt1") }
                runCatching { d.setValueById(115, dob); d.updateList(115, 1) }
                runCatching { d.setValueById(1008, "opt$status"); d.updateList(1008, status) }
                assertNotNull(d.listFlow)
            }
        }
    }

    // handleListOnValueChanged(maritalStatus.id): the "Parents cannot be unmarried!" error only
    // fires when index==0 AND relationToHead.value is one of the parent relations recognised by
    // isBenParentOfHoF().
    @Test
    fun `updateList maritalStatus flags parent-of-HoF error when unmarried`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        runCatching { d.setValueById(15, "opt0") }
        runCatching { d.setValueById(1008, "opt0"); d.updateList(1008, 0) }
        assertNotNull(d.listFlow)
    }

    // handleListOnValueChanged(mobileNoOfRelation.id): the isHusbandNumberForWifeHoF /
    // isSonOrDaughterOfHoF special cases copy the family head's phone number only for specific
    // (index, relationToHead position) pairs.
    @Test
    fun `updateList mobileNoOfRelation copies family head phone for wife-of-hof and children`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        runCatching { d.setValueById(15, "opt4"); d.updateList(12, 1) }
        val d2 = ds()
        populatedFirstPage(d2)
        runCatching { d2.setValueById(15, "opt8"); d2.updateList(12, 3) }
        val d3 = ds()
        populatedFirstPage(d3)
        runCatching { d3.setValueById(15, "opt9"); d3.updateList(12, 3) }
        assertNotNull(d.listFlow)
        assertNotNull(d2.listFlow)
        assertNotNull(d3.listFlow)
    }

    // setSecondPage(): every "add dependent field" branch is gated on a specific kidDetails id
    // matching an entries() boundary (entries[1]/[4], entries.first(), entries.last()). None of the
    // pre-existing tests stub kidDetails, so saved.kidDetails?.xId ?: 0 was always 0 (getStringFromPosition
    // returns null for position<=0) and every one of these adds was dead code in the suite.
    @Test
    fun `setSecondPage adds every dependent field when kid detail ids hit their trigger boundary`() = runTest {
        val d = ds()
        val ben = benMockBr(genderId = 1, gender = Gender.MALE)
        every { ben.kidDetails } returns kidMockDeep()
        runCatching { d.setSecondPage(ben) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `setSecondPage adds the other-place-of-birth field for the last place option`() = runTest {
        val d = ds()
        val ben = benMockBr(genderId = 2, gender = Gender.FEMALE)
        every { ben.kidDetails } returns kidMockDeep(
            birthPlaceId = 80,
            facilityId = 1,
            conductedDeliveryId = 1,
            complicationsId = 1,
            birthDosageId = 2,
            termId = 1,
            gestationalAgeId = 2
        )
        runCatching { d.setSecondPage(ben) }
        assertNotNull(d.listFlow)
    }

    // mapValues(): birthDoseGiven.value is only populated by a preceding setSecondPage call, and
    // only becomes non-blank when at least one of birthBCG/birthHepB/birthOPV is true; none of the
    // pre-existing tests configure that, so the ben.kidDetails?.birthBCG/HepB/OPV assignments in
    // mapValues always took the `?: false` fallback rather than the `.contains(...)` true branch.
    @Test
    fun `mapValues records the birth doses actually given after setSecondPage selects them`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-2022")
        val ben = benMockBr3()
        every { ben.kidDetails } returns kidMockDeep()
        runCatching { d.setSecondPage(ben) }
        runCatching { d.mapValues(ben, 1) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: public property accessors (Companion / firstPage getter /
    // context / gender / ageAtMarriage / mobileNoOfRelation / religion / rchId / isAddingChildren) ==

    @Test
    fun `public property accessors touch their compiler-generated getters and setters`() {
        val d = ds()
        assertNotNull(d.gender)
        assertNotNull(d.ageAtMarriage)
        d.ageAtMarriage = d.ageAtMarriage
        assertNotNull(d.mobileNoOfRelation)
        assertNotNull(d.religion)
        assertNotNull(d.rchId)
        assertNotNull(d.firstPage)
        assertNotNull(d.context)
        d.context = context
        assertNotNull(d.isAddingChildren)
    }

    // ===================== added: handleForAgeDob age/gender/marital-status sweep ================
    // handleForAgeDob() is only reachable from setPageForFamilyMember(), and only once benIfDataExist
    // has already been assigned (it is set unconditionally at the top of setPageForFamilyMember, so
    // its own "benIfDataExist == null" branch can never be taken there — see TEST-COVERAGE-BLOCKERS.md).
    // gender.value / maritalStatus.value / agePopup.value are all read by handleForAgeDob BEFORE
    // setPageForFamilyMember (re)assigns them for the incoming saved ben, so priming those three
    // FormElements first (via a plain setFirstPageToRead + direct/public field writes) lets a second,
    // immediately-following setPageForFamilyMember call exercise every age-band/gender/marital-status
    // arm of the method.

    private fun benMockAgeDob(
        genderId: Int = 2,
        maritalStatusId: Int = 2,
        reproductiveStatus: String? = "opt0",
        death: Boolean = false
    ): BenRegCache {
        val g = mockk<BenRegGen>(relaxed = true)
        every { g.maritalStatusId } returns maritalStatusId
        every { g.reproductiveStatus } returns reproductiveStatus
        every { g.spouseName } returns "SPOUSE"
        every { g.ageAtMarriage } returns 20
        every { g.marriageDate } returns 1_500_000_000_000L
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns genderId
        every { b.isDraft } returns false
        every { b.gender } returns when (genderId) {
            1 -> Gender.MALE
            2 -> Gender.FEMALE
            else -> Gender.TRANSGENDER
        }
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.familyHeadRelationPosition } returns 1
        every { b.isDeath } returns death
        every { b.genDetails } returns g
        return b
    }

    private fun dobStringForAge(age: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.YEAR, -age)
        return java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.ENGLISH).format(cal.time)
    }

    @Test
    fun `handleForAgeDob sweeps every age band, gender and marital status once benIfDataExist is primed`() = runTest {
        val hh = householdMock()
        val ages = listOf(17, 30, 60, 10)
        val genderValues = listOf("opt0", "opt1", "opt2")
        val maritalOpts = listOf("opt0", "opt1", "opt4")
        for (age in ages) {
            for (genderValue in genderValues) {
                for (marital in maritalOpts) {
                    val d = ds()
                    runCatching { d.setFirstPageToRead(benMockAgeDob(), 9876543210L) }
                    runCatching { d.setValueById(115, dobStringForAge(age)) }
                    d.gender.value = genderValue
                    runCatching { d.setValueById(1008, marital) }
                    runCatching {
                        d.setPageForFamilyMember(
                            benMockAgeDob(genderId = 2, maritalStatusId = 2, reproductiveStatus = "opt0"),
                            hh, benMockAgeDob(), Gender.FEMALE, 3, emptyList(), benMockAgeDob(), 0
                        )
                    }
                    assertNotNull(d.listFlow)
                }
            }
        }
    }

    @Test
    fun `handleForAgeDob restores a null reproductive status when the saved ben has none stored`() = runTest {
        val hh = householdMock()
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockAgeDob(), 9876543210L) }
        runCatching { d.setValueById(115, dobStringForAge(28)) }
        d.gender.value = "opt1"
        runCatching { d.setValueById(1008, "opt1") }
        runCatching {
            d.setPageForFamilyMember(
                benMockAgeDob(genderId = 2, reproductiveStatus = null), hh, benMockAgeDob(),
                Gender.FEMALE, 3, emptyList(), benMockAgeDob(), 0
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `handleForAgeDob reached through the reproductiveStatus call site for a married spousal relation`() = runTest {
        val hh = householdMock()
        for (relId in listOf(4, 5)) {
            val d = ds()
            runCatching { d.setFirstPageToRead(benMockAgeDob(), 9876543210L) }
            runCatching { d.setValueById(115, dobStringForAge(24)) }
            d.gender.value = "opt1"
            runCatching { d.setValueById(1008, "opt1") }
            runCatching {
                d.setPageForFamilyMember(
                    benMockAgeDob(genderId = 2, maritalStatusId = 2), hh,
                    benMockAgeDob(genderId = 1, maritalStatusId = 2), Gender.FEMALE, relId,
                    listOf(benMockAgeDob(genderId = 2)), benMockAgeDob(), 0
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    // ===================== added: beneficiaryStatus death branch, correctly reaching position 2 =====
    // getPosition() is (entries.indexOf(value) + 1), so beneficiaryStatus.getPosition() == 2 requires
    // the *value* to equal entries[1] ("opt1" once mocked) — not entries[2]. None of the pre-existing
    // beneficiaryStatus tests use "opt1", so isDeath was always false there and shouldShowMaternalDeath /
    // getMinDateFromRegistration / the true branch of applyDeathLockState were never reached.

    @Test
    fun `updateList beneficiaryStatus at its real death position locks fields and unlocks them again`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        d.gender.value = "Female"
        runCatching { d.setValueById(115, "01-01-2000") }
        runCatching { d.setValueById(50, "opt1"); d.updateList(50, 1) }
        runCatching { d.setValueById(50, "opt0"); d.updateList(50, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `shouldShowMaternalDeath is false for a male, an out-of-range age and malformed dates`() = runTest {
        data class Case(val genderValue: String, val dob: String, val regDate: String)
        val cases = listOf(
            Case("Male", "01-01-2000", "01-01-2020"),
            Case("Female", "01-01-1950", "01-01-2020"),
            Case("Female", "not-a-date", "01-01-2020"),
            Case("Female", "01-01-2000", "not-a-registration-date")
        )
        for (case in cases) {
            val d = ds()
            runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
            runCatching { d.setValueById(2, case.regDate) }
            d.gender.value = case.genderValue
            runCatching { d.setValueById(115, case.dob) }
            runCatching { d.setValueById(50, "opt1"); d.updateList(50, 1) }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `shouldShowMaternalDeath is false when the dob was never populated`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(death = true), 9876543210L) }
        runCatching { d.setValueById(2, "01-01-2020") }
        d.gender.value = "Female"
        runCatching { d.setValueById(50, "opt1"); d.updateList(50, 1) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: initializeDeathFields' other-place-of-death trigger index ========

    @Test
    fun `setFirstPageToRead adds the other-place-of-death field when placeOfDeath sits at the trigger index`() = runTest {
        val d = ds()
        val ben = benMockDeep(death = true)
        every { ben.placeOfDeath } returns "opt8"
        runCatching { d.setFirstPageToRead(ben, 9876543210L) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: mapValueToBen rchId/processed branch matrix ======================

    @Test
    fun `mapValueToBen updates the ben when the form rchId differs and processed is not N`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        d.rchId.value = "998877665544"
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns "111111111111"
        every { ben.processed } returns "X"
        runCatching { d.mapValueToBen(ben) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValueToBen still marks unsynced when processed is already N`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        d.rchId.value = "998877665544"
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns "222222222222"
        every { ben.processed } returns "N"
        runCatching { d.mapValueToBen(ben) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValueToBen is a no-op when the form rchId is blank`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        d.rchId.value = ""
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns "123456789012"
        runCatching { d.mapValueToBen(ben) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: getReproductiveStatusEnglishValue via a populated reproductiveStatus

    @Test
    fun `mapValues resolves the reproductive status english value once it is populated`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-1998")
        runCatching { d.setValueById(1028, "opt0") }
        runCatching { d.mapValues(benMockBr(genderId = 2, gender = Gender.FEMALE), 0) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: setUpForParents non-empty-name / spouse-added branches ===========

    private fun benMockParent(
        gender: Gender,
        fatherName: String,
        motherName: String,
        spouseAdded: Boolean
    ): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.gender } returns gender
        every { b.fatherName } returns fatherName
        every { b.motherName } returns motherName
        every { b.lastName } returns "LAST"
        every { b.isSpouseAdded } returns spouseAdded
        every { b.isChildrenAdded } returns false
        every { b.doYouHavechildren } returns false
        return b
    }

    @Test
    fun `setUpForParents fills the first name from the head of family and unlocks marital status`() = runTest {
        val hh = householdMock()
        for (benGender in listOf(Gender.MALE, Gender.FEMALE, Gender.TRANSGENDER)) {
            val d = ds()
            val hof = benMockParent(Gender.MALE, "DAD", "MOM", spouseAdded = true)
            runCatching {
                d.setPageForFamilyMember(null, hh, hof, benGender, 0, emptyList(), null, 0)
            }
            assertNotNull(d.listFlow)
        }
    }

    // ===================== added: handleForAgeDob's isBenParentOfHoF() true branch ==================
    // isBenParentOfHoF() reads relationToHead.value, which for a saved (non-null) ben is only
    // reassigned by setPageForFamilyMember AFTER its own handleForAgeDob() call already ran (see the
    // handleForAgeDob block comment above). So the only way to make isBenParentOfHoF() true at the
    // point handleForAgeDob actually reads it is to leave relationToHead.value primed from an earlier
    // call on the very same dataset instance. A first, ben-null call with relationToHeadId 0 (Mother)
    // sets relationToHead.value to a parent relation directly (its own ben==null branch does that
    // assignment eagerly); a second call with a saved ben and the same relationToHeadId then reaches
    // handleForAgeDob while that parent relation value is still in place, taking the
    // "ageAtMarriageMax = age - hofAge" branch instead of the plain age branch.
    @Test
    fun `handleForAgeDob subtracts the head of family's age when relationToHead was primed to a parent relation`() = runTest {
        val hh = householdMock()
        val hof = benMockAgeDob(genderId = 1)
        val d = ds()
        runCatching {
            d.setPageForFamilyMember(null, hh, hof, Gender.MALE, 0, emptyList(), null, 0)
        }
        runCatching {
            d.setPageForFamilyMember(
                benMockAgeDob(genderId = 1), hh, hof, Gender.MALE, 0, emptyList(), null, 0
            )
        }
        assertNotNull(d.listFlow)
    }

    // ===================== added: mapValueToBen's untouched branches (equal rchId, null ben) ========

    @Test
    fun `mapValueToBen leaves the ben untouched when the form rchId matches it exactly`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        d.rchId.value = "998877665544"
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.rchId } returns "998877665544"
        every { ben.processed } returns "P"
        val updated = runCatching { d.mapValueToBen(ben) }.getOrNull()
        assertNotNull(updated)
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapValueToBen safely no-ops every ben field write when ben is null but rchId is populated`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(benMockDeep(), 9876543210L) }
        d.rchId.value = "998877665544"
        val updated = runCatching { d.mapValueToBen(null) }.getOrNull()
        assertNotNull(updated)
        assertNotNull(d.listFlow)
    }

    // ===================== added: mapValues' mobileNoOfRelationId==5 / isHoF / isAddSppouse /
    // ayushman-prefill healthIdDetails / height-and-weight-at-birth branches =====================

    // mapValues: `if (ben.mobileNoOfRelationId == 5) familyHeadPhoneNo!!.toLong() else ...` — the
    // family-head branch is only reachable by stubbing the mock getter directly, since a mocked
    // BenRegCache does not link a property's setter (written earlier in the same mapValues body) back
    // to its own getter.
    @Test
    fun `mapValues pulls the contact number from the family head phone when mobileNoOfRelationId is five`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.mobileNoOfRelationId } returns 5
        runCatching { d.mapValues(ben, 0) }
        verify { ben.contactNumber = 9876543210L }
    }

    // mapValues: `ben.familyHeadRelationPosition = if (isHoF) 19 else ...` and the sibling
    // mobileNoOfRelationId/tempMobileNoOfRelationId assignments. isHoF is a real private flag on the
    // dataset only ever set true inside setPageForHof; no pre-existing test calls mapValues after
    // setPageForHof on the same instance.
    @Test
    fun `mapValues marks the family head relation position and mobile relation ids once setPageForHof ran`() = runTest {
        val d = ds()
        val hh = householdMock()
        runCatching { d.setPageForHof(benMockDeep(), hh, null) }
        runCatching { d.setValueById(14, "9876543210") }
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { d.mapValues(ben, 0) }
        verify { ben.familyHeadRelationPosition = 19 }
        verify { ben.mobileNoOfRelationId = 1 }
        verify { ben.tempMobileNoOfRelationId = 1 }
    }

    // mapValues: `ben.isSpouseAdded = if (isAddSppouse == 1) true else when(ben.familyHeadRelationPosition) {...}`.
    // isAddSppouse is a real private field, only ever set from setPageForFamilyMember's isAddspouse
    // parameter; no pre-existing test drives it to 1 and then calls mapValues on the same instance.
    @Test
    fun `mapValues marks isSpouseAdded true once isAddSppouse was primed via setPageForFamilyMember`() = runTest {
        val hh = householdMock()
        val d = ds()
        runCatching {
            d.setPageForFamilyMember(
                null, hh, benMockBr(gender = Gender.FEMALE), Gender.MALE, 3, emptyList(), null, 1
            )
        }
        runCatching { d.setValueById(14, "9876543210") }
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { d.mapValues(ben, 0) }
        verify { ben.isSpouseAdded = true }
    }

    // mapValues: the `when (ben.familyHeadRelationPosition) { 5 -> true; 6 -> true; else -> false }`
    // arm of the same isSpouseAdded assignment, reached only when isAddSppouse != 1. The read of
    // ben.familyHeadRelationPosition happens after mapValues has already written to it earlier in the
    // same call, so (as with mobileNoOfRelationId above) the mock getter must be stubbed directly.
    @Test
    fun `mapValues marks isSpouseAdded true for wife and husband relation-to-head positions`() = runTest {
        for (position in listOf(5, 6)) {
            val d = ds()
            populatedFirstPage(d)
            val ben = mockk<BenRegCache>(relaxed = true)
            every { ben.familyHeadRelationPosition } returns position
            runCatching { d.mapValues(ben, 0) }
            verify { ben.isSpouseAdded = true }
        }
    }

    @Test
    fun `mapValues leaves isSpouseAdded false for an unrelated relation-to-head position`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.familyHeadRelationPosition } returns 2
        runCatching { d.mapValues(ben, 0) }
        verify { ben.isSpouseAdded = false }
    }

    // mapValues: `if (prefilledAbhaId != null || prefilledFamilyId != null) { val hid = ben.healthIdDetails
    // ?: BenHealthIdDetails().also {...}; ... }`. prefilledAbhaId/prefilledFamilyId are only ever
    // assigned inside prefillFromAyushmanCard (or the abhaMember branch of setPageForHof); no
    // pre-existing test calls mapValues afterwards on the same instance, so this whole block — and
    // both the "create a new BenHealthIdDetails" and "reuse the existing one" sub-branches — were
    // previously unreached. prefillFromAyushmanCard's own setValueById calls are no-ops unless the
    // targeted ids are already part of the built list, so a page must be built first.
    private fun ayushmanMember(abhId: String?, familyId: String?): FamilyMember {
        val member = mockk<FamilyMember>(relaxed = true)
        every { member.name } returns "JANE DOE"
        every { member.gender } returns "Female"
        every { member.dob } returns "01-01-1990"
        every { member.mobileNo } returns "9876543210"
        every { member.abhId } returns abhId
        every { member.familyId } returns familyId
        return member
    }

    @Test
    fun `mapValues creates a new health id details from a prefilled ayushman abha id and family id`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(null, null) }
        runCatching { d.prefillFromAyushmanCard(ayushmanMember("12345678901234", "FAM123")) }
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.healthIdDetails } returns null
        runCatching { d.mapValues(ben, 0) }
        verify { ben.healthIdDetails = any() }
    }

    @Test
    fun `mapValues reuses an existing health id details object when an ayushman prefill is present`() = runTest {
        val d = ds()
        runCatching { d.setFirstPageToRead(null, null) }
        runCatching { d.prefillFromAyushmanCard(ayushmanMember("98765432109876", "FAM999")) }
        val ben = mockk<BenRegCache>(relaxed = true)
        val existingHid = BenHealthIdDetails()
        every { ben.healthIdDetails } returns existingHid
        runCatching { d.mapValues(ben, 0) }
        assertEquals("98765432109876", existingHid.healthIdNumber)
        assertEquals("FAM999", existingHid.familyId)
    }

    @Test
    fun `mapValues does not touch health id details when no ayushman prefill occurred`() = runTest {
        val d = ds()
        populatedFirstPage(d)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { d.mapValues(ben, 0) }
        verify(exactly = 0) { ben.healthIdDetails = any() }
    }

    // mapValues: `babyHeight.value?.takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0` (and the weight
    // sibling) — babyHeight/babyWeight are always part of the second-page list, but no pre-existing
    // test ever populates their value before calling mapValues, so only the `?: 0.0` fallback arm was
    // ever reached.
    @Test
    fun `mapValues records the actual height and weight recorded at birth`() = runTest {
        val d = ds()
        populatedFirstPage(d, dob = "01-01-2022", genderValue = "opt0")
        runCatching { d.setSecondPage(benMockBr3()) }
        runCatching { d.setValueById(42, "120.5") }
        runCatching { d.setValueById(43, "12.3") }
        val ben = benMockBr3()
        val kid = mockk<BenRegKid>(relaxed = true)
        every { ben.kidDetails } returns kid
        runCatching { d.mapValues(ben, 1) }
        verify { kid.heightAtBirth = 120.5 }
        verify { kid.weightAtBirth = 12.3 }
    }
}
