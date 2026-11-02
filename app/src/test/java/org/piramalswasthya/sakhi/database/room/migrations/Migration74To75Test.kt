package org.piramalswasthya.sakhi.database.room.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.sql.DriverManager

class Migration74To75Test {

    @Test
    fun `renames the stored recommendation column and preserves its value`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE BENEFICIARY (beneficiaryId INTEGER PRIMARY KEY NOT NULL)")
                statement.execute(
                    """
                    CREATE TABLE TB_SUSPECTED (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        benId INTEGER NOT NULL,
                        visitDate INTEGER NOT NULL,
                        visitLabel TEXT,
                        typeOfTBCase TEXT,
                        reasonForSuspicion TEXT,
                        hasSymptoms INTEGER NOT NULL,
                        isSputumCollected INTEGER,
                        sputumSubmittedAt TEXT,
                        nikshayId TEXT,
                        sputumTestResult TEXT,
                        isChestXRayDone INTEGER,
                        chestXRayResult TEXT,
                        referralFacility TEXT,
                        isTBConfirmed INTEGER,
                        isDRTBConfirmed INTEGER,
                        recommendedForTpt INTEGER,
                        isConfirmed INTEGER NOT NULL,
                        referred INTEGER,
                        followUps TEXT,
                        syncState INTEGER NOT NULL,
                        FOREIGN KEY(benId) REFERENCES BENEFICIARY(beneficiaryId)
                            ON UPDATE CASCADE ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                statement.execute("INSERT INTO BENEFICIARY (beneficiaryId) VALUES (19)")
                statement.execute(
                    """
                    INSERT INTO TB_SUSPECTED (
                        benId, visitDate, hasSymptoms, recommendedForTpt, isConfirmed, syncState
                    ) VALUES (19, 1700000000000, 1, 1, 0, 0)
                    """.trimIndent()
                )
            }

            val db = mockk<SupportSQLiteDatabase>()
            every { db.execSQL(any<String>()) } answers {
                connection.createStatement().use { it.execute(firstArg()) }
                Unit
            }

            Migration74To75.migrate(db)

            val columns = connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA table_info('TB_SUSPECTED')").use { result ->
                    buildList {
                        while (result.next()) add(result.getString("name"))
                    }
                }
            }
            assertTrue(columns.contains("isRecommendedForTpt"))
            assertFalse(columns.contains("recommendedForTpt"))
            val storedValue = connection.createStatement().use { statement ->
                statement.executeQuery("SELECT isRecommendedForTpt FROM TB_SUSPECTED WHERE benId = 19").use { result ->
                    result.next()
                    result.getInt(1)
                }
            }
            assertEquals(1, storedValue)
        }
    }
}
