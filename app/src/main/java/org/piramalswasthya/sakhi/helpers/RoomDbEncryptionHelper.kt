package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.io.FileInputStream

object RoomDbEncryptionHelper {

    private const val TAG = "RoomDbEncryptionHelper"
    private val SQLITE_MAGIC = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

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


    private fun isPlainSqlite(dbFile: File): Boolean {
        if (!dbFile.exists() || dbFile.length() < SQLITE_MAGIC.size) return false
        return try {
            val header = ByteArray(SQLITE_MAGIC.size)
            FileInputStream(dbFile).use { it.read(header) }
            header.contentEquals(SQLITE_MAGIC)
        } catch (_: Exception) {
            false
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

        if (isPlainSqlite(dbFile)) {
            Log.d(TAG, "Plain DB detected via header check. Encrypting...")
            encryptPlainDb(dbFile, passphrase)
            return
        }


        if (canOpenWithKey(dbFile, passphrase)) {
            Log.d(TAG, "DB already encrypted with current key")
            return
        }


        Log.w(TAG, "DB encrypted with unknown key or corrupted. Deleting for fresh start.")
        dbFile.delete()
        File(dbFile.parent, "$dbName-encrypted").let { if (it.exists()) it.delete() }
    }

    private fun encryptPlainDb(dbFile: File, passphrase: CharArray) {
        val passphraseStr = String(passphrase)
        val tempEncrypted = File(dbFile.parent, "${dbFile.name}-encrypted")
        if (tempEncrypted.exists()) tempEncrypted.delete()

        SQLiteDatabase.openDatabase(
            tempEncrypted.absolutePath,
            passphraseStr,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
            null,
            null
        ).close()

        val plainDb = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            "",
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null,
            null
        )
        val dbVersion: Int
        try {
            plainDb.rawQuery("PRAGMA user_version", null).use { cursor ->
                dbVersion = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
            Log.d(TAG, "Plain DB version: $dbVersion")

            plainDb.execSQL(
                "ATTACH DATABASE '${tempEncrypted.absolutePath}' AS encrypted KEY '$passphraseStr'"
            )
            plainDb.rawQuery("SELECT sqlcipher_export('encrypted')", null)
                .use { it.moveToFirst() }
            plainDb.execSQL("DETACH DATABASE encrypted")
        } finally {
            plainDb.close()
        }

        dbFile.delete()
        tempEncrypted.renameTo(dbFile)

        val encDb = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            passphraseStr,
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null,
            null
        )
        try {
            encDb.execSQL("PRAGMA user_version = $dbVersion")
            encDb.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        } finally {
            encDb.close()
        }
        Log.d(TAG, "Encryption complete. DB version set to $dbVersion")
    }

    private fun canOpenWithKey(dbFile: File, passphrase: CharArray): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                String(passphrase),
                null,
                SQLiteDatabase.OPEN_READONLY,
                null,
                null
            )
            true
        } catch (_: Exception) {
            false
        } finally {
            db?.close()
        }
    }
}
