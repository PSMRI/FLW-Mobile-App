package org.piramalswasthya.sakhi.database.room

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

class InAppDbMigrationTest {

    private lateinit var connection: Connection

    @Before
    fun setUp() {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        // Reproduce the historical (buggy) BENEFICIARY schema from MIGRATION_18_19:
        // isDeath declared INTEGER but with a string DEFAULT.
        connection.createStatement().use { stmt ->
            stmt.execute(
                "CREATE TABLE BENEFICIARY (" +
                        "beneficiaryId INTEGER PRIMARY KEY, " +
                        "isDeath INTEGER NOT NULL DEFAULT 'undefined'" +
                        ")"
            )
        }
    }

    @After
    fun tearDown() {
        connection.close()
    }

    private fun insertBen(id: Int, isDeath: String) {
        connection.prepareStatement(
            "INSERT INTO BENEFICIARY (beneficiaryId, isDeath) VALUES (?, ?)"
        ).use { ps ->
            ps.setInt(1, id)
            ps.setString(2, isDeath)
            ps.executeUpdate()
        }
    }

    private fun countWhere(clause: String): Int {
        connection.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) FROM BENEFICIARY WHERE $clause").use { rs ->
                rs.next()
                return rs.getInt(1)
            }
        }
    }

    @Test
    fun `corrupted isDeath rows are excluded by a bare isDeath equals 0 filter before migration`() {
        insertBen(1, "undefined")
        insertBen(2, "undefined")
        insertBen(3, "undefined")
        insertBen(4, "1")
        insertBen(5, "0")

        // In SQLite 'undefined' = 0 is FALSE, so the three corrupted rows are missed.
        assertEquals(1, countWhere("isDeath = 0"))
    }

    @Test
    fun `migration normalizes corrupted isDeath values to 0`() {
        insertBen(1, "undefined")
        insertBen(2, "undefined")
        insertBen(3, "undefined")
        insertBen(4, "1")
        insertBen(5, "0")

        connection.createStatement().use { stmt ->
            stmt.execute(InAppDb.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL)
        }

        // All corrupted rows now count as alive.
        assertEquals(4, countWhere("isDeath = 0"))
        // No rows left with the corrupted text value.
        assertEquals(0, countWhere("isDeath = 'undefined'"))
    }

    @Test
    fun `migration preserves genuine deaths`() {
        insertBen(1, "undefined")
        insertBen(2, "1")
        insertBen(3, "1")

        connection.createStatement().use { stmt ->
            stmt.execute(InAppDb.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL)
        }

        assertEquals(2, countWhere("isDeath = 1"))
    }

    @Test
    fun `migration leaves already correct values untouched`() {
        insertBen(1, "0")
        insertBen(2, "1")

        connection.createStatement().use { stmt ->
            stmt.execute(InAppDb.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL)
        }

        assertEquals(1, countWhere("isDeath = 0"))
        assertEquals(1, countWhere("isDeath = 1"))
    }

    // =====================================================
    // InAppDb.tableExists() / InAppDb.columnExists()
    // The migration helpers used by MIGRATION_56_57..60_61.
    // =====================================================

    @Test
    fun `tableExists returns true when sqlite_master has a row`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>(), any<Array<Any?>>()) } returns cursor
        every { cursor.count } returns 1

        assertTrue(InAppDb.tableExists(db, "BENEFICIARY"))
        verify { cursor.close() }
    }

    @Test
    fun `tableExists returns false when sqlite_master has no rows`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>(), any<Array<Any?>>()) } returns cursor
        every { cursor.count } returns 0

        assertFalse(InAppDb.tableExists(db, "NO_SUCH_TABLE"))
        verify { cursor.close() }
    }

    @Test
    fun `tableExists queries sqlite_master with the table name bound`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>(), any<Array<Any?>>()) } returns cursor
        every { cursor.count } returns 1

        InAppDb.tableExists(db, "VHNC")

        verify {
            db.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf<Any?>("VHNC")
            )
        }
    }

    @Test
    fun `columnExists returns true when pragma lists the column`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>()) } returns cursor
        every { cursor.moveToNext() } returnsMany listOf(true, true, false)
        every { cursor.getColumnIndexOrThrow("name") } returns 1
        every { cursor.getString(1) } returnsMany listOf("beneficiaryId", "isDeath")

        assertTrue(InAppDb.columnExists(db, "BENEFICIARY", "isDeath"))
        verify { cursor.close() }
    }

    @Test
    fun `columnExists returns false when pragma never lists the column`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>()) } returns cursor
        every { cursor.moveToNext() } returnsMany listOf(true, true, false)
        every { cursor.getColumnIndexOrThrow("name") } returns 1
        every { cursor.getString(1) } returnsMany listOf("beneficiaryId", "isDeath")

        assertFalse(InAppDb.columnExists(db, "BENEFICIARY", "abha_familyId"))
        verify { cursor.close() }
    }

    @Test
    fun `columnExists returns false for an empty pragma result`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>()) } returns cursor
        every { cursor.moveToNext() } returns false

        assertFalse(InAppDb.columnExists(db, "HOUSEHOLD", "isDeactivate"))
        verify { cursor.close() }
    }

    @Test
    fun `columnExists asks sqlite for the table info pragma`() {
        val db = mockk<SupportSQLiteDatabase>()
        val cursor = mockk<Cursor>(relaxed = true)
        every { db.query(any<String>()) } returns cursor
        every { cursor.moveToNext() } returns false

        InAppDb.columnExists(db, "TB_SCREENING", "bmi")

        verify { db.query("PRAGMA table_info(TB_SCREENING)") }
    }

    @Test
    fun `normalize isDeath sql targets only corrupted values`() {
        assertTrue(
            InAppDb.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL
                .contains("isDeath IS NULL OR (isDeath <> 0 AND isDeath <> 1)")
        )
        assertTrue(
            InAppDb.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL
                .startsWith("UPDATE BENEFICIARY SET isDeath = 0")
        )
    }
}
