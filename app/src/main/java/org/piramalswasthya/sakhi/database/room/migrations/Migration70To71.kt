package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration70To71 : Migration(70, 71) {
    override fun migrate(database: SupportSQLiteDatabase) {
        if (!columnExists(database, "children_under_five_all_visit", "serverId")) {
            database.execSQL(
                "ALTER TABLE children_under_five_all_visit ADD COLUMN serverId INTEGER"
            )
        }
        database.execSQL(
            "DROP INDEX IF EXISTS `index_children_under_five_all_visit_benId_hhId_visitDate_formId`"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_children_under_five_all_visit_serverId_formId` " +
                    "ON `children_under_five_all_visit` (`serverId`, `formId`)"
        )
    }
}
