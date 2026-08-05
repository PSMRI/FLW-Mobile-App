package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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
import org.piramalswasthya.sakhi.network.AmritApiService
import org.junit.Assert.assertSame
import retrofit2.Response

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
}
