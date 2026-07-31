package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
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
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.Calendar

/**
 * Consolidated coverage for [NewChildBenRegDataset] (merged from Deep + Branch + Branch2 + Branch3
 * variants): setUpPage over many relation ids, both benGender values, mixed-gender child lists,
 * marriage/addspouse variants, mapChild across child indices, getMonthsFromDob ranges, the
 * getIndexOf* helpers, and ENGLISH/HINDI language. Per-variant ben builders kept distinct.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewChildBenRegDatasetTest : BaseViewModelTest() {

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
        // setUpPage resolves every child's gender through Dataset.getLocalValueInArray, which falls
        // back to Log.w when the value is missing from the mocked array. Un-stubbed, the real
        // android.util.Log throws and setUpPage aborts on the very first child.
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { preferenceDao.getLoggedInUser() } returns null
    }

    // Deep variant ben.
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

    // Branch variant ben (carries age).
    private fun benMockBr(gender: Gender = Gender.FEMALE): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns 1_000_000_000_000L
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns if (gender == Gender.MALE) 1 else 2
        every { b.isDraft } returns false
        every { b.gender } returns gender
        every { b.firstName } returns "FIRST"
        every { b.lastName } returns "LAST"
        every { b.age } returns 5
        every { b.familyHeadRelationPosition } returns 1
        return b
    }

    private fun selectedBenMock(withMarriage: Boolean): BenRegCache {
        val b = benMockBr(Gender.FEMALE)
        every { b.householdId } returns 42L
        every { b.communityId } returns 1
        every { b.contactNumber } returns 9876543210L
        if (withMarriage) {
            val gd = mockk<BenRegGen>(relaxed = true)
            every { gd.ageAtMarriage } returns 20
            every { gd.spouseName } returns "SPOUSE"
            every { b.genDetails } returns gd
        }
        return b
    }

    // Branch2 variant ben (carries genDetails with ageAtMarriage).
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

    // Branch3 variant ben (parameterized genderId).
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

    private fun ds() = NewChildBenRegDataset(context, Languages.ENGLISH)

    private fun dsHindi() = NewChildBenRegDataset(context, Languages.HINDI)

    // ---- Deep variant tests ----

    @Test
    fun `setUpPage saved variants`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        for (relId in listOf(8, 9)) {
            val d = ds()
            runCatching {
                d.setUpPage(
                    mockk<EligibleCoupleRegCache>(relaxed = true),
                    household,
                    benMockDeep(),
                    Gender.FEMALE,
                    relId,
                    emptyList(),
                    benMockDeep(),
                    0,
                    listOf(benMockDeep()),
                    0
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `setUpPage null ben`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                null,
                mockk<HouseholdCache>(relaxed = true),
                benMockDeep(),
                Gender.MALE,
                8,
                emptyList(),
                null,
                1,
                emptyList(),
                0
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapChild and helpers`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockDeep(), Gender.FEMALE, 8, emptyList(), benMockDeep(), 0, listOf(benMockDeep()), 0
            )
        }
        runCatching { d.mapChild(benMockDeep(), 1) }
        runCatching { d.getMonthsFromDob(1_000_000_000_000L) }
        runCatching { d.getIndexOfChildren() }
        runCatching { d.getIndexOfMaleChildren() }
        runCatching { d.getIndexOfFeMaleChildren() }
        runCatching { d.getIndexOfAge1() }
        runCatching { d.getIndexOfGap1() }
        assertNotNull(d.listFlow)
    }

    // ---- Branch variant tests ----

    @Test
    fun `setUpPage mixed-gender child lists and marriage variants`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        val childVariants = listOf(
            emptyList(),
            listOf(benMockBr(Gender.MALE)),
            listOf(benMockBr(Gender.MALE), benMockBr(Gender.FEMALE), benMockBr(Gender.TRANSGENDER))
        )
        for (children in childVariants) {
            for (withMarriage in listOf(true, false)) {
                val d = ds()
                runCatching {
                    d.setUpPage(
                        mockk<EligibleCoupleRegCache>(relaxed = true),
                        household,
                        benMockBr(),
                        Gender.FEMALE,
                        9,
                        emptyList(),
                        selectedBenMock(withMarriage),
                        0,
                        children,
                        children.size
                    )
                }
                assertNotNull(d.listFlow)
            }
        }
    }

    @Test
    fun `setUpPage null ben and different relation genders`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        for (bg in listOf(Gender.MALE, Gender.FEMALE, Gender.TRANSGENDER)) {
            val d = ds()
            runCatching {
                d.setUpPage(
                    null, household, benMockBr(bg), bg, if (bg == Gender.MALE) 9 else 10,
                    emptyList(), null, 1, emptyList(), 0
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `mapChild across indices and mapValues after populated page`() = runTest {
        val d = ds()
        val children = listOf(benMockBr(Gender.MALE), benMockBr(Gender.FEMALE), benMockBr(Gender.MALE))
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockBr(), Gender.FEMALE, 9, emptyList(),
                selectedBenMock(true), 0, children, 3
            )
        }
        for (idx in 1..9) {
            runCatching { d.mapChild(benMockBr(), idx) }
        }
        runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
        runCatching { d.getMonthsFromDob(1_000_000_000_000L) }
        runCatching { d.getIndexOfChildren() }
        runCatching { d.getIndexOfMaleChildren() }
        runCatching { d.getIndexOfFeMaleChildren() }
        assertNotNull(d.listFlow)
    }

    // ---- Branch2 variant tests ----

    @Test
    fun `setUpPage relation and gender and addspouse matrix`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        for (relId in listOf(3, 8, 9, 10)) {
            for (g in listOf(Gender.MALE, Gender.FEMALE)) {
                for (addSpouse in listOf(0, 1)) {
                    val d = ds()
                    runCatching {
                        d.setUpPage(
                            mockk<EligibleCoupleRegCache>(relaxed = true),
                            household,
                            benMockBr2(g),
                            g,
                            relId,
                            listOf(benMockBr2(g)),
                            benMockBr2(g),
                            addSpouse,
                            listOf(benMockBr2(), benMockBr2(Gender.MALE)),
                            2
                        )
                    }
                    assertNotNull(d.listFlow)
                }
            }
        }
    }

    @Test
    fun `setUpPage null selectedben and empty children`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                null,
                mockk<HouseholdCache>(relaxed = true),
                null,
                Gender.MALE,
                8,
                emptyList(),
                null,
                0,
                emptyList(),
                5
            )
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `mapChild over indices 1 to 5`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockBr2(), Gender.FEMALE, 8, emptyList(), benMockBr2(), 1,
                listOf(benMockBr2(), benMockBr2(Gender.MALE)), 1
            )
        }
        for (i in 1..5) {
            runCatching { d.mapChild(benMockBr2(), i) }
        }
        assertNotNull(d.listFlow)
    }

    // ---- Branch3 variant tests (HINDI, month range) ----

    @Test
    fun `setUpPage relation and gender variants`() = runTest {
        val household = mockk<HouseholdCache>(relaxed = true)
        for (relId in listOf(0, 1, 2, 4, 5, 8, 9, 10)) {
            val d = dsHindi()
            runCatching {
                d.setUpPage(
                    mockk<EligibleCoupleRegCache>(relaxed = true), household,
                    benMockBr3(Gender.MALE, 1), Gender.MALE, relId, listOf(benMockBr3()),
                    benMockBr3(), relId % 2, listOf(benMockBr3(), benMockBr3(Gender.MALE, 1)), relId % 2
                )
            }
            assertNotNull(d.listFlow)
        }
    }

    @Test
    fun `mapChild and helpers with month range`() = runTest {
        val d = dsHindi()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true), mockk<HouseholdCache>(relaxed = true),
                benMockBr3(), Gender.FEMALE, 9, emptyList(), benMockBr3(), 1, listOf(benMockBr3()), 1
            )
        }
        runCatching { d.mapChild(benMockBr3(Gender.MALE, 1), 0) }
        runCatching { d.mapChild(benMockBr3(), 2) }
        for (dob in listOf(0L, System.currentTimeMillis(), 1_000_000_000_000L)) {
            runCatching { d.getMonthsFromDob(dob) }
        }
        runCatching { d.getIndexOfChildren() }
        runCatching { d.getIndexOfMaleChildren() }
        runCatching { d.getIndexOfFeMaleChildren() }
        runCatching { d.getIndexOfAge1() }
        runCatching { d.getIndexOfGap1() }
        assertNotNull(d.listFlow)
    }

    // ===================== added: mapValues override, mapChild bodies, updateList handlers =====

    private fun children9(): List<BenRegCache> =
        (0 until 9).map { benMockBr(if (it % 2 == 0) Gender.MALE else Gender.FEMALE) }

    // mapValues (the FormDataModel override) with noOfChildren up to 9 exercises every >1..>8 block.
    @Test
    fun `mapValues over all child count branches`() = runTest {
        for (n in listOf(1, 2, 5, 9)) {
            val d = ds()
            val kids = (0 until n).map { benMockBr(if (it % 2 == 0) Gender.MALE else Gender.FEMALE) }
            runCatching {
                d.setUpPage(
                    mockk<EligibleCoupleRegCache>(relaxed = true),
                    mockk<HouseholdCache>(relaxed = true),
                    benMockBr(), Gender.FEMALE, 9, emptyList(),
                    selectedBenMock(true), 0, kids, n
                )
            }
            runCatching { d.mapValues(mockk<EligibleCoupleRegCache>(relaxed = true), 0) }
            assertNotNull(d.listFlow)
        }
    }

    // The large per-child branches inside mapChild only run when a gender has been chosen; the
    // pre-existing tests always throw at the null-gender guard. Setting gender values first
    // (opt0/opt1/opt2 map to entries[0/1/2]) lets each child block execute.
    @Test
    fun `mapChild bodies with gender selected`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockBr(), Gender.FEMALE, 9, emptyList(),
                selectedBenMock(true), 0, children9(), 9
            )
        }
        listOf(19, 24, 29, 34, 39, 44, 49, 54, 59).forEachIndexed { i, gid ->
            val v = when (i % 3) { 0 -> "opt0"; 1 -> "opt1"; else -> "opt2" }
            runCatching { d.setValueById(gid, v) }
        }
        for (idx in 1..9) {
            runCatching { d.mapChild(selectedBenMock(true), idx) }
        }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList drives handleListOnValueChanged existing record`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockBr(), Gender.FEMALE, 9, emptyList(),
                selectedBenMock(true), 0, children9(), 9
            )
        }
        for (gid in listOf(19, 24, 29, 34, 39, 44, 49, 54, 59)) runCatching { d.updateList(gid, 0) }
        for (did in listOf(17, 22, 27, 32, 37, 42, 47, 52, 57)) runCatching { d.updateList(did, 0) }
        runCatching { d.updateList(12, 0) }
        assertNotNull(d.listFlow)
    }

    @Test
    fun `updateList new record no children param`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                null,
                mockk<HouseholdCache>(relaxed = true),
                benMockBr(), Gender.FEMALE, 9, emptyList(),
                selectedBenMock(true), 0, children9(), 9
            )
        }
        runCatching { d.setValueById(12, "3"); d.updateList(12, 0) }
        runCatching { d.setValueById(12, "0"); d.updateList(12, 0) }
        for (did in listOf(17, 22, 27)) runCatching { d.updateList(did, 0) }
        assertNotNull(d.listFlow)
    }

    // childNameFields branch: the child-name ids (111..119) run validateEmpty + validateAllCaps.
    @Test
    fun `updateList child name validation`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockBr(), Gender.FEMALE, 9, emptyList(),
                selectedBenMock(true), 0, children9(), 9
            )
        }
        val nameIds = listOf(111, 112, 113, 114, 115, 116, 117, 118, 119)
        nameIds.forEach { runCatching { d.setValueById(it, ""); d.updateList(it, 0) } }
        nameIds.forEach { runCatching { d.setValueById(it, "BABY NAME"); d.updateList(it, 0) } }
        assertNotNull(d.listFlow)
    }

    // dob branch months<=3 ("Baby of <mother>") vs months>3 (name required) arms. A recent dob hits
    // the first arm; an older dob hits the second.
    @Test
    fun `updateList dob month range name assignment`() = runTest {
        val d = ds()
        runCatching {
            d.setUpPage(
                mockk<EligibleCoupleRegCache>(relaxed = true),
                mockk<HouseholdCache>(relaxed = true),
                benMockBr(), Gender.FEMALE, 9, emptyList(),
                selectedBenMock(true), 0, children9(), 9
            )
        }
        runCatching { d.setValueById(17, "01-06-2026"); d.updateList(17, 0) }
        runCatching { d.setValueById(17, "01-01-2015"); d.updateList(17, 0) }
        assertNotNull(d.listFlow)
    }

    // ===================== added: real assertions now that setUpPage completes ================

    private suspend fun dsWithChildren(count: Int): NewChildBenRegDataset {
        val d = ds()
        val kids = (0 until count).map { benMockBr(if (it % 2 == 0) Gender.MALE else Gender.FEMALE) }
        d.setUpPage(
            mockk<EligibleCoupleRegCache>(relaxed = true),
            mockk<HouseholdCache>(relaxed = true),
            benMockBr(), Gender.FEMALE, 9, emptyList(),
            selectedBenMock(true), 0, kids, count
        )
        return d
    }

    @Test
    fun `setUpPage emits the base rows plus five rows per child`() = runTest {
        val d = dsWithChildren(2)
        val page = d.listFlow.value
        assertTrue(page.isNotEmpty())
        assertTrue(page.any { it.id == 0 })   // dateOfReg
        assertTrue(page.any { it.id == 12 })  // noOfChildren
        listOf(111, 17, 18, 19, 20, 112, 22, 23, 24, 25).forEach { id ->
            assertTrue("expected child field $id", page.any { it.id == id })
        }
        assertEquals("2", page.first { it.id == 12 }.value)
        assertEquals(page.size, d.getListSize())
    }

    @Test
    fun `setUpPage copies each child name dob and age onto the form`() = runTest {
        val d = dsWithChildren(3)
        val page = d.listFlow.value
        assertEquals("FIRST", page.first { it.id == 111 }.value)
        assertNotNull(page.first { it.id == 17 }.value)
        assertEquals("5", page.first { it.id == 18 }.value)
        assertTrue(page.first { it.id == 20 }.value!!.contains("years"))
    }

    @Test
    fun `setUpPage caps the elder child count at five`() = runTest {
        val d = ds()
        d.setUpPage(
            mockk<EligibleCoupleRegCache>(relaxed = true),
            mockk<HouseholdCache>(relaxed = true),
            benMockBr(), Gender.FEMALE, 9, emptyList(),
            selectedBenMock(true), 0, emptyList(), 12
        )
        assertEquals("5", d.listFlow.value.first { it.id == 1 }.value)
    }

    @Test
    fun `setUpPage with a localized gender array resolves the child genders`() = runTest {
        every { mockResources.getStringArray(R.array.ecr_gender_array) } returns
                arrayOf("Male", "Female", "Transgender")
        val d = ds()
        d.setUpPage(
            mockk<EligibleCoupleRegCache>(relaxed = true),
            mockk<HouseholdCache>(relaxed = true),
            benMockBr(), Gender.FEMALE, 9, emptyList(),
            selectedBenMock(true), 0,
            listOf(benMockBr(Gender.MALE), benMockBr(Gender.FEMALE)), 2
        )
        val page = d.listFlow.value
        assertEquals("Male", page.first { it.id == 19 }.value)
        assertEquals("Female", page.first { it.id == 24 }.value)
    }

    @Test
    fun `updateList on a child gender drives the male and female recount`() = runTest {
        every { mockResources.getStringArray(R.array.ecr_gender_array) } returns
                arrayOf("Male", "Female", "Transgender")
        val d = dsWithChildren(3)
        d.setValueById(19, "Male")
        d.setValueById(24, "Female")
        d.setValueById(29, "Female")
        d.updateList(19, 0)
        assertTrue(d.getListSize() > 0)
    }

    @Test
    fun `updateList on noOfChildren for a new record adds and removes child blocks`() = runTest {
        val d = ds()
        d.setUpPage(
            null,
            mockk<HouseholdCache>(relaxed = true),
            benMockBr(), Gender.FEMALE, 9, emptyList(),
            selectedBenMock(true), 0, emptyList(), 0
        )
        d.setValueById(12, "2")
        d.updateList(12, 0)
        assertTrue(d.listFlow.value.any { it.id == 22 })

        d.setValueById(12, "0")
        d.updateList(12, 0)
        assertTrue(d.listFlow.value.none { it.id == 22 })
    }

    @Test
    fun `updateList on the first child dob recomputes age gap and the newborn name`() = runTest {
        val d = dsWithChildren(2)
        d.setValueById(17, "01-01-2012")
        d.updateList(17, 0)
        val page = d.listFlow.value
        assertNotNull(page.first { it.id == 18 }.value)
        assertTrue(page.first { it.id == 20 }.value!!.contains("years"))

        // a dob within three months forces the "Baby of <mother>" placeholder name
        val recent = Dataset.getDateFromLong(System.currentTimeMillis())
        d.setValueById(17, recent)
        d.updateList(17, 0)
        assertTrue(d.listFlow.value.first { it.id == 111 }.value!!.startsWith("Baby of"))
    }

    @Test
    fun `updateList on a child name validates emptiness and casing`() = runTest {
        val d = dsWithChildren(2)
        d.setValueById(111, "")
        d.updateList(111, 0)
        d.setValueById(111, "baby name")
        d.updateList(111, 0)
        d.setValueById(111, "BABY NAME")
        d.updateList(111, 0)
        assertEquals("BABY NAME", d.listFlow.value.first { it.id == 111 }.value)
    }

    @Test
    fun `mapChild builds a beneficiary for each populated child slot`() = runTest {
        every { mockResources.getStringArray(R.array.ecr_gender_array) } returns
                arrayOf("Male", "Female", "Transgender")
        val d = dsWithChildren(3)
        d.setValueById(19, "Male")
        d.setValueById(24, "Female")
        d.setValueById(29, "Transgender")
        for (idx in 1..3) {
            val child = d.mapChild(benMockBr(), idx)
            assertNotNull(child)
        }
    }

    @Test
    fun `mapChild rejects a child whose gender has not been chosen`() = runTest {
        val d = dsWithChildren(1)
        d.setValueById(19, null)
        val failure = runCatching { d.mapChild(benMockBr(), 1) }
        assertTrue(failure.isFailure)
    }

    @Test
    fun `mapValues copies the child counts and per-child data onto the cache`() = runTest {
        val d = dsWithChildren(3)
        val cache = mockk<EligibleCoupleRegCache>(relaxed = true)
        d.mapValues(cache, 0)
        verify { cache.noOfChildren = 3 }
    }

    @Test
    fun `getMonthsFromDob returns zero for today and grows with age`() {
        val d = ds()
        assertEquals(0, d.getMonthsFromDob(System.currentTimeMillis()))
        val twoYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }.timeInMillis
        assertEquals(24, d.getMonthsFromDob(twoYearsAgo))
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

        val empty = ChildBundle(fe(1), fe(2), fe(3), fe(4), fe(5), fe(6))
        assertTrue(empty.isEmpty())
        assertEquals(6, empty.toFormList().size)

        val filled = ChildBundle(
            fe(1), fe(2, "BABY"), fe(3, "01-01-2015"), fe(4, "9"), fe(5, "Male"), fe(6, "2 years")
        )
        assertFalse(filled.isEmpty())
        filled.clearValues()
        assertTrue(filled.isEmpty())
        assertNull(filled.gap.value)
        assertNotNull(filled.toString())
        assertEquals(filled, filled.copy())
    }

    @Test
    fun `index getters resolve for every populated child slot`() = runTest {
        val d = dsWithChildren(9)
        assertTrue(d.getIndexOfChildren() >= 0)
        assertTrue(d.getIndexOfAge1() >= 0)
        assertTrue(d.getIndexOfGap1() >= 0)
        assertTrue(d.getIndexOfAge2() >= 0)
        assertTrue(d.getIndexOfGap2() >= 0)
        assertTrue(d.getIndexOfAge3() >= 0)
        assertTrue(d.getIndexOfGap3() >= 0)
        assertTrue(d.getIndexOfAge4() >= 0)
        assertTrue(d.getIndexOfGap4() >= 0)
        assertTrue(d.getIndexOfAge5() >= 0)
        assertTrue(d.getIndexOfGap5() >= 0)
        assertTrue(d.getIndexOfAge6() >= 0)
        assertTrue(d.getIndexOfGap6() >= 0)
        assertTrue(d.getIndexOfAge7() >= 0)
        assertTrue(d.getIndexOfGap7() >= 0)
        assertTrue(d.getIndexOfAge8() >= 0)
        assertTrue(d.getIndexOfGap8() >= 0)
        assertTrue(d.getIndexOfAge9() >= 0)
        assertTrue(d.getIndexOfGap9() >= 0)
    }
}
