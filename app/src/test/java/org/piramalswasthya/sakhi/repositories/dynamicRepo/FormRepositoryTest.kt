package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDaoHBYC
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: FormResponseJsonDao
    @MockK private lateinit var hbycDao: FormResponseJsonDaoHBYC
    @MockK private lateinit var ancDao: FormResponseANCJsonDao

    private lateinit var repo: FormRepository

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { db.formSchemaDao() } returns schemaDao
        every { db.formResponseJsonDao() } returns jsonDao
        every { db.formResponseJsonDaoHBYC() } returns hbycDao
        every { db.formResponseJsonDaoANC() } returns ancDao
        repo = FormRepository(context, pref, api, db)
    }

    // ---------------- generic (HBNC) ----------------

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getInfantByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<FormResponseJsonEntity>())
        coEvery { jsonDao.getSyncedVisitsByRchId(3L) } returns list
        assertEquals(list, repo.getInfantByRchId(3L))
    }

    @Test
    fun `getSyncedVisitsByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<FormResponseJsonEntity>())
        coEvery { jsonDao.getSyncedVisitsByRchId(4L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchId(4L))
    }

    @Test
    fun `insertFormResponse delegates to dao`() = runTest {
        val entity = mockk<FormResponseJsonEntity>(relaxed = true)
        repo.insertFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `insertOrUpdateFormResponse inserts entity when none existing`() = runTest {
        val entity = mockk<FormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        repo.insertOrUpdateFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<FormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getFormResponse(1L, "1") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson(1L, "1"))
    }

    @Test
    fun `loadFormResponseJson returns null when no record`() = runTest {
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null
        assertNull(repo.loadFormResponseJson(1L, "1"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<FormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms() } returns list
        assertEquals(list, repo.getUnsyncedForms())
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(9)
        coVerify { jsonDao.markAsSynced(9, any()) }
    }

    @Test
    fun `syncFormToServer returns false when no user logged in`() = runTest {
        every { pref.getLoggedInUser() } returns null
        assertFalse(repo.syncFormToServer(mockk(relaxed = true)))
    }

    // ---------------- api visit lists ----------------

    @Test
    fun `getAllHbncVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllHbncVisits(request) } returns response
        assertEquals(response, repo.getAllHbncVisits(request))
    }

    @Test
    fun `getAllHbycVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllHbycVisits(request) } returns response
        assertEquals(response, repo.getAllHbycVisits(request))
    }

    @Test
    fun `getAllAncVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllAncVisits(request) } returns response
        assertEquals(response, repo.getAllAncVisits(request))
    }

    // ---------------- HBYC ----------------

    @Test
    fun `getInfantByRchIdHBYC delegates to dao`() = runTest {
        val list = listOf(mockk<FormResponseJsonEntityHBYC>())
        coEvery { hbycDao.getSyncedVisitsByRchId(2L) } returns list
        assertEquals(list, repo.getInfantByRchIdHBYC(2L))
    }

    @Test
    fun `getSyncedVisitsByRchIdHBYC delegates to dao`() = runTest {
        val list = listOf(mockk<FormResponseJsonEntityHBYC>())
        coEvery { hbycDao.getSyncedVisitsByRchId(6L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchIdHBYC(6L))
    }

    @Test
    fun `insertFormResponseHBYC delegates to dao`() = runTest {
        val entity = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        repo.insertFormResponseHBYC(entity)
        coVerify { hbycDao.insertFormResponse(entity) }
    }

    @Test
    fun `loadFormResponseJsonHBYC returns stored json`() = runTest {
        val entity = mockk<FormResponseJsonEntityHBYC>()
        every { entity.formDataJson } returns "HJSON"
        coEvery { hbycDao.getFormResponse(1L, "1") } returns entity
        assertEquals("HJSON", repo.loadFormResponseJsonHBYC(1L, "1"))
    }

    @Test
    fun `getUnsyncedFormsHBYC delegates to dao`() = runTest {
        val list = listOf(mockk<FormResponseJsonEntityHBYC>())
        coEvery { hbycDao.getUnsyncedForms() } returns list
        assertEquals(list, repo.getUnsyncedFormsHBYC())
    }

    @Test
    fun `markFormAsSyncedHBYC calls dao markAsSynced`() = runTest {
        repo.markFormAsSyncedHBYC(3)
        coVerify { hbycDao.markAsSynced(3, any()) }
    }

    @Test
    fun `syncFormToServerHBYC returns false when no user logged in`() = runTest {
        every { pref.getLoggedInUser() } returns null
        assertFalse(repo.syncFormToServerHBYC(mockk(relaxed = true)))
    }

    // ---------------- ANC ----------------

    @Test
    fun `getInfantByRchIdANC delegates to dao`() = runTest {
        val list = listOf(mockk<ANCFormResponseJsonEntity>())
        coEvery { ancDao.getSyncedVisitsByRchId(2L) } returns list
        assertEquals(list, repo.getInfantByRchIdANC(2L))
    }

    @Test
    fun `getSyncedVisitsByRchIdANC delegates to dao`() = runTest {
        val list = listOf(mockk<ANCFormResponseJsonEntity>())
        coEvery { ancDao.getSyncedVisitsByRchId(7L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchIdANC(7L))
    }

    @Test
    fun `insertFormResponseANC delegates to dao`() = runTest {
        val entity = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        repo.insertFormResponseANC(entity)
        coVerify { ancDao.insertFormResponse(entity) }
    }

    @Test
    fun `loadFormResponseJsonANC returns stored json`() = runTest {
        val entity = mockk<ANCFormResponseJsonEntity>()
        every { entity.formDataJson } returns "AJSON"
        coEvery { ancDao.getFormResponse(1L, "d") } returns entity
        assertEquals("AJSON", repo.loadFormResponseJsonANC(1L, "d"))
    }

    @Test
    fun `getUnsyncedFormsANC delegates to dao`() = runTest {
        val list = listOf(mockk<ANCFormResponseJsonEntity>())
        coEvery { ancDao.getUnsyncedForms() } returns list
        assertEquals(list, repo.getUnsyncedFormsANC())
    }

    @Test
    fun `markFormAsSyncedANC calls dao markAsSynced`() = runTest {
        repo.markFormAsSyncedANC(5)
        coVerify { ancDao.markAsSynced(5, any()) }
    }

    @Test
    fun `syncFormToServerANC returns false when no user logged in`() = runTest {
        every { pref.getLoggedInUser() } returns null
        assertFalse(repo.syncFormToServerANC(mockk(relaxed = true)))
    }

    @Test
    fun `saveDownloadedVisitListANC does nothing for empty list`() = runTest {
        repo.saveDownloadedVisitListANC(emptyList())
        coVerify(exactly = 0) { ancDao.insertFormResponse(any()) }
    }

    @Test
    fun `getLastVisitForBenANC returns null when no visits`() = runTest {
        coEvery { ancDao.getVisitsForBen(any()) } returns emptyList()
        assertNull(repo.getLastVisitForBenANC(1L))
    }

    // ---------------- schema ----------------

    @Test
    fun `getFormSchema saves and returns schema when api succeeds and no local schema`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        every { schema.formId } returns "F1"
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.data } returns schema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", "en") } returns response
        coEvery { schemaDao.getSchema("F1") } returns null

        val result = repo.getFormSchema("F1", "en")

        assertSame(schema, result)
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema returns null when api fails and no local schema`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        coEvery { schemaDao.getSchema(any()) } returns null

        assertNull(repo.getFormSchema("F1", "en"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `saveFormSchemaToDb inserts built entity`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        repo.saveFormSchemaToDb(schema, "en")
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `downloadAllFormsSchemas does not save when server returns unsuccessful`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema(any(), any()) } returns response

        repo.downloadAllFormsSchemas("en")

        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `downloadAllFormsSchemas swallows exceptions`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("network")
        repo.downloadAllFormsSchemas("en")
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    // ---------------- insertOrUpdate existing-record paths ----------------

    @Test
    fun `insertOrUpdateFormResponse copies id and inserts when existing found`() = runTest {
        val entity = mockk<FormResponseJsonEntity>(relaxed = true)
        val existing = mockk<FormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any()) } returns existing
        repo.insertOrUpdateFormResponse(entity)
        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `insertOrUpdateFormResponseHBYC copies id and inserts when existing found`() = runTest {
        val entity = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        val existing = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        coEvery { hbycDao.getFormResponse(any(), any()) } returns existing
        repo.insertOrUpdateFormResponseHBYC(entity)
        coVerify { hbycDao.insertFormResponse(any()) }
    }

    @Test
    fun `insertOrUpdateFormResponseHBYC inserts entity when none existing`() = runTest {
        val entity = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        coEvery { hbycDao.getFormResponse(any(), any()) } returns null
        repo.insertOrUpdateFormResponseHBYC(entity)
        coVerify { hbycDao.insertFormResponse(entity) }
    }

    @Test
    fun `insertOrUpdateFormResponseANC copies id and inserts when existing found`() = runTest {
        val entity = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        val existing = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        coEvery { ancDao.getFormResponse(any(), any()) } returns existing
        repo.insertOrUpdateFormResponseANC(entity)
        coVerify { ancDao.insertFormResponse(any()) }
    }

    @Test
    fun `insertOrUpdateFormResponseANC inserts entity when none existing`() = runTest {
        val entity = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        coEvery { ancDao.getFormResponse(any(), any()) } returns null
        repo.insertOrUpdateFormResponseANC(entity)
        coVerify { ancDao.insertFormResponse(entity) }
    }

    // ---------------- null-record load paths ----------------

    @Test
    fun `loadFormResponseJsonHBYC returns null when no record`() = runTest {
        coEvery { hbycDao.getFormResponse(any(), any()) } returns null
        assertNull(repo.loadFormResponseJsonHBYC(1L, "1"))
    }

    @Test
    fun `loadFormResponseJsonANC returns null when no record`() = runTest {
        coEvery { ancDao.getFormResponse(any(), any()) } returns null
        assertNull(repo.loadFormResponseJsonANC(1L, "d"))
    }

    // ---------------- getLastVisitForBenANC happy path ----------------

    @Test
    fun `getLastVisitForBenANC returns latest by parsed date`() = runTest {
        val older = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        every { older.visitDate } returns "01-01-2020"
        val newer = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        every { newer.visitDate } returns "01-01-2021"
        coEvery { ancDao.getVisitsForBen(1L) } returns listOf(older, newer)
        assertSame(newer, repo.getLastVisitForBenANC(1L))
    }

    // ---------------- empty-list downloads ----------------

    @Test
    fun `saveDownloadedVisitList does nothing for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList())
        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveDownloadedVisitListHBYC does nothing for empty list`() = runTest {
        repo.saveDownloadedVisitListHBYC(emptyList())
        coVerify(exactly = 0) { hbycDao.insertFormResponse(any()) }
    }

    // ---------------- downloadAllFormsSchemas branches ----------------

    @Test
    fun `downloadAllFormsSchemas saves schema for every form when no local schema exists`() = runTest {
        val schema = FormSchemaDto("F1", "Form One", 2)
        val apiResponse = ApiResponse(success = true, data = schema)
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        coEvery { schemaDao.getSchema(any()) } returns null

        repo.downloadAllFormsSchemas("en")

        coVerify(exactly = repo.ALL_FORM_IDS.size) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `downloadAllFormsSchemas updates schema when local version is older`() = runTest {
        val schema = FormSchemaDto("F1", "Form One", 3)
        val apiResponse = ApiResponse(success = true, data = schema)
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        val localEntity = FormSchemaEntity(
            formId = "F1", formName = "Form One", language = "en", version = 1, schemaJson = "{}"
        )
        coEvery { schemaDao.getSchema(any()) } returns localEntity

        repo.downloadAllFormsSchemas("en")

        coVerify(exactly = repo.ALL_FORM_IDS.size) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `downloadAllFormsSchemas skips update when local schema is already latest`() = runTest {
        val schema = FormSchemaDto("F1", "Form One", 1)
        val apiResponse = ApiResponse(success = true, data = schema)
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema(any(), any()) } returns response
        val localEntity = FormSchemaEntity(
            formId = "F1", formName = "Form One", language = "en", version = 1, schemaJson = "{}"
        )
        coEvery { schemaDao.getSchema(any()) } returns localEntity

        repo.downloadAllFormsSchemas("en")

        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `downloadAllFormsSchemas skips form when api response data is null`() = runTest {
        val apiResponse = ApiResponse<FormSchemaDto>(success = true, data = null)
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema(any(), any()) } returns response

        repo.downloadAllFormsSchemas("en")

        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
        coVerify(exactly = 0) { schemaDao.getSchema(any()) }
    }

    // ---------------- getFormSchema fallback branches ----------------

    @Test
    fun `getFormSchema falls back to local schema when api throws`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("net")
        val dto = FormSchemaDto("F1", "Form One", 1)
        val entity = FormSchemaEntity(
            formId = "F1", formName = "Form One", language = "en", version = 1, schemaJson = dto.toJson()
        )
        coEvery { schemaDao.getSchema("F1") } returns entity

        val result = repo.getFormSchema("F1", "en")

        assertEquals(dto, result)
    }

    @Test
    fun `getFormSchema returns null when api throws and no local schema`() = runTest {
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("boom")
        coEvery { schemaDao.getSchema(any()) } returns null

        assertNull(repo.getFormSchema("F1", "en"))
    }

    @Test
    fun `getFormSchema falls back to local schema when api unsuccessful and local exists`() = runTest {
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        coEvery { api.fetchFormSchema("F1", "en") } returns response
        val dto = FormSchemaDto("F1", "Form One", 1)
        val entity = FormSchemaEntity(
            formId = "F1", formName = "Form One", language = "en", version = 1, schemaJson = dto.toJson()
        )
        coEvery { schemaDao.getSchema("F1") } returns entity

        val result = repo.getFormSchema("F1", "en")

        assertEquals(dto, result)
    }

    @Test
    fun `getFormSchema falls back to local schema when api data is null`() = runTest {
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.data } returns null
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", "en") } returns response
        val dto = FormSchemaDto("F1", "Form One", 1)
        val entity = FormSchemaEntity(
            formId = "F1", formName = "Form One", language = "en", version = 1, schemaJson = dto.toJson()
        )
        coEvery { schemaDao.getSchema("F1") } returns entity

        val result = repo.getFormSchema("F1", "en")

        assertEquals(dto, result)
    }

    @Test
    fun `getFormSchema returns api schema without saving when local already latest`() = runTest {
        val schema = FormSchemaDto("F1", "Form One", 1)
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.data } returns schema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", "en") } returns response
        val localEntity = FormSchemaEntity(
            formId = "F1", formName = "Form One", language = "en", version = 1, schemaJson = "{}"
        )
        coEvery { schemaDao.getSchema("F1") } returns localEntity

        val result = repo.getFormSchema("F1", "en")

        assertSame(schema, result)
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    // ---------------- syncFormToServer success/failure branches ----------------

    @Test
    fun `syncFormToServer returns true when submission succeeds`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha1"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<FormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.fromEntity(entity, "asha1") } returns request
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns true
        coEvery { api.submitForm(listOf(request)) } returns response

        assertTrue(repo.syncFormToServer(entity))
    }

    @Test
    fun `syncFormToServer returns false when mapper cannot build request`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha1"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<FormResponseJsonEntity>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.fromEntity(entity, "asha1") } returns null

        assertFalse(repo.syncFormToServer(entity))
        coVerify(exactly = 0) { api.submitForm(any()) }
    }

    @Test
    fun `syncFormToServer returns false when api call throws`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha1"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<FormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.fromEntity(entity, "asha1") } returns request
        coEvery { api.submitForm(listOf(request)) } throws RuntimeException("network down")

        assertFalse(repo.syncFormToServer(entity))
    }

    @Test
    fun `syncFormToServerHBYC returns true when submission succeeds`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha2"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.fromEntity(entity, "asha2") } returns request
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns true
        coEvery { api.submitFormhbyc(listOf(request)) } returns response

        assertTrue(repo.syncFormToServerHBYC(entity))
    }

    @Test
    fun `syncFormToServerHBYC returns false when mapper cannot build request`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha2"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.fromEntity(entity, "asha2") } returns null

        assertFalse(repo.syncFormToServerHBYC(entity))
        coVerify(exactly = 0) { api.submitFormhbyc(any()) }
    }

    @Test
    fun `syncFormToServerHBYC returns false when api call throws`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha2"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<FormResponseJsonEntityHBYC>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.fromEntity(entity, "asha2") } returns request
        coEvery { api.submitFormhbyc(listOf(request)) } throws RuntimeException("network down")

        assertFalse(repo.syncFormToServerHBYC(entity))
    }

    @Test
    fun `syncFormToServerANC returns true when submission succeeds`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha3"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.formEntity(entity, "asha3") } returns request
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns true
        coEvery { api.submitFromANC(listOf(request)) } returns response

        assertTrue(repo.syncFormToServerANC(entity))
    }

    @Test
    fun `syncFormToServerANC returns false when mapper cannot build request`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha3"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.formEntity(entity, "asha3") } returns null

        assertFalse(repo.syncFormToServerANC(entity))
        coVerify(exactly = 0) { api.submitFromANC(any()) }
    }

    @Test
    fun `syncFormToServerANC returns false when api call throws`() = runTest {
        val user = mockk<User>()
        every { user.userName } returns "asha3"
        every { pref.getLoggedInUser() } returns user
        val entity = mockk<ANCFormResponseJsonEntity>(relaxed = true)
        val request = mockk<FormSubmitRequest>(relaxed = true)
        mockkObject(FormSubmitRequestMapper)
        every { FormSubmitRequestMapper.formEntity(entity, "asha3") } returns request
        coEvery { api.submitFromANC(listOf(request)) } throws RuntimeException("network down")

        assertFalse(repo.syncFormToServerANC(entity))
    }

    // ---------------- saveDownloadedVisitList (HBNC) item-level branches ----------------

    @Test
    fun `saveDownloadedVisitList inserts entity when visit_day present`() = runTest {
        val fields = JsonObject().apply {
            addProperty("visit_day", "Day1")
            add("nullField", com.google.gson.JsonNull.INSTANCE)
        }
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )
        coEvery { jsonDao.getFormResponse(any(), any()) } returns null

        repo.saveDownloadedVisitList(listOf(item))

        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveDownloadedVisitList skips item without visit_day field`() = runTest {
        val fields = JsonObject()
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )

        repo.saveDownloadedVisitList(listOf(item))

        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveDownloadedVisitList swallows exception for a bad item`() = runTest {
        val fields = JsonObject().apply { addProperty("visit_day", "Day1") }
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )
        coEvery { jsonDao.getFormResponse(any(), any()) } throws RuntimeException("db error")

        repo.saveDownloadedVisitList(listOf(item))

        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    // ---------------- saveDownloadedVisitListHBYC item-level branches ----------------

    @Test
    fun `saveDownloadedVisitListHBYC inserts entity when visit_day present`() = runTest {
        val fields = JsonObject().apply { addProperty("visit_day", "Day1") }
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )
        coEvery { hbycDao.getFormResponse(any(), any()) } returns null

        repo.saveDownloadedVisitListHBYC(listOf(item))

        coVerify { hbycDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveDownloadedVisitListHBYC skips item without visit_day field`() = runTest {
        val fields = JsonObject()
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )

        repo.saveDownloadedVisitListHBYC(listOf(item))

        coVerify(exactly = 0) { hbycDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveDownloadedVisitListHBYC swallows exception for a bad item`() = runTest {
        val fields = JsonObject().apply { addProperty("visit_day", "Day1") }
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )
        coEvery { hbycDao.getFormResponse(any(), any()) } throws RuntimeException("db error")

        repo.saveDownloadedVisitListHBYC(listOf(item))

        coVerify(exactly = 0) { hbycDao.insertFormResponse(any()) }
    }

    // ---------------- saveDownloadedVisitListANC item-level branches ----------------

    @Test
    fun `saveDownloadedVisitListANC inserts entity built from index-based visitDay`() = runTest {
        val fields = JsonObject().apply { addProperty("someField", "value") }
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )
        val saved = slot<ANCFormResponseJsonEntity>()
        coEvery { ancDao.insertFormResponse(capture(saved)) } just Runs

        repo.saveDownloadedVisitListANC(listOf(item))

        assertEquals("Visit-1", saved.captured.visitDay)
        assertEquals(200L, saved.captured.benId)
    }

    @Test
    fun `saveDownloadedVisitListANC swallows exception for a bad item`() = runTest {
        val fields = JsonObject().apply { addProperty("someField", "value") }
        val item = HBNCVisitResponse(
            id = 1, houseHoldId = 100L, beneficiaryId = 200L,
            visitDate = "01-01-2024", eyeSide = "", fields = fields
        )
        coEvery { ancDao.insertFormResponse(any()) } throws RuntimeException("db error")

        repo.saveDownloadedVisitListANC(listOf(item))

        coVerify(exactly = 1) { ancDao.insertFormResponse(any()) }
    }
}
