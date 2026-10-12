package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration31To32 : Migration(31, 32) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `children_under_five_all_visit` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `hhId` INTEGER NOT NULL,
                `visitDate` TEXT NOT NULL,
                `formId` TEXT NOT NULL,
                `version` INTEGER NOT NULL,
                `formDataJson` TEXT NOT NULL,
                `isSynced` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `syncedAt` INTEGER
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_children_under_five_all_visit_benId_hhId_visitDate_formId`
            ON `children_under_five_all_visit` (`benId`, `hhId`, `visitDate`, `formId`)
            """.trimIndent()
        )
    }
}
