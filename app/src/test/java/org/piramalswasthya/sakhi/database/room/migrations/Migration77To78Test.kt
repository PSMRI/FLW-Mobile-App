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

    private fun mockDb(existingColumns: List<String> = emptyList()): SupportSQLiteDatabase {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        every { db.execSQL(any<String>()) } answers {
            executedSql.add(firstArg<String>())
            Unit
        }
        every { db.query(any<String>()) } answers {
            val cursor = mockk<Cursor>(relaxed = true)
            var index = -1
            every { cursor.moveToNext() } answers {
                index += 1
                index < existingColumns.size
            }
            every { cursor.getColumnIndexOrThrow("name") } returns 0
            every { cursor.getString(0) } answers { existingColumns[index] }
            cursor
        }
        return db
    }

    @Test
    fun `migrates from 77 to 78`() {
        assertEquals(77, Migration77To78.startVersion)
        assertEquals(78, Migration77To78.endVersion)
    }

    @Test
    fun `creates the gc referral follow up table with its final columns when missing`() {
        Migration77To78.migrate(mockDb())

        assertEquals(2, executedSql.size)
        val createTable = executedSql[0]
        assertTrue(createTable.startsWith("CREATE TABLE IF NOT EXISTS `gc_referral_follow_up`"))
        assertTrue(createTable.contains("`referralReason` TEXT NOT NULL DEFAULT ''"))
        assertTrue(createTable.contains("`referralMonth` INTEGER NOT NULL DEFAULT 0"))
        assertTrue(createTable.contains("`followUpStatus` TEXT,"))
        assertEquals(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_gc_referral_follow_up_benId_referralReason_referralMonth` " +
                    "ON `gc_referral_follow_up` (`benId`, `referralReason`, `referralMonth`)",
            executedSql[1]
        )
    }

    @Test
    fun `74 to 75 creates the table before altering it when an earlier build skipped it`() {
        Migration74To75.migrate(mockDb())

        assertTrue(executedSql[0].startsWith("CREATE TABLE IF NOT EXISTS `gc_referral_follow_up`"))
        assertTrue(executedSql.indexOfFirst { it.startsWith("ALTER TABLE gc_referral_follow_up") } > 0)
    }

    @Test
    fun `74 to 75 skips the column adds when the columns already exist`() {
        Migration74To75.migrate(mockDb(listOf("referralReason", "referralMonth")))

        assertTrue(executedSql.none { it.startsWith("ALTER TABLE") })
    }
}
