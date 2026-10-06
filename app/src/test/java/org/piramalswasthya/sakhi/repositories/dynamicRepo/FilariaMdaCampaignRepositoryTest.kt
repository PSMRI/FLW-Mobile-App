package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMdaCampaignJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMdaCampaignRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: FilariaMdaCampaignJsonDao

    private lateinit var repo: FilariaMdaCampaignRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.formSchemaDao() } returns schemaDao
        every { db.formResponseFilariaMDACampaignJsonDao() } returns jsonDao
        repo = FilariaMdaCampaignRepository(context, api, pref, db)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getSyncedVisitsByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<FilariaMDACampaignFormResponseJsonEntity>())
        coEvery { jsonDao.getCampaignSyncedVisitsByRchId() } returns list
        assertEquals(list, repo.getSyncedVisitsByRchId())
    }

    @Test
    fun `getBottleList returns empty for no data`() = runTest {
        coEvery { jsonDao.getCampaignFormJsonList() } returns emptyList()
        assertTrue(repo.getBottleList().isEmpty())
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<FilariaMDACampaignFormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getCampaignLatestForBenForm("01-01-2026") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson("01-01-2026"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<FilariaMDACampaignFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedCampaignForms("mda") } returns list
        assertEquals(list, repo.getUnsyncedForms("mda"))
    }

    @Test
    fun `insertFormResponse delegates to dao and returns result`() = runTest {
        val entity = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.insertOncePerYear(entity) } returns true
        assertTrue(repo.insertFormResponse(entity))
        coVerify { jsonDao.insertOncePerYear(entity) }
    }

    @Test
    fun `markFormAsSynced calls dao markCampaignAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markCampaignAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns false for invalid entity`() = runTest {
        val entity = mockk<FilariaMDAFormResponseJsonEntity>(relaxed = true)
        assertFalse(repo.syncFormToServer("user", "mda", entity))
    }

    @Test
    fun `saveDownloadedVisitList returns early for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "mda")
        coVerify(exactly = 0) { jsonDao.upsertByYear(any()) }
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
    fun `getFormSchema returns null when api throws`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("network")
        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema returns null when api unsuccessful`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        assertNull(repo.getFormSchema("F1"))
    }

    @Test
    fun `saveFormSchemaToDb inserts built entity`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        repo.saveFormSchemaToDb(schema)
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `insertFormResponse returns false when dao returns false`() = runTest {
        val entity = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.insertOncePerYear(entity) } returns false
        assertFalse(repo.insertFormResponse(entity))
    }

    @Test
    fun `loadFormResponseJson returns null when no record`() = runTest {
        coEvery { jsonDao.getCampaignLatestForBenForm(any()) } returns null
        assertNull(repo.loadFormResponseJson("01-01-2026"))
    }

    @Test
    fun `saveDownloadedVisitList with empty list inserts nothing`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "mda")
        coVerify(exactly = 0) { jsonDao.upsertByYear(any()) }
    }

    @Test
    fun `getBottleList maps fields and assigns serial numbers`() = runTest {
        coEvery { jsonDao.getCampaignFormJsonList() } returns listOf(
            """{"fields":{"start_date":"01-01-2026","end_date":"05-01-2026","no_of_families":"10","no_of_individuals":"40"}}""",
            """{"fields":{"start_date":"02-02-2026","end_date":"06-02-2026","no_of_families":"20","no_of_individuals":"80"}}"""
        )

        val list = repo.getBottleList()

        assertEquals(2, list.size)
        assertEquals(1, list[0].srNo)
        assertEquals("01-01-2026", list[0].startDate)
        assertEquals("05-01-2026", list[0].endDate)
        assertEquals("10", list[0].noOffamilies)
        assertEquals("40", list[0].noOfIndividuals)
        assertEquals(2, list[1].srNo)
        assertEquals("20", list[1].noOffamilies)
    }

    @Test
    fun `getBottleList falls back to defaults for missing fields`() = runTest {
        coEvery { jsonDao.getCampaignFormJsonList() } returns listOf("""{"other":1}""")

        val list = repo.getBottleList()

        assertEquals(1, list.size)
        assertEquals("-", list[0].startDate)
        assertEquals("-", list[0].endDate)
        assertEquals("0", list[0].noOffamilies)
        assertEquals("0", list[0].noOfIndividuals)
    }

    @Test
    fun `getBottleList treats literal null string as zero counts`() = runTest {
        coEvery { jsonDao.getCampaignFormJsonList() } returns listOf(
            """{"fields":{"start_date":"01-01-2026","end_date":"05-01-2026","no_of_families":"null","no_of_individuals":"null"}}"""
        )

        val list = repo.getBottleList()

        assertEquals("0", list[0].noOffamilies)
        assertEquals("0", list[0].noOfIndividuals)
    }

    @Test
    fun `saveDownloadedVisitList builds entity and upserts using dd-MM-yyyy visit year`() = runTest {
        val fields = com.google.gson.JsonObject().apply {
            add("flag", JsonPrimitive(true))
            add("count", JsonPrimitive(5))
            add("name", JsonPrimitive("john"))
            add("note", JsonNull.INSTANCE)
        }
        val visit = HBNCVisitResponse(
            id = 1,
            houseHoldId = 10L,
            beneficiaryId = 20L,
            visitDate = "15-06-2026",
            eyeSide = "",
            fields = fields
        )
        val saved = slot<FilariaMDACampaignFormResponseJsonEntity>()
        coEvery { jsonDao.upsertByYear(capture(saved)) } just Runs

        repo.saveDownloadedVisitList(listOf(visit), "mda")

        assertEquals("2026", saved.captured.visitYear)
        val json = JSONObject(saved.captured.formDataJson)
        assertEquals(10L, json.getLong("houseHoldId"))
        val fieldsJson = json.getJSONObject("fields")
        assertTrue(fieldsJson.getBoolean("flag"))
        assertEquals(5, fieldsJson.getInt("count"))
        assertEquals("john", fieldsJson.getString("name"))
        assertTrue(fieldsJson.isNull("note"))
    }

    @Test
    fun `saveDownloadedVisitList parses yyyy-MM-dd date format for visit year`() = runTest {
        val fields = com.google.gson.JsonObject().apply { addProperty("a", "b") }
        val visit = HBNCVisitResponse(
            id = 1,
            houseHoldId = 1L,
            beneficiaryId = 1L,
            visitDate = "2026-06-15",
            eyeSide = "",
            fields = fields
        )
        val saved = slot<FilariaMDACampaignFormResponseJsonEntity>()
        coEvery { jsonDao.upsertByYear(capture(saved)) } just Runs

        repo.saveDownloadedVisitList(listOf(visit), "mda")

        assertEquals("0020", saved.captured.visitYear)
    }

    @Test
    fun `saveDownloadedVisitList sets empty visit year when date is unparseable`() = runTest {
        val fields = com.google.gson.JsonObject().apply { addProperty("a", "b") }
        val visit = HBNCVisitResponse(
            id = 1,
            houseHoldId = 1L,
            beneficiaryId = 1L,
            visitDate = "not-a-date",
            eyeSide = "",
            fields = fields
        )
        val saved = slot<FilariaMDACampaignFormResponseJsonEntity>()
        coEvery { jsonDao.upsertByYear(capture(saved)) } just Runs

        repo.saveDownloadedVisitList(listOf(visit), "mda")

        assertEquals("", saved.captured.visitYear)
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

        coEvery { jsonDao.upsertByYear(any()) } just Runs

        repo.saveDownloadedVisitList(listOf(badItem, goodItem), "mda")

        coVerify(exactly = 1) { jsonDao.upsertByYear(any()) }
    }

    @Test
    fun `syncFormToServer returns true when api call succeeds`() = runTest {
        val entity = FilariaMDAFormResponseJsonEntity(
            hhId = 1L,
            visitDate = "01-01-2026",
            visitMonth = "01-2026",
            formId = "F1",
            version = 1,
            formDataJson = """{"formId":"F1","fields":{"a":"b"}}""",
            isSynced = false
        )
        coEvery { api.submitEyeSurgeryForm(any(), any()) } returns Response.success(Unit)

        assertTrue(repo.syncFormToServer("user", "mda", entity))
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
    fun `getFormSchema returns null when apiResponse success flag is false`() = runTest {
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns false
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response

        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema returns null when apiSchema data is null`() = runTest {
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns null
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response

        assertNull(repo.getFormSchema("F1"))
    }
}
