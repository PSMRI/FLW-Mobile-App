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
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.EyeSurgeryFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class EyeSurgeryFormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb
    @MockK private lateinit var ncdReferalRepo: NcdReferalRepo

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: EyeSurgeryFormResponseJsonDao

    private lateinit var repo: EyeSurgeryFormRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.formSchemaDao() } returns schemaDao
        every { db.formResponseJsonDaoEyeSurgery() } returns jsonDao
        repo = EyeSurgeryFormRepository(context, api, pref, db, ncdReferalRepo)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getSyncedVisitsByRchId delegates to dao`() = runTest {
        val list = listOf(mockk<EyeSurgeryFormResponseJsonEntity>())
        coEvery { jsonDao.getSyncedVisitsByRchId(5L) } returns list
        assertEquals(list, repo.getSyncedVisitsByRchId(5L))
    }

    @Test
    fun `getAllVisitsByBenId delegates to dao`() = runTest {
        val list = listOf(mockk<EyeSurgeryFormResponseJsonEntity>())
        coEvery { jsonDao.getAllVisitsByBenId(6L) } returns list
        assertEquals(list, repo.getAllVisitsByBenId(6L))
    }

    @Test
    fun `getAllFormVisits delegates to api`() = runTest {
        val request = mockk<HBNCVisitRequest>()
        val response = mockk<Response<HBNCVisitListResponse>>()
        coEvery { api.getAllEyeSurgeryFormVisits("eye", request) } returns response
        assertEquals(response, repo.getAllFormVisits("eye", request))
    }

    @Test
    fun `getAllBenIds delegates to dao`() = runTest {
        coEvery { jsonDao.getAllUniqueBenIds() } returns listOf(1L, 2L)
        assertEquals(listOf(1L, 2L), repo.getAllBenIds())
    }

    @Test
    fun `insertFormResponse upserts by month`() = runTest {
        val entity = mockk<EyeSurgeryFormResponseJsonEntity>(relaxed = true)
        repo.insertFormResponse(entity)
        coVerify { jsonDao.upsertByMonth(entity) }
    }

    @Test
    fun `upsertByEye delegates to dao`() = runTest {
        val entity = mockk<EyeSurgeryFormResponseJsonEntity>(relaxed = true)
        repo.upsertByEye(entity)
        coVerify { jsonDao.upsertByEye(entity) }
    }

    @Test
    fun `loadFormResponseJson returns stored json`() = runTest {
        val entity = mockk<EyeSurgeryFormResponseJsonEntity>()
        every { entity.formDataJson } returns "JSON"
        coEvery { jsonDao.getLatestForBenForm(1L, "eye") } returns entity
        assertEquals("JSON", repo.loadFormResponseJson(1L, "eye"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<EyeSurgeryFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms("eye") } returns list
        assertEquals(list, repo.getUnsyncedForms("eye"))
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns false for invalid entity`() = runTest {
        val entity = mockk<EyeSurgeryFormResponseJsonEntity>(relaxed = true)
        assertFalse(repo.syncFormToServer("user", "eye", entity))
    }

    @Test
    fun `saveReferral saves referral through ncd repo`() = runTest {
        every { pref.getLoggedInUser() } returns null
        repo.saveReferral(1L, "Hospital", "Reason")
        coVerify { ncdReferalRepo.saveReferedNCD(any()) }
    }

    @Test
    fun `saveDownloadedVisitList returns early for empty list`() = runTest {
        repo.saveDownloadedVisitList(emptyList(), "eye")
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
        assertNull(repo.loadFormResponseJson(1L, "eye"))
    }

    @Test
    fun `saveDownloadedVisitList upserts entity built from server payload`() = runTest {
        val fields = JsonObject().apply {
            addProperty("visit_date", "01-01-2026")
            addProperty("count", 1)
            addProperty("done", false)
            add("nested", JsonObject())
        }
        val item = HBNCVisitResponse(
            id = 9,
            houseHoldId = 20L,
            beneficiaryId = 10L,
            visitDate = "01-01-2026",
            eyeSide = "LEFT",
            fields = fields
        )

        repo.saveDownloadedVisitList(listOf(item), "eye")

        coVerify { jsonDao.upsertByMonth(any()) }
    }

    @Test
    fun `saveDownloadedVisitList handles iso formatted visit date`() = runTest {
        val item = HBNCVisitResponse(
            id = 10,
            houseHoldId = 21L,
            beneficiaryId = 11L,
            visitDate = "2026-01-01",
            eyeSide = "RIGHT",
            fields = JsonObject()
        )

        repo.saveDownloadedVisitList(listOf(item), "eye")

        coVerify { jsonDao.upsertByMonth(any()) }
    }

    @Test
    fun `saveReferral uses logged in user name when available`() = runTest {
        repo.saveReferral(1L, "Hospital", "Reason")
        coVerify { ncdReferalRepo.saveReferedNCD(any()) }
    }
}
