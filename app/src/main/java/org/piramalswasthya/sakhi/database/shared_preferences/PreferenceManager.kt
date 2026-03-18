package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.piramalswasthya.sakhi.R
import timber.log.Timber
import java.security.KeyStore

class PreferenceManager private constructor() {

    companion object {

        private const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"

        @Volatile
        private var INSTANCE: SharedPreferences? = null
        internal fun getInstance(context: Context): SharedPreferences {

            synchronized(this) {
                INSTANCE?.let { return it }

                val instance: SharedPreferences = try {
                    createEncryptedPreferences(context)
                } catch (e: Exception) {
                    Timber.e(e, "EncryptedSharedPreferences failed, recovering")
                    clearCorruptedKeystore(context)
                    createEncryptedPreferences(context)
                }
                INSTANCE = instance
                return instance
            }

        }

        private fun createEncryptedPreferences(context: Context): SharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            return EncryptedSharedPreferences.create(
                context.resources.getString(R.string.PREF_NAME),
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        private fun clearCorruptedKeystore(context: Context) {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                    keyStore.deleteEntry(MASTER_KEY_ALIAS)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete master key from KeyStore")
            }
            try {
                val prefName = context.resources.getString(R.string.PREF_NAME)
                val prefsFile = java.io.File(context.filesDir.parent, "shared_prefs/$prefName.xml")
                if (prefsFile.exists()) prefsFile.delete()
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear preferences file")
            }
        }
    }

}