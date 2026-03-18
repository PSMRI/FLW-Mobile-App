package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import timber.log.Timber
import java.security.KeyStore
import java.util.UUID

object DatabaseKeyManager {

    private const val PREF_NAME = "room_db_encryption_pref"
    private const val KEY_DB_PASSWORD = "room_db_password"
    private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"

    fun getDatabasePassphrase(context: Context): CharArray {

        val prefs: SharedPreferences = try {
            createEncryptedPreferences(context)
        } catch (e: Exception) {
            Timber.e(e, "DatabaseKeyManager: EncryptedSharedPreferences failed, recovering")
            clearCorruptedPrefs(context)
            createEncryptedPreferences(context)
        }

        var passphrase = prefs.getString(KEY_DB_PASSWORD, null)

        if (passphrase == null) {
            passphrase = generateStrongPassword()
            prefs.edit().putString(KEY_DB_PASSWORD, passphrase).apply()
        }

        return  passphrase.toCharArray()
    }

    private fun createEncryptedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun clearCorruptedPrefs(context: Context) {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MASTER_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Timber.e(e, "DatabaseKeyManager: Failed to delete master key")
        }
        try {
            val prefsFile = java.io.File(context.filesDir.parent, "shared_prefs/$PREF_NAME.xml")
            if (prefsFile.exists()) prefsFile.delete()
        } catch (e: Exception) {
            Timber.e(e, "DatabaseKeyManager: Failed to clear preferences file")
        }
    }

    private fun generateStrongPassword(): String {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString()
    }
}
