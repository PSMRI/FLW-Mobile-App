package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration47To48 : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.let {
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentUlcerationId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentTinglingId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN hypopigmentedPatchId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN thickenedSkinId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinNodulesId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinPatchDiscolorationId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentNumbnessId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN clawingFingersId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN tinglingNumbnessExtremitiesId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN inabilityCloseEyelidId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN difficultyHoldingObjectsId INTEGER DEFAULT 1")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN weaknessFeetId INTEGER DEFAULT 1")

            // ===== Symptom String fields =====
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentUlceration TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentTingling TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN hypopigmentedPatch TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN thickenedSkin TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinNodules TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinPatchDiscoloration TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentNumbness TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN clawingFingers TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN tinglingNumbnessExtremities TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN inabilityCloseEyelid TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN difficultyHoldingObjects TEXT")
            it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN weaknessFeet TEXT")

        }
    }
}
