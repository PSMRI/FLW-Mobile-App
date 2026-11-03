package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.recreateBenBasicCacheView

object Migration64To65 : Migration(64, 65) {
    override fun migrate(database: SupportSQLiteDatabase) {
        recreateBenBasicCacheView(database)
    }
}
