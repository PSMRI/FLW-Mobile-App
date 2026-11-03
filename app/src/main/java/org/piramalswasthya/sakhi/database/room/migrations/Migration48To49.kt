package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration48To49 : Migration(48, 49) {
    override fun migrate(db: SupportSQLiteDatabase) {

        val columns = listOf(
            "villageName TEXT",
            "anm INTEGER DEFAULT 0",
            "aww INTEGER DEFAULT 0",
            "noOfPregnantWomen INTEGER DEFAULT 0",
            "noOfLactatingMother INTEGER DEFAULT 0",
            "noOfCommittee INTEGER DEFAULT 0",
            "followupPrevious INTEGER"
        )

        columns.forEach { columnDef ->
            db.execSQL("ALTER TABLE VHNC ADD COLUMN $columnDef")
        }
    }
}
