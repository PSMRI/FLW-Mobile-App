package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceDao @Inject constructor(@ApplicationContext private val context: Context) {

    private val pref = PreferenceManager.getInstance(context)
    fun deleteAmritToken() {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_D2D_API_KEY)
        editor.remove(prefKey)
        editor.apply()
    }

    fun deleteJWTToken() {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_primary_JWT_API_KEY)
        editor.remove(prefKey)
        editor.apply()
    }

    fun getAmritToken(): String? {
        val prefKey = context.getString(R.string.PREF_primary_API_KEY)
        return pref.getString(prefKey, null)
    }

    fun registerAmritToken(token: String) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_primary_API_KEY)
        editor.putString(prefKey, token)
        editor.apply()
    }


    fun getJWTAmritToken(): String? {
        val prefKey = context.getString(R.string.PREF_primary_JWT_API_KEY)
        return pref.getString(prefKey, null)
    }

    fun registerJWTAmritToken(token: String) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_primary_JWT_API_KEY)
        editor.putString(prefKey, token)
        editor.apply()
    }

    fun registerRefreshToken(token: String) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_primary_REFRESH_TOKEN)
        editor.putString(prefKey, token)
        editor.apply()
    }

    fun getRefreshToken(): String? {
        val prefKey = context.getString(R.string.PREF_primary_REFRESH_TOKEN)
        return pref.getString(prefKey, null)
    }

    fun registerLoginCred(userName: String, password: String) {
        val editor = pref.edit()
        val prefUserKey = context.getString(R.string.PREF_rem_me_uname)
        val prefUserPwdKey = context.getString(R.string.PREF_rem_me_pwd)
//        val prefUserStateKey = context.getString(R.string.PREF_rem_me_state)
        editor.putString(prefUserKey, userName)
        editor.putString(prefUserPwdKey, password)
//        editor.putString(prefUserStateKey, state)
        editor.apply()
    }

    fun deleteForLogout() {
        pref.edit().clear().apply()
    }

    fun deleteLoginCred() {
        val editor = pref.edit()
        val prefUserKey = context.getString(R.string.PREF_rem_me_uname)
        val prefUserPwdKey = context.getString(R.string.PREF_rem_me_pwd)
        editor.remove(prefUserKey)
        editor.remove(prefUserPwdKey)
        editor.apply()
    }

    fun getRememberedUserName(): String? {
        val key = context.getString(R.string.PREF_rem_me_uname)
        return pref.getString(key, null)
    }

    fun getRememberedPassword(): String? {
        val key = context.getString(R.string.PREF_rem_me_pwd)
        return pref.getString(key, null)
    }

    fun getRememberedState(): String? {
        val key = context.getString(R.string.PREF_rem_me_state)
        return pref.getString(key, null)
    }

    fun saveLocationRecord(locationRecord: LocationRecord) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_location_record_entry)
        val locationRecordJson = Gson().toJson(locationRecord)
        editor.putString(prefKey, locationRecordJson)
        editor.apply()
    }

    fun getLocationRecord(): LocationRecord? {
        val prefKey = context.getString(R.string.PREF_location_record_entry)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, LocationRecord::class.java)
    }

    fun setLastSyncedTimeStamp(lastSaved: Long) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_full_load_pull_progress)
        editor.putLong(prefKey, lastSaved)
        editor.apply()
    }

    fun getLastSyncedTimeStamp(): Long {
        val prefKey = context.getString(R.string.PREF_full_load_pull_progress)
        return pref.getLong(prefKey, Konstants.defaultTimeStamp)
    }

    fun setFirstSyncLastSyncedPage(page: Int) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_first_pull_amrit_last_synced_page)
        editor.putInt(prefKey, page)
        editor.apply()
    }

    fun getFirstSyncLastSyncedPage(): Int {
        val prefKey = context.getString(R.string.PREF_first_pull_amrit_last_synced_page)
        return pref.getInt(prefKey, 0)
    }


    fun saveSetLanguage(language: Languages) {
        val key = context.getString(R.string.PREF_current_saved_language)
        val editor = pref.edit()
        editor.putString(key, language.symbol)
        editor.apply()
    }

    fun getCurrentLanguage(): Languages {
        val key = context.getString(R.string.PREF_current_saved_language)
        return when (pref.getString(key, null)) {
            Languages.ASSAMESE.symbol -> Languages.ASSAMESE
            Languages.HINDI.symbol -> Languages.HINDI
            Languages.ENGLISH.symbol -> Languages.ENGLISH
            else -> Languages.ENGLISH
        }
    }

    fun saveProfilePicUri(uri: Uri?) {

        val key = context.getString(R.string.PREF_current_dp_uri)
        val editor = pref.edit()
        editor.putString(key, uri?.toString())
        editor.apply()
        Timber.d("Saving profile pic @ $uri")
    }

    fun getProfilePicUri(): Uri? {
        val key = context.getString(R.string.PREF_current_dp_uri)
        val uriString = pref.getString(key, null)
        return uriString?.let { Uri.parse(it) }
    }

    fun savePublicKeyForAbha(publicKey: String) {
        val key = "AUTH_CERT"
        val editor = pref.edit()
        editor.putString(key, publicKey)
        editor.apply()
    }

    fun getPublicKeyForAbha(): String? {
        val key = "AUTH_CERT"
        return pref.getString(key, null)
    }

    fun registerUser(user: User) {
        val editor = pref.edit()
        val prefKey = context.getString(R.string.PREF_user_entry)
        val userJson = Gson().toJson(user)
        editor.putString(prefKey, userJson)
        editor.apply()
    }

    fun getLoggedInUser(): User? {
        val prefKey = context.getString(R.string.PREF_user_entry)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, User::class.java)
    }

    fun lastUpdatedAmritToken(currentTimeMillis: Long) {

    }

    fun saveStateId(villageId: Int) {
        pref.edit().putInt("PREF_VILLAGE_ID", villageId).apply()
    }
    fun getStateId(): Int {
        return pref.getInt("PREF_VILLAGE_ID", -1)
    }

    var isFullPullComplete: Boolean
        get() = pref.getBoolean("FIRST TIME FULL PULL DONE", false)
        set(value) {
            pref.edit().putBoolean("FIRST TIME FULL PULL DONE", value).apply()
        }

    var isDevModeEnabled: Boolean
        get() = pref.getBoolean("DEV-MODE", false)
        set(value) {
            pref.edit().putBoolean("DEV-MODE", value).apply()
        }

    var lastAmritTokenFetchTimestamp: Long
        get() = pref.getLong("last amrit token timestamp ", 0L)
        set(value) {
            pref.edit().putLong("last amrit token timestamp ", value).apply()
        }

    var lastIncentivePullTimestamp: Long
        get() = pref.getLong("last incentive update timestamp ", Konstants.defaultTimeStamp)
        set(value) {
            pref.edit().putLong("last incentive update timestamp ", value).apply()
        }

    var lastAshaPullTimestamp: Long
        get() = pref.getLong("last asha update timestamp ", Konstants.defaultTimeStamp)
        set(value) {
            pref.edit().putLong("last asha update timestamp ", value).apply()
        }

    fun saveUserGender(gender: String) {
        pref.edit().putString("PREF_USER_GENDER", gender).apply()
    }

    fun getUserGender(): String? {
        return pref.getString("PREF_USER_GENDER", null)
    }

    fun saveUserDob(dob: String) {
        pref.edit().putString("PREF_USER_DOB", dob).apply()
    }

    fun getUserDob(): String? {
        return pref.getString("PREF_USER_DOB", null)
    }

    fun saveUserMobile(mobile: String) {
        pref.edit().putString("PREF_USER_MOBILE", mobile).apply()
    }

    fun getUserMobile(): String? {
        return pref.getString("PREF_USER_MOBILE", null)
    }

    fun saveEmployeeId(mobile: String) {
        pref.edit().putString("PREF_EMPID", mobile).apply()
    }

    fun getEmployeeId(): String? {
        return pref.getString("PREF_EMPID", null)
    }

    fun saveUserEmail(email: String) {
        pref.edit().putString("PREF_USER_EMAIL", email).apply()
    }

    fun getUserEmail(): String? {
        return pref.getString("PREF_USER_EMAIL", null)
    }

    fun saveSupervisorName(name: String?) {
        pref.edit().putString("PREF_SUPERVISOR_NAME", name).apply()
    }

    fun getSupervisorName(): String? {
        return pref.getString("PREF_SUPERVISOR_NAME", null)
    }

    fun saveSupervisorEmpID(name: String?) {
        pref.edit().putString("PREF_SUPERVISOR_EMPID", name).apply()
    }

    fun getSupervisorEmpID(): String? {
        return pref.getString("PREF_SUPERVISOR_EMPID", null)
    }

    fun saveSupervisorContact(name: String?) {
        pref.edit().putString("PREF_SUPERVISOR_Contact", name).apply()
    }

    fun getSupervisorContact(): String? {
        return pref.getString("PREF_SUPERVISOR_Contact", null)
    }

    fun saveSupervisorId(id: Int) {
        pref.edit().putInt("PREF_SUPERVISOR_ID", id).apply()
    }

    fun getSupervisorId(): Int {
        return pref.getInt("PREF_SUPERVISOR_ID", -1)
    }

    fun saveSupervisorDistrict(district: String?) {
        pref.edit().putString("PREF_SUPERVISOR_DISTRICT", district).apply()
    }

    fun getSupervisorDistrict(): String? {
        return pref.getString("PREF_SUPERVISOR_DISTRICT", null)
    }

    fun saveSupervisorBlock(block: String?) {
        pref.edit().putString("PREF_SUPERVISOR_BLOCK", block).apply()
    }

    fun getSupervisorBlock(): String? {
        return pref.getString("PREF_SUPERVISOR_BLOCK", null)
    }

    fun saveSupervisorState(state: String?) {
        pref.edit().putString("PREF_SUPERVISOR_STATE", state).apply()
    }

    fun getSupervisorState(): String? {
        return pref.getString("PREF_SUPERVISOR_STATE", null)
    }


    fun saveSupervisorSubcenter(subcenter: String?) {
        pref.edit().putString("PREF_SUPERVISOR_SUBCENTER", subcenter).apply()
    }

    fun getSupervisorSubcenter(): String? {
        return pref.getString("PREF_SUPERVISOR_SUBCENTER", null)
    }

    fun saveSupervisorFacilityType(type: String?) {
        pref.edit().putString("PREF_SUPERVISOR_FACILITY_TYPE", type).apply()
    }

    fun getSupervisorFacilityType(): String? {
        return pref.getString("PREF_SUPERVISOR_FACILITY_TYPE", null)
    }

    fun saveFacilityId(facilityId: Int) {
        pref.edit().putInt("facilityId", facilityId).apply()
    }

    fun getFacilityId(): Int {
        return pref.getInt("facilityId", -1)
    }

    fun saveDesignationId(designationId: Int) {
        pref.edit().putInt("designation_id", designationId).apply()
    }

    fun getDesignationId(): Int {
        return pref.getInt("designation_id", -1)
    }
    fun saveLocationType(locationType: String) {
        pref.edit().putString("location_type", locationType).apply()
    }

    fun getLocationType(): String {
        return pref.getString("location_type", "") ?: ""
    }

    fun saveDistrict(district: String) {
        pref.edit().putString("district", district).apply()
    }

    fun getDistrict(): String {
        return pref.getString("district", "") ?: ""
    }

    fun saveBlock(block: String) {
        pref.edit().putString("block", block).apply()
    }

    fun getBlock(): String {
        return pref.getString("block", "") ?: ""
    }
    fun saveState(state: String) {
        pref.edit().putString("state", state).apply()
    }

    fun getState(): String {
        return pref.getString("state", "") ?: ""
    }
}