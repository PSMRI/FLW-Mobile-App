package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import android.content.res.AssetManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.BenIfaFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BenIfaFormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: BenIfaFormResponseJsonDao

    private lateinit var repo: BenIfaFormRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.formSchemaDao() } returns schemaDao
        every { db.formResponseJsonDaoBenIfa() } returns jsonDao
        repo = BenIfaFormRepository(context, api, pref, db)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getSyncedVisitsByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<BenIfaFormResponseJsonEntity>())
        coEvery { jsonDao.getSyncedVisitsByRchId(5L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchId(5L))
    }

    @Test
    fun `getAllFormVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllEyeSurgeryFormVisits("ifa", request) } returns response
        assertEquals(response, repo.getAllFormVisits("ifa", request))
    }

    @Test
    fun `insertFormResponse delegates to dao`() = runTest {
        val entity = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        repo.insertFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `insertOrUpdateFormResponse inserts entity when none existing`() = runTest {
        val entity = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        repo.insertOrUpdateFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<BenIfaFormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getFormResponse(1L, "d") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson(1L, "d"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<BenIfaFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms("ifa") } returns list
        assertEquals(list, repo.getUnsyncedForms("ifa"))
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns false for invalid entity`() = runTest {
        val entity = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        assertFalse(repo.syncFormToServer("user", "ifa", entity))
    }

    @Test
    fun `canAddNewVisit returns true when no stored visits`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns emptyList()
        assertTrue(repo.canAddNewVisit(1L))
    }

    @Test
    fun `getBottleList returns empty for no data`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns emptyList()
        assertTrue(repo.getBottleList(1L, "ifa").isEmpty())
    }

    @Test
    fun `saveDownloadedVisitList does nothing for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "ifa")
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

        assertSame(schema, repo.getFormSchema("F1"))
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema falls back to null when api throws and db empty`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("network")
        coEvery { schemaDao.getSchema(any()) } returns null
        every { context.assets } throws RuntimeException("no assets")

        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema falls back when api unsuccessful`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        coEvery { schemaDao.getSchema(any()) } returns null
        every { context.assets } throws RuntimeException("no assets")

        assertNull(repo.getFormSchema("F1"))
    }

    @Test
    fun `getFormSchema falls back when api body reports failure`() = runTest {
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns false
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        coEvery { schemaDao.getSchema(any()) } returns null
        every { context.assets } throws RuntimeException("no assets")

        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `saveFormSchemaToDb inserts built entity`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        repo.saveFormSchemaToDb(schema)
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `loadFormResponseJson returns null when no record`() = runTest {
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        assertNull(repo.loadFormResponseJson(1L, "d"))
    }

    @Test
    fun `insertOrUpdateFormResponse reuses existing record id`() = runTest {
        val existing = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        val incoming = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any()) } returns existing

        repo.insertOrUpdateFormResponse(incoming)

        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `getBottleList parses fields and keeps latest three`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns listOf(
            """{"fields":{"visit_date":"01-01-2026","ifa_quantity":"2"}}""",
            """{"fields":{"visit_date":"05-02-2026","ifa_quantity":"3"}}""",
            """{"fields":{"visit_date":"09-03-2026","ifa_quantity":"4"}}""",
            """{"fields":{"visit_date":"11-04-2026","ifa_quantity":"5"}}"""
        )
        val result = repo.getBottleList(1L, "ifa")
        assertEquals(3, result.size)
        assertEquals("11-04-2026", result[0].dateOfProvision)
        assertEquals("5", result[0].bottleNumber)
    }

    @Test
    fun `getBottleList swallows malformed json entries`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns listOf("not-json")
        assertTrue(repo.getBottleList(1L, "ifa").isEmpty())
    }

    @Test
    fun `canAddNewVisit returns true when stored visit is from another year`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns listOf(
            """{"visitDate":"01-01-2000"}"""
        )
        assertTrue(repo.canAddNewVisit(1L))
    }

    @Test
    fun `canAddNewVisit ignores unparsable visit dates`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns listOf("garbage")
        assertTrue(repo.canAddNewVisit(1L))
    }

    @Test
    fun `saveDownloadedVisitList upserts entity built from server payload`() = runTest {
        val fields = JsonObject().apply {
            addProperty("visit_date", "01-01-2026")
            addProperty("ifa_quantity", 3)
            addProperty("given", true)
            add("nested", JsonObject())
        }
        val item = HBNCVisitResponse(
            id = 7,
            houseHoldId = 20L,
            beneficiaryId = 10L,
            visitDate = "01-01-2026",
            eyeSide = "L",
            fields = fields
        )
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null

        repo.saveDownloadedVisitList(listOf(item), "ifa")

        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `getFormSchema loads schema from assets when api and db both empty`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("network")
        coEvery { schemaDao.getSchema(any()) } returns null
        val json = """{"formId":"F1","formName":"N","version":1,"sections":[]}"""
        val assetManager = mockk<AssetManager>()
        every { assetManager.open("hbnc_form_1stday.json") } returns ByteArrayInputStream(json.toByteArray())
        every { context.assets } returns assetManager

        val result = repo.getFormSchema("F1")

        assertEquals("F1", result?.formId)
    }

    @Test
    fun `getFormSchema skips saving when local schema version is already up to date`() = runTest {
        val apiSchema = mockk<FormSchemaDto>(relaxed = true)
        every { apiSchema.formId } returns "F1"
        every { apiSchema.version } returns 2
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns apiSchema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response

        val localEntity = mockk<FormSchemaEntity>(relaxed = true)
        every { localEntity.version } returns 2
        coEvery { schemaDao.getSchema("F1") } returns localEntity

        val result = repo.getFormSchema("F1")

        assertSame(apiSchema, result)
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `saveDownloadedVisitList skips item when field processing throws`() = runTest {
        val badFields = mockk<JsonObject>()
        every { badFields.entrySet() } throws RuntimeException("bad fields")
        val item = HBNCVisitResponse(
            id = 8,
            houseHoldId = 40L,
            beneficiaryId = 40L,
            visitDate = "01-01-2026",
            eyeSide = "L",
            fields = badFields
        )

        repo.saveDownloadedVisitList(listOf(item), "ifa")

        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `canAddNewVisit returns false when a stored visit is from the current month`() = runTest {
        val currentMonthVisit = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Calendar.getInstance().time)
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns listOf(
            """{"visitDate":"$currentMonthVisit"}"""
        )

        assertFalse(repo.canAddNewVisit(1L))
    }

    @Test
    fun `getBottleList defaults missing visit date field to placeholder`() = runTest {
        coEvery { jsonDao.getFormJsonList(any(), any()) } returns listOf(
            """{"fields":{"ifa_quantity":"2"}}"""
        )

        val result = repo.getBottleList(1L, "ifa")

        assertEquals(1, result.size)
        assertEquals("-", result[0].dateOfProvision)
    }

    @Test
    fun `syncFormToServer returns true when mapper produces request and api call succeeds`() = runTest {
        mockkObject(FormSubmitRequestMapper)
        val entity = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        every { FormSubmitRequestMapper.fromEntity(entity, "user") } returns request
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns true
        coEvery { api.submitEyeSurgeryForm("ifa", listOf(request)) } returns response

        assertTrue(repo.syncFormToServer("user", "ifa", entity))

        unmockkObject(FormSubmitRequestMapper)
    }

    @Test
    fun `syncFormToServer returns false when api call reports failure`() = runTest {
        mockkObject(FormSubmitRequestMapper)
        val entity = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        every { FormSubmitRequestMapper.fromEntity(entity, "user") } returns request
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns false
        coEvery { api.submitEyeSurgeryForm("ifa", listOf(request)) } returns response

        assertFalse(repo.syncFormToServer("user", "ifa", entity))

        unmockkObject(FormSubmitRequestMapper)
    }

    @Test
    fun `syncFormToServer returns false when api call throws`() = runTest {
        mockkObject(FormSubmitRequestMapper)
        val entity = mockk<BenIfaFormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        every { FormSubmitRequestMapper.fromEntity(entity, "user") } returns request
        coEvery { api.submitEyeSurgeryForm("ifa", listOf(request)) } throws RuntimeException("net")

        assertFalse(repo.syncFormToServer("user", "ifa", entity))

        unmockkObject(FormSubmitRequestMapper)
    }
}
