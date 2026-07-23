package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.res.Resources
import android.text.TextUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.BenPncDomain
import org.piramalswasthya.sakhi.model.BenWithAdolescentDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain
import org.piramalswasthya.sakhi.model.BenWithEcrDomain
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.model.BenWithHRNPADomain
import org.piramalswasthya.sakhi.model.BenWithHRNPTListDomain
import org.piramalswasthya.sakhi.model.BenWithHRPADomain
import org.piramalswasthya.sakhi.model.BenWithHRPTListDomain
import org.piramalswasthya.sakhi.model.BenWithMalariaConfirmedDomain
import org.piramalswasthya.sakhi.model.BenWithPwrDomain
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain
import org.piramalswasthya.sakhi.model.BenWithTbSuspectedDomain
import org.piramalswasthya.sakhi.model.ChildRegDomain
import org.piramalswasthya.sakhi.model.ECTDomain
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.GeneralOPEDBeneficiary
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.InfantRegDomain
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.PregnantWomenVisitDomain
import org.piramalswasthya.sakhi.model.ReferalCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommonUtilsTest {

    @Before
    fun setUp() {
        mockkStatic(TextUtils::class)
        every { TextUtils.isDigitsOnly(any()) } answers {
            firstArg<CharSequence>().all { it.isDigit() }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // --- getAgeFromDob ---

    @Test
    fun `getAgeFromDob returns correct age for adult`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -25)
        val age = getAgeFromDob(cal.timeInMillis)
        assertEquals(25, age)
    }

    @Test
    fun `getAgeFromDob returns 0 for newborn`() {
        val age = getAgeFromDob(System.currentTimeMillis())
        assertEquals(0, age)
    }

    @Test
    fun `getAgeFromDob returns 0 for null`() {
        val age = getAgeFromDob(null)
        assertEquals(0, age)
    }

    @Test
    fun `getAgeFromDob returns correct age for elderly`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -80)
        val age = getAgeFromDob(cal.timeInMillis)
        assertEquals(80, age)
    }

    @Test
    fun `getAgeFromDob returns correct age for child`() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -5)
        val age = getAgeFromDob(cal.timeInMillis)
        assertEquals(5, age)
    }

    // --- filterBenList by text ---

    @Test
    fun `filterBenList with empty query returns full list`() {
        val list = listOf(createBen(name = "Asha"), createBen(name = "Priya"))
        val result = filterBenList(list, "")
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBenList matches by name`() {
        val list = listOf(
            createBen(name = "Asha Devi"),
            createBen(name = "Priya Singh"),
            createBen(name = "Asha Kumari")
        )
        val result = filterBenList(list, "Asha")
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBenList matches by partial name case insensitive`() {
        val list = listOf(createBen(name = "ASHA DEVI"), createBen(name = "Priya"))
        val result = filterBenList(list, "asha")
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList matches by mobile number`() {
        val list = listOf(
            createBen(name = "Asha", mobileNo = "9876543210"),
            createBen(name = "Priya", mobileNo = "1234567890")
        )
        val result = filterBenList(list, "9876")
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList with no match returns empty`() {
        val list = listOf(createBen(name = "Asha"), createBen(name = "Priya"))
        val result = filterBenList(list, "Nonexistent")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList on empty list returns empty`() {
        val result = filterBenList(emptyList(), "Asha")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList matches by benId`() {
        val list = listOf(createBen(benId = 12345, name = "Asha"))
        val result = filterBenList(list, "12345")
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList matches by family head name`() {
        val list = listOf(
            createBen(name = "Asha", familyHeadName = "Raman Kumar"),
            createBen(name = "Priya", familyHeadName = "Suresh")
        )
        val result = filterBenList(list, "raman")
        assertEquals(1, result.size)
    }

    // --- filterBenList by type ---

    @Test
    fun `filterBenList type 1 returns only ABHA present`() {
        val list = listOf(
            createBen(name = "WithAbha", abhaId = "ABHA123"),
            createBen(name = "WithoutAbha", abhaId = null)
        )
        val result = filterBenList(list, 1)
        assertEquals(1, result.size)
        assertEquals("WithAbha", result[0].benName)
    }

    @Test
    fun `filterBenList type 2 returns only ABHA absent`() {
        val list = listOf(
            createBen(name = "WithAbha", abhaId = "ABHA123"),
            createBen(name = "WithoutAbha", abhaId = null)
        )
        val result = filterBenList(list, 2)
        assertEquals(1, result.size)
        assertEquals("WithoutAbha", result[0].benName)
    }

    @Test
    fun `filterBenList type 3 returns age 30 and above alive`() {
        val cal30 = Calendar.getInstance().apply { add(Calendar.YEAR, -30) }
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(name = "Above30", dob = cal30.timeInMillis, isDeathValue = "false"),
            createBen(name = "Below30", dob = cal25.timeInMillis, isDeathValue = "false")
        )
        val result = filterBenList(list, 3)
        assertEquals(1, result.size)
        assertEquals("Above30", result[0].benName)
    }

    @Test
    fun `filterBenList type 0 returns all`() {
        val list = listOf(createBen(name = "A"), createBen(name = "B"))
        val result = filterBenList(list, 0)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterBenList type 4 WARA returns eligible women`() {
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(
                name = "EligibleWoman",
                gender = "Female",
                dob = cal25.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            ),
            createBen(
                name = "Male",
                gender = "Male",
                dob = cal25.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertEquals(1, result.size)
        assertEquals("EligibleWoman", result[0].benName)
    }

    @Test
    fun `filterBenList WARA rejects age below 20`() {
        val cal18 = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        val list = listOf(
            createBen(
                name = "YoungWoman",
                gender = "Female",
                dob = cal18.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList WARA rejects age above 49`() {
        val cal50 = Calendar.getInstance().apply { add(Calendar.YEAR, -50) }
        val list = listOf(
            createBen(
                name = "OlderWoman",
                gender = "Female",
                dob = cal50.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList WARA rejects dead beneficiary`() {
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(
                name = "DeadWoman",
                gender = "Female",
                dob = cal25.timeInMillis,
                isDeathValue = "true",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterBenList WARA accepts reproductive status 2`() {
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(
                name = "ReproTwo",
                gender = "Female",
                dob = cal25.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 2
            )
        )
        val result = filterBenList(list, 4)
        assertEquals(1, result.size)
    }

    @Test
    fun `filterBenList WARA rejects male gender`() {
        val cal25 = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }
        val list = listOf(
            createBen(
                name = "Man",
                gender = "Male",
                dob = cal25.timeInMillis,
                isDeathValue = "false",
                reproductiveStatusId = 1
            )
        )
        val result = filterBenList(list, 4)
        assertTrue(result.isEmpty())
    }

    // --- Helper: Create BenBasicDomain for tests (real object) ---

    private fun createBen(
        benId: Long = 1L,
        hhId: Long = 100L,
        name: String = "Test",
        gender: String = "Female",
        dob: Long = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }.timeInMillis,
        mobileNo: String = "9999999999",
        familyHeadName: String = "Head",
        abhaId: String? = null,
        isDeathValue: String? = "false",
        reproductiveStatusId: Int = 1
    ): BenBasicDomain {
        return BenBasicDomain(
            benId = benId,
            hhId = hhId,
            reproductiveStatusId = reproductiveStatusId,
            regDate = "17-03-2026",
            benName = name,
            gender = gender,
            dob = dob,
            relToHeadId = 1,
            mobileNo = mobileNo,
            familyHeadName = familyHeadName,
            abhaId = abhaId,
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false,
            isDeathValue = isDeathValue
        )
    }

    // ---------------------------------------------------------------
    // Builders (mockk based)
    // ---------------------------------------------------------------

    private fun mkBen(
        benId: Long = 0L,
        benFullName: String = "",
        benName: String = "",
        benSurname: String? = null,
        familyHeadName: String = "",
        age: String = "",
        mobileNo: String = "",
        rchId: String? = null,
        spouseName: String? = null,
        fatherName: String? = null,
        motherName: String? = null,
        gender: String = "",
        regDate: String = "",
        hhId: Long = 0L,
        abhaId: String? = null,
        dob: Long = 0L
    ): BenBasicDomain {
        val b = mockk<BenBasicDomain>(relaxed = true)
        every { b.benId } returns benId
        every { b.benFullName } returns benFullName
        every { b.benName } returns benName
        every { b.benSurname } returns benSurname
        every { b.familyHeadName } returns familyHeadName
        every { b.age } returns age
        every { b.mobileNo } returns mobileNo
        every { b.rchId } returns rchId
        every { b.spouseName } returns spouseName
        every { b.fatherName } returns fatherName
        every { b.motherName } returns motherName
        every { b.gender } returns gender
        every { b.regDate } returns regDate
        every { b.hhId } returns hhId
        every { b.abhaId } returns abhaId
        every { b.dob } returns dob
        return b
    }

    // ===============================================================
    // filterForBen branch coverage (via filterBenList text overload)
    // ===============================================================

    @Test fun `filterForBen matches by gender exact`() {
        val list = listOf(mkBen(gender = "female"), mkBen(gender = "male"))
        assertEquals(1, filterBenList(list, "female").size)
    }

    @Test fun `filterForBen matches by spouse name`() {
        val list = listOf(mkBen(spouseName = "Ramesh"), mkBen(spouseName = "Ganesh"))
        assertEquals(1, filterBenList(list, "ramesh").size)
    }

    @Test fun `filterForBen matches by father name`() {
        val list = listOf(mkBen(fatherName = "Suresh"), mkBen(fatherName = "Mahesh"))
        assertEquals(1, filterBenList(list, "suresh").size)
    }

    @Test fun `filterForBen matches by abhaId ignoring dashes`() {
        val list = listOf(mkBen(abhaId = "12-34-56"), mkBen(abhaId = "99-99-99"))
        assertEquals(1, filterBenList(list, "123456").size)
    }

    @Test fun `filterForBen matches by regDate`() {
        val list = listOf(mkBen(regDate = "17-03-2026"), mkBen(regDate = "01-01-2020"))
        assertEquals(1, filterBenList(list, "17-03-2026").size)
    }

    @Test fun `filterForBen matches by age`() {
        val list = listOf(mkBen(age = "25years"), mkBen(age = "10years"))
        assertEquals(1, filterBenList(list, "25years").size)
    }

    @Test fun `filterForBen matches by hhId`() {
        val list = listOf(mkBen(hhId = 555L), mkBen(hhId = 111L))
        assertEquals(1, filterBenList(list, "555").size)
    }

    // ===============================================================
    // filterBenList(list, rchPresent: Boolean)
    // ===============================================================

    @Test fun `filterBenList rchPresent false returns all`() {
        val list = listOf(mkBen(rchId = "123"), mkBen(rchId = null))
        assertEquals(2, filterBenList(list, false).size)
    }

    @Test fun `filterBenList rchPresent true keeps only digit rchId`() {
        val list = listOf(mkBen(rchId = "123"), mkBen(rchId = null))
        val result = filterBenList(list, true)
        assertEquals(1, result.size)
    }

    // ===============================================================
    // filterOPDBenList / filterOPD
    // ===============================================================

    private fun mkOpd(name: String?): GeneralOPEDBeneficiary {
        val o = mockk<GeneralOPEDBeneficiary>(relaxed = true)
        every { o.benName } returns name
        return o
    }

    @Test fun `filterOPDBenList empty text returns all`() {
        val list = listOf(mkOpd("Asha"), mkOpd("Priya"))
        assertEquals(2, filterOPDBenList(list, "").size)
    }

    @Test fun `filterOPDBenList matches by name case insensitive`() {
        val list = listOf(mkOpd("Asha Devi"), mkOpd("Priya Singh"))
        assertEquals(1, filterOPDBenList(list, "ASHA").size)
    }

    @Test fun `filterOPDBenList no match returns empty`() {
        val list = listOf(mkOpd("Asha"), mkOpd("Priya"))
        assertTrue(filterOPDBenList(list, "xyz").isEmpty())
    }

    // ===============================================================
    // filterAdolescentList
    // ===============================================================

    private fun mkAdolescent(ben: BenBasicDomain): BenWithAdolescentDomain {
        val a = mockk<BenWithAdolescentDomain>(relaxed = true)
        every { a.ben } returns ben
        return a
    }

    @Test fun `filterAdolescentList identity overload returns same list`() {
        val list = listOf(mkAdolescent(mkBen()), mkAdolescent(mkBen()))
        assertEquals(2, filterAdolescentList(list).size)
    }

    @Test fun `filterAdolescentList empty text returns all`() {
        val list = listOf(mkAdolescent(mkBen(benFullName = "Asha")))
        assertEquals(1, filterAdolescentList(list, "").size)
    }

    @Test fun `filterAdolescentList matches by name`() {
        val list = listOf(
            mkAdolescent(mkBen(benFullName = "Asha Devi")),
            mkAdolescent(mkBen(benFullName = "Priya Singh"))
        )
        assertEquals(1, filterAdolescentList(list, "asha").size)
    }

    @Test fun `filterAdolescentList matches by mobile`() {
        val list = listOf(
            mkAdolescent(mkBen(benFullName = "Asha", mobileNo = "9876543210")),
            mkAdolescent(mkBen(benFullName = "Priya", mobileNo = "1112223334"))
        )
        assertEquals(1, filterAdolescentList(list, "9876").size)
    }

    @Test fun `filterAdolescentList no match returns empty`() {
        val list = listOf(mkAdolescent(mkBen(benFullName = "Asha")))
        assertTrue(filterAdolescentList(list, "zzz").isEmpty())
    }

    // ===============================================================
    // filterBenFormList(PregnantWomenVisitDomain, filterText)
    // ===============================================================

    private fun mkPwVisit(
        benId: Long = 0L,
        name: String = "",
        familyHeadName: String = "",
        age: String = "",
        spouseName: String = "",
        weeks: Int = 0
    ): PregnantWomenVisitDomain {
        val p = mockk<PregnantWomenVisitDomain>(relaxed = true)
        every { p.benId } returns benId
        every { p.name } returns name
        every { p.familyHeadName } returns familyHeadName
        every { p.age } returns age
        every { p.spouseName } returns spouseName
        every { p.weeksOfPregnancy } returns weeks
        return p
    }

    @Test fun `filterBenFormList pwVisit matches by name`() {
        val list = listOf(mkPwVisit(name = "Asha"), mkPwVisit(name = "Priya"))
        assertEquals(1, filterBenFormList(list, "asha").size)
    }

    @Test fun `filterBenFormList pwVisit matches by weeks of pregnancy`() {
        val list = listOf(mkPwVisit(name = "Asha", weeks = 24), mkPwVisit(name = "Priya", weeks = 10))
        assertEquals(1, filterBenFormList(list, "24").size)
    }

    @Test fun `filterBenFormList pwVisit no match returns empty`() {
        val list = listOf(mkPwVisit(name = "Asha", weeks = 24))
        assertTrue(filterBenFormList(list, "zzz").isEmpty())
    }

    // ===============================================================
    // filterEcTrackingList
    // ===============================================================

    private fun mkEct(
        ben: BenBasicDomain = mkBen(),
        numChildren: String = "",
        ectDate: Long = 0L
    ): BenWithEctListDomain {
        val e = mockk<BenWithEctListDomain>(relaxed = true)
        every { e.ben } returns ben
        every { e.numChildren } returns numChildren
        every { e.ectDate } returns ectDate
        every { e.savedECTRecords } returns emptyList()
        every { e.allSynced } returns SyncState.SYNCED
        return e
    }

    @Test fun `filterEcTrackingList blank returns all`() {
        val list = listOf(mkEct(mkBen(benFullName = "Asha")))
        assertEquals(1, filterEcTrackingList(list, "").size)
    }

    @Test fun `filterEcTrackingList matches by full name`() {
        val list = listOf(
            mkEct(mkBen(benFullName = "Asha Devi")),
            mkEct(mkBen(benFullName = "Priya Singh"))
        )
        assertEquals(1, filterEcTrackingList(list, "asha").size)
    }

    @Test fun `filterEcTrackingList matches by numChildren`() {
        val list = listOf(
            mkEct(mkBen(benFullName = "Asha"), numChildren = "3"),
            mkEct(mkBen(benFullName = "Priya"), numChildren = "1")
        )
        assertEquals(1, filterEcTrackingList(list, "3").size)
    }

    @Test fun `filterEcTrackingList no match returns empty`() {
        val list = listOf(mkEct(mkBen(benFullName = "Asha")))
        assertTrue(filterEcTrackingList(list, "zzz").isEmpty())
    }

    // ===============================================================
    // filterEcRegistrationList
    // ===============================================================

    private fun mkEcr(ben: BenBasicDomain): BenWithEcrDomain {
        val e = mockk<BenWithEcrDomain>(relaxed = true)
        every { e.ben } returns ben
        return e
    }

    @Test fun `filterEcRegistrationList blank returns all`() {
        val list = listOf(mkEcr(mkBen(benFullName = "Asha")))
        assertEquals(1, filterEcRegistrationList(list, "").size)
    }

    @Test fun `filterEcRegistrationList matches by name`() {
        val list = listOf(
            mkEcr(mkBen(benFullName = "Asha Devi")),
            mkEcr(mkBen(benFullName = "Priya"))
        )
        assertEquals(1, filterEcRegistrationList(list, "asha").size)
    }

    @Test fun `filterEcRegistrationList matches by mobile`() {
        val list = listOf(
            mkEcr(mkBen(benFullName = "Asha", mobileNo = "9876543210")),
            mkEcr(mkBen(benFullName = "Priya", mobileNo = "1112223334"))
        )
        assertEquals(1, filterEcRegistrationList(list, "9876").size)
    }

    @Test fun `filterEcRegistrationList no match returns empty`() {
        val list = listOf(mkEcr(mkBen(benFullName = "Asha")))
        assertTrue(filterEcRegistrationList(list, "zzz").isEmpty())
    }

    // ===============================================================
    // filterPwrRegistrationList (text + rchPresent)
    // ===============================================================

    private fun mkPwr(ben: BenBasicDomain): BenWithPwrDomain {
        val p = mockk<BenWithPwrDomain>(relaxed = true)
        every { p.ben } returns ben
        return p
    }

    @Test fun `filterPwrRegistrationList text blank returns all`() {
        val list = listOf(mkPwr(mkBen(benFullName = "Asha")))
        assertEquals(1, filterPwrRegistrationList(list, "").size)
    }

    @Test fun `filterPwrRegistrationList text matches by name`() {
        val list = listOf(
            mkPwr(mkBen(benFullName = "Asha Devi")),
            mkPwr(mkBen(benFullName = "Priya"))
        )
        assertEquals(1, filterPwrRegistrationList(list, "asha").size)
    }

    @Test fun `filterPwrRegistrationList rchPresent false returns all`() {
        val list = listOf(mkPwr(mkBen(rchId = "123")), mkPwr(mkBen(rchId = null)))
        assertEquals(2, filterPwrRegistrationList(list, false).size)
    }

    @Test fun `filterPwrRegistrationList rchPresent true keeps digit rchId`() {
        val list = listOf(mkPwr(mkBen(rchId = "123")), mkPwr(mkBen(rchId = null)))
        assertEquals(1, filterPwrRegistrationList(list, true).size)
    }

    // ===============================================================
    // BenWithAncListDomain based filters
    // ===============================================================

    private fun mkAnc(
        ben: BenBasicDomain = mkBen(),
        ancDate: Long = 0L,
        lmpString: String? = null,
        eddString: String? = null,
        weeks: String = ""
    ): BenWithAncListDomain {
        val a = mockk<BenWithAncListDomain>(relaxed = true)
        every { a.ben } returns ben
        every { a.ancDate } returns ancDate
        every { a.lmpString } returns lmpString
        every { a.eddString } returns eddString
        every { a.weeksOfPregnancy } returns weeks
        return a
    }

    @Test fun `filterPwAncList blank returns all`() {
        val list = listOf(mkAnc(mkBen(benFullName = "Asha")))
        assertEquals(1, filterPwAncList(list, "").size)
    }

    @Test fun `filterPwAncList matches by name and sorts`() {
        val list = listOf(
            mkAnc(mkBen(benFullName = "Asha Devi"), ancDate = 100L),
            mkAnc(mkBen(benFullName = "Priya"), ancDate = 200L)
        )
        assertEquals(1, filterPwAncList(list, "asha").size)
    }

    @Test fun `filterPwAncList matches by weeks of pregnancy`() {
        val list = listOf(
            mkAnc(mkBen(benFullName = "Asha"), weeks = "24weeks"),
            mkAnc(mkBen(benFullName = "Priya"), weeks = "10weeks")
        )
        assertEquals(1, filterPwAncList(list, "24weeks").size)
    }

    @Test fun `filterPwAncList matches by lmp string`() {
        val list = listOf(
            mkAnc(mkBen(benFullName = "Asha"), lmpString = "01-01-2026"),
            mkAnc(mkBen(benFullName = "Priya"), lmpString = "05-05-2025")
        )
        assertEquals(1, filterPwAncList(list, "01-01-2026").size)
    }

    @Test fun `filterPwAncList matches by edd string`() {
        val list = listOf(
            mkAnc(mkBen(benFullName = "Asha"), eddString = "10-10-2026"),
            mkAnc(mkBen(benFullName = "Priya"), eddString = "12-12-2025")
        )
        assertEquals(1, filterPwAncList(list, "10-10-2026").size)
    }

    @Test fun `filterAbortionList blank returns all`() {
        val list = listOf(mkAnc(mkBen(benFullName = "Asha")))
        assertEquals(1, filterAbortionList(list, "").size)
    }

    @Test fun `filterAbortionList matches by name`() {
        val list = listOf(
            mkAnc(mkBen(benFullName = "Asha Devi")),
            mkAnc(mkBen(benFullName = "Priya"))
        )
        assertEquals(1, filterAbortionList(list, "asha").size)
    }

    @Test fun `filterAbortionList no match returns empty`() {
        val list = listOf(mkAnc(mkBen(benFullName = "Asha")))
        assertTrue(filterAbortionList(list, "zzz").isEmpty())
    }

    // ===============================================================
    // filterPncDomainList
    // ===============================================================

    private fun mkPnc(
        ben: BenBasicDomain = mkBen(),
        deliveryDate: String = "",
        pncDate: Long = 0L
    ): BenPncDomain {
        val p = mockk<BenPncDomain>(relaxed = true)
        every { p.ben } returns ben
        every { p.deliveryDate } returns deliveryDate
        every { p.pncDate } returns pncDate
        return p
    }

    @Test fun `filterPncDomainList blank returns all`() {
        val list = listOf(mkPnc(mkBen(benFullName = "Asha")))
        assertEquals(1, filterPncDomainList(list, "").size)
    }

    @Test fun `filterPncDomainList matches by name`() {
        val list = listOf(
            mkPnc(mkBen(benFullName = "Asha Devi")),
            mkPnc(mkBen(benFullName = "Priya"))
        )
        assertEquals(1, filterPncDomainList(list, "asha").size)
    }

    @Test fun `filterPncDomainList matches by delivery date`() {
        val list = listOf(
            mkPnc(mkBen(benFullName = "Asha"), deliveryDate = "01-01-2024"),
            mkPnc(mkBen(benFullName = "Priya"), deliveryDate = "05-05-2023")
        )
        assertEquals(1, filterPncDomainList(list, "2024").size)
    }

    // ===============================================================
    // filterInfantDomainList
    // ===============================================================

    private fun mkInfant(
        motherBen: BenBasicDomain = mkBen(),
        babyName: String = ""
    ): InfantRegDomain {
        val i = mockk<InfantRegDomain>(relaxed = true)
        every { i.motherBen } returns motherBen
        every { i.babyName } returns babyName
        return i
    }

    @Test fun `filterInfantDomainList blank returns all`() {
        val list = listOf(mkInfant(mkBen(benName = "Asha"), babyName = "Baby"))
        assertEquals(1, filterInfantDomainList(list, "").size)
    }

    @Test fun `filterInfantDomainList matches by baby name`() {
        val list = listOf(
            mkInfant(mkBen(benName = "Asha"), babyName = "Rahul"),
            mkInfant(mkBen(benName = "Priya"), babyName = "Sita")
        )
        assertEquals(1, filterInfantDomainList(list, "rahul").size)
    }

    @Test fun `filterInfantDomainList matches by mother name`() {
        val list = listOf(
            mkInfant(mkBen(benName = "Asha"), babyName = "Rahul"),
            mkInfant(mkBen(benName = "Priya"), babyName = "Sita")
        )
        assertEquals(1, filterInfantDomainList(list, "asha").size)
    }

    // ===============================================================
    // TB / Malaria filters
    // ===============================================================

    private fun mkTbScreening(ben: BenBasicDomain): BenWithTbScreeningDomain {
        val t = mockk<BenWithTbScreeningDomain>(relaxed = true)
        every { t.ben } returns ben
        return t
    }

    private fun mkTbSuspected(ben: BenBasicDomain): BenWithTbSuspectedDomain {
        val t = mockk<BenWithTbSuspectedDomain>(relaxed = true)
        every { t.ben } returns ben
        return t
    }

    private fun mkMalaria(ben: BenBasicDomain): BenWithMalariaConfirmedDomain {
        val t = mockk<BenWithMalariaConfirmedDomain>(relaxed = true)
        every { t.ben } returns ben
        return t
    }

    @Test fun `filterTbScreeningList blank returns all`() {
        val list = listOf(mkTbScreening(mkBen(benFullName = "Asha")))
        assertEquals(1, filterTbScreeningList(list, "").size)
    }

    @Test fun `filterTbScreeningList matches by name and gender`() {
        val list = listOf(
            mkTbScreening(mkBen(benFullName = "Asha", gender = "female")),
            mkTbScreening(mkBen(benFullName = "Ravi", gender = "male"))
        )
        assertEquals(1, filterTbScreeningList(list, "asha").size)
        assertEquals(1, filterTbScreeningList(list, "female").size)
    }

    @Test fun `filterTbSuspectedList blank returns all`() {
        val list = listOf(mkTbSuspected(mkBen(benFullName = "Asha")))
        assertEquals(1, filterTbSuspectedList(list, "").size)
    }

    @Test fun `filterTbSuspectedList matches by name`() {
        val list = listOf(
            mkTbSuspected(mkBen(benFullName = "Asha Devi")),
            mkTbSuspected(mkBen(benFullName = "Priya"))
        )
        assertEquals(1, filterTbSuspectedList(list, "asha").size)
    }

    @Test fun `filterMalariaConfirmedList blank returns all`() {
        val list = listOf(mkMalaria(mkBen(benFullName = "Asha")))
        assertEquals(1, filterMalariaConfirmedList(list, "").size)
    }

    @Test fun `filterMalariaConfirmedList matches by mobile`() {
        val list = listOf(
            mkMalaria(mkBen(benFullName = "Asha", mobileNo = "9876543210")),
            mkMalaria(mkBen(benFullName = "Priya", mobileNo = "1112223334"))
        )
        assertEquals(1, filterMalariaConfirmedList(list, "9876").size)
    }

    // ===============================================================
    // filterBenFormList(BenBasicDomainForForm, text)
    // ===============================================================

    private fun mkFormBen(
        benId: Long = 0L,
        benName: String = "",
        benSurname: String? = null,
        familyHeadName: String = "",
        age: String = "",
        mobileNo: String = "",
        gender: String = "",
        regDate: String = "",
        hhId: Long = 0L,
        rchId: String? = null,
        spouseName: String? = null,
        fatherName: String? = null,
        dateOfDeath: String? = null
    ): BenBasicDomainForForm {
        val b = mockk<BenBasicDomainForForm>(relaxed = true)
        every { b.benId } returns benId
        every { b.benName } returns benName
        every { b.benSurname } returns benSurname
        every { b.familyHeadName } returns familyHeadName
        every { b.age } returns age
        every { b.mobileNo } returns mobileNo
        every { b.gender } returns gender
        every { b.regDate } returns regDate
        every { b.hhId } returns hhId
        every { b.rchId } returns rchId
        every { b.spouseName } returns spouseName
        every { b.fatherName } returns fatherName
        every { b.dateOfDeath } returns dateOfDeath
        return b
    }

    @Test fun `filterBenFormList form ben blank returns all`() {
        val list = listOf(mkFormBen(benName = "Asha"))
        assertEquals(1, filterBenFormList(list, "").size)
    }

    @Test fun `filterBenFormList form ben matches by name`() {
        val list = listOf(
            mkFormBen(benName = "Asha", benSurname = "Devi"),
            mkFormBen(benName = "Priya")
        )
        assertEquals(1, filterBenFormList(list, "asha").size)
    }

    @Test fun `filterBenFormList form ben matches by mobile`() {
        val list = listOf(
            mkFormBen(benName = "Asha", mobileNo = "9876543210"),
            mkFormBen(benName = "Priya", mobileNo = "1112223334")
        )
        assertEquals(1, filterBenFormList(list, "9876").size)
    }

    @Test fun `filterBenFormList form ben matches by date of death`() {
        val list = listOf(
            mkFormBen(benName = "Asha", dateOfDeath = "01-01-2024"),
            mkFormBen(benName = "Priya", dateOfDeath = null)
        )
        assertEquals(1, filterBenFormList(list, "01-01-2024").size)
    }

    // ===============================================================
    // filterBenFormList(ChildRegDomain, text)
    // ===============================================================

    private fun mkChildReg(
        motherBen: BenBasicDomain = mkBen(),
        childBen: BenBasicDomain? = null
    ): ChildRegDomain {
        val c = mockk<ChildRegDomain>(relaxed = true)
        every { c.motherBen } returns motherBen
        every { c.childBen } returns childBen
        return c
    }

    @Test fun `filterBenFormList childReg blank returns all`() {
        val list = listOf(mkChildReg(mkBen(benName = "Asha")))
        assertEquals(1, filterBenFormList(list, "").size)
    }

    @Test fun `filterBenFormList childReg matches by mother name`() {
        val list = listOf(
            mkChildReg(mkBen(benName = "Asha", benFullName = "Asha")),
            mkChildReg(mkBen(benName = "Priya", benFullName = "Priya"))
        )
        assertEquals(1, filterBenFormList(list, "asha").size)
    }

    @Test fun `filterBenFormList childReg matches by child name`() {
        val list = listOf(
            mkChildReg(mkBen(benName = "Asha"), childBen = mkBen(benName = "Rahul")),
            mkChildReg(mkBen(benName = "Priya"), childBen = mkBen(benName = "Sita"))
        )
        assertEquals(1, filterBenFormList(list, "rahul").size)
    }

    @Test fun `filterBenFormList childReg matches by child benId`() {
        val list = listOf(
            mkChildReg(mkBen(benName = "Asha"), childBen = mkBen(benId = 7777L)),
            mkChildReg(mkBen(benName = "Priya"), childBen = mkBen(benId = 8888L))
        )
        assertEquals(1, filterBenFormList(list, "7777").size)
    }

    // ===============================================================
    // HRP / HRNP filters
    // ===============================================================

    private fun mkHrpa(ben: BenBasicDomain): BenWithHRPADomain {
        val h = mockk<BenWithHRPADomain>(relaxed = true)
        every { h.ben } returns ben
        return h
    }

    private fun mkHrnpa(ben: BenBasicDomain): BenWithHRNPADomain {
        val h = mockk<BenWithHRNPADomain>(relaxed = true)
        every { h.ben } returns ben
        return h
    }

    private fun mkHrpt(ben: BenBasicDomain): BenWithHRPTListDomain {
        val h = mockk<BenWithHRPTListDomain>(relaxed = true)
        every { h.ben } returns ben
        return h
    }

    private fun mkHrnpt(ben: BenBasicDomain): BenWithHRNPTListDomain {
        val h = mockk<BenWithHRNPTListDomain>(relaxed = true)
        every { h.ben } returns ben
        return h
    }

    @Test fun `filterBenHRPFormList blank returns all`() {
        val list = listOf(mkHrpa(mkBen(benName = "Asha")))
        assertEquals(1, filterBenHRPFormList(list, "").size)
    }

    @Test fun `filterBenHRPFormList matches by name`() {
        val list = listOf(
            mkHrpa(mkBen(benName = "Asha", benFullName = "Asha")),
            mkHrpa(mkBen(benName = "Priya", benFullName = "Priya"))
        )
        assertEquals(1, filterBenHRPFormList(list, "asha").size)
    }

    @Test fun `filterBenHRNPFormList blank returns all`() {
        val list = listOf(mkHrnpa(mkBen(benName = "Asha")))
        assertEquals(1, filterBenHRNPFormList(list, "").size)
    }

    @Test fun `filterBenHRNPFormList matches by mobile`() {
        val list = listOf(
            mkHrnpa(mkBen(benName = "Asha", mobileNo = "9876543210")),
            mkHrnpa(mkBen(benName = "Priya", mobileNo = "1112223334"))
        )
        assertEquals(1, filterBenHRNPFormList(list, "9876").size)
    }

    @Test fun `filterBenHRPTFormList blank returns all`() {
        val list = listOf(mkHrpt(mkBen(benName = "Asha")))
        assertEquals(1, filterBenHRPTFormList(list, "").size)
    }

    @Test fun `filterBenHRPTFormList matches by name`() {
        val list = listOf(
            mkHrpt(mkBen(benName = "Asha", benFullName = "Asha")),
            mkHrpt(mkBen(benName = "Priya", benFullName = "Priya"))
        )
        assertEquals(1, filterBenHRPTFormList(list, "asha").size)
    }

    @Test fun `filterBenHRNPTFormList blank returns all`() {
        val list = listOf(mkHrnpt(mkBen(benName = "Asha")))
        assertEquals(1, filterBenHRNPTFormList(list, "").size)
    }

    @Test fun `filterBenHRNPTFormList matches by name`() {
        val list = listOf(
            mkHrnpt(mkBen(benName = "Asha", benFullName = "Asha")),
            mkHrnpt(mkBen(benName = "Priya", benFullName = "Priya"))
        )
        assertEquals(1, filterBenHRNPTFormList(list, "asha").size)
    }

    // ===============================================================
    // filterImmunList / filterForImm
    // ===============================================================

    private fun mkImm(
        age: String = "",
        benName: String = "",
        benSurname: String? = null,
        motherName: String? = null,
        mobileNo: String = ""
    ): ImmunizationDetailsDomain {
        val ben = mkBen(
            age = age,
            benName = benName,
            benSurname = benSurname,
            motherName = motherName,
            mobileNo = mobileNo
        )
        val imm = mockk<ImmunizationDetailsDomain>(relaxed = true)
        every { imm.ben } returns ben
        return imm
    }

    @Test fun `filterImmunList empty text returns all`() {
        val list = listOf(mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "").size)
    }

    @Test fun `filterImmunList matches by name`() {
        val list = listOf(mkImm(age = "5 years", benName = "Asha"), mkImm(age = "5 years", benName = "Priya"))
        assertEquals(1, filterImmunList(list, "asha").size)
    }

    @Test fun `filterImmunList matches by mother name`() {
        val list = listOf(mkImm(age = "5 years", motherName = "Sita"))
        assertEquals(1, filterImmunList(list, "sita").size)
    }

    @Test fun `filterImmunList matches by mobile`() {
        val list = listOf(mkImm(age = "5 years", mobileNo = "9876543210"))
        assertEquals(1, filterImmunList(list, "98765").size)
    }

    @Test fun `filterImmunList 5-6 branch matches 5 years`() {
        val list = listOf(mkImm(age = "5 years"), mkImm(age = "3 years"))
        assertEquals(1, filterImmunList(list, "5-6").size)
    }

    @Test fun `filterImmunList 16-24 branch matches 1 year`() {
        val list = listOf(mkImm(age = "1 year 6 months"), mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "16-24").size)
    }

    @Test fun `filterImmunList 9-12 branch matches 10 months`() {
        val list = listOf(mkImm(age = "10 months"), mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "9-12").size)
    }

    @Test fun `filterImmunList 6 weeks branch matches 1 month`() {
        val list = listOf(mkImm(age = "1 month"), mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "6 weeks").size)
    }

    @Test fun `filterImmunList birth dose branch matches day age`() {
        val list = listOf(mkImm(age = "5 days"), mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "birth dose").size)
    }

    @Test fun `filterImmunList 10 weeks branch matches 3 months`() {
        // filterForImm strips spaces from the token, so age must match "3months"
        val list = listOf(mkImm(age = "3months"), mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "10 weeks").size)
    }

    @Test fun `filterImmunList 14 weeks branch matches 4 months`() {
        val list = listOf(mkImm(age = "4months"), mkImm(age = "5 years"))
        assertEquals(1, filterImmunList(list, "14 weeks").size)
    }

    // ===============================================================
    // Sort functions
    // ===============================================================

    private fun mkEcrSort(benId: Long, dob: Long, created: Long, sync: SyncState): BenWithEcrDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { ecr.createdDate } returns created
        every { ecr.syncState } returns sync
        val item = mockk<BenWithEcrDomain>(relaxed = true)
        every { item.ben } returns ben
        every { item.ecr } returns ecr
        return item
    }

    @Test fun `sortEcRegistrationList orders by all filter types`() {
        val a = mkEcrSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkEcrSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach {
            assertEquals(2, sortEcRegistrationList(list, it).size)
        }
        assertEquals(2L, sortEcRegistrationList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortEcRegistrationList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
        assertEquals(1L, sortEcRegistrationList(list, EcFilterType.AGE_WISE).first().ben.benId)
    }

    private fun mkEctSort(benId: Long, dob: Long, ectDate: Long, sync: SyncState): BenWithEctListDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val rec = mockk<ECTDomain>(relaxed = true)
        every { rec.syncState } returns sync
        val item = mockk<BenWithEctListDomain>(relaxed = true)
        every { item.ben } returns ben
        every { item.ectDate } returns ectDate
        every { item.savedECTRecords } returns listOf(rec)
        every { item.allSynced } returns sync
        return item
    }

    @Test fun `sortEcTrackingList orders by all filter types`() {
        val a = mkEctSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkEctSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach {
            assertEquals(2, sortEcTrackingList(list, it).size)
        }
        assertEquals(2L, sortEcTrackingList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortEcTrackingList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
    }

    private fun mkPncSort(benId: Long, dob: Long, pncDate: Long, sync: SyncState): BenPncDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val item = mockk<BenPncDomain>(relaxed = true)
        every { item.ben } returns ben
        every { item.pncDate } returns pncDate
        every { item.syncState } returns sync
        return item
    }

    @Test fun `sortPncList orders by all filter types`() {
        val a = mkPncSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkPncSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortPncList(list, it).size) }
        assertEquals(2L, sortPncList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortPncList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
    }

    private fun mkPwrSort(benId: Long, dob: Long, created: Long, sync: SyncState): BenWithPwrDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { pwr.createdDate } returns created
        every { pwr.syncState } returns sync
        val item = mockk<BenWithPwrDomain>(relaxed = true)
        every { item.ben } returns ben
        every { item.pwr } returns pwr
        return item
    }

    @Test fun `sortPwrList orders by all filter types`() {
        val a = mkPwrSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkPwrSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortPwrList(list, it).size) }
        assertEquals(2L, sortPwrList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortPwrList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
    }

    private fun mkAncSort(
        benId: Long,
        dob: Long,
        ancDate: Long,
        abortionDate: Long?,
        sync: SyncState
    ): BenWithAncListDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val item = mockk<BenWithAncListDomain>(relaxed = true)
        every { item.ben } returns ben
        every { item.ancDate } returns ancDate
        every { item.abortionDate } returns abortionDate
        every { item.syncState } returns sync
        return item
    }

    @Test fun `sortAncList orders by all filter types`() {
        val a = mkAncSort(1L, 100L, 1000L, 500L, SyncState.SYNCED)
        val b = mkAncSort(2L, 200L, 2000L, 900L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortAncList(list, it).size) }
        assertEquals(2L, sortAncList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortAncList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
    }

    @Test fun `sortAbortionList orders by all filter types`() {
        val a = mkAncSort(1L, 100L, 1000L, 500L, SyncState.SYNCED)
        val b = mkAncSort(2L, 200L, 2000L, 900L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortAbortionList(list, it).size) }
        assertEquals(2L, sortAbortionList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortAbortionList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
    }

    private fun mkChildRegSort(benId: Long, dob: Long, created: Long, sync: SyncState): ChildRegDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val infant = mockk<InfantRegCache>(relaxed = true)
        every { infant.createdDate } returns created
        every { infant.syncState } returns sync
        val item = mockk<ChildRegDomain>(relaxed = true)
        every { item.motherBen } returns ben
        every { item.infant } returns infant
        return item
    }

    @Test fun `sortChildRegList orders by all filter types`() {
        val a = mkChildRegSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkChildRegSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortChildRegList(list, it).size) }
        assertEquals(2L, sortChildRegList(list, EcFilterType.NEWEST_FIRST).first().motherBen.benId)
        assertEquals(1L, sortChildRegList(list, EcFilterType.OLDEST_FIRST).first().motherBen.benId)
    }

    private fun mkInfantSort(benId: Long, dob: Long, created: Long, sync: SyncState): InfantRegDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val ir = mockk<InfantRegCache>(relaxed = true)
        every { ir.createdDate } returns created
        val item = mockk<InfantRegDomain>(relaxed = true)
        every { item.motherBen } returns ben
        every { item.savedIr } returns ir
        every { item.syncState } returns sync
        return item
    }

    @Test fun `sortInfantRegList orders by all filter types`() {
        val a = mkInfantSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkInfantSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortInfantRegList(list, it).size) }
        assertEquals(2L, sortInfantRegList(list, EcFilterType.NEWEST_FIRST).first().motherBen.benId)
        assertEquals(1L, sortInfantRegList(list, EcFilterType.OLDEST_FIRST).first().motherBen.benId)
    }

    private fun mkHwcSort(benId: Long, dob: Long, revisit: Long, sync: SyncState): BenWithCbacReferDomain {
        val ben = mkBen(benId = benId, dob = dob)
        val refer = mockk<ReferalCache>(relaxed = true)
        every { refer.revisitDate } returns revisit
        every { refer.syncState } returns sync
        val item = mockk<BenWithCbacReferDomain>(relaxed = true)
        every { item.ben } returns ben
        every { item.referalCac } returns refer
        return item
    }

    @Test fun `sortHwcList orders by all filter types`() {
        val a = mkHwcSort(1L, 100L, 1000L, SyncState.SYNCED)
        val b = mkHwcSort(2L, 200L, 2000L, SyncState.UNSYNCED)
        val list = listOf(a, b)
        EcFilterType.values().forEach { assertEquals(2, sortHwcList(list, it).size) }
        assertEquals(2L, sortHwcList(list, EcFilterType.NEWEST_FIRST).first().ben.benId)
        assertEquals(1L, sortHwcList(list, EcFilterType.OLDEST_FIRST).first().ben.benId)
    }

    // ===============================================================
    // Pure date / age helpers
    // ===============================================================

    @Test fun `getWeeksOfPregnancy returns weeks from lmp`() {
        val reg = TimeUnit.DAYS.toMillis(70)
        val lmp = 0L
        assertEquals(10, getWeeksOfPregnancy(reg, lmp))
    }

    @Test fun `getWeeksOfPregnancy returns 0 for null lmp`() {
        assertEquals(0, getWeeksOfPregnancy(TimeUnit.DAYS.toMillis(70), null))
    }

    @Test fun `getTodayMillis returns start of today`() {
        val millis = getTodayMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
        assertTrue(millis <= System.currentTimeMillis())
    }

    @Test fun `setToStartOfTheDay sets time to start of day`() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 15)
        cal.set(Calendar.MINUTE, 30)
        cal.setToStartOfTheDay()
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test fun `setToEndOfTheDay sets time to end of day`() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 5)
        cal.setToEndOfTheDay()
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, cal.get(Calendar.MINUTE))
        assertEquals(59, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test fun `getDateFromLong returns date with same time`() {
        val time = 1_600_000_000_000L
        val date = getDateFromLong(time)
        assertEquals(time, date.time)
    }

    @Test fun `getDateString formats a long`() {
        val millis = 1_600_000_000_000L
        val expected = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date(millis))
        assertEquals(expected, getDateString(millis))
    }

    @Test fun `getDateString returns null for null`() {
        assertNull(getDateString(null))
    }

    @Test fun `getPatientTypeByAge returns new_born_baby for today`() {
        assertEquals("new_born_baby", getPatientTypeByAge(Date()))
    }

    @Test fun `getPatientTypeByAge returns new_born_baby for days old`() {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -5) }
        assertEquals("new_born_baby", getPatientTypeByAge(cal.time))
    }

    @Test fun `getPatientTypeByAge returns infant for months old`() {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }
        assertEquals("infant", getPatientTypeByAge(cal.time))
    }

    @Test fun `getPatientTypeByAge returns child`() {
        val cal = Calendar.getInstance().apply { add(Calendar.YEAR, -5) }
        assertEquals("child", getPatientTypeByAge(cal.time))
    }

    @Test fun `getPatientTypeByAge returns adolescence`() {
        val cal = Calendar.getInstance().apply { add(Calendar.YEAR, -15) }
        assertEquals("adolescence", getPatientTypeByAge(cal.time))
    }

    @Test fun `getPatientTypeByAge returns adult`() {
        val cal = Calendar.getInstance().apply { add(Calendar.YEAR, -40) }
        assertEquals("adult", getPatientTypeByAge(cal.time))
    }

    // ===============================================================
    // Context localization helpers
    // ===============================================================

    @Test fun `getLocalizedAge returns days for recently born`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.day) } returns "Day"
        every { ctx.getString(R.string.days) } returns "Days"
        val dob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5) - TimeUnit.HOURS.toMillis(2)
        assertEquals("5 Days", getLocalizedAge(ctx, dob))
    }

    @Test fun `getLocalizedAge returns singular day`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.day) } returns "Day"
        every { ctx.getString(R.string.days) } returns "Days"
        val dob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1) - TimeUnit.HOURS.toMillis(2)
        assertEquals("1 Day", getLocalizedAge(ctx, dob))
    }

    @Test fun `getLocalizedAge returns years for adult`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.year) } returns "Year"
        every { ctx.getString(R.string.years) } returns "Years"
        every { ctx.getString(R.string.month) } returns "Month"
        every { ctx.getString(R.string.months) } returns "Months"
        every { ctx.getString(R.string.day) } returns "Day"
        every { ctx.getString(R.string.days) } returns "Days"
        val dob = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }.timeInMillis
        val result = getLocalizedAge(ctx, dob)
        assertTrue("Should contain 2, was: $result", result.contains("2"))
        assertTrue("Should contain Years, was: $result", result.contains("Years"))
    }

    @Test fun `getLocalizedAgeUnit maps years`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.years) } returns "Years"
        assertEquals("Years", ctx.getLocalizedAgeUnit(AgeUnit.YEARS))
    }

    @Test fun `getLocalizedAgeUnit maps months`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.months) } returns "Months"
        assertEquals("Months", ctx.getLocalizedAgeUnit(AgeUnit.MONTHS))
    }

    @Test fun `getLocalizedAgeUnit maps days`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.days) } returns "Days"
        assertEquals("Days", ctx.getLocalizedAgeUnit(AgeUnit.DAYS))
    }

    @Test fun `getLocalizedAgeUnit returns empty for null`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        assertEquals("", ctx.getLocalizedAgeUnit(null))
    }

    @Test fun `getLocalizedVisit formats visit number`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.visit, 3) } returns "Visit 3"
        assertEquals("Visit 3", ctx.getLocalizedVisit(3))
    }

    @Test fun `getLocalizedMonthText returns empty for null`() {
        val ctx = mockk<Context>(relaxed = true)
        assertEquals("", ctx.getLocalizedMonthText(null))
    }

    @Test fun `getLocalizedMonthText replaces month token`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.month) } returns "mahina"
        every { ctx.getString(R.string.months) } returns "mahine"
        assertEquals("5 mahina", ctx.getLocalizedMonthText("5 Month"))
    }

    @Test fun `getLocalizedGender maps male`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.male) } returns "Male"
        assertEquals("Male", ctx.getLocalizedGender(Gender.MALE))
    }

    @Test fun `getLocalizedGender maps female`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.female) } returns "Female"
        assertEquals("Female", ctx.getLocalizedGender(Gender.FEMALE))
    }

    @Test fun `getLocalizedGender maps transgender`() {
        val ctx = mockk<Context>(relaxed = true)
        val res = mockk<Resources>(relaxed = true)
        every { ctx.resources } returns res
        every { res.getString(R.string.transgender) } returns "Transgender"
        assertEquals("Transgender", ctx.getLocalizedGender(Gender.TRANSGENDER))
    }

    @Test fun `getLocalizedGender returns empty for null`() {
        val ctx = mockk<Context>(relaxed = true)
        assertEquals("", ctx.getLocalizedGender(null))
    }
}
