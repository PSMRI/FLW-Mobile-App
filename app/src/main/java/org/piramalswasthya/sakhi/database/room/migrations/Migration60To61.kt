package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration60To61 : Migration(60, 61) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            if (tableExists(database, "BENEFICIARY")) {
                database.execSQL(MIGRATION_60_61_NORMALIZE_ISDEATH_SQL)
            }
        } catch (_: Exception) {
        }
    }
}
