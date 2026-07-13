package org.piramalswasthya.sakhi.database.room

import org.junit.After
import org.junit.Assert.assertEquals
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
}
