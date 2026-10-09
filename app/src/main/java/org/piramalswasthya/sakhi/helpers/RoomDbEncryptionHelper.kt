package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object RoomDbEncryptionHelper {

    private const val TAG = "RoomDbEncryptionHelper"
    private val SQLITE_MAGIC = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    // Remembers that the current key already opened this DB, so later starts skip
    // canOpenWithKey (full SQLCipher key derivation, a main-thread ANR source).
    private const val MARKER_PREFS = "room_db_key_check"
    private const val KEY_VERIFIED_HASH = "verified_key_hash"

    private fun ensureSqlCipherLoaded(context: Context) {
        NativeLibraryLoader.init(context)
        NativeLibraryLoader.load(NativeLibraryLoader.SQLCIPHER)
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

    // "db_encrypt_check" trace: runs on app start, a known ANR source (canOpenWithKey).
    // The "outcome" attribute separates the fast key check from a full encryption pass.
    fun encryptIfNeeded(
        context: Context,
        dbName: String,
        passphrase: CharArray
    ): Unit = PerfTracer.trace<Unit>("db_encrypt_check") { trace ->
        ensureSqlCipherLoaded(context)

        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            trace?.putAttribute("outcome", "no_db")
            return@trace
        }

        if (isPlainSqlite(dbFile)) {
            Log.d(TAG, "Plain DB detected via header check. Encrypting...")
            trace?.putAttribute("outcome", "encrypted_plain_db")
            encryptPlainDb(dbFile, passphrase)
            markKeyVerified(context, passphrase)
            return@trace
        }

        if (isKeyVerified(context, passphrase)) {
            trace?.putAttribute("outcome", "key_cached")
            return@trace
        }

        if (canOpenWithKey(dbFile, passphrase)) {
            Log.d(TAG, "DB already encrypted with current key")
            trace?.putAttribute("outcome", "key_ok")
            markKeyVerified(context, passphrase)
            return@trace
        }


        trace?.putAttribute("outcome", "reset")
        Log.w(TAG, "DB encrypted with unknown key or corrupted. Deleting for fresh start.")
        clearKeyVerified(context)
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

    /**
     * Call when the encrypted DB fails to open, so the next start runs the full key check
     * (and the reset path) again instead of trusting the stored marker.
     */
    fun clearKeyVerified(context: Context) {
        // commit(): may run from the crash handler just before the process dies.
        markerPrefs(context).edit().remove(KEY_VERIFIED_HASH).commit()
    }

    /** True when [e] or a cause is SQLite's "file is not a database" (wrong key or corrupt file). */
    fun isNotADatabaseError(e: Throwable): Boolean =
        generateSequence(e) { it.cause }.take(10).any {
            it.message?.contains("file is not a database", ignoreCase = true) == true
        }

    private fun isKeyVerified(context: Context, passphrase: CharArray): Boolean =
        markerPrefs(context).getString(KEY_VERIFIED_HASH, null) == keyHash(passphrase)

    private fun markKeyVerified(context: Context, passphrase: CharArray) {
        markerPrefs(context).edit().putString(KEY_VERIFIED_HASH, keyHash(passphrase)).apply()
    }

    private fun markerPrefs(context: Context) =
        context.getSharedPreferences(MARKER_PREFS, Context.MODE_PRIVATE)

    // One-way hash of a 72-char random passphrase: identifies the key without exposing it.
    private fun keyHash(passphrase: CharArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(String(passphrase).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

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
