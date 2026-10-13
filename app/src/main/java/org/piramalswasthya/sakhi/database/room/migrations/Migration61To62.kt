package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration61To62 : Migration(61, 62) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            database.execSQL(
                """
                            CREATE TABLE IF NOT EXISTS `NOTIFICATION` (
                                `notificationId` INTEGER NOT NULL,
                                `userId` INTEGER NOT NULL,
                                `role` TEXT,
                                `eventType` TEXT NOT NULL,
                                `navId` TEXT,
                                `title` TEXT NOT NULL,
                                `body` TEXT NOT NULL,
                                `priority` TEXT,
                                `createdTs` INTEGER NOT NULL,
                                `read` INTEGER NOT NULL,
                                `cleared` INTEGER NOT NULL,
                                `viewed` INTEGER NOT NULL,
                                `senderUserId` INTEGER,
                                `receiverUserId` INTEGER,
                                `beneficiaryId` INTEGER,
                                `activityId` INTEGER,
                                `referenceId` INTEGER,
                                PRIMARY KEY(`notificationId`)
                            )
                            """.trimIndent()
            )
        } catch (_: Exception) {
        }
    }
}
