package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration72To73 : Migration(72, 73) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP INDEX IF EXISTS `ind_health_problem_type`")
        database.execSQL("DROP TABLE IF EXISTS `HEALTH_PROBLEM`")
        database.execSQL("DROP TABLE IF EXISTS `HEALTH_PROBLEM_TYPE`")
        if (!columnExists(database, "GENERAL_CHECKUP", "formData")) {
            database.execSQL("ALTER TABLE GENERAL_CHECKUP ADD COLUMN formData TEXT")
        }
    }
}
