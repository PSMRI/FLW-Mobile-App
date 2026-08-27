package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.junit.Assert.assertSame
import retrofit2.Response
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYFormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: CUFYFormResponseJsonDao

    private lateinit var repo: CUFYFormRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.formSchemaDao() } returns schemaDao
        every { db.CUFYFormResponseJsonDao() } returns jsonDao
        repo = CUFYFormRepository(context, api, pref, db)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getSyncedVisitsByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<CUFYFormResponseJsonEntity>())
        coEvery { jsonDao.getSyncedVisitsByRchId(5L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchId(5L))
    }

    @Test
    fun `getAllFormVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllFormVisits("cufy", request) } returns response
        assertEquals(response, repo.getAllFormVisits("cufy", request))
    }

    @Test
    fun `getBottleList returns empty for no data`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns emptyList()
        assertTrue(repo.getBottleList(1L, "cufy").isEmpty())
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getFormResponse(1L, "d") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson(1L, "d"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<CUFYFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms("cufy") } returns list
        assertEquals(list, repo.getUnsyncedForms("cufy"))
    }

    @Test
    fun `getSavedDataByFormId delegates to dao`() = runTest {
        val list = listOf(mockk<CUFYFormResponseJsonEntity>())
        coEvery { jsonDao.getFormsDataByFormID("cufy", 2L) } returns list
        assertEquals(list, repo.getSavedDataByFormId("cufy", 2L))
    }

    @Test
    fun `insertOrUpdateFormResponse inserts entity when none existing`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        repo.insertOrUpdateFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `insertFormResponse inserts new record when id is zero`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        every { entity.id } returns 0
        repo.insertFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns false for invalid entity`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        assertFalse(repo.syncFormToServer("user", "cufy", entity))
    }

    @Test
    fun `saveDownloadedVisitList does nothing for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "cufy")
        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `getFormSchema saves and returns schema when api succeeds and no local schema`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        every { schema.formId } returns "F1"
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns schema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        coEvery { schemaDao.getSchema("F1") } returns null

        val result = repo.getFormSchema("F1")

        assertSame(schema, result)
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema does not save when api response not successful and falls back to db`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        coEvery { schemaDao.getSchema("F1") } returns null
        // db returns null too -> loadSchemaFromAssets will be attempted, but assets throws and is caught
        every { context.assets } throws RuntimeException("no assets")

        val result = repo.getFormSchema("F1")

        assertEquals(null, result)
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `saveFormSchemaToDb inserts built entity`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        repo.saveFormSchemaToDb(schema)
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `insertOrUpdateFormResponse copies id and inserts when existing found`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        val existing = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any()) } returns existing
        repo.insertOrUpdateFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `insertFormResponse inserts new when id positive but no existing record`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        every { entity.id } returns 7
        coEvery { jsonDao.getFormResponseById(7) } returns null
        repo.insertFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `insertFormResponse updates when id positive and existing record found`() = runTest {
        val entity = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        every { entity.id } returns 7
        val existing = mockk<CUFYFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponseById(7) } returns existing
        repo.insertFormResponse(entity)
        coVerify { jsonDao.updateFormResponse(any()) }
        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `loadFormResponseJson returns null when no record`() = runTest {
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        assertEquals(null, repo.loadFormResponseJson(1L, "d"))
    }

    private fun bottleJson(date: String, count: String) =
        """{"fields":{"ifa_provision_date":"$date","ifa_bottle_count":"$count"}}"""

    @Test
    fun `getBottleList maps date and count and numbers rows in insertion order`() = runTest {
        coEvery { jsonDao.getFormJsonList(1L, "F1") } returns listOf(
            bottleJson("01-01-2026", "2"),
            bottleJson("02-01-2026", "3")
        )
        val list = repo.getBottleList(1L, "F1")
        assertEquals(2, list.size)
        assertEquals(1, list[0].srNo)
        assertEquals("2", list[0].bottleNumber)
        assertEquals("01-01-2026", list[0].dateOfProvision)
        assertEquals(2, list[1].srNo)
        assertEquals("3", list[1].bottleNumber)
    }

    @Test
    fun `getBottleList sorts by provision date not insertion order`() = runTest {
        coEvery { jsonDao.getFormJsonList(1L, "F1") } returns listOf(
            bottleJson("05-03-2026", "9"),
            bottleJson("01-01-2026", "1"),
            bottleJson("03-02-2026", "5")
        )
        val list = repo.getBottleList(1L, "F1")
        assertEquals(
            listOf("01-01-2026", "03-02-2026", "05-03-2026"),
            list.map { it.dateOfProvision }
        )
    }

    @Test
    fun `getBottleList falls back to dash when fields block absent`() = runTest {
        coEvery { jsonDao.getFormJsonList(1L, "F1") } returns listOf("""{"other":1}""")
        val list = repo.getBottleList(1L, "F1")
        assertEquals(1, list.size)
        assertEquals("-", list[0].dateOfProvision)
        assertEquals("-", list[0].bottleNumber)
    }

    @Test
    fun `getBottleList skips rows whose json cannot be parsed`() = runTest {
        coEvery { jsonDao.getFormJsonList(1L, "F1") } returns listOf(
            "not-json",
            bottleJson("01-01-2026", "2")
        )
        val list = repo.getBottleList(1L, "F1")
        assertEquals(1, list.size)
        assertEquals("2", list[0].bottleNumber)
    }

    @Test
    fun `getBottleList keeps unparseable dates but does not crash`() = runTest {
        coEvery { jsonDao.getFormJsonList(1L, "F1") } returns listOf(
            bottleJson("garbage", "1"),
            bottleJson("01-01-2026", "2")
        )
        assertEquals(2, repo.getBottleList(1L, "F1").size)
    }

    @Test
    fun `syncFormToServer returns true when api call succeeds`() = runTest {
        val entity = CUFYFormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDate = "01-01-2026",
            formId = "F1",
            version = 1,
            formDataJson = """{"formId":"F1","fields":{"a":"b"}}""",
            isSynced = false
        )
        coEvery { api.submitChildCareForm(any(), any()) } returns Response.success(mockk())
        assertTrue(repo.syncFormToServer("user", "form", entity))
    }

    @Test
    fun `syncFormToServer returns false when api throws`() = runTest {
        val entity = CUFYFormResponseJsonEntity(
            benId = 1L,
            hhId = 2L,
            visitDate = "01-01-2026",
            formId = "F1",
            version = 1,
            formDataJson = """{"formId":"F1","fields":{"a":"b"}}""",
            isSynced = false
        )
        coEvery { api.submitChildCareForm(any(), any()) } throws RuntimeException("boom")
        assertFalse(repo.syncFormToServer("user", "form", entity))
    }

    @Test
    fun `insertFormResponse merges follow-up dates without duplicates`() = runTest {
        val existing = CUFYFormResponseJsonEntity(
            id = 7,
            benId = 1L,
            hhId = 2L,
            visitDate = "01-01-2026",
            formId = "F1",
            version = 1,
            formDataJson = """{"formId":"F1","beneficiaryId":1,"houseHoldId":2,"visitDate":"01-01-2026","fields":{"follow_up_visit_date":["01-01-2026","02-01-2026"]}}""",
            isSynced = false
        )
        val incoming = existing.copy(
            formDataJson = """{"formId":"F1","beneficiaryId":1,"houseHoldId":2,"visitDate":"01-01-2026","fields":{"follow_up_visit_date":["02-01-2026","03-01-2026"],"note":"x"}}"""
        )
        coEvery { jsonDao.getFormResponseById(7) } returns existing
        val saved = slot<CUFYFormResponseJsonEntity>()
        coEvery { jsonDao.updateFormResponse(capture(saved)) } returns 1

        repo.insertFormResponse(incoming)

        val fields = JSONObject(saved.captured.formDataJson).getJSONObject("fields")
        val dates = fields.getJSONArray("follow_up_visit_date")
        assertEquals(3, dates.length())
        assertEquals("01-01-2026", dates.getString(0))
        assertEquals("02-01-2026", dates.getString(1))
        assertEquals("03-01-2026", dates.getString(2))
        assertEquals("x", fields.getString("note"))
    }

    @Test
    fun `insertFormResponse merge tolerates malformed existing json`() = runTest {
        val existing = CUFYFormResponseJsonEntity(
            id = 7,
            benId = 1L,
            hhId = 2L,
            visitDate = "01-01-2026",
            formId = "F1",
            version = 1,
            formDataJson = "not-json",
            isSynced = false
        )
        val incoming = existing.copy(formDataJson = """{"fields":{"a":"b"}}""")
        coEvery { jsonDao.getFormResponseById(7) } returns existing
        val saved = slot<CUFYFormResponseJsonEntity>()
        coEvery { jsonDao.updateFormResponse(capture(saved)) } returns 1

        repo.insertFormResponse(incoming)

        assertEquals("""{"fields":{"a":"b"}}""", saved.captured.formDataJson)
    }

    @Test
    fun `insertFormResponse merge drops blank follow-up dates`() = runTest {
        val existing = CUFYFormResponseJsonEntity(
            id = 7,
            benId = 1L,
            hhId = 2L,
            visitDate = "01-01-2026",
            formId = "F1",
            version = 1,
            formDataJson = """{"fields":{"follow_up_visit_date":["","01-01-2026"]}}""",
            isSynced = false
        )
        val incoming = existing.copy(
            formDataJson = """{"fields":{"follow_up_visit_date":["  ",""]}}"""
        )
        coEvery { jsonDao.getFormResponseById(7) } returns existing
        val saved = slot<CUFYFormResponseJsonEntity>()
        coEvery { jsonDao.updateFormResponse(capture(saved)) } returns 1

        repo.insertFormResponse(incoming)

        val dates = JSONObject(saved.captured.formDataJson)
            .getJSONObject("fields").getJSONArray("follow_up_visit_date")
        assertEquals(1, dates.length())
        assertEquals("01-01-2026", dates.getString(0))
    }

    private fun mockSamResources(): Resources {
        val resources = mockk<Resources>(relaxed = true)
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns resources
        every { resources.getString(R.string.check_sam_) } returns "check_sam"
        every { resources.getString(R.string.follow_up_sam) } returns "follow_up_sam"
        every { resources.getString(R.string.nrc_admitted) } returns "nrc_admitted"
        every { resources.getString(R.string.referred_to_nrc) } returns "referred_to_nrc"
        every { context.getString(R.string.is_child_referred_nrc) } returns "referred_key"
        every { context.getString(R.string.is_child_admitted_nrc) } returns "admitted_key"
        every { context.getString(R.string.is_child_discharged_nrc) } returns "discharged_key"
        every { context.getString(R.string.sam_status) } returns "sam_status_key"
        return resources
    }

    private fun samForm(visitDate: String, fieldsJson: String) = CUFYFormResponseJsonEntity(
        benId = 1L,
        hhId = 2L,
        visitDate = visitDate,
        formId = FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID,
        version = 1,
        formDataJson = """{"fields":$fieldsJson}""",
        isSynced = false
    )

    @Test
    fun `getCurrentSamStatus returns check_sam_ when no sam forms saved`() = runTest {
        mockSamResources()
        coEvery {
            jsonDao.getFormsDataByFormID(FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID, 1L)
        } returns emptyList()

        assertEquals("check_sam", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus returns check_sam_ when discharged and improved`() = runTest {
        mockSamResources()
        val form = samForm("2026-01-01", """{"discharged_key":"Yes","sam_status_key":"Improved"}""")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(form)

        assertEquals("check_sam", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus returns follow_up_sam when discharged and not improved`() = runTest {
        mockSamResources()
        val form = samForm("2026-01-01", """{"discharged_key":"Yes","sam_status_key":"Not Improved"}""")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(form)

        assertEquals("follow_up_sam", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus returns nrc_admitted when admitted but not discharged`() = runTest {
        mockSamResources()
        val form = samForm("2026-01-01", """{"admitted_key":"Yes"}""")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(form)

        assertEquals("nrc_admitted", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus returns referred_to_nrc when only referred`() = runTest {
        mockSamResources()
        val form = samForm("2026-01-01", """{"referred_key":"Yes"}""")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(form)

        assertEquals("referred_to_nrc", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus returns check_sam_ when no flags are set`() = runTest {
        mockSamResources()
        val form = samForm("2026-01-01", """{}""")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(form)

        assertEquals("check_sam", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus returns check_sam_ when latest form json is malformed`() = runTest {
        mockSamResources()
        val form = samForm("2026-01-01", "{}").copy(formDataJson = "not-json")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(form)

        assertEquals("check_sam", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `getCurrentSamStatus picks most recently dated form when multiple exist`() = runTest {
        mockSamResources()
        val older = samForm("2026-01-01", """{"admitted_key":"Yes"}""")
        val newer = samForm("2026-02-01", """{"referred_key":"Yes"}""")
        coEvery { jsonDao.getFormsDataByFormID(any(), any()) } returns listOf(older, newer)

        assertEquals("referred_to_nrc", repo.getCurrentSamStatus(1L))
    }

    @Test
    fun `saveDownloadedVisitList builds entity json from visit fields and inserts`() = runTest {
        val fields = com.google.gson.JsonObject().apply {
            add("flag", JsonPrimitive(true))
            add("count", JsonPrimitive(5))
            add("name", JsonPrimitive("john"))
            add("note", JsonNull.INSTANCE)
            add("list", com.google.gson.JsonArray().apply { add("a"); add("b") })
        }
        val visit = HBNCVisitResponse(
            id = 1,
            houseHoldId = 10L,
            beneficiaryId = 20L,
            visitDate = "01-01-2026",
            eyeSide = "",
            fields = fields
        )
        coEvery { jsonDao.getFormResponse(20L, "01-01-2026") } returns null
        val saved = slot<CUFYFormResponseJsonEntity>()
        coEvery { jsonDao.insertFormResponse(capture(saved)) } just Runs

        repo.saveDownloadedVisitList(listOf(visit), "F1")

        val json = JSONObject(saved.captured.formDataJson)
        assertEquals(20L, json.getLong("beneficiaryId"))
        assertEquals(10L, json.getLong("houseHoldId"))
        val fieldsJson = json.getJSONObject("fields")
        assertTrue(fieldsJson.getBoolean("flag"))
        assertEquals(5, fieldsJson.getInt("count"))
        assertEquals("john", fieldsJson.getString("name"))
        assertTrue(fieldsJson.isNull("note"))
        assertTrue(fieldsJson.getString("list").contains("a"))
    }

    @Test
    fun `saveDownloadedVisitList skips item that throws and still saves the next`() = runTest {
        val badItem = mockk<HBNCVisitResponse>()
        every { badItem.fields } throws RuntimeException("boom")

        val fields = com.google.gson.JsonObject().apply { addProperty("a", "b") }
        val goodItem = HBNCVisitResponse(
            id = 2,
            houseHoldId = 1L,
            beneficiaryId = 2L,
            visitDate = "01-01-2026",
            eyeSide = "",
            fields = fields
        )

        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        coEvery { jsonDao.insertFormResponse(any()) } just Runs

        repo.saveDownloadedVisitList(listOf(badItem, goodItem), "F1")

        coVerify(exactly = 1) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `getFormSchema does not resave when local version already current`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        every { schema.formId } returns "F1"
        every { schema.version } returns 2
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns schema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        val localEntity = mockk<FormSchemaEntity>()
        every { localEntity.version } returns 2
        coEvery { schemaDao.getSchema("F1") } returns localEntity

        val result = repo.getFormSchema("F1")

        assertSame(schema, result)
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema falls back when apiResponse success flag is false`() = runTest {
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns false
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        coEvery { schemaDao.getSchema("F1") } returns null
        every { context.assets } throws RuntimeException("no assets")

        val result = repo.getFormSchema("F1")

        assertEquals(null, result)
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema falls back when apiSchema data is null`() = runTest {
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns null
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        coEvery { schemaDao.getSchema("F1") } returns null
        every { context.assets } throws RuntimeException("no assets")

        val result = repo.getFormSchema("F1")

        assertEquals(null, result)
    }

    @Test
    fun `getFormSchema falls back to db saved schema when api unsuccessful`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        val dbEntity = mockk<FormSchemaEntity>()
        every { dbEntity.schemaJson } returns """{"formId":"F1","formName":"HBNC","version":3,"sections":[]}"""
        coEvery { schemaDao.getSchema("F1") } returns dbEntity

        val result = repo.getFormSchema("F1")

        assertEquals("F1", result?.formId)
        assertEquals(3, result?.version)
    }

    @Test
    fun `getFormSchema loads schema from assets when db and api both unavailable`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        coEvery { schemaDao.getSchema("F1") } returns null

        val json = """{"formId":"hbnc_form_001","formName":"HBNC Day 1","version":1,"sections":[]}"""
        val assetManager = mockk<AssetManager>()
        every { assetManager.open("hbnc_form_1stday.json") } returns ByteArrayInputStream(json.toByteArray())
        every { context.assets } returns assetManager

        val result = repo.getFormSchema("F1")

        assertEquals("hbnc_form_001", result?.formId)
    }
}
