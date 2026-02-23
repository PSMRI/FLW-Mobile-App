package org.piramalswasthya.sakhi.helpers

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

object RoomDbEncryptionHelper {

    @Volatile
    private var libsLoaded = false

    private fun ensureSqlCipherLoaded(context: Context) {
        if (!libsLoaded) {
            synchronized(this) {
                if (!libsLoaded) {
                    SQLiteDatabase.loadLibs(context)
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
        ensureSqlCipherLoaded(context)

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
            SQLiteDatabase.OPEN_READWRITE
        )

        val encryptedDb = SQLiteDatabase.openDatabase(
            tempEncrypted.absolutePath,
            passphrase,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY
        )

        plainDb.rawExecSQL(
            "ATTACH DATABASE '${tempEncrypted.absolutePath}' AS encrypted KEY '${String(passphrase)}'"
        )
        plainDb.rawExecSQL("SELECT sqlcipher_export('encrypted')")
        plainDb.rawExecSQL("DETACH DATABASE encrypted")

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
                passphrase,
                null,
                SQLiteDatabase.OPEN_READONLY
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
            passphrase,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        db.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")

        db.close()
    }
}