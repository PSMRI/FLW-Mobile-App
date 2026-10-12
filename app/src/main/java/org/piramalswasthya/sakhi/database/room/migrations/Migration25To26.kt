package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration25To26 : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        if (tableExists(database, "PREGNANCY_ANC")) {
            if (!columnExists(database, "PREGNANCY_ANC", "lmpDate")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN lmpDate INTEGER")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "visitDate")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN visitDate INTEGER")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "weekOfPregnancy")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN weekOfPregnancy INTEGER")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "placeOfDeath")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfDeath TEXT")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "placeOfDeathId")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfDeathId INTEGER")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "otherPlaceOfDeath")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN otherPlaceOfDeath TEXT")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "methodOfTermination")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN methodOfTermination TEXT")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "methodOfTerminationId")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN methodOfTerminationId INTEGER")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "terminationDoneBy")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN terminationDoneBy TEXT")
            }
            if (!columnExists(database, "PREGNANCY_ANC", "terminationDoneById")) {
                database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN terminationDoneById INTEGER")
            }
        }
    }
}
