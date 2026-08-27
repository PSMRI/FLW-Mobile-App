package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.database.room.dao.MosquitoNetFormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MosquitoNetFormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: MosquitoNetFormResponseDao

    private lateinit var repo: MosquitoNetFormRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.formSchemaDao() } returns schemaDao
        every { db.formResponseMosquitoNetJsonDao() } returns jsonDao
        repo = MosquitoNetFormRepository(context, api, pref, db)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getAllFormVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllDiseaseMosquitoFormVisits("net", request) } returns response
        assertEquals(response, repo.getAllFormVisits("net", request))
    }

    @Test
    fun `getAllByHhId delegates to dao`() = runTest {
        val list = listOf(mockk<MosquitoNetFormResponseJsonEntity>())
        coEvery { jsonDao.getAllByHhId(2L) } returns list
        assertEquals(list, repo.getAllByHhId(2L))
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<MosquitoNetFormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getLatestForHhForm(1L, "net") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson(1L, "net"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<MosquitoNetFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms("net") } returns list
        assertEquals(list, repo.getUnsyncedForms("net"))
    }

    @Test
    fun `insertFormResponse delegates to dao and returns result`() = runTest {
        val entity = mockk<MosquitoNetFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.insertWithLimit(entity) } returns true
        assertTrue(repo.insertFormResponse(entity))
        coVerify { jsonDao.insertWithLimit(entity) }
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns false for invalid entity`() = runTest {
        val entity = mockk<MosquitoNetFormResponseJsonEntity>(relaxed = true)
        assertFalse(repo.syncFormToServer("user", "net", entity))
    }

    @Test
    fun `getBottleList returns empty for no data`() = runTest {
        coEvery { jsonDao.getFormJsonList(any()) } returns emptyList()
        assertTrue(repo.getBottleList(1L).isEmpty())
    }

    @Test
    fun `saveDownloadedVisitList returns early for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "net")
        coVerify(exactly = 0) { jsonDao.insertWithLimit(any()) }
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
        coEvery { jsonDao.getLatestForHhForm(any(), any()) } returns null
        assertNull(repo.loadFormResponseJson(1L, "net"))
    }

    @Test
    fun `insertFormResponse returns false when dao returns false`() = runTest {
        val entity = mockk<MosquitoNetFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.insertWithLimit(entity) } returns false
        assertFalse(repo.insertFormResponse(entity))
    }

    @Test
    fun `getBottleList numbers rows from stored json`() = runTest {
        coEvery { jsonDao.getFormJsonList(any()) } returns listOf(
            """{"fields":{"visit_date":"01-01-2026","is_net_distributed":"Yes"}}""",
            """{"fields":{"visit_date":"02-02-2026","is_net_distributed":"No"}}"""
        )
        val result = repo.getBottleList(1L)
        assertEquals(2, result.size)
        assertEquals(1, result[0].srNo)
        assertEquals("Yes", result[0].bottleNumber)
        assertEquals(2, result[1].srNo)
    }

    @Test
    fun `getBottleList defaults missing fields object`() = runTest {
        coEvery { jsonDao.getFormJsonList(any()) } returns listOf("""{"other":1}""")
        val result = repo.getBottleList(1L)
        assertEquals(1, result.size)
        assertEquals("-", result[0].dateOfProvision)
    }

    @Test
    fun `saveDownloadedVisitList inserts entity built from server payload`() = runTest {
        val fields = JsonObject().apply {
            addProperty("visit_date", "01-01-2026")
            addProperty("nets", 2)
            addProperty("distributed", true)
            add("nested", JsonObject())
            add("skipped", com.google.gson.JsonNull.INSTANCE)
        }
        val item = HBNCVisitResponse(
            id = 5,
            houseHoldId = 20L,
            beneficiaryId = 10L,
            visitDate = "01-01-2026",
            eyeSide = "L",
            fields = fields
        )
        coEvery { jsonDao.insertWithLimit(any()) } returns true

        repo.saveDownloadedVisitList(listOf(item), "net")

        coVerify { jsonDao.insertWithLimit(any()) }
    }

    @Test
    fun `saveDownloadedVisitList skips items with null visitDate`() = runTest {
        val json = """{"id":6,"houseHoldId":21,"beneficiaryId":11,"eyeSide":"L"}"""
        val item = com.google.gson.Gson().fromJson(json, HBNCVisitResponse::class.java)

        repo.saveDownloadedVisitList(listOf(item), "net")

        coVerify(exactly = 0) { jsonDao.insertWithLimit(any()) }
    }

    @Test
    fun `syncFormToServer returns true when mapper succeeds and api call is successful`() = runTest {
        val entity = mockk<MosquitoNetFormResponseJsonEntity>(relaxed = true)
        every { entity.formDataJson } returns """{"formId":"F1","houseHoldId":1,"fields":{}}"""
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns true
        coEvery { api.submitDiseaseMosquitoForm("net", any()) } returns response

        assertTrue(repo.syncFormToServer("user1", "net", entity))
    }

    @Test
    fun `syncFormToServer returns false when api call throws`() = runTest {
        val entity = mockk<MosquitoNetFormResponseJsonEntity>(relaxed = true)
        every { entity.formDataJson } returns """{"formId":"F1","houseHoldId":1,"fields":{}}"""
        coEvery { api.submitDiseaseMosquitoForm("net", any()) } throws RuntimeException("network")

        assertFalse(repo.syncFormToServer("user1", "net", entity))
    }

    @Test
    fun `getFormSchema saves when local version is older than server version`() = runTest {
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
        val localEntity = mockk<FormSchemaEntity>(relaxed = true)
        every { localEntity.version } returns 1
        coEvery { schemaDao.getSchema("F1") } returns localEntity

        assertSame(schema, repo.getFormSchema("F1"))
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema skips save when local version is up to date`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        every { schema.formId } returns "F1"
        every { schema.version } returns 1
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns schema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        val localEntity = mockk<FormSchemaEntity>(relaxed = true)
        every { localEntity.version } returns 1
        coEvery { schemaDao.getSchema("F1") } returns localEntity

        assertSame(schema, repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema falls back to bundled asset schema when api and db both miss`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("network")
        coEvery { schemaDao.getSchema(any()) } returns null
        val assetManager = mockk<android.content.res.AssetManager>()
        every { context.assets } returns assetManager
        val json = """{"formId":"asset-form","formName":"Asset Form","version":1}"""
        every { assetManager.open("hbnc_form_1stday.json") } returns
            java.io.ByteArrayInputStream(json.toByteArray())

        val result = repo.getFormSchema("F1")

        assertEquals("asset-form", result?.formId)
    }
}
