package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration40To41 : Migration(40, 41) {
    override fun migrate(db: SupportSQLiteDatabase) {

        if (tableExists(db, "PREGNANCY_ANC")) {

            if (!columnExists(db, "PREGNANCY_ANC", "isYesOrNo")) {
                db.execSQL(
                    "ALTER TABLE PREGNANCY_ANC ADD COLUMN isYesOrNo INTEGER "
                )
            }

            if (!columnExists(db, "PREGNANCY_ANC", "dateSterilisation")) {
                db.execSQL(
                    "ALTER TABLE PREGNANCY_ANC ADD COLUMN dateSterilisation INTEGER"
                )
            }

            if (columnExists(db, "PREGNANCY_ANC", "isPaiucdId")) {
                db.execSQL(
                    """
                    UPDATE PREGNANCY_ANC
                    SET isPaiucdId = CASE
                        WHEN isPaiucdId = 1 THEN 1
                        ELSE 0
                    END
                    """.trimIndent()
                )
            }

            db.execSQL(
                "ALTER TABLE PREGNANCY_ANC ADD COLUMN isPaiucd TEXT"
            )

            db.execSQL(
                "ALTER TABLE PREGNANCY_ANC ADD COLUMN remarks TEXT"
            )
            db.execSQL(
                "ALTER TABLE PREGNANCY_ANC ADD COLUMN isPaiucdId INTEGER"
            )
            db.execSQL(
                "ALTER TABLE PREGNANCY_ANC ADD COLUMN serialNo TEXT "
            )
            db.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg1 TEXT")
            db.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg2 TEXT")


        }

    }
}
