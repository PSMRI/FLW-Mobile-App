package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity

class FilariaMDAFormResponseJsonDaoTest {

    private val inserted = mutableListOf<FilariaMDAFormResponseJsonEntity>()
    private var byMonthResult: FilariaMDAFormResponseJsonEntity? = null

    private val dao: FilariaMDAFormResponseJsonDao = object : FilariaMDAFormResponseJsonDao {

        override suspend fun insertFormResponse(response: FilariaMDAFormResponseJsonEntity) {
            inserted.add(response)
        }

        override suspend fun insertAll(responses: List<FilariaMDAFormResponseJsonEntity>) {
            inserted.addAll(responses)
        }

        override suspend fun getFormResponse(hhId: Long, visitDate: String): FilariaMDAFormResponseJsonEntity? =
            inserted.lastOrNull()

        override suspend fun deleteFormResponse(hhId: Long, visitDate: String) {}

        override suspend fun getUnsyncedForms(formId: String): List<FilariaMDAFormResponseJsonEntity> =
            inserted.filter { !it.isSynced }

        override suspend fun markAsSynced(id: Int, syncedAt: String) {}

        override suspend fun getSyncedVisitsByRchId(hhId: Long): List<FilariaMDAFormResponseJsonEntity> =
            inserted.filter { it.hhId == hhId }

        override suspend fun updateVisitBenId(newHhId: Long, oldHhId: Long) {}

        override suspend fun getFormJsonList(hhId: Long): List<String> = inserted.map { it.formDataJson }

        override suspend fun getLatest3Json(hhId: Long): List<String> = inserted.map { it.formDataJson }

        override suspend fun getByBenFormMonth(
            hhId: Long,
            formId: String,
            visitMonth: String
        ): FilariaMDAFormResponseJsonEntity? = byMonthResult

        override suspend fun getLatestForBenForm(hhId: Long, formId: String): FilariaMDAFormResponseJsonEntity? =
            inserted.lastOrNull()
    }

    private fun entity(
        id: Int = 0,
        hhId: Long = 21L,
        visitMonth: String = "2026-06",
        formId: String = "filaria_mda"
    ) = FilariaMDAFormResponseJsonEntity(
        id = id,
        hhId = hhId,
        visitDate = "2026-06-15",
        visitMonth = visitMonth,
        formId = formId,
        version = 1,
        formDataJson = "{}"
    )

    @Test
    fun `upsertByMonth inserts a new record when nothing exists for the month`() = runTest {
        byMonthResult = null

        dao.upsertByMonth(entity())

        assertEquals(1, inserted.size)
        assertEquals(0, inserted.first().id)
    }

    @Test
    fun `upsertByMonth overwrites the existing record id for the month`() = runTest {
        byMonthResult = entity(id = 44)

        dao.upsertByMonth(entity(id = 0))

        assertEquals(1, inserted.size)
        assertEquals(44, inserted.first().id)
    }

    @Test
    fun `insertOncePerMonth stores the entity and returns true when none exists yet`() = runTest {
        byMonthResult = null

        val result = dao.insertOncePerMonth(entity())

        assertTrue(result)
        assertEquals(1, inserted.size)
    }

    @Test
    fun `insertOncePerMonth refuses and skips insert when the month is already recorded`() = runTest {
        byMonthResult = entity(id = 6)

        val result = dao.insertOncePerMonth(entity())

        assertFalse(result)
        assertTrue(inserted.isEmpty())
    }

    @Test
    fun `the dao contract keeps the other queries reachable from the default methods`() = runTest {
        byMonthResult = null
        val stored = entity(hhId = 5L)

        dao.upsertByMonth(stored)

        assertEquals(1, dao.getSyncedVisitsByRchId(5L).size)
        assertEquals(1, dao.getFormJsonList(5L).size)
        assertEquals(1, dao.getLatest3Json(5L).size)
        assertEquals(1, dao.getUnsyncedForms("filaria_mda").size)
        assertEquals(stored, dao.getLatestForBenForm(5L, "filaria_mda"))
    }
}
