package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.util.Range
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
/** FLW-1199: Status of Women in view mode, and spouse name for Divorced. */
class BenRegFormDatasetReadModeTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var res: Resources

    @MockK
    private lateinit var hiRes: Resources

    private val englishStatus = mapOf(
        1 to "Eligible Couple", 2 to "Pregnant Woman", 3 to "Postnatal Mother", 4 to "Elderly Woman",
        5 to "Adolescent Girl", 6 to "Permanently Sterilised", 7 to "Not Applicable",
    )

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
        every { HelperUtil.getLocalizedResources(any(), any()) } returns res
        every { HelperUtil.getLocalizedResources(any(), Languages.HINDI) } returns hiRes
        // Status-of-women arrays exactly as compiled into the APK (aapt2 dump), in English and Hindi.
        stub(res, "Adolescent Girl", listOf("Eligible Couple", "Pregnant Woman", "Postnatal Mother", "Permanently Sterilised"),
            "Not Applicable", "Elderly Woman")
        stub(hiRes, "किशोरी लड़की", listOf("लक्ष्य दम्पति", "गर्भवती महिला", "प्रसवोत्तर माता", "स्थायी नसबंदीकृत महिला"),
            "लागू नहीं", "वृद्धा महिला")
        mockkConstructor(Range::class)
        every { anyConstructed<Range<Int>>().contains(any<Int>()) } returns true
    }

    private fun stub(r: Resources, ag: String, ecPwPncPs: List<String>, na: String, elderly: String) {
        every { r.getStringArray(any()) } returns Array(30) { i -> "opt$i" }
        every { r.getStringArray(R.array.nbr_reproductive_status_array1) } returns arrayOf(ag)
        every { r.getStringArray(R.array.nbr_reproductive_status_array2) } returns ecPwPncPs.toTypedArray()
        every { r.getStringArray(R.array.nbr_reproductive_status_array3) } returns arrayOf(na)
        every { r.getStringArray(R.array.nbr_reproductive_status_array4) } returns ecPwPncPs.toTypedArray()
        every { r.getStringArray(R.array.nbr_reproductive_status_array5) } returns arrayOf(elderly)
        every { r.getString(any()) } returns "x"
        every { r.getString(R.string.dd_ag) } returns ag
        every { r.getString(any(), any()) } returns "x"
    }

    private fun savedWoman(maritalStatusId: Int, statusId: Int, english: String?, gender: Gender = Gender.FEMALE): BenRegCache {
        val g = mockk<BenRegGen>(relaxed = true)
        every { g.maritalStatusId } returns maritalStatusId
        every { g.reproductiveStatusId } returns statusId
        every { g.reproductiveStatus } returns english
        every { g.spouseName } returns "SPOUSE"
        every { g.marriageDate } returns 0L
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns Calendar.getInstance().apply { add(Calendar.YEAR, -30) }.timeInMillis
        every { b.regDate } returns 1_600_000_000_000L
        every { b.genderId } returns gender.ordinal + 1
        every { b.gender } returns gender
        every { b.isDraft } returns false
        every { b.isDeath } returns false
        every { b.isKid } returns false
        every { b.isMarried } returns (maritalStatusId == 2)
        every { b.familyHeadRelationPosition } returns 18
        every { b.genDetails } returns g
        return b
    }

    private suspend fun readPage(
        maritalStatusId: Int,
        statusId: Int,
        english: String? = englishStatus[statusId],
        language: Languages = Languages.ENGLISH,
        gender: Gender = Gender.FEMALE,
    ) =
        BenRegFormDataset(context, language).also {
            it.setFirstPageToRead(savedWoman(maritalStatusId, statusId, english, gender), 9876543210L)
        }

    private fun BenRegFormDataset.field(id: Int) = listFlow.value.find { it.id == id }

    // FLW-1199 / FLW-1152: view mode must show the saved Status of Women for every marital status.
    @Test
    fun `read mode shows the stored status of women for every status and non-single marital status`() = runTest {
        for (marital in 2..5) for ((statusId, english) in englishStatus) {
            if (statusId == 5) continue // adolescent girl is only stored for unmarried women
            assertEquals(
                "marital=$marital status=$english",
                english,
                readPage(marital, statusId).field(REPRODUCTIVE_STATUS)?.value
            )
        }
    }

    @Test
    fun `read mode shows the stored status in the user's language`() = runTest {
        assertEquals(
            "स्थायी नसबंदीकृत महिला",
            readPage(DIVORCED, 6, language = Languages.HINDI).field(REPRODUCTIVE_STATUS)?.value
        )
        assertEquals("लक्ष्य दम्पति", readPage(MARRIED, 1, language = Languages.HINDI).field(REPRODUCTIVE_STATUS)?.value)
        assertEquals("वृद्धा महिला", readPage(4, 4, language = Languages.HINDI).field(REPRODUCTIVE_STATUS)?.value)
    }

    @Test
    fun `read mode shows permanently sterilised for a divorced woman`() = runTest {
        assertEquals("Permanently Sterilised", readPage(DIVORCED, 6).field(REPRODUCTIVE_STATUS)?.value)
    }

    @Test
    fun `read mode keeps sterilisation hidden for an unmarried woman`() = runTest {
        assertEquals("", readPage(UNMARRIED, 6).field(REPRODUCTIVE_STATUS)?.value)
    }

    @Test
    fun `read mode shows adolescent girl for an unmarried woman`() = runTest {
        assertEquals("Adolescent Girl", readPage(UNMARRIED, 5).field(REPRODUCTIVE_STATUS)?.value)
    }

    @Test
    fun `read mode falls back to the status id when no label was stored`() = runTest {
        assertEquals("Permanently Sterilised", readPage(DIVORCED, 6, english = null).field(REPRODUCTIVE_STATUS)?.value)
        assertEquals("Permanently Sterilised", readPage(MARRIED, 6, english = "null").field(REPRODUCTIVE_STATUS)?.value)
        assertEquals("", readPage(UNMARRIED, 6, english = "").field(REPRODUCTIVE_STATUS)?.value)
        assertEquals("Adolescent Girl", readPage(UNMARRIED, 5, english = null).field(REPRODUCTIVE_STATUS)?.value)
    }

    // FLW-1199: husband name is mandatory for Divorced, like Separated and Widow.
    @Test
    fun `husband name is mandatory after switching to divorced, separated or widow`() = runTest {
        for (marital in listOf(DIVORCED, 4, 5)) {
            val d = readPage(MARRIED, 2)
            d.setValueById(MARITAL_STATUS, "opt${marital - 1}")
            d.updateList(MARITAL_STATUS, marital - 1)
            val husband = d.field(HUSBAND_NAME)
            assertEquals("marital=$marital husband field shown", true, husband != null)
            assertEquals("marital=$marital husband required", true, husband!!.required)

            husband.value = ""
            d.updateList(HUSBAND_NAME, 0)
            assertNotNull("marital=$marital empty husband name must error", husband.errorText)
        }
    }

    @Test
    fun `viewing a divorced record does not relax husband name for the edit that follows`() = runTest {
        val d = readPage(DIVORCED, 6)
        d.setValueById(MARITAL_STATUS, "opt2")
        d.updateList(MARITAL_STATUS, 2)
        assertEquals(true, d.field(HUSBAND_NAME)?.required)
    }

    // FLW-1199 scope: only husband name became mandatory; wife / spouse name stay optional when Divorced.
    @Test
    fun `wife and spouse name stay optional for divorced but required for separated and widow`() = runTest {
        for ((gender, fieldId) in listOf(Gender.MALE to WIFE_NAME, Gender.TRANSGENDER to SPOUSE_NAME)) {
            val divorced = readPage(MARRIED, 0, english = null, gender = gender)
            divorced.setValueById(MARITAL_STATUS, "opt2")
            divorced.updateList(MARITAL_STATUS, 2)
            assertEquals("$gender divorced", false, divorced.field(fieldId)?.required)
        }
        for (marital in listOf(4, 5)) {
            val d = readPage(MARRIED, 0, english = null, gender = Gender.MALE)
            d.setValueById(MARITAL_STATUS, "opt${marital - 1}")
            d.updateList(MARITAL_STATUS, marital - 1)
            assertEquals("male marital=$marital", true, d.field(WIFE_NAME)?.required)
        }
    }

    private companion object {
        const val REPRODUCTIVE_STATUS = 1028
        const val MARITAL_STATUS = 1008
        const val HUSBAND_NAME = 1009
        const val WIFE_NAME = 1010
        const val SPOUSE_NAME = 1011
        const val UNMARRIED = 1
        const val MARRIED = 2
        const val DIVORCED = 3
    }
}
