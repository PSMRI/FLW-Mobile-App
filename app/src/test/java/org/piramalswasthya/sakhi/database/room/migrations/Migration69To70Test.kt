package org.piramalswasthya.sakhi.database.room.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class Migration69To70Test {

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
    fun `migrates from 69 to 70`() {
        assertEquals(69, Migration69To70.startVersion)
        assertEquals(70, Migration69To70.endVersion)
    }

    @Test
    fun `adds all three beneficiary living place columns when missing`() {
        Migration69To70.migrate(mockDb(emptyList()))

        assertEquals(
            listOf(
                "ALTER TABLE BENEFICIARY ADD COLUMN livingPlace TEXT",
                "ALTER TABLE BENEFICIARY ADD COLUMN otherLivingPlace TEXT",
                "ALTER TABLE BENEFICIARY ADD COLUMN institutionName TEXT"
            ),
            executedSql
        )
    }

    @Test
    fun `adds only the columns that are missing`() {
        Migration69To70.migrate(mockDb(listOf("livingPlace", "institutionName")))

        assertEquals(listOf("ALTER TABLE BENEFICIARY ADD COLUMN otherLivingPlace TEXT"), executedSql)
    }

    @Test
    fun `does nothing when all columns already exist`() {
        Migration69To70.migrate(mockDb(listOf("livingPlace", "otherLivingPlace", "institutionName")))

        assertEquals(emptyList<String>(), executedSql)
    }
}
