package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.piramalswasthya.sakhi.R

class PreferenceManager private constructor() {

    companion object {

        @Volatile
        private var INSTANCE: SharedPreferences? = null
        internal fun getInstance(context: Context): SharedPreferences {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                    instance = EncryptedSharedPreferences.create(
                        context.resources.getString(R.string.PREF_NAME),
                        masterKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    INSTANCE = instance
                }
                return instance
            }

        }
    }

}