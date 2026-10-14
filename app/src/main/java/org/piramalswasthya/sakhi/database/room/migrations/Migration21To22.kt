package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration21To22 : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS PREGNANCY_ANC_NEW (
                id INTEGER NOT NULL PRIMARY KEY,
                benId INTEGER NOT NULL,
                visitNumber INTEGER NOT NULL,
                isActive INTEGER NOT NULL,
                ancDate INTEGER NOT NULL,
                isAborted INTEGER NOT NULL,
                abortionType TEXT,
                abortionTypeId INTEGER NOT NULL,
                abortionFacility TEXT,
                abortionFacilityId INTEGER NOT NULL,
                abortionDate INTEGER,
                weight INTEGER,
                bpSystolic INTEGER,
                bpDiastolic INTEGER,
                pulseRate TEXT,
                hb REAL,
                fundalHeight INTEGER,
                urineAlbumin TEXT,
                urineAlbuminId INTEGER NOT NULL,
                randomBloodSugarTest TEXT,
                randomBloodSugarTestId INTEGER NOT NULL,
                numFolicAcidTabGiven INTEGER NOT NULL,
                numIfaAcidTabGiven INTEGER NOT NULL,
                anyHighRisk INTEGER,
                highRisk TEXT,
                highRiskId INTEGER NOT NULL,
                otherHighRisk TEXT,
                referralFacility TEXT,
                referralFacilityId INTEGER NOT NULL,
                hrpConfirmed INTEGER,
                hrpConfirmedBy TEXT,
                hrpConfirmedById INTEGER NOT NULL,
                maternalDeath INTEGER,
                maternalDeathProbableCause TEXT,
                maternalDeathProbableCauseId INTEGER NOT NULL,
                otherMaternalDeathProbableCause TEXT,
                deathDate INTEGER,
                pregnantWomanDelivered INTEGER,
                processed TEXT,
                createdBy TEXT NOT NULL,
                createdDate INTEGER NOT NULL,
                updatedBy TEXT NOT NULL,
                updatedDate INTEGER NOT NULL,
                syncState INTEGER NOT NULL,
                frontFilePath TEXT,
                backFilePath TEXT,
                FOREIGN KEY(benId) REFERENCES BENEFICIARY(beneficiaryId)
                    ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent()
        )

        database.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        if (tableExists(database, "PREGNANCY_ANC")) {
            database.execSQL(
                """
            INSERT INTO PREGNANCY_ANC_NEW (
                id, benId, visitNumber, isActive, ancDate, isAborted,
                abortionType, abortionTypeId, abortionFacility, abortionFacilityId, abortionDate,
                weight, bpSystolic, bpDiastolic, pulseRate, hb, fundalHeight,
                urineAlbumin, urineAlbuminId, randomBloodSugarTest, randomBloodSugarTestId,
                numFolicAcidTabGiven, numIfaAcidTabGiven, anyHighRisk, highRisk, highRiskId,
                otherHighRisk, referralFacility, referralFacilityId,
                hrpConfirmed, hrpConfirmedBy, hrpConfirmedById,
                maternalDeath, maternalDeathProbableCause, maternalDeathProbableCauseId,
                otherMaternalDeathProbableCause, deathDate, pregnantWomanDelivered,
                processed, createdBy, createdDate, updatedBy, updatedDate, syncState,
                frontFilePath, backFilePath
            )
            SELECT
                id, benId, visitNumber, isActive, ancDate, isAborted,
                abortionType, abortionTypeId, abortionFacility, abortionFacilityId, abortionDate,
                weight, bpSystolic, bpDiastolic, pulseRate, hb, fundalHeight,
                urineAlbumin, urineAlbuminId, randomBloodSugarTest, randomBloodSugarTestId,
                numFolicAcidTabGiven, numIfaAcidTabGiven, anyHighRisk, highRisk, highRiskId,
                otherHighRisk, referralFacility, referralFacilityId,
                hrpConfirmed, hrpConfirmedBy, hrpConfirmedById,
                maternalDeath, maternalDeathProbableCause, maternalDeathProbableCauseId,
                otherMaternalDeathProbableCause, deathDate, pregnantWomanDelivered,
                processed, createdBy, createdDate, updatedBy, updatedDate, syncState,
                NULL AS frontFilePath,
                NULL AS backFilePath
            FROM PREGNANCY_ANC
        """.trimIndent()
            )
            database.execSQL("DROP TABLE PREGNANCY_ANC")
        }
        database.execSQL("ALTER TABLE PREGNANCY_ANC_NEW RENAME TO PREGNANCY_ANC")

        database.execSQL("CREATE INDEX IF NOT EXISTS ind_mha ON PREGNANCY_ANC(benId)")

        if (tableExists(database, "DELIVERY_OUTCOME")) {
            if (!columnExists(database, "DELIVERY_OUTCOME", "isDeath")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeath INTEGER DEFAULT 0")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "isDeathValue")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeathValue TEXT")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "dateOfDeath")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN dateOfDeath TEXT")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "placeOfDeath")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeath TEXT")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "placeOfDeathId")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeathId INTEGER DEFAULT 0")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "otherPlaceOfDeath")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN otherPlaceOfDeath TEXT")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "mcp1File")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp1File TEXT")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "mcp2File")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp2File TEXT")
            }
            if (!columnExists(database, "DELIVERY_OUTCOME", "jsyFile")) {
                database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN jsyFile TEXT")
            }
        }
    }
}
