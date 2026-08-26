package org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity

class EyeSurgeryFormResponseJsonDaoTest {

    private val inserted = mutableListOf<EyeSurgeryFormResponseJsonEntity>()
    private var byMonthResult: EyeSurgeryFormResponseJsonEntity? = null
    private var byEyeResult: EyeSurgeryFormResponseJsonEntity? = null

    private val dao: EyeSurgeryFormResponseJsonDao = object : EyeSurgeryFormResponseJsonDao {

        override suspend fun insertFormResponse(response: EyeSurgeryFormResponseJsonEntity) {
            inserted.add(response)
        }

        override suspend fun insertAll(responses: List<EyeSurgeryFormResponseJsonEntity>) {
            inserted.addAll(responses)
        }

        override suspend fun getFormResponse(
            benId: Long,
            visitDate: String
        ): EyeSurgeryFormResponseJsonEntity? = inserted.lastOrNull()

        override suspend fun deleteFormResponse(benId: Long, visitDate: String) {}

        override suspend fun getUnsyncedForms(formId: String): List<EyeSurgeryFormResponseJsonEntity> =
            inserted.filter { !it.isSynced }

        override suspend fun markAsSynced(id: Int, syncedAt: String) {}

        override suspend fun getSyncedVisitsByRchId(benId: Long): List<EyeSurgeryFormResponseJsonEntity> =
            inserted.filter { it.benId == benId }

        override suspend fun updateVisitBenId(oldBenId: Long, newBenId: Long) {}

        override suspend fun getAllUniqueBenIds(): List<Long> = inserted.map { it.benId }.distinct()

        override suspend fun getFormJsonList(benId: Long, formId: String): List<String> =
            inserted.map { it.formDataJson }

        override suspend fun getByBenFormMonth(
            benId: Long,
            formId: String,
            eyeSide: String
        ): EyeSurgeryFormResponseJsonEntity? = byMonthResult

        override suspend fun getLatestForBenForm(
            benId: Long,
            formId: String
        ): EyeSurgeryFormResponseJsonEntity? = inserted.lastOrNull()

        override suspend fun getByBenAndEye(
            benId: Long,
            formId: String,
            eyeSide: String
        ): EyeSurgeryFormResponseJsonEntity? = byEyeResult

        override suspend fun getAllVisitsByBenId(benId: Long): List<EyeSurgeryFormResponseJsonEntity> =
            inserted.filter { it.benId == benId }
    }

    private fun entity(
        id: Int = 0,
        benId: Long = 1L,
        eyeSide: String = "LEFT",
        formId: String = "eye_surgery"
    ) = EyeSurgeryFormResponseJsonEntity(
        id = id,
        benId = benId,
        hhId = 10L,
        visitDate = "2026-06-15",
        visitMonth = "2026-06",
        eyeSide = eyeSide,
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
        byMonthResult = entity(id = 55)

        dao.upsertByMonth(entity(id = 0))

        assertEquals(1, inserted.size)
        assertEquals(55, inserted.first().id)
    }

    @Test
    fun `upsertByEye inserts a new record when nothing exists for the eye side`() = runTest {
        byEyeResult = null

        dao.upsertByEye(entity(eyeSide = "RIGHT"))

        assertEquals(1, inserted.size)
        assertEquals(0, inserted.first().id)
        assertEquals("RIGHT", inserted.first().eyeSide)
    }

    @Test
    fun `upsertByEye overwrites the existing record id for the eye side`() = runTest {
        byEyeResult = entity(id = 99, eyeSide = "LEFT")

        dao.upsertByEye(entity(id = 0, eyeSide = "LEFT"))

        assertEquals(1, inserted.size)
        assertEquals(99, inserted.first().id)
    }

    @Test
    fun `the dao contract keeps the other queries reachable from the default methods`() = runTest {
        byMonthResult = null
        val stored = entity(benId = 7L)

        dao.upsertByMonth(stored)

        assertEquals(1, dao.getAllVisitsByBenId(7L).size)
        assertEquals(1, dao.getSyncedVisitsByRchId(7L).size)
        assertEquals(listOf(7L), dao.getAllUniqueBenIds())
        assertEquals(1, dao.getFormJsonList(7L, "eye_surgery").size)
    }
}
