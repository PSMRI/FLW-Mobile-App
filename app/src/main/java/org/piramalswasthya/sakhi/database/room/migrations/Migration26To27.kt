package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration26To27 : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE INCENTIVE_ACTIVITY ADD COLUMN groupName TEXT NOT NULL DEFAULT 'undefined'")

    }
}
