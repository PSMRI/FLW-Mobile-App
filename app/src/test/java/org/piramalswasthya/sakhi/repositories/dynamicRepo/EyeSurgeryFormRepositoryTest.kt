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
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.EyeSurgeryFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
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
}
