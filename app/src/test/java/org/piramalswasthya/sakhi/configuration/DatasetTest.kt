package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.Calendar

/**
 * Unit tests for the abstract base class [Dataset].
 *
 * [Dataset] cannot be instantiated directly, so the whole inherited surface is driven through a
 * minimal nested subclass ([Probe]) declared inside this test class. Being a subclass, [Probe] can
 * legally reach the `protected` helpers (setUpPage / trigger* / validate* / date helpers) and
 * re-expose them via thin `expose*` wrappers; nothing in `app/src/main` is touched.
 *
 * Only ONE top-level class lives in this file — [Probe] is a nested private class, not a second
 * test class (it carries no @Test methods and is never run by JUnit).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "err"
        every { mockResources.getString(any(), any()) } returns "err2"
    }

    // =====================================================================================
    // Minimal concrete subclass used to reach Dataset's protected members.
    // =====================================================================================

    private class Probe(context: Context, language: Languages) : Dataset(context, language) {

        /** Value returned by handleListOnValueChanged; drives the emit / no-emit arms of updateList. */
        var handleResult: Int = 0
        var lastFormId: Int = Int.MIN_VALUE
        var lastIndex: Int = Int.MIN_VALUE
        var mappedModel: FormDataModel? = null
        var mappedPage: Int = -99

        override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
            lastFormId = formId
            lastIndex = index
            return handleResult
        }

        override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
            mappedModel = cacheModel
            mappedPage = pageNumber
        }

        // ---- list plumbing ----
        suspend fun exposeSetUpPage(list: List<FormElement>) = setUpPage(list)
        fun exposeElements(): List<FormElement> = formElements
        fun exposeIndexOfElement(element: FormElement) = getIndexOfElement(element)

        // ---- FormElement member extensions ----
        fun exposePosition(element: FormElement) = element.getPosition()
        fun exposeStringFromPosition(element: FormElement, position: Int) =
            element.getStringFromPosition(position)

        fun exposeSpauseFromPosition(element: FormElement, position: Int) =
            element.getStringSpauseFromPosition(position)

        fun exposeEnglishStringFromPosition(element: FormElement, position: Int) =
            element.getEnglishStringFromPosition(position)

        // ---- trigger helpers ----
        fun exposeTriggerList(
            source: FormElement,
            passedIndex: Int,
            triggerIndex: Int,
            target: List<FormElement>,
            side: List<FormElement>? = null
        ) = triggerDependants(
            source = source,
            passedIndex = passedIndex,
            triggerIndex = triggerIndex,
            target = target,
            targetSideEffect = side
        )

        fun exposeTriggerSingle(
            source: FormElement,
            passedIndex: Int,
            triggerIndex: Int,
            target: FormElement,
            side: List<FormElement>? = null
        ) = triggerDependants(
            source = source,
            passedIndex = passedIndex,
            triggerIndex = triggerIndex,
            target = target,
            targetSideEffect = side
        )

        fun exposeTriggerReverse(
            source: FormElement,
            passedIndex: Int,
            triggerIndex: Int,
            target: List<FormElement>,
            side: List<FormElement>? = null
        ) = triggerDependantsReverse(
            source = source,
            passedIndex = passedIndex,
            triggerIndex = triggerIndex,
            target = target,
            targetSideEffect = side
        )

        fun exposeTriggerForHide(
            source: FormElement,
            passedIndex: Int,
            triggerIndex: Int,
            target: FormElement,
            side: List<FormElement>? = null
        ) = triggerforHide(
            source = source,
            passedIndex = passedIndex,
            triggerIndex = triggerIndex,
            target = target,
            targetSideEffect = side
        )

        fun exposeTriggerAddRemove(
            source: FormElement,
            removeItems: List<FormElement>,
            addItems: List<FormElement>,
            position: Int = -1
        ) = triggerDependants(
            source = source,
            removeItems = removeItems,
            addItems = addItems,
            position = position
        )

        fun exposeInfantTrigger(
            source: FormElement,
            removeItems: List<FormElement>,
            addItems: List<FormElement>,
            position: Int = -1
        ) = infantTriggerDependants(
            source = source,
            removeItems = removeItems,
            addItems = addItems,
            position = position
        )

        // ---- date / age helpers ----
        fun exposeDiffMonths(a: Calendar, b: Calendar) = getDiffMonths(a, b)
        fun exposeEddFromLmp(lmp: Long) = getEddFromLmp(lmp)
        fun exposeAncMaxFromLmp(lmp: Long) = getANCMaxFromLmp(lmp)
        fun exposeMinFromMaxForLmp(lmp: Long) = getMinFromMaxForLmp(lmp)
        fun exposeDoMFromDoR(years: Int?, regDate: Long) = getDoMFromDoR(years, regDate)
        fun exposeCalculateAge(date: Long) = calculateAge(date)
        fun exposeCalculateDob(age: Int) = calculateDob(age)

        fun exposeAgeAndAgeUnitFromDob(
            dob: Long,
            ageAtMarriageElement: FormElement? = null,
            timeStampDateOfMarriage: Long? = null
        ) = assignValuesToAgeAndAgeUnitFromDob(dob, ageAtMarriageElement, timeStampDateOfMarriage)

        fun exposeAgeFromDob(dob: Long, ageElement: FormElement) =
            assignValuesToAgeFromDob(dob, ageElement)

        suspend fun exposeEmitAlertError(res: Int) = emitAlertErrorMessage(res)

        // ---- validators ----
        fun vEmpty(e: FormElement) = validateEmptyOnEditText(e)
        fun vAllCaps(e: FormElement) = validateAllCapsOrSpaceOnEditText(e)
        fun vNonNumericHindi(e: FormElement) = validateEditTextWithTextNonNumericHindiEnabled(e)
        fun vAllCapsHindi(e: FormElement) = validateAllCapsOrSpaceOnEditTextWithHindiEnabled(e)
        fun vFullLength(e: FormElement) = validateEditTextFullLengthOccupied(e)
        fun vAlphaSpace(e: FormElement) = validateAllAlphabetsSpaceOnEditText(e)
        fun vAlphaSpecial(e: FormElement) = validateAllAlphabetsSpecialOnEditText(e)
        fun vAlphaSpecialDigit(e: FormElement) = validateAllAlphabetsSpecialAndNumericOnEditText(e)
        fun vAlphaNumericSpace(e: FormElement) = validateAllAlphaNumericSpaceOnEditText(e)
        fun vAbha(e: FormElement) = validateABHANumberEditText(e)
        fun vAlphaNumeric(e: FormElement) = validateAllAlphaNumericOnEditText(e)
        fun vIfsc(e: FormElement) = validateIFSCEditText(e)
        fun vNoAlphaSpace(e: FormElement) = validateNoAlphabetSpaceOnEditText(e)
        fun vBirthCert(e: FormElement) = validateBirthCertificateNumber(e)
        fun vAllDigit(e: FormElement) = validateAllDigitOnEditText(e)
        fun vUploads(uploads: List<FormElement>, minRequired: Int = 2) =
            validateUploads(uploads, minRequired)

        fun vIntMinMax(e: FormElement) = validateIntMinMax(e)
        fun vDoubleMinMax(e: FormElement) = validateDoubleMinMax(e)
        fun vDouble1Dp(e: FormElement) = validateDoubleUpto1DecimalPlaces(e)
        fun vMobile(e: FormElement) = validateMobileNumberOnEditText(e)
        fun vRchId(e: FormElement) = validateRchIdOnEditText(e)
        fun vAllZeros(e: FormElement) = validateAllZerosOnEditText(e)
        fun vWeight(e: FormElement) = validateWeightOnEditText(e)
        fun vMcp(e: FormElement) = validateMcpOnEditText(e)
        fun vAadhar(e: FormElement) = validateAadharNoOnEditText(e)
        fun vBp(e: FormElement) = validateForBp(e)
        fun exposeIsValid(value: String) = value.isValid()
    }

    // =====================================================================================
    // helpers
    // =====================================================================================

    private fun ds(language: Languages = Languages.ENGLISH) = Probe(context, language)

    private fun el(
        id: Int,
        value: String? = null,
        required: Boolean = false,
        inputType: InputType = InputType.EDIT_TEXT,
        entries: Array<String>? = null,
        allCaps: Boolean = false,
        etMaxLength: Int = 50,
        min: Long? = null,
        max: Long? = null,
        minDecimal: Double? = null,
        maxDecimal: Double? = null
    ) = FormElement(
        id = id,
        inputType = inputType,
        required = required,
        title = "field$id",
        arrayId = 1,
        entries = entries,
        value = value,
        allCaps = allCaps,
        etMaxLength = etMaxLength,
        min = min,
        max = max,
        minDecimal = minDecimal,
        maxDecimal = maxDecimal
    )

    // =====================================================================================
    // list plumbing: setUpPage / listFlow / getIndexById / setValueById / getListSize
    // =====================================================================================

    @Test
    fun `setUpPage publishes the list on listFlow and index helpers resolve`() = runTest {
        val d = ds()
        val a = el(1)
        val b = el(2)
        val c = el(3)
        d.exposeSetUpPage(listOf(a, b, c))

        assertEquals(3, d.getListSize())
        assertEquals(3, d.listFlow.value.size)
        assertEquals(0, d.getIndexById(1))
        assertEquals(2, d.getIndexById(3))
        assertEquals(-1, d.getIndexById(999))
        assertEquals(1, d.exposeIndexOfElement(b))
    }

    @Test
    fun `setValueById sets only the matching element and ignores unknown ids`() = runTest {
        val d = ds()
        val a = el(1)
        val b = el(2)
        d.exposeSetUpPage(listOf(a, b))

        d.setValueById(2, "hello")
        assertEquals("hello", b.value)
        assertNull(a.value)

        d.setValueById(404, "nope")
        assertNull(a.value)

        d.setValueById(2, null)
        assertNull(b.value)
    }

    @Test
    fun `setUpPage called twice replaces the previous page`() = runTest {
        val d = ds()
        d.exposeSetUpPage(listOf(el(1), el(2), el(3)))
        assertEquals(3, d.getListSize())
        d.exposeSetUpPage(listOf(el(9)))
        assertEquals(1, d.getListSize())
        assertEquals(1, d.listFlow.value.size)
        assertEquals(9, d.listFlow.value[0].id)
    }

    @Test
    fun `updateList emits when handler returns an index and clears dropdown error`() = runTest {
        val d = ds()
        val dropdown = el(7, inputType = InputType.DROPDOWN).apply { errorText = "boom" }
        d.exposeSetUpPage(listOf(dropdown, el(8)))

        d.handleResult = 0
        d.updateList(7, 3)

        assertEquals(7, d.lastFormId)
        assertEquals(3, d.lastIndex)
        assertNull(dropdown.errorText)
        assertEquals(2, d.listFlow.value.size)
    }

    @Test
    fun `updateList with handler returning minus one does not clear a non-dropdown error`() =
        runTest {
            val d = ds()
            val editText = el(7, inputType = InputType.EDIT_TEXT).apply { errorText = "boom" }
            d.exposeSetUpPage(listOf(editText))

            d.handleResult = -1
            d.updateList(7, 0)

            assertEquals("boom", editText.errorText)
            assertEquals(7, d.lastFormId)
        }

    @Test
    fun `updateList for an id not present in the list still dispatches the handler`() = runTest {
        val d = ds()
        d.exposeSetUpPage(listOf(el(1)))
        d.handleResult = 0
        d.updateList(12345, 2)
        assertEquals(12345, d.lastFormId)
        assertEquals(2, d.lastIndex)
    }

    @Test
    fun `mapValues override receives the model and page number`() = runTest {
        val d = ds()
        val model = object : FormDataModel {}
        d.mapValues(model, 4)
        assertEquals(model, d.mappedModel)
        assertEquals(4, d.mappedPage)
    }

    // =====================================================================================
    // alert error flow
    // =====================================================================================

    @Test
    fun `emitAlertErrorMessage publishes and resetErrorMessageFlow clears it`() = runTest {
        val d = ds()
        assertNull(d.alertErrorMessageFlow.value)
        d.exposeEmitAlertError(1)
        assertEquals("err", d.alertErrorMessageFlow.value)
        d.resetErrorMessageFlow()
        assertNull(d.alertErrorMessageFlow.value)
    }

    // =====================================================================================
    // FormElement member extensions
    // =====================================================================================

    @Test
    fun `getPosition and getStringFromPosition round trip`() {
        val d = ds()
        val entries = arrayOf("a", "b", "c")
        val chosen = el(1, value = "b", entries = entries)
        val empty = el(2, entries = entries)

        assertEquals(2, d.exposePosition(chosen))
        assertEquals(0, d.exposePosition(empty))
        assertEquals("b", d.exposeStringFromPosition(chosen, 2))
        assertNull(d.exposeStringFromPosition(chosen, 0))
        assertNull(d.exposeStringFromPosition(chosen, -3))
        assertNull(d.exposeStringFromPosition(chosen, 99))
    }

    @Test
    fun `getStringSpauseFromPosition falls back to the second entry for non positive positions`() {
        val d = ds()
        val e = el(1, entries = arrayOf("a", "b", "c"))
        assertEquals("b", d.exposeSpauseFromPosition(e, 0))
        assertEquals("b", d.exposeSpauseFromPosition(e, -5))
        assertEquals("a", d.exposeSpauseFromPosition(e, 1))
        assertEquals("c", d.exposeSpauseFromPosition(e, 3))
    }

    @Test
    fun `getEnglishStringFromPosition reads the english resource array`() {
        val d = ds()
        val e = el(1, entries = arrayOf("a", "b"))
        assertNull(d.exposeEnglishStringFromPosition(e, 0))
        assertEquals("opt0", d.exposeEnglishStringFromPosition(e, 1))
        assertEquals("opt2", d.exposeEnglishStringFromPosition(e, 3))
        assertNull(d.exposeEnglishStringFromPosition(e, 900))
    }

    // =====================================================================================
    // trigger helpers
    // =====================================================================================

    @Test
    fun `triggerDependants with a list target adds once and removes on mismatch`() = runTest {
        val d = ds()
        val source = el(1)
        val t1 = el(10, value = "x")
        val t2 = el(11, value = "y")
        d.exposeSetUpPage(listOf(source, el(2)))

        assertEquals(0, d.exposeTriggerList(source, 0, 0, listOf(t1, t2)))
        assertEquals(4, d.getListSize())
        // already present -> no-op
        assertEquals(-1, d.exposeTriggerList(source, 0, 0, listOf(t1, t2)))

        // mismatching index removes them and nulls the values
        assertEquals(0, d.exposeTriggerList(source, 1, 0, listOf(t1, t2)))
        assertEquals(2, d.getListSize())
        assertNull(t1.value)
        assertNull(t2.value)
        // nothing left to remove
        assertEquals(-1, d.exposeTriggerList(source, 1, 0, listOf(t1, t2)))
    }

    @Test
    fun `triggerDependants with a list target also clears the side effect elements`() = runTest {
        val d = ds()
        val source = el(1)
        val target = el(10)
        val side = el(20, value = "keepme").apply { errorText = "e" }
        d.exposeSetUpPage(listOf(source, side))

        d.exposeTriggerList(source, 0, 0, listOf(target), listOf(side))
        assertTrue(d.exposeElements().contains(target))

        d.exposeTriggerList(source, 5, 0, listOf(target), listOf(side))
        assertFalse(d.exposeElements().contains(target))
        assertFalse(d.exposeElements().contains(side))
        assertNull(side.value)
        assertNull(side.errorText)
    }

    @Test
    fun `triggerDependants with a single target adds and removes`() = runTest {
        val d = ds()
        val source = el(1)
        val target = el(10, value = "v")
        val side = el(20, value = "s")
        d.exposeSetUpPage(listOf(source, side))

        assertEquals(0, d.exposeTriggerSingle(source, 2, 2, target))
        assertTrue(d.exposeElements().contains(target))
        assertEquals(-1, d.exposeTriggerSingle(source, 2, 2, target))

        assertEquals(0, d.exposeTriggerSingle(source, 1, 2, target, listOf(side)))
        assertFalse(d.exposeElements().contains(target))
        assertNull(target.value)
        assertNull(side.value)
        assertEquals(-1, d.exposeTriggerSingle(source, 1, 2, target))
    }

    @Test
    fun `triggerDependants with a single target inserts safely when source is absent`() = runTest {
        val d = ds()
        val orphan = el(1)
        val target = el(10)
        d.exposeSetUpPage(listOf(el(2), el(3)))

        d.exposeTriggerSingle(orphan, 0, 0, target)
        assertTrue(d.exposeElements().contains(target))
    }

    @Test
    fun `triggerDependantsReverse adds on mismatch and removes on match`() = runTest {
        val d = ds()
        val source = el(1)
        val t1 = el(10, value = "a")
        val side = el(20, value = "b")
        d.exposeSetUpPage(listOf(source, side))

        assertEquals(0, d.exposeTriggerReverse(source, 5, 0, listOf(t1)))
        assertTrue(d.exposeElements().contains(t1))
        assertEquals(-1, d.exposeTriggerReverse(source, 5, 0, listOf(t1)))

        assertEquals(0, d.exposeTriggerReverse(source, 0, 0, listOf(t1), listOf(side)))
        assertFalse(d.exposeElements().contains(t1))
        assertNull(t1.value)
        assertNull(side.value)
        assertEquals(-1, d.exposeTriggerReverse(source, 0, 0, listOf(t1)))
    }

    @Test
    fun `triggerforHide removes the target and its side effects`() = runTest {
        val d = ds()
        val source = el(1)
        val target = el(10, value = "v")
        val side = el(20, value = "s")
        d.exposeSetUpPage(listOf(source, target, side))

        assertEquals(0, d.exposeTriggerForHide(source, 0, 0, target, listOf(side)))
        assertFalse(d.exposeElements().contains(target))
        assertFalse(d.exposeElements().contains(side))
        assertNull(target.value)
        assertNull(side.value)

        assertEquals(-1, d.exposeTriggerForHide(source, 0, 0, target))
    }

    @Test
    fun `triggerDependants add remove honours default explicit and bottom positions`() = runTest {
        val d = ds()
        val source = el(1)
        val stale = el(2, value = "old")
        val fresh = el(30)
        d.exposeSetUpPage(listOf(source, stale, el(3)))

        // default position: right after the source
        val at = d.exposeTriggerAddRemove(source, listOf(stale), listOf(fresh))
        assertEquals(1, at)
        assertNull(stale.value)
        assertEquals(1, d.exposeIndexOfElement(fresh))

        // explicit position 0
        val at0 = d.exposeTriggerAddRemove(source, emptyList(), listOf(fresh), position = 0)
        assertEquals(0, at0)
        assertEquals(0, d.exposeIndexOfElement(fresh))

        // -2 means "append at the bottom"
        val bottom = d.exposeTriggerAddRemove(source, emptyList(), listOf(fresh), position = -2)
        assertEquals(d.getListSize() - 1, d.exposeIndexOfElement(fresh))
        assertTrue(bottom >= 0)
    }

    @Test
    fun `infantTriggerDependants always appends the added items at the bottom`() = runTest {
        val d = ds()
        val source = el(1)
        val stale = el(2, value = "old")
        val add1 = el(30)
        val add2 = el(31)
        d.exposeSetUpPage(listOf(source, stale, el(3)))

        val pos = d.exposeInfantTrigger(source, listOf(stale), listOf(add1, add2))
        assertNull(stale.value)
        assertEquals(pos, d.exposeIndexOfElement(add1))
        assertEquals(pos + 1, d.exposeIndexOfElement(add2))
        assertEquals(d.getListSize() - 1, d.exposeIndexOfElement(add2))
    }

    // =====================================================================================
    // date / age helpers
    // =====================================================================================

    @Test
    fun `getDiffMonths returns month gap inside a year and minus one across years`() {
        val d = ds()
        val a = Calendar.getInstance().apply { set(2020, Calendar.JANUARY, 15) }
        val b = Calendar.getInstance().apply { set(2020, Calendar.MAY, 20) }
        assertEquals(4, d.exposeDiffMonths(a, b))

        val older = Calendar.getInstance().apply { set(2018, Calendar.JANUARY, 15) }
        assertEquals(-1, d.exposeDiffMonths(older, b))

        // same month, earlier day-of-month on the later date
        val c = Calendar.getInstance().apply { set(2020, Calendar.MAY, 10) }
        assertEquals(3, d.exposeDiffMonths(a, c))
    }

    @Test
    fun `lmp derived dates move in the expected direction`() {
        val d = ds()
        val lmp = 1_600_000_000_000L
        assertTrue(d.exposeEddFromLmp(lmp) > lmp)
        assertTrue(d.exposeAncMaxFromLmp(lmp) > lmp)
        assertTrue(d.exposeMinFromMaxForLmp(lmp) < lmp)
    }

    @Test
    fun `getDoMFromDoR returns null for a null years-since-marriage`() {
        val d = ds()
        assertNull(d.exposeDoMFromDoR(null, 1_600_000_000_000L))
        val computed = d.exposeDoMFromDoR(5, 1_600_000_000_000L)
        assertNotNull(computed)
        assertTrue(computed!! < System.currentTimeMillis())
    }

    @Test
    fun `assignValuesToAgeFromDob writes the year difference and zero for newborns`() {
        val d = ds()
        val thirty = Calendar.getInstance().apply { add(Calendar.YEAR, -30) }.timeInMillis
        val age = el(1).apply { errorText = "stale" }
        assertEquals(-1, d.exposeAgeFromDob(thirty, age))
        assertEquals("30", age.value)
        assertNull(age.errorText)

        val newborn = el(2)
        d.exposeAgeFromDob(Calendar.getInstance().timeInMillis, newborn)
        assertEquals("0", newborn.value)
    }

    @Test
    fun `assignValuesToAgeAndAgeUnitFromDob fills age at marriage and caps its max`() {
        val d = ds()
        val dob = Calendar.getInstance().apply { add(Calendar.YEAR, -30) }.timeInMillis
        val marriage = Calendar.getInstance().apply { add(Calendar.YEAR, -10) }.timeInMillis
        val ageAtMarriage = el(1, value = "stale")

        assertEquals(-1, d.exposeAgeAndAgeUnitFromDob(dob, ageAtMarriage, marriage))
        assertTrue(ageAtMarriage.max == 30L)
        assertEquals("20", ageAtMarriage.value)
    }

    @Test
    fun `assignValuesToAgeAndAgeUnitFromDob handles null element and same-day dob`() {
        val d = ds()
        assertEquals(-1, d.exposeAgeAndAgeUnitFromDob(Calendar.getInstance().timeInMillis))

        val e = el(1, value = "x")
        assertEquals(-1, d.exposeAgeAndAgeUnitFromDob(Calendar.getInstance().timeInMillis, e))
        assertNull(e.value)

        val months = Calendar.getInstance().apply { add(Calendar.MONTH, -5) }.timeInMillis
        assertEquals(-1, d.exposeAgeAndAgeUnitFromDob(months, el(2)))
    }

    @Test
    fun `calculateAge and calculateDob are inverse enough to round trip`() {
        val d = ds()
        val dob = d.exposeCalculateDob(25)
        assertEquals(25, d.exposeCalculateAge(dob))
    }

    // =====================================================================================
    // companion date utilities
    // =====================================================================================

    @Test
    fun `getLongFromDate parses valid dates and returns zero otherwise`() {
        assertTrue(Dataset.getLongFromDate("01-01-2020") > 0L)
        assertEquals(0L, Dataset.getLongFromDate(null))
        assertEquals(0L, Dataset.getLongFromDate(""))
        assertEquals(0L, Dataset.getLongFromDate("not-a-date"))
    }

    @Test
    fun `getDateFromLong formats non zero millis and returns null for zero`() {
        assertNull(Dataset.getDateFromLong(0L))
        val formatted = Dataset.getDateFromLong(Dataset.getLongFromDate("15-03-2021"))
        assertEquals("15-03-2021", formatted)
    }

    @Test
    fun `dateFormate converts yyyy-MM-dd to dd-MM-yyyy and guards bad input`() {
        assertEquals("15-01-2020", Dataset.dateFormate("2020-01-15"))
        assertNull(Dataset.dateFormate(null))
        assertNull(Dataset.dateFormate(""))
        assertNull(Dataset.dateFormate("null"))
        assertNull(Dataset.dateFormate("garbage"))
    }

    @Test
    fun `dateReverseFormat converts dd-MM-yyyy to yyyy-MM-dd and guards bad input`() {
        assertEquals("2020-01-15", Dataset.dateReverseFormat("15-01-2020"))
        assertNull(Dataset.dateReverseFormat(""))
        assertNull(Dataset.dateReverseFormat("garbage"))
    }

    @Test
    fun `getFinancialYear straddles the April boundary`() {
        assertEquals("2020 - 2021", Dataset.getFinancialYear("01-05-2020"))
        assertEquals("2019 - 2020", Dataset.getFinancialYear("01-01-2020"))
        assertNull(Dataset.getFinancialYear(null))
    }

    @Test
    fun `getMonth returns the zero based calendar month`() {
        assertEquals(4, Dataset.getMonth("01-05-2020"))
        assertEquals(0, Dataset.getMonth("31-01-2020"))
        assertNull(Dataset.getMonth(null))
    }

    @Test
    fun `getMinDateOfReg points at the first of january 2020`() {
        val cal = Calendar.getInstance().apply { timeInMillis = Dataset.getMinDateOfReg() }
        assertEquals(2020, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
    }

    // =====================================================================================
    // localisation helpers
    // =====================================================================================

    @Test
    fun `getLocalValueInArray resolves english entries and logs unknown ones`() {
        val d = ds()
        assertEquals("opt5", d.getLocalValueInArray(1, "opt5"))
        assertNull(d.getLocalValueInArray(1, null))
        assertNull(d.getLocalValueInArray(1, ""))
        assertNull(d.getLocalValueInArray(1, "definitely-not-there"))
    }

    @Test
    fun `getEnglishValueInArray resolves localized entries and logs unknown ones`() {
        val d = ds()
        assertEquals("opt7", d.getEnglishValueInArray(1, "opt7"))
        assertNull(d.getEnglishValueInArray(1, null))
        assertNull(d.getEnglishValueInArray(1, ""))
        assertNull(d.getEnglishValueInArray(1, "definitely-not-there"))
    }

    @Test
    fun `getEnglishCheckboxValues maps indexes to english labels`() {
        val d = ds()
        assertEquals("opt0|opt2|opt5", d.getEnglishCheckboxValues(1, "0|2|5"))
        assertEquals("opt1", d.getEnglishCheckboxValues(1, " 1 "))
        assertNull(d.getEnglishCheckboxValues(1, null))
        assertNull(d.getEnglishCheckboxValues(1, ""))
        assertNull(d.getEnglishCheckboxValues(1, "abc|xyz"))
        assertNull(d.getEnglishCheckboxValues(1, "500|900"))
    }

    @Test
    fun `getCheckboxIndexesFromValues passes indexes through and maps labels back`() {
        val d = ds()
        assertEquals("1|2", d.getCheckboxIndexesFromValues(1, "1|2"))
        assertEquals("1|3", d.getCheckboxIndexesFromValues(1, "opt3|opt1"))
        assertNull(d.getCheckboxIndexesFromValues(1, null))
        assertNull(d.getCheckboxIndexesFromValues(1, ""))
        assertNull(d.getCheckboxIndexesFromValues(1, "nope|nada"))
    }

    // =====================================================================================
    // isValidChildGap
    // =====================================================================================

    @Test
    fun `isValidChildGap accepts twins and gaps of a year or more`() {
        val d = ds()
        val twin = el(1, value = "01-01-2015")
        assertEquals(-1, d.isValidChildGap(twin, "01-01-2015"))
        assertNull(twin.errorText)

        val spaced = el(2, value = "01-01-2017")
        d.isValidChildGap(spaced, "01-01-2015")
        assertNull(spaced.errorText)
    }

    @Test
    fun `isValidChildGap rejects short gaps and unparseable dates`() {
        val d = ds()
        val tooClose = el(1, value = "01-06-2015")
        d.isValidChildGap(tooClose, "01-01-2015")
        assertNotNull(tooClose.errorText)

        val broken = el(2, value = "not-a-date")
        d.isValidChildGap(broken, "01-01-2015")
        assertEquals("Invalid date format or parsing error", broken.errorText)

        val nullFirst = el(3, value = "01-01-2015")
        d.isValidChildGap(nullFirst, null)
        assertEquals("Invalid date format or parsing error", nullFirst.errorText)
    }

    // =====================================================================================
    // validators
    // =====================================================================================

    @Test
    fun `validateEmptyOnEditText only complains for required empty fields`() {
        val d = ds()
        val required = el(1, required = true)
        assertEquals(-1, d.vEmpty(required))
        assertEquals("err", required.errorText)

        required.value = "filled"
        d.vEmpty(required)
        assertNull(required.errorText)

        val optional = el(2, required = false).apply { errorText = "untouched" }
        d.vEmpty(optional)
        assertEquals("untouched", optional.errorText)
    }

    @Test
    fun `validateAllCapsOrSpaceOnEditText enforces upper case only for english`() {
        val english = ds(Languages.ENGLISH)
        val good = el(1, value = "JOHN DOE", allCaps = true)
        english.vAllCaps(good)
        assertNull(good.errorText)

        val bad = el(2, value = "John Doe", allCaps = true)
        english.vAllCaps(bad)
        assertEquals("err", bad.errorText)

        val emptyOptional = el(3, allCaps = true).apply { errorText = "stale" }
        english.vAllCaps(emptyOptional)
        assertNull(emptyOptional.errorText)

        val notAllCaps = el(4, value = "John", allCaps = false).apply { errorText = null }
        english.vAllCaps(notAllCaps)
        assertNull(notAllCaps.errorText)

        // a non-english dataset skips the check entirely
        val hindi = ds(Languages.HINDI)
        val skipped = el(5, value = "John Doe", allCaps = true)
        hindi.vAllCaps(skipped)
        assertNull(skipped.errorText)
    }

    @Test
    fun `validateAllCapsOrSpaceOnEditTextWithHindiEnabled upper-cases latin and keeps devanagari`() {
        val d = ds()
        val empty = el(1, required = true, allCaps = true)
        d.vAllCapsHindi(empty)
        assertEquals("err", empty.errorText)

        val emptyOptional = el(2, required = false, allCaps = true).apply { errorText = "stale" }
        d.vAllCapsHindi(emptyOptional)
        assertNull(emptyOptional.errorText)

        val lower = el(3, value = "john", allCaps = true)
        d.vAllCapsHindi(lower)
        assertEquals("err", lower.errorText)

        val upper = el(4, value = " JOHN DOE ", allCaps = true)
        d.vAllCapsHindi(upper)
        assertEquals("JOHN DOE", upper.value)

        val devanagari = el(5, value = "राम", allCaps = true)
        d.vAllCapsHindi(devanagari)
        assertEquals("राम", devanagari.value)

        val notAllCaps = el(6, value = "john", allCaps = false)
        d.vAllCapsHindi(notAllCaps)
        assertEquals("john", notAllCaps.value)
    }

    @Test
    fun `validateEditTextWithTextNonNumericHindiEnabled rejects digits and normalises case`() {
        val d = ds()
        val empty = el(1, required = true)
        d.vNonNumericHindi(empty)
        assertEquals("err", empty.errorText)

        val emptyOptional = el(2, required = false).apply { errorText = "stale" }
        d.vNonNumericHindi(emptyOptional)
        assertNull(emptyOptional.errorText)

        val digits = el(3, value = "JOHN1")
        d.vNonNumericHindi(digits)
        assertEquals("err", digits.errorText)

        val ok = el(4, value = " JOHN DOE ")
        d.vNonNumericHindi(ok)
        assertEquals("JOHN DOE", ok.value)
    }

    @Test
    fun `validateEditTextFullLengthOccupied compares against etMaxLength`() {
        val d = ds()
        val exact = el(1, value = "abc", etMaxLength = 3)
        d.vFullLength(exact)
        assertNull(exact.errorText)

        val short = el(2, value = "ab", etMaxLength = 3)
        d.vFullLength(short)
        assertEquals("err2", short.errorText)

        val empty = el(3, etMaxLength = 3).apply { errorText = "stale" }
        d.vFullLength(empty)
        assertNull(empty.errorText)
    }

    @Test
    fun `alphabet and alphanumeric validators flag the wrong character classes`() {
        val d = ds()
        val alphaOk = el(1, value = "JOHN DOE")
        d.vAlphaSpace(alphaOk)
        assertNull(alphaOk.errorText)

        val alphaBad = el(2, value = "JOHN1")
        d.vAlphaSpace(alphaBad)
        assertEquals("err", alphaBad.errorText)

        val specialOk = el(3, value = "JOHN.DOE")
        d.vAlphaSpecial(specialOk)
        assertNull(specialOk.errorText)

        val specialBad = el(4, value = "JOHN9")
        d.vAlphaSpecial(specialBad)
        assertEquals("err", specialBad.errorText)

        val alnumSpaceOk = el(5, value = "AB 12")
        d.vAlphaNumericSpace(alnumSpaceOk)
        assertNull(alnumSpaceOk.errorText)

        val alnumSpaceBad = el(6, value = "AB-12")
        d.vAlphaNumericSpace(alnumSpaceBad)
        assertEquals("err", alnumSpaceBad.errorText)

        val alnumOk = el(7, value = "AB12")
        d.vAlphaNumeric(alnumOk)
        assertNull(alnumOk.errorText)

        val alnumBad = el(8, value = "AB 12")
        d.vAlphaNumeric(alnumBad)
        assertEquals("err", alnumBad.errorText)

        val emptyAlnum = el(9).apply { errorText = "stale" }
        d.vAlphaNumeric(emptyAlnum)
        assertNull(emptyAlnum.errorText)
    }

    @Test
    fun `validateAllAlphabetsSpecialAndNumericOnEditText accepts punctuation and rejects symbols`() {
        val d = ds()
        val ok = el(1, value = "Ram Kumar 12, S/O.")
        d.vAlphaSpecialDigit(ok)
        assertNull(ok.errorText)

        val bad = el(2, value = "Ram €")
        d.vAlphaSpecialDigit(bad)
        assertEquals("err", bad.errorText)
    }

    @Test
    fun `validateNoAlphabetSpaceOnEditText rejects letters and spaces`() {
        val d = ds()
        val digits = el(1, value = "12345")
        d.vNoAlphaSpace(digits)
        assertNull(digits.errorText)

        val spaced = el(2, value = "12 45")
        d.vNoAlphaSpace(spaced)
        assertEquals("err", spaced.errorText)

        val empty = el(3).apply { errorText = "stale" }
        d.vNoAlphaSpace(empty)
        assertNull(empty.errorText)
    }

    @Test
    fun `validateABHANumberEditText requires exactly fourteen digits`() {
        val d = ds()
        assertTrue(d.exposeIsValid("12345678901234"))
        assertFalse(d.exposeIsValid("1234"))

        val ok = el(1, value = "12345678901234")
        d.vAbha(ok)
        assertNull(ok.errorText)

        val bad = el(2, value = "1234")
        d.vAbha(bad)
        assertEquals("err", bad.errorText)

        val empty = el(3).apply { errorText = "stale" }
        d.vAbha(empty)
        assertNull(empty.errorText)
    }

    @Test
    fun `validateIFSCEditText enforces four letters then seven digits`() {
        val d = ds()
        val ok = el(1, value = "SBIN0001234")
        d.vIfsc(ok)
        assertNull(ok.errorText)

        val bad = el(2, value = "sbin1")
        d.vIfsc(bad)
        assertEquals("err", bad.errorText)

        val empty = el(3).apply { errorText = "stale" }
        d.vIfsc(empty)
        assertNull(empty.errorText)
    }

    @Test
    fun `validateBirthCertificateNumber covers charset length repetition and digit skew`() {
        val d = ds()
        val empty = el(1, value = "  ")
        assertEquals(0, d.vBirthCert(empty))
        assertNull(empty.errorText)

        val symbols = el(2, value = "AB#123")
        assertEquals(-1, d.vBirthCert(symbols))
        assertEquals("err", symbols.errorText)

        val tooShort = el(3, value = "AB12")
        assertEquals(-1, d.vBirthCert(tooShort))
        assertEquals("err", tooShort.errorText)

        val allSame = el(4, value = "AAAAAA")
        assertEquals(-1, d.vBirthCert(allSame))
        assertEquals("err", allSame.errorText)

        val skewedDigits = el(5, value = "1111122")
        assertEquals(-1, d.vBirthCert(skewedDigits))
        assertEquals("err", skewedDigits.errorText)

        val good = el(6, value = "AB123456")
        assertEquals(0, d.vBirthCert(good))
        assertNull(good.errorText)
    }

    @Test
    fun `validateAllDigitOnEditText and validateAllZerosOnEditText`() {
        val d = ds()
        val digits = el(1, value = "12345")
        d.vAllDigit(digits)
        assertNull(digits.errorText)

        val mixed = el(2, value = "12a45")
        d.vAllDigit(mixed)
        assertEquals("err", mixed.errorText)

        val emptyDigits = el(3).apply { errorText = "stale" }
        d.vAllDigit(emptyDigits)
        assertNull(emptyDigits.errorText)

        val zeros = el(4, value = "0000")
        d.vAllZeros(zeros)
        assertEquals("Cannot be 0", zeros.errorText)

        val nonZero = el(5, value = "1000")
        d.vAllZeros(nonZero)
        assertNull(nonZero.errorText)

        val emptyZeros = el(6).apply { errorText = "stale" }
        d.vAllZeros(emptyZeros)
        assertNull(emptyZeros.errorText)
    }

    @Test
    fun `validateUploads requires the minimum number of populated slots`() {
        val d = ds()
        val a = el(1)
        val b = el(2)
        assertEquals(0, d.vUploads(listOf(a, b), 2))
        assertEquals("err", a.errorText)
        assertEquals("err", b.errorText)

        a.value = "file-a"
        b.value = "file-b"
        assertEquals(-1, d.vUploads(listOf(a, b), 2))
        assertNull(a.errorText)
        assertNull(b.errorText)
    }

    @Test
    fun `validateIntMinMax reports below-min above-max and ignores non numeric`() {
        val d = ds()
        val low = el(1, value = "5", min = 10, max = 20)
        d.vIntMinMax(low)
        assertNotNull(low.errorText)

        val high = el(2, value = "25", min = 10, max = 20)
        d.vIntMinMax(high)
        assertNotNull(high.errorText)

        val ok = el(3, value = "15", min = 10, max = 20)
        d.vIntMinMax(ok)
        assertNull(ok.errorText)

        val notNumeric = el(4, value = "abc", min = 10, max = 20).apply { errorText = "stale" }
        d.vIntMinMax(notNumeric)
        assertNull(notNumeric.errorText)

        val noBounds = el(5, value = "15")
        d.vIntMinMax(noBounds)
        assertNull(noBounds.errorText)
    }

    @Test
    fun `validateDoubleMinMax normalises a leading dot and checks the decimal bounds`() {
        val d = ds()
        val low = el(1, value = ".5", minDecimal = 1.0, maxDecimal = 5.0)
        d.vDoubleMinMax(low)
        assertNotNull(low.errorText)

        val high = el(2, value = "9.5", minDecimal = 1.0, maxDecimal = 5.0)
        d.vDoubleMinMax(high)
        assertNotNull(high.errorText)

        val ok = el(3, value = "3.5", minDecimal = 1.0, maxDecimal = 5.0)
        d.vDoubleMinMax(ok)
        assertNull(ok.errorText)

        val empty = el(4, minDecimal = 1.0, maxDecimal = 5.0).apply { errorText = "stale" }
        d.vDoubleMinMax(empty)
        assertNull(empty.errorText)
    }

    @Test
    fun `validateDoubleUpto1DecimalPlaces guards separators and precision`() {
        val d = ds()
        val twoDots = el(1, value = "1.2.3")
        d.vDouble1Dp(twoDots)
        assertEquals("Invalid value", twoDots.errorText)

        val plainDigits = el(2, value = "12")
        d.vDouble1Dp(plainDigits)
        assertNull(plainDigits.errorText)

        val plainBad = el(3, value = "1a")
        d.vDouble1Dp(plainBad)
        assertEquals("Invalid value", plainBad.errorText)

        val decimalBad = el(4, value = "1.a")
        d.vDouble1Dp(decimalBad)
        assertEquals("Invalid value", decimalBad.errorText)

        val tooPrecise = el(5, value = "1.23")
        d.vDouble1Dp(tooPrecise)
        assertEquals("Only 1 decimal place allowed", tooPrecise.errorText)

        val ok = el(6, value = "1.2")
        d.vDouble1Dp(ok)
        assertNull(ok.errorText)
    }

    @Test
    fun `validateMobileNumberOnEditText rejects low and repeated numbers`() {
        val d = ds()
        val tooLow = el(1, value = "5999999999")
        d.vMobile(tooLow)
        assertEquals("err", tooLow.errorText)

        val repeated = el(2, value = "9999999999")
        d.vMobile(repeated)
        assertEquals("err", repeated.errorText)

        val ok = el(3, value = "9876543210")
        d.vMobile(ok)
        assertNull(ok.errorText)

        val notNumeric = el(4, value = "abcd").apply { errorText = "stale" }
        d.vMobile(notNumeric)
        assertNull(notNumeric.errorText)
    }

    @Test
    fun `validateRchIdOnEditText validateMcpOnEditText and validateAadharNoOnEditText`() {
        val d = ds()
        val shortRch = el(1, value = "1234")
        d.vRchId(shortRch)
        assertEquals("err", shortRch.errorText)

        val uniformRch = el(2, value = "111111111111")
        d.vRchId(uniformRch)
        assertEquals("All digits cannot be 1", uniformRch.errorText)

        val okRch = el(3, value = "123456789012")
        d.vRchId(okRch)
        assertNull(okRch.errorText)

        val shortMcp = el(4, value = "1234")
        d.vMcp(shortMcp)
        assertEquals("err", shortMcp.errorText)

        val zeroMcp = el(5, value = "000000000000")
        d.vMcp(zeroMcp)
        assertEquals("Cannot be 0", zeroMcp.errorText)

        val okMcp = el(6, value = "123456789012")
        d.vMcp(okMcp)
        assertNull(okMcp.errorText)

        val shortAadhar = el(7, value = "1234")
        d.vAadhar(shortAadhar)
        assertEquals("err", shortAadhar.errorText)

        val zeroAadhar = el(8, value = "000000000000")
        d.vAadhar(zeroAadhar)
        assertEquals("err", zeroAadhar.errorText)

        val okAadhar = el(9, value = "123456789012")
        d.vAadhar(okAadhar)
        assertNull(okAadhar.errorText)
    }

    @Test
    fun `validateWeightOnEditText covers every gram band`() {
        val d = ds()
        val empty = el(1).apply { errorText = "stale" }
        d.vWeight(empty)
        assertNull(empty.errorText)

        val notNumeric = el(2, value = "abc")
        d.vWeight(notNumeric)
        assertEquals("err", notNumeric.errorText)

        val zero = el(3, value = "0")
        d.vWeight(zero)
        assertEquals("err", zero.errorText)

        val kilograms = el(4, value = "3.2")
        d.vWeight(kilograms)
        assertEquals("err", kilograms.errorText)

        val tooLight = el(5, value = "300")
        d.vWeight(tooLight)
        assertEquals("err", tooLight.errorText)

        val tooHeavy = el(6, value = "8000")
        d.vWeight(tooHeavy)
        assertEquals("err", tooHeavy.errorText)

        val ok = el(7, value = "3000")
        d.vWeight(ok)
        assertNull(ok.errorText)
    }

    @Test
    fun `validateForBp covers format systole diastole and inversion`() {
        val d = ds()
        val empty = el(1).apply { errorText = "stale" }
        d.vBp(empty)
        assertNull(empty.errorText)

        val malformed = el(2, value = "abc")
        d.vBp(malformed)
        assertEquals("err", malformed.errorText)

        val lowSys = el(3, value = "40/35")
        d.vBp(lowSys)
        assertEquals("err2", lowSys.errorText)

        val highSys = el(4, value = "310/80")
        d.vBp(highSys)
        assertEquals("err2", highSys.errorText)

        val lowDia = el(5, value = "120/20")
        d.vBp(lowDia)
        assertEquals("err2", lowDia.errorText)

        val highDia = el(6, value = "220/210")
        d.vBp(highDia)
        assertEquals("err2", highDia.errorText)

        val inverted = el(7, value = "100/120")
        d.vBp(inverted)
        assertEquals("err", inverted.errorText)

        val ok = el(8, value = "120/80")
        d.vBp(ok)
        assertNull(ok.errorText)
    }

    @Test
    fun `currentLanguage is exposed as constructed`() {
        assertEquals(Languages.ENGLISH, ds(Languages.ENGLISH).currentLanguage)
        assertEquals(Languages.HINDI, ds(Languages.HINDI).currentLanguage)
        assertEquals(Languages.ASSAMESE, ds(Languages.ASSAMESE).currentLanguage)
        assertEquals(Languages.BANGLA, ds(Languages.BANGLA).currentLanguage)
    }
}
