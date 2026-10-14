package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration13To14 : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.let {
            it.execSQL("alter table INCENTIVE_ACTIVITY add column fmrCode TEXT")
            it.execSQL("alter table INCENTIVE_ACTIVITY add column fmrCodeOld TEXT")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column systolic INTEGER")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column diastolic INTEGER")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column bloodGlucoseTest TEXT")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column fbg INTEGER")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column rbg INTEGER")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column ppbg INTEGER")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column hemoglobinTest TEXT")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column ifaGiven TEXT")
            it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column ifaQuantity INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column systolic INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column diastolic INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column bloodGlucoseTest TEXT")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column fbg INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column rbg INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column ppbg INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column hemoglobinTest TEXT")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column ifaGiven TEXT")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column ifaQuantity INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column fastingOgtt INTEGER")
            it.execSQL("alter table HRP_PREGNANT_TRACK add column after2hrsOgtt INTEGER")
        }
    }
}
