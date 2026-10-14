package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration43To44 : Migration(43, 44) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS LEPROSY_SCREENING (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                houseHoldDetailsId INTEGER NOT NULL,
                visitNumber INTEGER,
                isConfirmed INTEGER NOT NULL,
                homeVisitDate INTEGER NOT NULL,
                leprosyState TEXT,
                leprosyStatusDate INTEGER NOT NULL,
                lerosyStatusPosition INTEGER,
                beneficiaryStatus TEXT,
                beneficiaryStatusId INTEGER,
                dateOfDeath INTEGER NOT NULL,
                placeOfDeath TEXT,
                otherPlaceOfDeath TEXT,
                reasonForDeath TEXT,
                otherReasonForDeath TEXT,
                treatmentStatus TEXT,
                mdtBlisterPackRecived TEXT,
                leprosySymptoms TEXT,
                typeOfLeprosy TEXT,
                leprosySymptomsPosition INTEGER,
                visitLabel TEXT,
                leprosyStatus TEXT,
                referredTo INTEGER,
                referToName TEXT,
                otherReferredTo TEXT,
                treatmentEndDate INTEGER NOT NULL,
                treatmentStartDate INTEGER NOT NULL,
                currentVisitNumber INTEGER NOT NULL,
                totalFollowUpMonthsRequired INTEGER NOT NULL,
                diseaseTypeID INTEGER,
                remarks TEXT,
                syncState INTEGER NOT NULL,
                createdBy TEXT NOT NULL,
                createdDate INTEGER NOT NULL,
                modifiedBy TEXT NOT NULL,
                lastModDate INTEGER NOT NULL,
                FOREIGN KEY(benId) REFERENCES BENEFICIARY(beneficiaryId) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS ind_leprosysn ON LEPROSY_SCREENING (benId)
        """
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS LEPROSY_FOLLOW_UP (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                visitNumber INTEGER NOT NULL,
                followUpDate INTEGER NOT NULL,
                treatmentStatus TEXT,
                mdtBlisterPackReceived TEXT,
                treatmentCompleteDate INTEGER NOT NULL,
                remarks TEXT,
                homeVisitDate INTEGER NOT NULL,
                leprosySymptoms TEXT,
                typeOfLeprosy TEXT,
                leprosySymptomsPosition INTEGER,
                visitLabel TEXT,
                leprosyStatus TEXT,
                referredTo INTEGER,
                referToName TEXT,
                treatmentEndDate INTEGER NOT NULL,
                mdtBlisterPackRecived TEXT,
                treatmentStartDate INTEGER NOT NULL,
                syncState INTEGER NOT NULL,
                createdBy TEXT NOT NULL,
                createdDate INTEGER NOT NULL,
                modifiedBy TEXT NOT NULL,
                lastModDate INTEGER NOT NULL
            )
        """
        )

        database.execSQL("CREATE INDEX IF NOT EXISTS ind_leprosy_followup_ben ON LEPROSY_FOLLOW_UP (benId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS ind_leprosy_followup_visit ON LEPROSY_FOLLOW_UP (benId, visitNumber)")


    }
}
