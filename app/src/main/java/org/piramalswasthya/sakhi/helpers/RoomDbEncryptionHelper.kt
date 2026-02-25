package org.piramalswasthya.sakhi.helpers

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

object RoomDbEncryptionHelper {

    @Volatile
    private var libsLoaded = false

    private fun ensureSqlCipherLoaded() {
        if (!libsLoaded) {
            synchronized(this) {
                if (!libsLoaded) {
                    System.loadLibrary("sqlcipher")
                    libsLoaded = true
                }
            }
        }
    }

    fun encryptIfNeeded(
        context: Context,
        dbName: String,
        passphrase: CharArray
    ) {
        ensureSqlCipherLoaded()

        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) return

        // Already encrypted → nothing to do
        if (canOpenWithKey(dbFile, passphrase)) {
            return
        }

        val tempEncrypted = File(dbFile.parent, "$dbName-encrypted")

        val plainDb = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            "",
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null,
            null
        )

        val encryptedDb = SQLiteDatabase.openDatabase(
            tempEncrypted.absolutePath,
            String(passphrase),
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
            null,
            null
        )

        plainDb.execSQL(
            "ATTACH DATABASE '${tempEncrypted.absolutePath}' AS encrypted KEY '${String(passphrase)}'"
        )
        plainDb.execSQL("SELECT sqlcipher_export('encrypted')")
        plainDb.execSQL("DETACH DATABASE encrypted")

        plainDb.close()
        encryptedDb.close()

        dbFile.delete()
        tempEncrypted.renameTo(dbFile)

        dropRoomOwnedViews(dbFile, passphrase)
    }

    private fun canOpenWithKey(dbFile: File, passphrase: CharArray): Boolean {
        return try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                String(passphrase),
                null,
                SQLiteDatabase.OPEN_READONLY,
                null,
                null
            ).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Room owns BEN_BASIC_CACHE (declared in @Database.views)
     * So DB must NOT contain it before Room onCreate().
     */
    private fun dropRoomOwnedViews(dbFile: File, passphrase: CharArray) {
        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            String(passphrase),
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null,
            null
        )

        db.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")

        db.close()
    }
}
