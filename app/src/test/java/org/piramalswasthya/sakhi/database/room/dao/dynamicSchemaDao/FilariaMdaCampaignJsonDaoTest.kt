package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity

class FilariaMdaCampaignJsonDaoTest {

    private val inserted = mutableListOf<FilariaMDACampaignFormResponseJsonEntity>()
    private var byYearResult: FilariaMDACampaignFormResponseJsonEntity? = null

    private val dao: FilariaMdaCampaignJsonDao = object : FilariaMdaCampaignJsonDao {

        override suspend fun insertCampaignFormResponse(response: FilariaMDACampaignFormResponseJsonEntity) {
            inserted.add(response)
        }

        override suspend fun insertCampaignAll(responses: List<FilariaMDACampaignFormResponseJsonEntity>) {
            inserted.addAll(responses)
        }

        override suspend fun getCampaignFormResponse(visitDate: String): FilariaMDACampaignFormResponseJsonEntity? =
            inserted.lastOrNull()

        override suspend fun deleteCampaignFormResponse(visitDate: String) {}

        override suspend fun getUnsyncedCampaignForms(formId: String): List<FilariaMDACampaignFormResponseJsonEntity> =
            inserted.filter { !it.isSynced }

        override suspend fun markCampaignAsSynced(id: Int, syncedAt: String) {}

        override suspend fun getCampaignSyncedVisitsByRchId(): List<FilariaMDACampaignFormResponseJsonEntity> =
            inserted.toList()

        override suspend fun getCampaignFormJsonList(): List<String> = inserted.map { it.formDataJson }

        override suspend fun getCampaignLatest3Json(): List<String> = inserted.map { it.formDataJson }

        override suspend fun getCampaignByBenFormYear(
            formId: String,
            visitYear: String
        ): FilariaMDACampaignFormResponseJsonEntity? = byYearResult

        override suspend fun getCampaignLatestForBenForm(visitDate: String): FilariaMDACampaignFormResponseJsonEntity? =
            inserted.lastOrNull()
    }

    private fun entity(
        id: Int = 0,
        visitYear: String = "2026",
        formId: String = "filaria_mda_campaign"
    ) = FilariaMDACampaignFormResponseJsonEntity(
        id = id,
        visitDate = "2026-06-15",
        visitYear = visitYear,
        formId = formId,
        version = 1,
        formDataJson = "{}"
    )

    @Test
    fun `upsertByYear inserts a new record when nothing exists for the year`() = runTest {
        byYearResult = null

        dao.upsertByYear(entity())

        assertEquals(1, inserted.size)
        assertEquals(0, inserted.first().id)
    }

    @Test
    fun `upsertByYear overwrites the existing record id for the year`() = runTest {
        byYearResult = entity(id = 33)

        dao.upsertByYear(entity(id = 0))

        assertEquals(1, inserted.size)
        assertEquals(33, inserted.first().id)
    }

    @Test
    fun `insertOncePerYear stores the entity and returns true when none exists yet`() = runTest {
        byYearResult = null

        val result = dao.insertOncePerYear(entity())

        assertTrue(result)
        assertEquals(1, inserted.size)
    }

    @Test
    fun `insertOncePerYear refuses and skips insert when the year is already recorded`() = runTest {
        byYearResult = entity(id = 5)

        val result = dao.insertOncePerYear(entity())

        assertFalse(result)
        assertTrue(inserted.isEmpty())
    }

    @Test
    fun `the dao contract keeps the other queries reachable from the default methods`() = runTest {
        byYearResult = null
        val stored = entity()

        dao.upsertByYear(stored)

        assertEquals(1, dao.getCampaignSyncedVisitsByRchId().size)
        assertEquals(1, dao.getCampaignFormJsonList().size)
        assertEquals(1, dao.getUnsyncedCampaignForms("filaria_mda_campaign").size)
        assertEquals(stored.copy(id = inserted.first().id), dao.getCampaignLatestForBenForm("2026-06-15"))
    }
}
