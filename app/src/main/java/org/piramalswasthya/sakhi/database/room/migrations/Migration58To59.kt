package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration58To59 : Migration(58, 59) {
    override fun migrate(database: SupportSQLiteDatabase) {


        try {
            database.execSQL(
                "ALTER TABLE HOUSEHOLD ADD COLUMN loc_country_nameBangla TEXT"
            )
        } catch (_: Exception) {
        }

        try {
            database.execSQL(
                "ALTER TABLE HOUSEHOLD ADD COLUMN loc_state_nameBangla TEXT"
            )
        } catch (_: Exception) {
        }

        try {
            database.execSQL(
                "ALTER TABLE HOUSEHOLD ADD COLUMN loc_district_nameBangla TEXT"
            )
        } catch (_: Exception) {
        }

        try {
            database.execSQL(
                "ALTER TABLE HOUSEHOLD ADD COLUMN loc_block_nameBangla TEXT"
            )
        } catch (_: Exception) {
        }

        try {
            database.execSQL(
                "ALTER TABLE HOUSEHOLD ADD COLUMN loc_village_nameBangla TEXT"
            )
        } catch (_: Exception) {
        }
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN loc_district_nameBangla TEXT")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN loc_village_nameBangla TEXT")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN loc_country_nameBangla TEXT")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN loc_block_nameBangla TEXT")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN loc_state_nameBangla TEXT")

    }
}
