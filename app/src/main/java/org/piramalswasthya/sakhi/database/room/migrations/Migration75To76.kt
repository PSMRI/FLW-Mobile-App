package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

// TB Suspected: recommendedForTpt (from release-2.14, originally numbered 73 -> 74).
// Renumbered after merging into the 2.13 line, where 73 -> 75 are the General Check-up migrations.
object Migration75To76 : Migration(75, 76) {
    override fun migrate(database: SupportSQLiteDatabase) {
        if (!columnExists(database, "TB_SUSPECTED", "recommendedForTpt") &&
            !columnExists(database, "TB_SUSPECTED", "isRecommendedForTpt")
        ) {
            database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN recommendedForTpt INTEGER")
        }
    }
}
