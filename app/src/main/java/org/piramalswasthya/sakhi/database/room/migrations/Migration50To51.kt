package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration50To51 : Migration(50, 51) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.let {
            it.execSQL("alter table PHCReviewMeeting add column villageName TEXT")
            it.execSQL("alter table PHCReviewMeeting add column mitaninHistory TEXT")
            it.execSQL("alter table PHCReviewMeeting add column mitaninActivityCheckList TEXT")
            it.execSQL("alter table PHCReviewMeeting add column placeId INTEGER DEFAULT 0")
            it.execSQL(
                """
            CREATE UNIQUE INDEX IF NOT EXISTS index_PHCReviewMeeting_id
            ON PHCReviewMeeting(id)
            """.trimIndent()
            )

        }
    }
}
