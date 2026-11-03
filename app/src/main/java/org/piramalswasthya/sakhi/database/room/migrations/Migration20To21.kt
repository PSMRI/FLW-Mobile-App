package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration20To21 : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE CDR ADD COLUMN cdr1File TEXT")
        database.execSQL("ALTER TABLE CDR ADD COLUMN cdr2File TEXT")
        database.execSQL("ALTER TABLE CDR ADD COLUMN cdrDeathCertFile TEXT")
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `GENERAL_OPD_ACTIVITY` (
                `benFlowID` INTEGER,
                `beneficiaryRegID` INTEGER,
                `benVisitID` INTEGER,
                `visitCode` INTEGER,
                `benVisitNo` INTEGER,
                `nurseFlag` INTEGER,
                `doctorFlag` INTEGER,
                `pharmacist_flag` INTEGER,
                `lab_technician_flag` INTEGER,
                `radiologist_flag` INTEGER,
                `oncologist_flag` INTEGER,
                `specialist_flag` INTEGER,
                `agentId` TEXT,
                `visitDate` TEXT,
                `modified_by` TEXT,
                `modified_date` TEXT,
                `benName` TEXT,
                `deleted` INTEGER,
                `firstName` TEXT,
                `lastName` TEXT,
                `age` TEXT,
                `ben_age_val` INTEGER,
                `genderID` INTEGER,
                `genderName` TEXT,
                `preferredPhoneNum` TEXT,
                `fatherName` TEXT,
                `spouseName` TEXT,
                `districtName` TEXT,
                `servicePointName` TEXT,
                `registrationDate` TEXT,
                `benVisitDate` TEXT,
                `consultationDate` TEXT,
                `consultantID` INTEGER,
                `consultantName` TEXT,
                `visitSession` TEXT,
                `servicePointID` INTEGER,
                `districtID` INTEGER,
                `villageID` INTEGER,
                `vanID` INTEGER,
                `beneficiaryId` INTEGER NOT NULL,
                `dob` TEXT,
                `tc_SpecialistLabFlag` INTEGER,
                `visitReason` TEXT,
                `visitCategory` TEXT,
                PRIMARY KEY(`beneficiaryId`)
            )
        """
        )
    }
}
