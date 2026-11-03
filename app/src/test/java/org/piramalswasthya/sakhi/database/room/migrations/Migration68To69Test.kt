package org.piramalswasthya.sakhi.database.room.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class Migration68To69Test {

    private val executedSql = mutableListOf<String>()

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

    private fun mockDb(existingColumns: List<String>): SupportSQLiteDatabase {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        every { db.execSQL(any<String>()) } answers {
            executedSql.add(firstArg<String>())
            Unit
        }
        every { db.query(any<String>()) } answers { columnCursor(existingColumns) }
        return db
    }

    @Test
    fun `migrates from 68 to 69`() {
        assertEquals(68, Migration68To69.startVersion)
        assertEquals(69, Migration68To69.endVersion)
    }

    @Test
    fun `adds both tb screening columns when missing`() {
        Migration68To69.migrate(mockDb(emptyList()))

        assertEquals(
            listOf(
                "ALTER TABLE TB_SCREENING ADD COLUMN failureToGainWeight INTEGER DEFAULT NULL",
                "ALTER TABLE TB_SCREENING ADD COLUMN decreasedActivityOrPlayfulness INTEGER DEFAULT NULL"
            ),
            executedSql
        )
    }

    @Test
    fun `adds only the column that is missing`() {
        Migration68To69.migrate(mockDb(listOf("failureToGainWeight")))

        assertEquals(
            listOf("ALTER TABLE TB_SCREENING ADD COLUMN decreasedActivityOrPlayfulness INTEGER DEFAULT NULL"),
            executedSql
        )
    }

    @Test
    fun `does nothing when both columns already exist`() {
        Migration68To69.migrate(mockDb(listOf("failureToGainWeight", "decreasedActivityOrPlayfulness")))

        assertEquals(emptyList<String>(), executedSql)
    }
}
