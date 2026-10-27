package org.piramalswasthya.sakhi.database.room.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Migration64To65Test {

    private val executedSql = mutableListOf<String>()

    private fun mockDb(): SupportSQLiteDatabase {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        every { db.execSQL(any<String>()) } answers {
            executedSql.add(firstArg<String>())
            Unit
        }
        return db
    }

    @Test
    fun `migrates from 64 to 65`() {
        assertEquals(64, Migration64To65.startVersion)
        assertEquals(65, Migration64To65.endVersion)
    }

    @Test
    fun `drops and recreates the BEN_BASIC_CACHE view`() {
        Migration64To65.migrate(mockDb())

        assertEquals(2, executedSql.size)
        assertEquals("DROP VIEW IF EXISTS `BEN_BASIC_CACHE`", executedSql[0])
        assertTrue(executedSql[1].startsWith("CREATE VIEW `BEN_BASIC_CACHE` AS SELECT b.beneficiaryId as benId"))
        assertTrue(executedSql[1].contains("FROM BENEFICIARY b JOIN HOUSEHOLD h ON b.householdId = h.householdId"))
        assertTrue(executedSql[1].endsWith("DESC, b.beneficiaryId DESC"))
    }
}
