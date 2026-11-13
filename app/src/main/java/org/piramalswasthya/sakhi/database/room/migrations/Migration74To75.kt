package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

// General Check-up follow-up per referral (FLW-1229): a beneficiary with two referrals (Cold and
// Fever) gets one follow-up each, keyed like NCD_REFER (benId, referralReason, month). Old rows
// are linked when the beneficiary has exactly one General Check-up referral.
object Migration74To75 : Migration(74, 75) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Migration73To74.migrate(database)
        if (!columnExists(database, "gc_referral_follow_up", "referralReason")) {
            database.execSQL("ALTER TABLE gc_referral_follow_up ADD COLUMN referralReason TEXT NOT NULL DEFAULT ''")
        }
        if (!columnExists(database, "gc_referral_follow_up", "referralMonth")) {
            database.execSQL("ALTER TABLE gc_referral_follow_up ADD COLUMN referralMonth INTEGER NOT NULL DEFAULT 0")
        }
        val gcReferrals = "FROM NCD_REFER r WHERE r.benId = gc_referral_follow_up.benId " +
                "AND (r.type = 'GENERAL_CHECKUP' OR r.referralReason LIKE 'General Check-up: %')"
        database.execSQL(
            "UPDATE gc_referral_follow_up SET " +
                    "referralReason = (SELECT COALESCE(r.referralReason, '') $gcReferrals), " +
                    "referralMonth = (SELECT r.revisitMonth $gcReferrals) " +
                    "WHERE (SELECT COUNT(*) $gcReferrals) = 1"
        )
        // A follow-up saved on this phone kept the referral date; use it when there are several referrals.
        val sameDay = "$gcReferrals AND strftime('%Y-%m-%d', r.revisitDate / 1000, 'unixepoch', 'localtime') = " +
                "gc_referral_follow_up.referredOnDate"
        database.execSQL(
            "UPDATE gc_referral_follow_up SET " +
                    "referralReason = (SELECT COALESCE(r.referralReason, '') $sameDay), " +
                    "referralMonth = (SELECT r.revisitMonth $sameDay) " +
                    "WHERE referralReason = '' AND (SELECT COUNT(*) $sameDay) = 1"
        )
        // Several unlinked rows of one beneficiary would collide on the new key. Keep one: an
        // unsynced row first (not on the server yet), then the latest.
        database.execSQL(
            "DELETE FROM gc_referral_follow_up WHERE EXISTS (SELECT 1 FROM gc_referral_follow_up o " +
                    "WHERE o.benId = gc_referral_follow_up.benId " +
                    "AND o.referralReason = gc_referral_follow_up.referralReason " +
                    "AND o.referralMonth = gc_referral_follow_up.referralMonth " +
                    "AND (o.isSynced < gc_referral_follow_up.isSynced " +
                    "OR (o.isSynced = gc_referral_follow_up.isSynced AND o.id > gc_referral_follow_up.id)))"
        )
        database.execSQL("DROP INDEX IF EXISTS `index_gc_referral_follow_up_benId_followUpDate`")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_gc_referral_follow_up_benId_referralReason_referralMonth` " +
                    "ON `gc_referral_follow_up` (`benId`, `referralReason`, `referralMonth`)"
        )
    }
}
