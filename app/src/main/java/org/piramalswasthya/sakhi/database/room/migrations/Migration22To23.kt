package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration22To23 : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE PNC_VISIT ADD COLUMN otherPlaceOfDeath TEXT"
        )
        database.execSQL("ALTER TABLE GENERAL_OPD_ACTIVITY ADD COLUMN village TEXT")
    }
}
