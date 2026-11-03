package org.piramalswasthya.sakhi.database.room.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class Migration70To71Test {

    private val executedSql = mutableListOf<String>()

    private val dropOldIndex =
        "DROP INDEX IF EXISTS `index_children_under_five_all_visit_benId_hhId_visitDate_formId`"

    private val createNewIndex =
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_children_under_five_all_visit_serverId_formId` " +
                "ON `children_under_five_all_visit` (`serverId`, `formId`)"

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
    fun `migrates from 70 to 71`() {
        assertEquals(70, Migration70To71.startVersion)
        assertEquals(71, Migration70To71.endVersion)
    }

    @Test
    fun `adds serverId and swaps the unique index when column is missing`() {
        Migration70To71.migrate(mockDb(emptyList()))

        assertEquals(
            listOf(
                "ALTER TABLE children_under_five_all_visit ADD COLUMN serverId INTEGER",
                dropOldIndex,
                createNewIndex
            ),
            executedSql
        )
    }

    @Test
    fun `only swaps the unique index when serverId already exists`() {
        Migration70To71.migrate(mockDb(listOf("serverId")))

        assertEquals(listOf(dropOldIndex, createNewIndex), executedSql)
    }
}
