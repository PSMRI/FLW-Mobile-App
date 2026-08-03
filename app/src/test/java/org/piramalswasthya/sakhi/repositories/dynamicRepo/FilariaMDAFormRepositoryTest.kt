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
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMDAFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMDAFormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: FilariaMDAFormResponseJsonDao

    private lateinit var repo: FilariaMDAFormRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.formSchemaDao() } returns schemaDao
        every { db.formResponseFilariaMDAJsonDao() } returns jsonDao
        repo = FilariaMDAFormRepository(context, api, pref, db)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getSyncedVisitsByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<FilariaMDAFormResponseJsonEntity>())
        coEvery { jsonDao.getSyncedVisitsByRchId(5L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchId(5L))
    }

    @Test
    fun `getAllFormVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllEyeSurgeryFormVisits("mda", request) } returns response
        assertEquals(response, repo.getAllFormVisits("mda", request))
    }

    @Test
    fun `getBottleList returns empty for no data`() = runTest {
        coEvery { jsonDao.getFormJsonList(any()) } returns emptyList()
        assertTrue(repo.getBottleList(1L).isEmpty())
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<FilariaMDAFormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getLatestForBenForm(1L, "mda") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson(1L, "mda"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<FilariaMDAFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms("mda") } returns list
        assertEquals(list, repo.getUnsyncedForms("mda"))
    }

    @Test
    fun `insertFormResponse delegates to dao and returns result`() = runTest {
        val entity = mockk<FilariaMDAFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.insertOncePerMonth(entity) } returns true
        assertTrue(repo.insertFormResponse(entity))
        coVerify { jsonDao.insertOncePerMonth(entity) }
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns false for invalid entity`() = runTest {
        val entity = mockk<FilariaMDAFormResponseJsonEntity>(relaxed = true)
        assertFalse(repo.syncFormToServer("user", "mda", entity))
    }

    @Test
    fun `saveDownloadedVisitList returns early for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "mda")
        coVerify(exactly = 0) { jsonDao.upsertByMonth(any()) }
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
        coEvery { jsonDao.getLatestForBenForm(any(), any()) } returns null
        assertNull(repo.loadFormResponseJson(1L, "mda"))
    }

    @Test
    fun `insertFormResponse returns false when dao returns false`() = runTest {
        val entity = mockk<FilariaMDAFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.insertOncePerMonth(entity) } returns false
        assertFalse(repo.insertFormResponse(entity))
    }

    @Test
    fun `getBottleList numbers rows from stored json`() = runTest {
        coEvery { jsonDao.getFormJsonList(any()) } returns listOf(
            """{"fields":{"mda_distribution_date":"01-01-2026","is_medicine_distributed":"Yes"}}""",
            """{"fields":{"mda_distribution_date":"02-02-2026","is_medicine_distributed":"No"}}"""
        )
        val result = repo.getBottleList(1L)
        assertEquals(2, result.size)
        assertEquals(1, result[0].srNo)
        assertEquals("Yes", result[0].bottleNumber)
        assertEquals("02-02-2026", result[1].dateOfProvision)
    }

    @Test
    fun `getBottleList defaults missing fields object`() = runTest {
        coEvery { jsonDao.getFormJsonList(any()) } returns listOf("""{"other":1}""")
        val result = repo.getBottleList(1L)
        assertEquals(1, result.size)
        assertEquals("-", result[0].dateOfProvision)
        assertEquals("-", result[0].bottleNumber)
    }

    @Test
    fun `saveDownloadedVisitList upserts entity built from server payload`() = runTest {
        val fields = JsonObject().apply {
            addProperty("mda_distribution_date", "01-01-2026")
            addProperty("dose", 2)
            addProperty("done", true)
            add("nested", JsonObject())
        }
        val item = HBNCVisitResponse(
            id = 3,
            houseHoldId = 20L,
            beneficiaryId = 10L,
            visitDate = "01-01-2026",
            eyeSide = "L",
            fields = fields
        )

        repo.saveDownloadedVisitList(listOf(item), "mda")

        coVerify { jsonDao.upsertByMonth(any()) }
    }

    @Test
    fun `saveDownloadedVisitList handles iso formatted visit date`() = runTest {
        val item = HBNCVisitResponse(
            id = 4,
            houseHoldId = 21L,
            beneficiaryId = 11L,
            visitDate = "2026-01-01",
            eyeSide = "R",
            fields = JsonObject()
        )

        repo.saveDownloadedVisitList(listOf(item), "mda")

        coVerify { jsonDao.upsertByMonth(any()) }
    }
}
