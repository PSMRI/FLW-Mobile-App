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
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.NCDReferalFormResponseJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormNCDFollowUpSubmitRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.network.AmritApiService
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
}
