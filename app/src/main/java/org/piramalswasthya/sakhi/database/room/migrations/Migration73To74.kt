package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration73To74 : Migration(73, 74) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN recommendedForTpt INTEGER")
    }
}
