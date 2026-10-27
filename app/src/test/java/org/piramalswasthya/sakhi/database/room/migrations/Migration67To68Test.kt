package org.piramalswasthya.sakhi.database.room.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Migration67To68Test {

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
    fun `migrates from 67 to 68`() {
        assertEquals(67, Migration67To68.startVersion)
        assertEquals(68, Migration67To68.endVersion)
    }

    @Test
    fun `creates the tpt follow up table and its unique index`() {
        Migration67To68.migrate(mockDb())

        assertEquals(2, executedSql.size)
        assertTrue(executedSql[0].startsWith("CREATE TABLE IF NOT EXISTS `tb_tpt_follow_up` ("))
        assertTrue(executedSql[0].contains("`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"))
        assertTrue(executedSql[0].contains("`fieldsJson` TEXT NOT NULL"))
        assertTrue(executedSql[0].endsWith("`updatedAt` INTEGER NOT NULL)"))
        assertEquals(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_tb_tpt_follow_up_benId_visitNo_followUpNo` " +
                    "ON `tb_tpt_follow_up` (`benId`, `visitNo`, `followUpNo`)",
            executedSql[1]
        )
    }
}
