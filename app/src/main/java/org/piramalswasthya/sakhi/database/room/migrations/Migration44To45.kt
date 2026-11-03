package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration44To45 : Migration(44, 45) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isSpouseAdded INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isChildrenAdded INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isMarried INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN noOfChildren INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN noOfAliveChildren INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN doYouHavechildren INTEGER NOT NULL DEFAULT 0")

        database.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        database.execSQL(
            "CREATE VIEW `BEN_BASIC_CACHE` AS " +
                    "SELECT b.beneficiaryId as benId,b.isMarried, b.noOfAliveChildren, b.noOfChildren, b.doYouHavechildren, b.isConsent as isConsent, b.motherName as motherName, b.householdId as hhId, b.regDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob, b.isDeath,b.isDeathValue,b.dateOfDeath,b.timeOfDeath,b.reasonOfDeath,b.reasonOfDeathId,b.placeOfDeath,b.placeOfDeathId,b.otherPlaceOfDeath,b.isSpouseAdded,b.isChildrenAdded, b.familyHeadRelationPosition as relToHeadId" +
                    ", b.contactNumber as mobileNo, b.fatherName, h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod" +
                    ", b.isHrpStatus as hrpStatus, b.syncState, b.gen_reproductiveStatusId as reproductiveStatusId, b.isKid, b.immunizationStatus" +
                    ", b.loc_village_id as villageId, b.abha_healthIdNumber as abhaId" +
                    ", b.isNewAbha" + // FIX: Using only one, correct source for isNewAbha.
                    ", IFNULL(cbac.benId IS NOT NULL, 0) as cbacFilled, cbac.syncState as cbacSyncState" +
                    ", IFNULL(cdr.benId IS NOT NULL, 0) as cdrFilled, cdr.syncState as cdrSyncState" +
                    ", IFNULL(mdsr.benId IS NOT NULL, 0) as mdsrFilled, mdsr.syncState as mdsrSyncState" +
                    ", IFNULL(pmsma.benId IS NOT NULL, 0) as pmsmaFilled, pmsma.syncState as pmsmaSyncState" +
                    ", IFNULL(hbnc.benId IS NOT NULL, 0) as hbncFilled" +
                    ", IFNULL(hbyc.benId IS NOT NULL, 0) as hbycFilled" +
                    ", IFNULL(pwr.benId IS NOT NULL, 0) as pwrFilled, pwr.syncState as pwrSyncState" +
                    ", IFNULL(pwa.pregnantWomanDelivered, 0) as isDelivered, IFNULL(pwa.hrpConfirmed, 0) as pwHrp" +
                    ", IFNULL(ecr.benId IS NOT NULL, 0) as ecrFilled" +
                    ", IFNULL(ect.benId IS NOT NULL, 0) as ectFilled" + // FIX: Removed duplicate ectFilled and used a safe version.
                    ", IFNULL((pwa.maternalDeath OR do.complication = 'DEATH' OR pnc.motherDeath), 0) as isMdsr" +
                    ", IFNULL(tbsn.benId IS NOT NULL, 0) as tbsnFilled, tbsn.syncState as tbsnSyncState" +
                    ", IFNULL(tbsp.benId IS NOT NULL, 0) as tbspFilled, tbsp.syncState as tbspSyncState" +
                    ", IFNULL(ir.motherBenId IS NOT NULL, 0) as irFilled, ir.syncState as irSyncState" +
                    ", IFNULL(cr.motherBenId IS NOT NULL, 0) as crFilled, cr.syncState as crSyncState" +
                    ", IFNULL(do.benId IS NOT NULL, 0) as doFilled, do.syncState as doSyncState" +
                    ", IFNULL((hrppa.benId IS NOT NULL AND hrppa.noOfDeliveries IS NOT NULL AND hrppa.timeLessThan18m IS NOT NULL AND hrppa.heightShort IS NOT NULL AND hrppa.age IS NOT NULL AND hrppa.rhNegative IS NOT NULL AND hrppa.homeDelivery IS NOT NULL AND hrppa.badObstetric IS NOT NULL AND hrppa.multiplePregnancy IS NOT NULL), 0) as hrppaFilled, hrppa.syncState as hrppaSyncState" +
                    ", IFNULL((hrpnpa.benId IS NOT NULL AND hrpnpa.noOfDeliveries IS NOT NULL AND hrpnpa.timeLessThan18m IS NOT NULL AND hrpnpa.heightShort IS NOT NULL AND hrpnpa.age IS NOT NULL AND hrpnpa.misCarriage IS NOT NULL AND hrpnpa.homeDelivery IS NOT NULL AND hrpnpa.medicalIssues IS NOT NULL AND hrpnpa.pastCSection IS NOT NULL), 0) as hrpnpaFilled, hrpnpa.syncState as hrpnpaSyncState" +
                    ", IFNULL(hrpmbp.benId IS NOT NULL, 0) as hrpmbpFilled, hrpmbp.syncState as hrpmbpSyncState" +
                    ", IFNULL(hrpt.benId IS NOT NULL, 0) as hrptFilled, IFNULL(((count(distinct hrpt.id) > 3) OR (((JulianDay('now')) - JulianDay(date(max(hrpt.visitDate)/1000,'unixepoch','localtime'))) < 1)), 0) as hrptrackingDone, hrpt.syncState as hrptSyncState" +
                    ", IFNULL(hrnpt.benId IS NOT NULL, 0) as hrnptFilled, IFNULL(((JulianDay('now') - JulianDay(date(max(hrnpt.visitDate)/1000,'unixepoch','localtime'))) < 1), 0) as hrnptrackingDone, hrnpt.syncState as hrnptSyncState " +
                    "FROM BENEFICIARY b " +
                    "JOIN HOUSEHOLD h ON b.householdId = h.householdId " +
                    "LEFT OUTER JOIN CBAC cbac ON b.beneficiaryId = cbac.benId " +
                    "LEFT OUTER JOIN CDR cdr ON b.beneficiaryId = cdr.benId " +
                    "LEFT OUTER JOIN MDSR mdsr ON b.beneficiaryId = mdsr.benId " +
                    "LEFT OUTER JOIN PMSMA pmsma ON b.beneficiaryId = pmsma.benId " +
                    "LEFT OUTER JOIN HBNC hbnc ON b.beneficiaryId = hbnc.benId " +
                    "LEFT OUTER JOIN HBYC hbyc ON b.beneficiaryId = hbyc.benId " +
                    "LEFT OUTER JOIN PREGNANCY_REGISTER pwr ON b.beneficiaryId = pwr.benId " +
                    "LEFT OUTER JOIN PREGNANCY_ANC pwa ON b.beneficiaryId = pwa.benId " +
                    "LEFT OUTER JOIN pnc_visit pnc ON b.beneficiaryId = pnc.benId " +
                    "LEFT OUTER JOIN ELIGIBLE_COUPLE_REG ecr ON b.beneficiaryId = ecr.benId " +
                    "LEFT OUTER JOIN ELIGIBLE_COUPLE_TRACKING ect ON (b.beneficiaryId = ect.benId AND CAST((strftime('%s','now') - ect.visitDate/1000)/60/60/24 AS INTEGER) < 30) " +
                    "LEFT OUTER JOIN TB_SCREENING tbsn ON b.beneficiaryId = tbsn.benId " +
                    "LEFT OUTER JOIN TB_SUSPECTED tbsp ON b.beneficiaryId = tbsp.benId " +
                    "LEFT OUTER JOIN MALARIA_SCREENING masp on b.beneficiaryId = masp.benId " +
                    "LEFT OUTER JOIN MALARIA_CONFIRMED macp on b.beneficiaryId = macp.benId " +
                    "LEFT OUTER JOIN HRP_PREGNANT_ASSESS hrppa ON b.beneficiaryId = hrppa.benId " +
                    "LEFT OUTER JOIN HRP_NON_PREGNANT_ASSESS hrpnpa ON b.beneficiaryId = hrpnpa.benId " +
                    "LEFT OUTER JOIN HRP_MICRO_BIRTH_PLAN hrpmbp ON b.beneficiaryId = hrpmbp.benId " +
                    "LEFT OUTER JOIN HRP_NON_PREGNANT_TRACK hrnpt ON b.beneficiaryId = hrnpt.benId " +
                    "LEFT OUTER JOIN HRP_PREGNANT_TRACK hrpt ON b.beneficiaryId = hrpt.benId " +
                    "LEFT OUTER JOIN DELIVERY_OUTCOME do ON b.beneficiaryId = do.benId " +
                    "LEFT OUTER JOIN INFANT_REG ir ON b.beneficiaryId = ir.motherBenId " +
                    "LEFT OUTER JOIN CHILD_REG cr ON b.beneficiaryId = cr.motherBenId " +
                    "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC"
        )

        database.execSQL("""
            UPDATE BENEFICIARY 
            SET isMarried = CASE 
                WHEN gen_maritalStatusId = 2 THEN 1 
                ELSE 0 
            END
        """.trimIndent())

        fun migrateTable(
            tableName: String,
            criticalColumns: List<String>,
            createTableColumns: String
        ) {
            val cursor = database.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'"
            )
            val tableExists = cursor.moveToFirst()
            cursor.close()

            if (tableExists) {
                val columnsCursor = database.query("PRAGMA table_info($tableName)")
                val existingColumns = mutableSetOf<String>()
                while (columnsCursor.moveToNext()) {
                    existingColumns.add(
                        columnsCursor.getString(
                            columnsCursor.getColumnIndexOrThrow(
                                "name"
                            )
                        )
                    )
                }
                columnsCursor.close()

                if (!existingColumns.contains("image2")) {
                    database.execSQL("ALTER TABLE $tableName ADD COLUMN image2 TEXT")
                }
                if (!existingColumns.contains("syncState")) {
                    database.execSQL("ALTER TABLE $tableName ADD COLUMN syncState INTEGER NOT NULL DEFAULT 0")
                }

                val missingCritical =
                    criticalColumns.filter { !existingColumns.contains(it) }

                if (missingCritical.isNotEmpty()) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS ${tableName}_temp ($createTableColumns)")

                    val copyColumns = existingColumns.intersect(criticalColumns.toSet())
                    if (copyColumns.isNotEmpty()) {
                        database.execSQL(
                            """
                            INSERT INTO ${tableName}_temp (${copyColumns.joinToString(",")})
                            SELECT ${copyColumns.joinToString(",")} FROM $tableName
                        """.trimIndent()
                        )
                    }

                    database.execSQL("DROP TABLE $tableName")
                    database.execSQL("ALTER TABLE ${tableName}_temp RENAME TO $tableName")
                }

            } else {
                database.execSQL("CREATE TABLE IF NOT EXISTS $tableName ($createTableColumns)")
            }
        }


        migrateTable(
            tableName = "PHCReviewMeeting",
            criticalColumns = listOf(
                "id",
                "phcReviewDate",
                "place",
                "noOfBeneficiariesAttended",
                "image1"
            ),
            createTableColumns = """
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                phcReviewDate TEXT NOT NULL,
                place TEXT,
                noOfBeneficiariesAttended INTEGER,
                image1 TEXT,
                image2 TEXT,
                syncState INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        migrateTable(
            tableName = "VHND",
            criticalColumns = listOf(
                "id",
                "vhndDate",
                "place",
                "noOfBeneficiariesAttended",
                "image1"
            ),
            createTableColumns = """
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                vhndDate TEXT NOT NULL,
                place TEXT,
                noOfBeneficiariesAttended INTEGER,
                image1 TEXT,
                image2 TEXT,
                syncState INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        migrateTable(
            tableName = "VHNC",
            criticalColumns = listOf(
                "id",
                "vhncDate",
                "place",
                "noOfBeneficiariesAttended",
                "image1"
            ),
            createTableColumns = """
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                vhncDate TEXT NOT NULL,
                place TEXT,
                noOfBeneficiariesAttended INTEGER,
                image1 TEXT,
                image2 TEXT,
                syncState INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        migrateTable(
            tableName = "AHDMeeting",
            criticalColumns = listOf(
                "id",
                "mobilizedForAHD",
                "ahdPlace",
                "ahdDate",
                "image1"
            ),
            createTableColumns = """
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                mobilizedForAHD TEXT,
                ahdPlace TEXT,
                ahdDate TEXT,
                image1 TEXT,
                image2 TEXT,
                syncState INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        migrateTable(
            tableName = "DewormingMeeting",
            criticalColumns = listOf(
                "id",
                "dewormingDone",
                "dewormingDate",
                "dewormingLocation",
                "ageGroup",
                "image1"
            ),
            createTableColumns = """
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dewormingDone TEXT,
                dewormingDate TEXT,
                dewormingLocation TEXT,
                ageGroup INTEGER,
                image1 TEXT,
                image2 TEXT,
                regDate TEXT,
                syncState INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        migrateTable(
            tableName = "NCD_REFER",
            criticalColumns = listOf(
                "id",
                "benId",
                "referredToInstituteID",
                "refrredToAdditionalServiceList",
                "referredToInstituteName",
                "referralReason",
                "revisitDate",
                "vanID",
                "parkingPlaceID",
                "beneficiaryRegID",
                "benVisitID",
                "visitCode",
                "providerServiceMapID",
                "createdBy",
                "isSpecialist",
                "syncState"
            ),
            createTableColumns = """
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                referredToInstituteID INTEGER DEFAULT 0,
                refrredToAdditionalServiceList TEXT DEFAULT 'undefined',
                referredToInstituteName TEXT DEFAULT 'undefined',
                referralReason TEXT DEFAULT 'undefined',
                revisitDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                vanID INTEGER DEFAULT 0,
                parkingPlaceID INTEGER DEFAULT 0,
                beneficiaryRegID INTEGER DEFAULT 0,
                benVisitID INTEGER DEFAULT 0,
                visitCode INTEGER DEFAULT 0,
                providerServiceMapID INTEGER DEFAULT 0,
                createdBy TEXT DEFAULT '',
                isSpecialist INTEGER DEFAULT 0,
                syncState INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(benId)
                    REFERENCES BENEFICIARY(beneficiaryId)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS ind_refcache
            ON NCD_REFER (benId)
        """
        )


        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS MALARIA_SCREENING (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        benId INTEGER NOT NULL,
        visitId INTEGER NOT NULL,
        houseHoldDetailsId INTEGER NOT NULL,

        caseDate INTEGER NOT NULL,
        screeningDate INTEGER NOT NULL,
        dateOfDeath INTEGER NOT NULL,
        dateOfRdt INTEGER NOT NULL,
        dateOfSlideTest INTEGER NOT NULL,
        dateOfVisitBySupervisor INTEGER NOT NULL,
        followUpDate INTEGER NOT NULL,

        beneficiaryStatus TEXT,
        beneficiaryStatusId INTEGER NOT NULL,

        placeOfDeath TEXT,
        otherPlaceOfDeath TEXT,
        reasonForDeath TEXT,
        otherReasonForDeath TEXT,

        rapidDiagnosticTest TEXT,
        slideTestPf TEXT,
        slideTestPv TEXT,
        slideTestName TEXT,

        caseStatus TEXT,
        referredTo INTEGER,
        referToName TEXT,
        otherReferredFacility TEXT,
        remarks TEXT,

        diseaseTypeID INTEGER,
        malariaTestType INTEGER,
        malariaSlideTestType INTEGER,

        feverMoreThanTwoWeeks INTEGER,
        fluLikeIllness INTEGER,
        shakingChills INTEGER,
        headache INTEGER,
        muscleAches INTEGER,
        tiredness INTEGER,
        nausea INTEGER,
        vomiting INTEGER,
        diarrhea INTEGER,

        createdBy TEXT,
        syncState INTEGER NOT NULL DEFAULT 0,

        FOREIGN KEY(benId)
            REFERENCES BENEFICIARY(beneficiaryId)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    )
"""
        )

        database.execSQL(
            """
    CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn
    ON MALARIA_SCREENING (benId, visitId)
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS AES_SCREENING (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        benId INTEGER NOT NULL,
        houseHoldDetailsId INTEGER NOT NULL,

        visitDate INTEGER NOT NULL,
        createdDate INTEGER NOT NULL,
        dateOfDeath INTEGER NOT NULL,

        beneficiaryStatus TEXT,
        beneficiaryStatusId INTEGER NOT NULL,

        placeOfDeath TEXT,
        otherPlaceOfDeath TEXT,
        reasonForDeath TEXT,
        otherReasonForDeath TEXT,

        aesJeCaseStatus TEXT,

        referredTo INTEGER,
        referToName TEXT,
        otherReferredFacility TEXT,

        diseaseTypeID INTEGER,
        followUpPoint INTEGER,

        createdBy TEXT,
        syncState INTEGER NOT NULL DEFAULT 0,

        FOREIGN KEY(benId)
            REFERENCES BENEFICIARY(beneficiaryId)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    )
"""
        )

        database.execSQL(
            """
    CREATE INDEX IF NOT EXISTS ind_aessn
    ON AES_SCREENING (benId)
"""
        )


        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS KALAZAR_SCREENING (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        benId INTEGER NOT NULL,
        houseHoldDetailsId INTEGER NOT NULL,

        visitDate INTEGER NOT NULL,
        createdDate INTEGER NOT NULL,
        dateOfDeath INTEGER NOT NULL,
        dateOfRdt INTEGER NOT NULL,

        beneficiaryStatus TEXT,
        beneficiaryStatusId INTEGER NOT NULL,

        placeOfDeath TEXT,
        otherPlaceOfDeath TEXT,
        reasonForDeath TEXT,
        otherReasonForDeath TEXT,

        rapidDiagnosticTest TEXT,
        kalaAzarCaseStatus TEXT,

        referredTo INTEGER,
        referToName TEXT,
        otherReferredFacility TEXT,

        diseaseTypeID INTEGER,
        followUpPoint INTEGER,

        createdBy TEXT,
        syncState INTEGER NOT NULL DEFAULT 0,

        FOREIGN KEY(benId)
            REFERENCES BENEFICIARY(beneficiaryId)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    )
"""
        )

        database.execSQL(
            """
    CREATE INDEX IF NOT EXISTS ind_kalazarsn
    ON KALAZAR_SCREENING (benId)
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS FILARIA_SCREENING (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        benId INTEGER NOT NULL,
        houseHoldDetailsId INTEGER NOT NULL,

        mdaHomeVisitDate INTEGER NOT NULL,
        createdDate INTEGER NOT NULL,

        sufferingFromFilariasis INTEGER,
        doseStatus TEXT,
        affectedBodyPart TEXT,
        otherDoseStatusDetails TEXT,
        filariasisCaseCount TEXT,

        medicineSideEffect TEXT,
        otherSideEffectDetails TEXT,

        diseaseTypeID INTEGER,
        createdBy TEXT,

        syncState INTEGER NOT NULL DEFAULT 0,

        FOREIGN KEY(benId)
            REFERENCES BENEFICIARY(beneficiaryId)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    )
"""
        )

        database.execSQL(
            """
    CREATE INDEX IF NOT EXISTS ind_filariasn
    ON FILARIA_SCREENING (benId)
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS MALARIA_CONFIRMED (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        diseaseId INTEGER NOT NULL,
        benId INTEGER NOT NULL,
        houseHoldDetailsId INTEGER NOT NULL,

        dateOfDiagnosis INTEGER NOT NULL,
        treatmentStartDate INTEGER NOT NULL,
        treatmentCompletionDate INTEGER NOT NULL,

        treatmentGiven TEXT,
        referralDate INTEGER NOT NULL,
        day TEXT,

        syncState INTEGER NOT NULL DEFAULT 0,

        FOREIGN KEY(benId)
            REFERENCES BENEFICIARY(beneficiaryId)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    )
"""
        )

        database.execSQL(
            """
    CREATE INDEX IF NOT EXISTS ind_malariacs
    ON MALARIA_CONFIRMED (benId)
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS IRS_ROUND (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        date INTEGER NOT NULL,
        rounds INTEGER NOT NULL,
        householdId INTEGER NOT NULL
    )
"""
        )

        database.execSQL(
            """
    CREATE INDEX IF NOT EXISTS ind_irs_round
    ON IRS_ROUND (householdId)
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS Adolescent_Health_Form_Data (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        userID INTEGER,
        benId INTEGER,

        visitDate INTEGER NOT NULL,
        healthStatus TEXT,
        ifaTabletDistributed INTEGER,
        quantityOfIfaTablets INTEGER,
        menstrualHygieneAwarenessGiven INTEGER,
        sanitaryNapkinDistributed INTEGER,
        noOfPacketsDistributed INTEGER,
        place TEXT,
        distributionDate INTEGER NOT NULL,
        referredToHealthFacility TEXT,
        counselingProvided INTEGER,
        counselingType TEXT,
        followUpDate INTEGER NOT NULL,

        referralStatus TEXT,
        syncState INTEGER NOT NULL DEFAULT 0,

        FOREIGN KEY(benId)
            REFERENCES BENEFICIARY(beneficiaryId)
            ON UPDATE CASCADE
            ON DELETE CASCADE
    )
"""
        )

        database.execSQL(
            """
    CREATE INDEX IF NOT EXISTS ind_adolescentsn
    ON Adolescent_Health_Form_Data (benId)
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS infant (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        rchId TEXT NOT NULL,
        name TEXT NOT NULL,
        motherName TEXT NOT NULL,
        fatherName TEXT,
        dob TEXT NOT NULL,
        gender TEXT NOT NULL,
        phoneNumber TEXT NOT NULL,
        sncuDischarged INTEGER NOT NULL DEFAULT 0
    )
"""
        )

        database.execSQL(
            """
    CREATE TABLE IF NOT EXISTS SAAS_BAHU_ACTIVITY (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,

        ashaId INTEGER NOT NULL,
        place TEXT,
        participants INTEGER,
        date INTEGER,

        sammelanImages TEXT,
        syncState INTEGER NOT NULL DEFAULT 0
    )
"""
        )
    }
}
