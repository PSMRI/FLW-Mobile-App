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
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.BenIfaFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.network.AmritApiService
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
}
