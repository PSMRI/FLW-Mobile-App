package org.piramalswasthya.sakhi.helpers

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.UUID

object DatabaseKeyManager {

    private const val PREF_NAME = "room_db_encryption_pref"
    private const val KEY_DB_PASSWORD = "room_db_password"

    fun getDatabasePassphrase(context: Context): CharArray {

        // ✅ Uses same API as your existing PreferenceManager
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val prefs = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphrase = prefs.getString(KEY_DB_PASSWORD, null)

        if (passphrase == null) {
            passphrase = generateStrongPassword()
            prefs.edit().putString(KEY_DB_PASSWORD, passphrase).apply()
        }

        return  passphrase.toCharArray()
    }

    private fun generateStrongPassword(): String {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString()
    }
}
