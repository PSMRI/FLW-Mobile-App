package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration39To40 : Migration(39, 40) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ALL_BEN_IFA_VISIT_HISTORY` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `hhId` INTEGER NOT NULL,
                `visitDate` TEXT NOT NULL,
                `formId` TEXT NOT NULL,
                `version` INTEGER NOT NULL,
                `formDataJson` TEXT NOT NULL,
                `isSynced` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `syncedAt` INTEGER
            )
        """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_ALL_BEN_IFA_VISIT_HISTORY_benId_hhId_visitDate_formId`
            ON `ALL_BEN_IFA_VISIT_HISTORY` (`benId`, `hhId`, `visitDate`, `formId`)
        """.trimIndent()
        )
    }
}
