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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.NCDReferalFormResponseJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.dynamicEntity.FormNCDFollowUpSubmitRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDFollowUpResponse
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import java.io.IOException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class NCDFollowUpFormRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var db: InAppDb

    @MockK private lateinit var schemaDao: FormSchemaDao
    @MockK private lateinit var jsonDao: NCDReferalFormResponseJsonDao

    private lateinit var repo: NCDFollowUpFormRepository

    @Before
    override fun setUp() {
        super.setUp()
        every { db.NCDReferalFormResponseJsonDao() } returns jsonDao
        every { db.formSchemaDao() } returns schemaDao
        repo = NCDFollowUpFormRepository(context, api, pref, db)
    }

    @Test
    fun `getSavedSchema delegates to dao`() = runTest {
        val entity = mockk<FormSchemaEntity>()
        coEvery { schemaDao.getSchema("F1") } returns entity
        assertEquals(entity, repo.getSavedSchema("F1"))
    }

    @Test
    fun `getAllVisitsByBeneficiary delegates to dao`() = runTest {
        val list = listOf(mockk<NCDReferalFormResponseJsonEntity>())
        coEvery { jsonDao.getAllVisitsByBeneficiary(1L, "ncd") } returns list
        assertEquals(list, repo.getAllVisitsByBeneficiary(1L, "ncd"))
    }

    @Test
    fun `getUnsyncedForms delegates to dao`() = runTest {
        val list = listOf(mockk<NCDReferalFormResponseJsonEntity>())
        coEvery { jsonDao.getUnsyncedForms("ncd") } returns list
        assertEquals(list, repo.getUnsyncedForms("ncd"))
    }

    @Test
    fun `markFormAsSynced calls dao markAsSynced`() = runTest {
        repo.markFormAsSynced(4)
        coVerify { jsonDao.markAsSynced(4, any()) }
    }

    @Test
    fun `syncFormToServer returns true when response successful`() = runTest {
        val request = mockk<FormNCDFollowUpSubmitRequest>()
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns true
        coEvery { api.submitNCDFollowUp(any()) } returns response
        assertTrue(repo.syncFormToServer("user", "ncd", request))
    }

    @Test
    fun `syncFormToServer returns false on exception`() = runTest {
        val request = mockk<FormNCDFollowUpSubmitRequest>()
        coEvery { api.submitNCDFollowUp(any()) } throws RuntimeException("boom")
        assertFalse(repo.syncFormToServer("user", "ncd", request))
    }

    @Test
    fun `fetchFormsFromServer throws when no user logged in`() = runTest {
        every { pref.getLoggedInUser() } returns null
        var thrown: Throwable? = null
        try {
            repo.fetchFormsFromServer("ncd", "user")
        } catch (e: Throwable) {
            thrown = e
        }
        assertTrue(thrown is IllegalStateException)
    }

    @Test
    fun `saveDownloadedForms inserts when no existing record`() = runTest {
        val entity = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        coEvery { jsonDao.getFormResponse(any(), any(), any()) } returns null
        repo.saveDownloadedForms(listOf(entity))
        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `saveVisitOrFollowUp inserts new entity when no existing record`() = runTest {
        coEvery { jsonDao.getFormResponse(any(), any(), any()) } returns null
        repo.saveVisitOrFollowUp(
            benId = 1L,
            hhId = 2L,
            visitNo = 1,
            followUpNo = 0,
            treatmentStartDate = "01-01-2026",
            followUpDate = null,
            diagnosisList = listOf("A", "B"),
            formId = "ncd",
            formJson = "{}",
            version = 1
        )
        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `getFormSchema returns api schema and saves it when newer`() = runTest {
        val schema = mockk<FormSchemaDto>(relaxed = true)
        every { schema.formId } returns "F1"
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns schema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response
        coEvery { schemaDao.getSchema(any()) } returns null

        assertSame(schema, repo.getFormSchema("F1"))
        coVerify { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema returns null when api throws and nothing stored locally`() = runTest {
        coEvery { schemaDao.getSchema(any()) } returns null
        coEvery { api.fetchFormSchema(any(), any()) } throws RuntimeException("network")

        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema returns null when api unsuccessful and nothing stored locally`() = runTest {
        coEvery { schemaDao.getSchema(any()) } returns null
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns false
        every { response.body() } returns null
        coEvery { api.fetchFormSchema(any(), any()) } returns response

        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getFormSchema returns null when api body has null data`() = runTest {
        coEvery { schemaDao.getSchema(any()) } returns null
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns null
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema(any(), any()) } returns response

        assertNull(repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `syncFormToServer returns false when response unsuccessful`() = runTest {
        val request = mockk<FormNCDFollowUpSubmitRequest>()
        val response = mockk<Response<Unit>>()
        every { response.isSuccessful } returns false
        coEvery { api.submitNCDFollowUp(any()) } returns response
        assertFalse(repo.syncFormToServer("user", "ncd", request))
    }

    @Test
    fun `saveDownloadedForms skips insert when existing record is not older`() = runTest {
        val existing = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        every { existing.updatedAt } returns 100L
        val entity = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        every { entity.updatedAt } returns 50L
        coEvery { jsonDao.getFormResponse(any(), any(), any()) } returns existing

        repo.saveDownloadedForms(listOf(entity))

        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveDownloadedForms inserts when existing record is older`() = runTest {
        val existing = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        every { existing.updatedAt } returns 10L
        val entity = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        every { entity.updatedAt } returns 99L
        coEvery { jsonDao.getFormResponse(any(), any(), any()) } returns existing

        repo.saveDownloadedForms(listOf(entity))

        coVerify { jsonDao.insertFormResponse(entity) }
    }

    @Test
    fun `saveDownloadedForms does nothing for empty list`() = runTest {
        repo.saveDownloadedForms(emptyList())
        coVerify(exactly = 0) { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveVisitOrFollowUp merges fields with existing record`() = runTest {
        val existing = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        every { existing.formDataJson } returns
                """{"formId":"ncd","fields":{"bp":"120","sugar":"90"}}"""
        coEvery { jsonDao.getFormResponse(any(), any(), any()) } returns existing

        repo.saveVisitOrFollowUp(
            benId = 1L,
            hhId = 2L,
            visitNo = 1,
            followUpNo = 1,
            treatmentStartDate = "01-01-2026",
            followUpDate = "02-01-2026",
            diagnosisList = listOf("D1"),
            formId = "ncd",
            formJson = """{"formId":"ncd","beneficiaryId":1,"houseHoldId":2,"visitNo":1,"followUpNo":1,"fields":{"bp":"130"}}""",
            version = 1
        )

        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `saveVisitOrFollowUp falls back when existing json is malformed`() = runTest {
        val existing = mockk<NCDReferalFormResponseJsonEntity>(relaxed = true)
        every { existing.formDataJson } returns "not-json"
        coEvery { jsonDao.getFormResponse(any(), any(), any()) } returns existing

        repo.saveVisitOrFollowUp(
            benId = 1L,
            hhId = 2L,
            visitNo = 1,
            followUpNo = 1,
            treatmentStartDate = "01-01-2026",
            followUpDate = null,
            diagnosisList = emptyList(),
            formId = "ncd",
            formJson = "{}",
            version = 1
        )

        coVerify { jsonDao.insertFormResponse(any()) }
    }

    @Test
    fun `getFormSchema skips saving when local schema version is already current`() = runTest {
        val apiSchema = mockk<FormSchemaDto>(relaxed = true)
        every { apiSchema.formId } returns "F1"
        every { apiSchema.version } returns 3
        val apiResponse = mockk<ApiResponse<FormSchemaDto>>()
        every { apiResponse.success } returns true
        every { apiResponse.data } returns apiSchema
        val response = mockk<Response<ApiResponse<FormSchemaDto>>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns apiResponse
        coEvery { api.fetchFormSchema("F1", any()) } returns response

        val localEntity = mockk<FormSchemaEntity>(relaxed = true)
        every { localEntity.version } returns 3
        every { localEntity.schemaJson } returns """{"formId":"F1","formName":"Form 1","version":3,"sections":[]}"""
        coEvery { schemaDao.getSchema("F1") } returns localEntity

        assertSame(apiSchema, repo.getFormSchema("F1"))
        coVerify(exactly = 0) { schemaDao.insertOrUpdate(any()) }
    }

    @Test
    fun `fetchFormsFromServer maps successful response into entities`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 1
        every { user.userName } returns "asha1"
        every { pref.getLoggedInUser() } returns user

        val submitRequest = FormNCDFollowUpSubmitRequest(
            id = 1,
            benId = 10L,
            hhId = 20L,
            visitNo = 1,
            followUpNo = 0,
            treatmentStartDate = "01-01-2026",
            followUpDate = null,
            diagnosisCodes = "D1",
            formId = "ncd",
            version = 1,
            formDataJson = "{}"
        )
        val followUpResponse = NCDFollowUpResponse(statusCode = 200, data = listOf(submitRequest))
        val response = mockk<Response<NCDFollowUpResponse>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns followUpResponse
        coEvery { api.getAllFormNCDFollowUp(any()) } returns response

        val result = repo.fetchFormsFromServer("ncd", "user")

        assertEquals(1, result.size)
        assertEquals(10L, result[0].benId)
        assertTrue(result[0].isSynced)
    }

    @Test
    fun `fetchFormsFromServer returns empty list when response unsuccessful`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 1
        every { user.userName } returns "asha1"
        every { pref.getLoggedInUser() } returns user

        val response = mockk<Response<NCDFollowUpResponse>>()
        every { response.isSuccessful } returns false
        coEvery { api.getAllFormNCDFollowUp(any()) } returns response

        val result = repo.fetchFormsFromServer("ncd", "user")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `fetchFormsFromServer rethrows IOException from api call`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 1
        every { user.userName } returns "asha1"
        every { pref.getLoggedInUser() } returns user
        coEvery { api.getAllFormNCDFollowUp(any()) } throws IOException("network down")

        var thrown: Throwable? = null
        try {
            repo.fetchFormsFromServer("ncd", "user")
        } catch (e: Throwable) {
            thrown = e
        }

        assertTrue(thrown is IOException)
    }
}
