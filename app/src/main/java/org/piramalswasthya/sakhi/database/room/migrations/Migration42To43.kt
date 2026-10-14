package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration42To43 : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {

        if (!tableExists(db, "LEPROSY_SCREENING")) {
            return
        }

        val columns = listOf(
            "leprosySymptoms" to "TEXT",
            "visitLabel" to "TEXT",
            "visitNumber" to "INTEGER",
            "leprosySymptomsPosition" to "INTEGER DEFAULT 1",
            "isConfirmed" to "INTEGER NOT NULL DEFAULT 0",
            "treatmentStartDate" to "INTEGER NOT NULL DEFAULT 0",
            "treatmentEndDate" to "INTEGER NOT NULL DEFAULT 0",
            "mdtBlisterPackRecived" to "TEXT",
            "treatmentStatus" to "TEXT",
            "leprosyState" to "TEXT"
        )

        for ((column, type) in columns) {
            if (!columnExists(db, "LEPROSY_SCREENING", column)) {
                db.execSQL(
                    "ALTER TABLE LEPROSY_SCREENING ADD COLUMN $column $type"
                )
            }
        }
    }
}
