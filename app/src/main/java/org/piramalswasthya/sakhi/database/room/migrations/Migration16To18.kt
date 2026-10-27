package org.piramalswasthya.sakhi.database.room.migrations

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration16To18 : Migration(16, 18) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ABHA_GENERATED_NEW` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `beneficiaryID` INTEGER NOT NULL,
                `beneficiaryRegID` INTEGER NOT NULL,
                `benName` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `message` TEXT NOT NULL,
                `txnId` TEXT NOT NULL,
                `benSurname` TEXT,
                `healthId` TEXT NOT NULL,
                `healthIdNumber` TEXT NOT NULL,
                `abhaProfileJson` TEXT NOT NULL,
                `isNewAbha` INTEGER NOT NULL,
                `providerServiceMapId` INTEGER NOT NULL,
                `syncState` INTEGER NOT NULL,
                FOREIGN KEY(`beneficiaryID`) REFERENCES `BENEFICIARY`(`beneficiaryId`) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent()
        )
        try {
            database.execSQL(
                """
                INSERT INTO ABHA_GENERATED_NEW (
                    id,
                    beneficiaryID,
                    beneficiaryRegID,
                    benName,
                    createdBy,
                    message,
                    txnId,
                    benSurname,
                    healthId,
                    healthIdNumber,
                    abhaProfileJson,
                    isNewAbha,
                    providerServiceMapId,
                    syncState
                )
                SELECT
                    id,
                    benId AS beneficiaryID,
                    hhId AS beneficiaryRegID,
                    benName,
                    '' AS createdBy,
                    '' AS message,
                    '' AS txnId,
                    benSurname,
                    healthId,
                    healthIdNumber,
                    '' AS abhaProfileJson,
                    isNewAbha,
                    0,
                    0
                FROM ABHA_GENERATED
            """.trimIndent()
            )
        } catch (e: Exception) {
            Log.w(
                "RoomMigration",
                "Skipping data copy: ABHA_GENERATED table not found",
                e
            )
        }

        try {
            database.execSQL("DROP TABLE IF EXISTS ABHA_GENERATED")
        } catch (_: Exception) {
        }

        database.execSQL("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")

        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ABHA_GENERATED_beneficiaryID` ON `ABHA_GENERATED` (`beneficiaryID`)")
    }
}
