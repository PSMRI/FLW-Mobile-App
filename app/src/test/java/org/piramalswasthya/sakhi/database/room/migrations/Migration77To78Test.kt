package org.piramalswasthya.sakhi.database.room.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Migration77To78Test {

    private val executedSql = mutableListOf<String>()

    private val gamificationTables = listOf(
        "BADGE_EARNED", "BADGE_STATE", "BADGE_SYNC_LOG", "BADGE_STREAK_FREEZE", "BADGE_CONFIG",
        "notification_templates", "notification_history", "form_save_log", "MONTHLY_RECAP"
    )

    private fun columnCursor(columns: List<String>): Cursor {
        val cursor = mockk<Cursor>(relaxed = true)
        var index = -1
        every { cursor.moveToNext() } answers {
            index += 1
            index < columns.size
        }
        every { cursor.getColumnIndexOrThrow("name") } returns 0
        every { cursor.getString(0) } answers { columns[index] }
        return cursor
    }

    private fun mockDb(tbScreeningColumns: List<String>): SupportSQLiteDatabase {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        every { db.execSQL(any<String>()) } answers {
            executedSql.add(firstArg<String>())
            Unit
        }
        every { db.query(any<String>()) } answers { columnCursor(tbScreeningColumns) }
        return db
    }

    private val tbColumns = listOf("failureToGainWeight", "decreasedActivityOrPlayfulness")

    @Test
    fun `migrates from 77 to 78`() {
        assertEquals(77, Migration77To78.startVersion)
        assertEquals(78, Migration77To78.endVersion)
    }

    @Test
    fun `creates every gamification table idempotently`() {
        Migration77To78.migrate(mockDb(tbColumns))

        gamificationTables.forEach { table ->
            assertTrue(
                "missing CREATE for $table",
                executedSql.any { it.startsWith("CREATE TABLE IF NOT EXISTS `$table`") }
            )
        }
        assertTrue(executedSql.all { it.contains("IF NOT EXISTS") })
    }

    @Test
    fun `release device with tb screening columns gets no ALTER`() {
        Migration77To78.migrate(mockDb(tbColumns))

        assertTrue(executedSql.none { it.startsWith("ALTER TABLE") })
    }

    @Test
    fun `gamification v69 device gets the missing tb screening columns`() {
        Migration77To78.migrate(mockDb(emptyList()))

        assertEquals(
            listOf(
                "ALTER TABLE TB_SCREENING ADD COLUMN failureToGainWeight INTEGER DEFAULT NULL",
                "ALTER TABLE TB_SCREENING ADD COLUMN decreasedActivityOrPlayfulness INTEGER DEFAULT NULL"
            ),
            executedSql.filter { it.startsWith("ALTER TABLE") }
        )
    }
}
