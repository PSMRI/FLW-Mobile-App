package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration18To19 : Migration(18, 19) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS PROFILE_ACTIVITY_new (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT,
                profileImage TEXT NOT NULL,
                village TEXT NOT NULL,
                employeeId INTEGER NOT NULL,
                dob TEXT NOT NULL,
                age INTEGER NOT NULL,
                mobileNumber TEXT NOT NULL,
                alternateMobileNumber TEXT NOT NULL,
                fatherOrSpouseName TEXT NOT NULL,
                dateOfJoining TEXT NOT NULL,
                bankAccount TEXT NOT NULL,
                ifsc TEXT NOT NULL,
                populationCovered INTEGER NOT NULL,
                choName TEXT NOT NULL,
                choMobile TEXT NOT NULL,
                awwName TEXT NOT NULL,
                awwMobile TEXT NOT NULL,
                anm1Name TEXT NOT NULL,
                anm1Mobile TEXT NOT NULL,
                anm2Name TEXT NOT NULL,
                anm2Mobile TEXT NOT NULL,
                abhaNumber TEXT NOT NULL,
                ashaHouseholdRegistration TEXT NOT NULL,
                ashaFamilyMember TEXT NOT NULL,
                providerServiceMapID TEXT NOT NULL,
                isFatherOrSpouse INTEGER NOT NULL DEFAULT 0,
                supervisorName TEXT NOT NULL,
                supervisorMobile TEXT NOT NULL
            )
        """
        )

        if (tableExists(database, "PROFILE_ACTIVITY")) {
            database.execSQL(
                """
                INSERT INTO PROFILE_ACTIVITY_new (
                    id, name, profileImage, village, employeeId, dob, age,
                    mobileNumber, alternateMobileNumber, fatherOrSpouseName,
                    dateOfJoining, bankAccount, ifsc, populationCovered,
                    choName, choMobile, awwName, awwMobile,
                    anm1Name, anm1Mobile, anm2Name, anm2Mobile,
                    abhaNumber, ashaHouseholdRegistration,
                    ashaFamilyMember, providerServiceMapID,
                    isFatherOrSpouse, supervisorName, supervisorMobile
                )
                SELECT
                    id, name, profileImage, village, employeeId, dob, age,
                    mobileNumber, alternateMobileNumber, fatherOrSpouseName,
                    dateOfJoining, bankAccount, ifsc, populationCovered,
                    choName, choMobile, awwName, awwMobile,
                    anm1Name, anm1Mobile, anm2Name, anm2Mobile,
                    abhaNumber, ashaHouseholdRegistration,
                    ashaFamilyMember, providerServiceMapID,
                    isFatherOrSpouse, supervisorName, supervisorMobile
                FROM PROFILE_ACTIVITY
            """
            )
        }

        database.execSQL("DROP TABLE IF EXISTS PROFILE_ACTIVITY")
        database.execSQL("ALTER TABLE PROFILE_ACTIVITY_new RENAME TO PROFILE_ACTIVITY")


        if (tableExists(database, "BENEFICIARY")) {
            val beneficiaryColumns = listOf(
                "isDeath INTEGER NOT NULL DEFAULT 0",
                "isDeathValue TEXT",
                "dateOfDeath TEXT",
                "timeOfDeath TEXT",
                "reasonOfDeath TEXT",
                "reasonOfDeathId INTEGER NOT NULL DEFAULT 0",
                "placeOfDeath TEXT",
                "placeOfDeathId INTEGER NOT NULL DEFAULT 'undefined'",
                "otherPlaceOfDeath TEXT",
                "isConsent INTEGER NOT NULL DEFAULT 0",
                "kid_isConsent INTEGER ",
                "suspectedTb TEXT DEFAULT 'undefined'",
                "kid_childName TEXT DEFAULT 'undefined'",
                "loc_country_nameHindi TEXT DEFAULT 'undefined'",
                "kid_birthOPV INTEGER DEFAULT 'undefined'",
                "kid_opvDate TEXT DEFAULT 'undefined'",
                "kid_conductedDelivery TEXT DEFAULT 'undefined'",
                "kid_conductedDeliveryOther TEXT DEFAULT 'undefined'",
                "kid_conductedDeliveryId INTEGER DEFAULT 'undefined'",
                "suspectedHrp TEXT DEFAULT 'undefined'",
                "familyHeadRelation TEXT DEFAULT 'undefined'",
                "kid_opvGivenDueDate TEXT DEFAULT 'undefined'",
                "kid_childMotherName TEXT DEFAULT 'undefined'",
                "community TEXT DEFAULT 'undefined'",
                "kid_birthPlace TEXT DEFAULT 'undefined'",
                "loc_village_nameHindi TEXT DEFAULT 'undefined'",
                "religionOthers TEXT DEFAULT 'undefined'",
                "kid_typeOfSchool TEXT DEFAULT 'undefined'",
                "hrpLastVisitDate TEXT DEFAULT 'undefined'",
                "kid_typeOfSchoolId INTEGER DEFAULT 'undefined'",
                "kid_complicationsId INTEGER DEFAULT 'undefined'",
                "kid_motherPosition INTEGER DEFAULT 'undefined'",
                "kid_feedingStarted TEXT DEFAULT 'undefined'",
                "hrpIdentificationDate TEXT DEFAULT 'undefined'",
                "kid_gestationalAge TEXT DEFAULT 'undefined'",
                "loc_state_nameHindi TEXT DEFAULT 'undefined'",
                "suspectedNcdDiseases TEXT DEFAULT 'undefined'",
                "kid_bcdBatchNo TEXT DEFAULT 'undefined'",
                "abha_isNewAbha INTEGER DEFAULT 'undefined'",
                "kid_bcgDate TEXT DEFAULT 'undefined'",
                "kid_bcgGivenDueDate TEXT DEFAULT 'undefined'",
                "kid_hptBatchNo TEXT DEFAULT 'undefined'",
                "loc_state_nameAssamese TEXT DEFAULT 'undefined'",
                "gen_maritalStatus TEXT DEFAULT 'undefined'",
                "loc_block_id INTEGER DEFAULT 'undefined'",
                "syncState INTEGER DEFAULT 'undefined'",
                "kid_birthPlaceId INTEGER DEFAULT 'undefined'",
                "kid_birthDosageId INTEGER DEFAULT 'undefined'",
                "abha_healthIdNumber TEXT DEFAULT 'undefined'",
                "loc_country_name TEXT DEFAULT 'undefined'",
                "kid_birthDefectsId INTEGER DEFAULT 'undefined'",
                "kid_vitaminKGivenDueDate TEXT DEFAULT 'undefined'",
                "createdDate INTEGER DEFAULT 'undefined'",
                "kid_birthCertificateFileBackView TEXT DEFAULT 'undefined'",
                "kid_birthCertificateFileFrontView TEXT DEFAULT 'undefined'",
                "kid_hptGivenDueDate TEXT DEFAULT 'undefined'",
                "kid_motherBenId INTEGER DEFAULT 'undefined'",
                "kid_childRegisteredSchoolId INTEGER DEFAULT 'undefined'",
                "kid_birthHepB INTEGER DEFAULT 'undefined'",
                "kid_birthBCG INTEGER DEFAULT 'undefined'",
                "kid_birthCertificateNumber TEXT DEFAULT 'undefined'",
                "kid_birthDosage TEXT DEFAULT 'undefined'",
                "kid_gestationalAgeId INTEGER DEFAULT 'undefined'",
                "kid_facilityId INTEGER DEFAULT 'undefined'",
                "kid_opvBatchNo TEXT DEFAULT 'undefined'",
                "confirmedHrp TEXT DEFAULT 'undefined'",
                "confirmedTb TEXT DEFAULT 'undefined'",
                "kid_corticosteroidGivenMother TEXT DEFAULT 'undefined'",
                "kid_birthDefects TEXT DEFAULT 'undefined'",
                "kid_deliveryTypeOther TEXT DEFAULT 'undefined'",
                "kid_deliveryTypeId INTEGER DEFAULT 'undefined'",
                "kid_term TEXT DEFAULT 'undefined'",
                "kid_facilityName TEXT DEFAULT 'undefined'",
                "kid_hptDate TEXT DEFAULT 'undefined'",
                "kid_vitaminKBatchNo TEXT DEFAULT 'undefined'",
                "confirmedNcdDiseases TEXT DEFAULT 'undefined'",
                "processed TEXT DEFAULT 'undefined'",
                "kid_childRegisteredAWC TEXT DEFAULT 'undefined'",
                "kid_facilityOther TEXT DEFAULT 'undefined'",
                "kid_deliveryType TEXT DEFAULT 'undefined'",
                "kid_heightAtBirth REAL DEFAULT 'undefined'",
                "kid_weightAtBirth REAL DEFAULT 'undefined'",
                "kid_placeName TEXT DEFAULT 'undefined'",
                "tempMobileNoOfRelationId INTEGER NOT NULL DEFAULT 'undefined'"
            )

            for (column in beneficiaryColumns) {
                val columnName = column.split(" ")[0]
                if (!columnExists(database, "BENEFICIARY", columnName)) {
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN $column")
                }
            }
        }

        if (tableExists(database, "PREGNANCY_ANC")) {
            val pregnancyAncColumns = listOf(
                "serialNo TEXT",
                "methodOfTermination TEXT",
                "methodOfTerminationId INTEGER NOT NULL DEFAULT 0",
                "terminationDoneBy TEXT",
                "terminationDoneById INTEGER NOT NULL DEFAULT 0",
                "isPaiucdId INTEGER NOT NULL DEFAULT 0",
                "isPaiucd TEXT",
                "remarks TEXT",
                "abortionImg1 TEXT",
                "abortionImg2 TEXT",
                "placeOfDeath TEXT",
                "placeOfDeathId INTEGER NOT NULL DEFAULT 0",
                "otherPlaceOfDeath TEXT"
            )

            for (column in pregnancyAncColumns) {
                val columnName = column.split(" ")[0]
                if (!columnExists(database, "PREGNANCY_ANC", columnName)) {
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN $column")
                }
            }
        }
    }
}
