package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

/**
 * Gamification tables (badges, evening notification, monthly recap).
 *
 * On feature/gamification these were created by a 68 -> 69 step, but release-2.14 already uses
 * 68 -> 69 (TB_SCREENING columns) and has moved on to 77, so the gamification tables are added on
 * top of the release chain here. Every statement is CREATE ... IF NOT EXISTS, so a test device
 * that already has the tables from an earlier gamification build passes through harmlessly.
 *
 * Such a device (gamification build, DB v69) skipped release-2.14's 68 -> 69, so it lacks the two
 * TB_SCREENING columns from [Migration68To69]. They are added here when missing, otherwise Room's
 * schema validation would crash on open. On a normal release device both columns already exist
 * and nothing is executed for them.
 */
object Migration77To78 : Migration(77, 78) {

    override fun migrate(db: SupportSQLiteDatabase) {
        createBadgeTables(db)
        createEveningNotificationTables(db)
        createMonthlyRecapTable(db)
        repairTbScreeningColumns(db)
    }

    private fun createBadgeTables(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BADGE_EARNED` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`userId` INTEGER NOT NULL, " +
                    "`badgeId` TEXT NOT NULL, " +
                    "`level` INTEGER NOT NULL, " +
                    "`caseRef` TEXT NOT NULL, " +
                    "`earnedAt` INTEGER NOT NULL, " +
                    "`synced` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_BADGE_EARNED_userId_badgeId_level_caseRef` " +
                    "ON `BADGE_EARNED` (`userId`, `badgeId`, `level`, `caseRef`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BADGE_STATE` (" +
                    "`userId` INTEGER NOT NULL, " +
                    "`badgeId` TEXT NOT NULL, " +
                    "`currentLevel` INTEGER NOT NULL, " +
                    "`progress` INTEGER NOT NULL, " +
                    "`nextTarget` INTEGER NOT NULL, " +
                    "`streakCount` INTEGER NOT NULL, " +
                    "`graceRemaining` INTEGER NOT NULL, " +
                    "`lastEvaluatedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`userId`, `badgeId`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BADGE_SYNC_LOG` (" +
                    "`weekKey` TEXT NOT NULL, " +
                    "`syncCompletedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`weekKey`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BADGE_STREAK_FREEZE` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`badgeId` TEXT NOT NULL, " +
                    "`startDate` INTEGER NOT NULL, " +
                    "`endDate` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_BADGE_STREAK_FREEZE_badgeId_startDate_endDate` " +
                    "ON `BADGE_STREAK_FREEZE` (`badgeId`, `startDate`, `endDate`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BADGE_CONFIG` (" +
                    "`key` TEXT NOT NULL, " +
                    "`value` TEXT NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`key`))"
        )
    }

    private fun createEveningNotificationTables(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notification_templates` (" +
                    "`templateId` TEXT NOT NULL, " +
                    "`bucket` TEXT NOT NULL, " +
                    "`language` TEXT NOT NULL, " +
                    "`bodyTemplate` TEXT NOT NULL, " +
                    "`libraryVersion` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`templateId`))"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_notification_templates_bucket_language` " +
                    "ON `notification_templates` (`bucket`, `language`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notification_history` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`templateId` TEXT NOT NULL, " +
                    "`shownDate` TEXT NOT NULL, " +
                    "`bucket` TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_notification_history_bucket` " +
                    "ON `notification_history` (`bucket`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `form_save_log` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`formType` TEXT NOT NULL, " +
                    "`beneficiaryId` INTEGER NOT NULL, " +
                    "`ashaWorkerId` INTEGER NOT NULL, " +
                    "`savedAt` INTEGER NOT NULL, " +
                    "`dateKey` TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_form_save_log_beneficiaryId_formType_dateKey` " +
                    "ON `form_save_log` (`beneficiaryId`, `formType`, `dateKey`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_form_save_log_dateKey` " +
                    "ON `form_save_log` (`dateKey`)"
        )
    }

    private fun createMonthlyRecapTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `MONTHLY_RECAP` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`userId` INTEGER NOT NULL, " +
                    "`recapYearMonth` INTEGER NOT NULL, " +
                    "`windowStartMillis` INTEGER NOT NULL, " +
                    "`windowEndMillis` INTEGER NOT NULL, " +
                    "`status` TEXT NOT NULL, " +
                    "`language` TEXT, " +
                    "`variantSeed` INTEGER NOT NULL, " +
                    "`snapshotVersion` INTEGER NOT NULL, " +
                    "`metricsJson` TEXT, " +
                    "`progressScene` INTEGER NOT NULL, " +
                    "`totalScenes` INTEGER, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "`startedAt` INTEGER, " +
                    "`completedAt` INTEGER)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_MONTHLY_RECAP_userId_recapYearMonth` " +
                    "ON `MONTHLY_RECAP` (`userId`, `recapYearMonth`)"
        )
    }

    private fun repairTbScreeningColumns(db: SupportSQLiteDatabase) {
        listOf(
            "failureToGainWeight INTEGER DEFAULT NULL",
            "decreasedActivityOrPlayfulness INTEGER DEFAULT NULL"
        ).forEach { definition ->
            val column = definition.substringBefore(" ")
            if (!columnExists(db, "TB_SCREENING", column)) {
                db.execSQL("ALTER TABLE TB_SCREENING ADD COLUMN $definition")
            }
        }
    }
}
