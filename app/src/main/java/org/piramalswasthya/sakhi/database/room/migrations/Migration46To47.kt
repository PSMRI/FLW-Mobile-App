package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration46To47 : Migration(46, 47) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.let {
            it.execSQL(
                """ALTER TABLE NCD_REFER 
                    ADD COLUMN type TEXT
                    """.trimIndent()
            )

        }
    }
}
