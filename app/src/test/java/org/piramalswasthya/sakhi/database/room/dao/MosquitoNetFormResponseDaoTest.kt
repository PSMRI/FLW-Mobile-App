package org.piramalswasthya.sakhi.database.room.dao

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import java.util.Calendar

class MosquitoNetFormResponseDaoTest {

    private val inserted = mutableListOf<MosquitoNetFormResponseJsonEntity>()
    private val yearsRequested = mutableListOf<String>()
    private val syncedIds = mutableListOf<Int>()
    private var existsCount = 0
    private var countForYear = 0
    private var oldest: MosquitoNetFormResponseJsonEntity? = null

    private val dao: MosquitoNetFormResponseDao = object : MosquitoNetFormResponseDao {

        override suspend fun insertFormResponse(response: MosquitoNetFormResponseJsonEntity) {
            inserted.add(response)
        }

        override suspend fun exists(hhId: Long, visitDate: String): Int = existsCount

        override suspend fun markAsSynced(id: Int, syncedAt: String) {
            syncedIds.add(id)
        }

        override suspend fun getLatestForHhForm(
            hhId: Long,
            formId: String
        ): MosquitoNetFormResponseJsonEntity? = inserted.lastOrNull()

        override suspend fun getAllByHhId(hhId: Long): List<MosquitoNetFormResponseJsonEntity> =
            inserted.filter { it.hhId == hhId }

        override suspend fun getUnsyncedForms(formId: String): List<MosquitoNetFormResponseJsonEntity> =
            inserted.filter { !it.isSynced }

        override suspend fun getCountForYear(hhId: Long, formId: String, year: String): Int {
            yearsRequested.add(year)
            return countForYear
        }

        override suspend fun getOldestForYear(
            hhId: Long,
            formId: String,
            year: String
        ): MosquitoNetFormResponseJsonEntity? = oldest

        override suspend fun getFormJsonList(hhId: Long): List<String> =
            inserted.map { it.formDataJson }
    }

    private fun entity(
        id: Int = 0,
        visitDate: String = "2026-06-15",
        hhId: Long = 11L,
        formId: String = "mosquito_net"
    ) = MosquitoNetFormResponseJsonEntity(
        id = id,
        hhId = hhId,
        formId = formId,
        version = 1,
        visitDate = visitDate,
        formDataJson = "{\"fields\":{\"nets\":\"2\"}}"
    )

    @Test
    fun `insertWithLimit stores the entity when the yearly limit is not reached`() = runTest {
        countForYear = 2

        val result = dao.insertWithLimit(entity())

        assertTrue(result)
        assertEquals(1, inserted.size)
        assertEquals(0, inserted.first().id)
        assertEquals(listOf("2026"), yearsRequested)
    }

    @Test
    fun `insertWithLimit stores the entity when nothing has been recorded this year`() = runTest {
        countForYear = 0

        assertTrue(dao.insertWithLimit(entity()))
        assertEquals(1, inserted.size)
    }

    @Test
    fun `insertWithLimit refuses a duplicate visit date for the same household`() = runTest {
        existsCount = 1
        countForYear = 1

        val result = dao.insertWithLimit(entity())

        assertFalse(result)
        assertTrue(inserted.isEmpty())
    }

    @Test
    fun `insertWithLimit overwrites the oldest row of the year once the limit is reached`() = runTest {
        countForYear = 4
        oldest = entity(id = 77, visitDate = "2026-01-02")

        val result = dao.insertWithLimit(entity(visitDate = "2026-06-15"))

        assertTrue(result)
        assertEquals(1, inserted.size)
        assertEquals(77, inserted.first().id)
        assertEquals("2026-06-15", inserted.first().visitDate)
    }

    @Test
    fun `insertWithLimit gives up when the limit is reached and no oldest row is found`() = runTest {
        countForYear = 9
        oldest = null

        val result = dao.insertWithLimit(entity())

        assertFalse(result)
        assertTrue(inserted.isEmpty())
    }

    @Test
    fun `insertWithLimit converts indian language digits before extracting the year`() = runTest {
        countForYear = 0
        val devanagariDate = "२०२६-०६-१५"

        assertTrue(dao.insertWithLimit(entity(visitDate = devanagariDate)))
        assertEquals(listOf("2026"), yearsRequested)
    }

    @Test
    fun `insertWithLimit falls back to the current year for an unparsable visit date`() = runTest {
        countForYear = 0
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

        assertTrue(dao.insertWithLimit(entity(visitDate = "not-a-date")))
        assertEquals(listOf(currentYear), yearsRequested)
    }

    @Test
    fun `insertWithLimit falls back to the current year for an empty visit date`() = runTest {
        countForYear = 0
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

        assertTrue(dao.insertWithLimit(entity(visitDate = "")))
        assertEquals(listOf(currentYear), yearsRequested)
    }

    @Test
    fun `insertWithLimit still resolves a four digit year for a dd-MM-yyyy visit date`() = runTest {
        countForYear = 0

        assertTrue(dao.insertWithLimit(entity(visitDate = "15-06-2026")))

        val year = yearsRequested.single()
        assertTrue("Expected a four digit year, was: $year", Regex("\\d{4}").matches(year))
    }

    @Test
    fun `the dao contract keeps the other queries reachable from the default methods`() = runTest {
        countForYear = 0
        val stored = entity(visitDate = "2026-07-01")

        assertTrue(dao.insertWithLimit(stored))
        dao.markAsSynced(stored.id, "2026-07-01T00:00:00.000Z")

        assertEquals(listOf(0), syncedIds)
        assertEquals(1, dao.getAllByHhId(11L).size)
        assertEquals(1, dao.getUnsyncedForms("mosquito_net").size)
        assertEquals(1, dao.getFormJsonList(11L).size)
        assertEquals(stored, dao.getLatestForHhForm(11L, "mosquito_net"))
        assertEquals(0, dao.exists(11L, "2026-07-01"))
    }
}
