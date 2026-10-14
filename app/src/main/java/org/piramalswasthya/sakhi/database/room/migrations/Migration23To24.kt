package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration23To24 : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE MDSR ADD COLUMN mdsr1File TEXT")
        database.execSQL("ALTER TABLE MDSR ADD COLUMN mdsr2File TEXT")
        database.execSQL("ALTER TABLE MDSR ADD COLUMN mdsrDeathCertFile TEXT")
    }
}
