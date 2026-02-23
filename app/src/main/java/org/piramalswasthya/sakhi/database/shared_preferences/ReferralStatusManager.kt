package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralStatusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("referral_status", Context.MODE_PRIVATE)
    }
    
    fun isReferred(benId: Long, referralType: String): Boolean {
        return prefs.getBoolean("${benId}_${referralType}", false)
    }
    
    fun markAsReferred(benId: Long, referralType: String) {
        prefs.edit().putBoolean("${benId}_${referralType}", true).apply()
    }
    
    fun clearReferralStatus(benId: Long, referralType: String) {
        prefs.edit().remove("${benId}_${referralType}").apply()
    }
}