package org.piramalswasthya.sakhi.database.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User

/**
 * Unit tests for [PreferenceDao].
 *
 * [PreferenceManager.getInstance] is object-mocked so the DAO talks to a plain
 * MockK [SharedPreferences] instead of the real EncryptedSharedPreferences.
 */
class PreferenceDaoTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var dao: PreferenceDao

    private val keyAmrit = "pref_amrit_api_key"
    private val keyD2d = "pref_d2d_api_key"
    private val keyJwt = "pref_jwt_api_key"
    private val keyRefresh = "pref_refresh_token"
    private val keyUname = "pref_rem_me_uname"
    private val keyPwd = "pref_rem_me_pwd"
    private val keyState = "pref_rem_me_state"
    private val keyLocation = "pref_location_record"
    private val keyPullProgress = "pref_full_load_pull_progress"
    private val keyLastPage = "pref_first_pull_last_page"
    private val keyLanguage = "pref_saved_language"
    private val keyDpUri = "pref_dp_uri"
    private val keyUser = "pref_user_entry"

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.clear() } returns editor
        every { editor.apply() } just Runs
        every { editor.commit() } returns true

        every { context.getString(R.string.PREF_primary_API_KEY) } returns keyAmrit
        every { context.getString(R.string.PREF_D2D_API_KEY) } returns keyD2d
        every { context.getString(R.string.PREF_primary_JWT_API_KEY) } returns keyJwt
        every { context.getString(R.string.PREF_primary_REFRESH_TOKEN) } returns keyRefresh
        every { context.getString(R.string.PREF_rem_me_uname) } returns keyUname
        every { context.getString(R.string.PREF_rem_me_pwd) } returns keyPwd
        every { context.getString(R.string.PREF_rem_me_state) } returns keyState
        every { context.getString(R.string.PREF_location_record_entry) } returns keyLocation
        every { context.getString(R.string.PREF_full_load_pull_progress) } returns keyPullProgress
        every { context.getString(R.string.PREF_first_pull_amrit_last_synced_page) } returns keyLastPage
        every { context.getString(R.string.PREF_current_saved_language) } returns keyLanguage
        every { context.getString(R.string.PREF_current_dp_uri) } returns keyDpUri
        every { context.getString(R.string.PREF_user_entry) } returns keyUser

        mockkObject(PreferenceManager.Companion)
        every { PreferenceManager.getInstance(any()) } returns prefs

        dao = PreferenceDao(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun sampleLocation() = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(2, "Assam"),
        district = LocationEntity(3, "Kamrup"),
        block = LocationEntity(4, "Block1"),
        village = LocationEntity(5, "Village1")
    )

    private fun sampleUser() = User(
        userId = 11,
        name = "Asha Worker",
        userName = "asha1",
        password = "pass",
        role = "ASHA",
        serviceMapId = 3,
        state = LocationEntity(2, "Assam"),
        district = LocationEntity(3, "Kamrup"),
        block = LocationEntity(4, "Block1"),
        villages = listOf(LocationEntity(5, "Village1"))
    )

    // ---------------------------------------------------------------
    // Amrit / JWT / refresh tokens
    // ---------------------------------------------------------------

    @Test
    fun `deleteAmritToken removes the d2d key and applies`() {
        dao.deleteAmritToken()

        verify { editor.remove(keyD2d) }
        verify { editor.apply() }
    }

    @Test
    fun `deleteJWTToken removes the jwt key and applies`() {
        dao.deleteJWTToken()

        verify { editor.remove(keyJwt) }
        verify { editor.apply() }
    }

    @Test
    fun `getAmritToken returns the stored token`() {
        every { prefs.getString(keyAmrit, null) } returns "amrit-token"

        assertEquals("amrit-token", dao.getAmritToken())
    }

    @Test
    fun `getAmritToken returns null when nothing stored`() {
        every { prefs.getString(keyAmrit, null) } returns null

        assertNull(dao.getAmritToken())
    }

    @Test
    fun `registerAmritToken writes the token under the primary api key`() {
        dao.registerAmritToken("new-token")

        verify { editor.putString(keyAmrit, "new-token") }
        verify { editor.apply() }
    }

    @Test
    fun `getJWTAmritToken returns the stored jwt`() {
        every { prefs.getString(keyJwt, null) } returns "jwt-token"

        assertEquals("jwt-token", dao.getJWTAmritToken())
    }

    @Test
    fun `registerJWTAmritToken writes the jwt and commits`() {
        dao.registerJWTAmritToken("jwt-token")

        verify { editor.putString(keyJwt, "jwt-token") }
        verify { editor.commit() }
    }

    @Test
    fun `registerRefreshToken writes the refresh token and commits`() {
        dao.registerRefreshToken("refresh-token")

        verify { editor.putString(keyRefresh, "refresh-token") }
        verify { editor.commit() }
    }

    @Test
    fun `getRefreshToken returns the stored refresh token`() {
        every { prefs.getString(keyRefresh, null) } returns "refresh-token"

        assertEquals("refresh-token", dao.getRefreshToken())
    }

    // ---------------------------------------------------------------
    // Login credentials
    // ---------------------------------------------------------------

    @Test
    fun `registerLoginCred stores both username and password`() {
        dao.registerLoginCred("asha1", "secret")

        verify { editor.putString(keyUname, "asha1") }
        verify { editor.putString(keyPwd, "secret") }
        verify { editor.apply() }
    }

    @Test
    fun `deleteForLogout clears the whole preference file`() {
        dao.deleteForLogout()

        verify { editor.clear() }
        verify { editor.apply() }
    }

    @Test
    fun `deleteLoginCred removes username and password only`() {
        dao.deleteLoginCred()

        verify { editor.remove(keyUname) }
        verify { editor.remove(keyPwd) }
        verify(exactly = 0) { editor.clear() }
    }

    @Test
    fun `getRememberedUserName returns stored value`() {
        every { prefs.getString(keyUname, null) } returns "asha1"

        assertEquals("asha1", dao.getRememberedUserName())
    }

    @Test
    fun `getRememberedPassword returns stored value`() {
        every { prefs.getString(keyPwd, null) } returns "secret"

        assertEquals("secret", dao.getRememberedPassword())
    }

    @Test
    fun `getRememberedState returns null when never stored`() {
        every { prefs.getString(keyState, null) } returns null

        assertNull(dao.getRememberedState())
    }

    // ---------------------------------------------------------------
    // Location record (gson)
    // ---------------------------------------------------------------

    @Test
    fun `saveLocationRecord stores the gson representation`() {
        val record = sampleLocation()

        dao.saveLocationRecord(record)

        verify { editor.putString(keyLocation, Gson().toJson(record)) }
    }

    @Test
    fun `getLocationRecord parses the stored json`() {
        val record = sampleLocation()
        every { prefs.getString(keyLocation, null) } returns Gson().toJson(record)

        assertEquals(record, dao.getLocationRecord())
    }

    @Test
    fun `getLocationRecord returns null when nothing stored`() {
        every { prefs.getString(keyLocation, null) } returns null

        assertNull(dao.getLocationRecord())
    }

    // ---------------------------------------------------------------
    // Sync bookkeeping
    // ---------------------------------------------------------------

    @Test
    fun `setLastSyncedTimeStamp writes a long`() {
        dao.setLastSyncedTimeStamp(123456789L)

        verify { editor.putLong(keyPullProgress, 123456789L) }
    }

    @Test
    fun `getLastSyncedTimeStamp reads with the konstants default`() {
        every { prefs.getLong(keyPullProgress, Konstants.defaultTimeStamp) } returns 987L

        assertEquals(987L, dao.getLastSyncedTimeStamp())
        verify { prefs.getLong(keyPullProgress, Konstants.defaultTimeStamp) }
    }

    @Test
    fun `setFirstSyncLastSyncedPage writes an int`() {
        dao.setFirstSyncLastSyncedPage(7)

        verify { editor.putInt(keyLastPage, 7) }
    }

    @Test
    fun `getFirstSyncLastSyncedPage defaults to zero`() {
        every { prefs.getInt(keyLastPage, 0) } returns 0

        assertEquals(0, dao.getFirstSyncLastSyncedPage())
    }

    // ---------------------------------------------------------------
    // Language
    // ---------------------------------------------------------------

    @Test
    fun `saveSetLanguage stores the language symbol`() {
        dao.saveSetLanguage(Languages.HINDI)

        verify { editor.putString(keyLanguage, "hi") }
    }

    @Test
    fun `getCurrentLanguage maps assamese symbol`() {
        every { prefs.getString(keyLanguage, null) } returns Languages.ASSAMESE.symbol

        assertEquals(Languages.ASSAMESE, dao.getCurrentLanguage())
    }

    @Test
    fun `getCurrentLanguage maps hindi symbol`() {
        every { prefs.getString(keyLanguage, null) } returns Languages.HINDI.symbol

        assertEquals(Languages.HINDI, dao.getCurrentLanguage())
    }

    @Test
    fun `getCurrentLanguage maps english symbol`() {
        every { prefs.getString(keyLanguage, null) } returns Languages.ENGLISH.symbol

        assertEquals(Languages.ENGLISH, dao.getCurrentLanguage())
    }

    @Test
    fun `getCurrentLanguage maps bangla symbol`() {
        every { prefs.getString(keyLanguage, null) } returns Languages.BANGLA.symbol

        assertEquals(Languages.BANGLA, dao.getCurrentLanguage())
    }

    @Test
    fun `getCurrentLanguage falls back to english for unknown symbol`() {
        every { prefs.getString(keyLanguage, null) } returns "zz"

        assertEquals(Languages.ENGLISH, dao.getCurrentLanguage())
    }

    @Test
    fun `getCurrentLanguage falls back to english when nothing stored`() {
        every { prefs.getString(keyLanguage, null) } returns null

        assertEquals(Languages.ENGLISH, dao.getCurrentLanguage())
    }

    // ---------------------------------------------------------------
    // Profile picture uri
    // ---------------------------------------------------------------

    @Test
    fun `saveProfilePicUri stores the string form of the uri`() {
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://dp/1"

        dao.saveProfilePicUri(uri)

        verify { editor.putString(keyDpUri, "content://dp/1") }
    }

    @Test
    fun `saveProfilePicUri stores null when uri is null`() {
        dao.saveProfilePicUri(null)

        verify { editor.putString(keyDpUri, null) }
    }

    @Test
    fun `getProfilePicUri parses the stored uri string`() {
        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { Uri.parse("content://dp/1") } returns uri
        every { prefs.getString(keyDpUri, null) } returns "content://dp/1"

        assertEquals(uri, dao.getProfilePicUri())
    }

    @Test
    fun `getProfilePicUri returns null when nothing stored`() {
        every { prefs.getString(keyDpUri, null) } returns null

        assertNull(dao.getProfilePicUri())
    }

    // ---------------------------------------------------------------
    // ABHA public key + user
    // ---------------------------------------------------------------

    @Test
    fun `savePublicKeyForAbha writes under AUTH_CERT`() {
        dao.savePublicKeyForAbha("pubkey")

        verify { editor.putString("AUTH_CERT", "pubkey") }
    }

    @Test
    fun `getPublicKeyForAbha reads AUTH_CERT`() {
        every { prefs.getString("AUTH_CERT", null) } returns "pubkey"

        assertEquals("pubkey", dao.getPublicKeyForAbha())
    }

    @Test
    fun `registerUser stores the gson representation of the user`() {
        val user = sampleUser()

        dao.registerUser(user)

        verify { editor.putString(keyUser, Gson().toJson(user)) }
    }

    @Test
    fun `getLoggedInUser parses the stored user json`() {
        val user = sampleUser()
        every { prefs.getString(keyUser, null) } returns Gson().toJson(user)

        assertEquals(user, dao.getLoggedInUser())
    }

    @Test
    fun `getLoggedInUser returns null when nothing stored`() {
        every { prefs.getString(keyUser, null) } returns null

        assertNull(dao.getLoggedInUser())
    }

    @Test
    fun `lastUpdatedAmritToken is a no-op that touches no preference`() {
        dao.lastUpdatedAmritToken(1000L)

        verify(exactly = 0) { editor.putLong(any(), any()) }
    }

    // ---------------------------------------------------------------
    // Simple int / boolean / long properties
    // ---------------------------------------------------------------

    @Test
    fun `saveStateId and getStateId use PREF_VILLAGE_ID`() {
        dao.saveStateId(9)
        every { prefs.getInt("PREF_VILLAGE_ID", -1) } returns 9

        verify { editor.putInt("PREF_VILLAGE_ID", 9) }
        assertEquals(9, dao.getStateId())
    }

    @Test
    fun `getStateId defaults to minus one`() {
        every { prefs.getInt("PREF_VILLAGE_ID", -1) } returns -1

        assertEquals(-1, dao.getStateId())
    }

    @Test
    fun `isFullPullComplete getter reads the flag`() {
        every { prefs.getBoolean("FIRST TIME FULL PULL DONE", false) } returns true

        assertTrue(dao.isFullPullComplete)
    }

    @Test
    fun `isFullPullComplete setter writes the flag`() {
        dao.isFullPullComplete = true

        verify { editor.putBoolean("FIRST TIME FULL PULL DONE", true) }
    }

    @Test
    fun `isDevModeEnabled getter reads the flag`() {
        every { prefs.getBoolean("DEV-MODE", false) } returns true

        assertTrue(dao.isDevModeEnabled)
    }

    @Test
    fun `isDevModeEnabled setter writes the flag`() {
        dao.isDevModeEnabled = false

        verify { editor.putBoolean("DEV-MODE", false) }
    }

    @Test
    fun `lastAmritTokenFetchTimestamp getter defaults to zero`() {
        every { prefs.getLong("last amrit token timestamp ", 0L) } returns 55L

        assertEquals(55L, dao.lastAmritTokenFetchTimestamp)
    }

    @Test
    fun `lastAmritTokenFetchTimestamp setter writes a long`() {
        dao.lastAmritTokenFetchTimestamp = 77L

        verify { editor.putLong("last amrit token timestamp ", 77L) }
    }

    @Test
    fun `lastIncentivePullTimestamp getter uses konstants default`() {
        every {
            prefs.getLong("last incentive update timestamp ", Konstants.defaultTimeStamp)
        } returns 88L

        assertEquals(88L, dao.lastIncentivePullTimestamp)
    }

    @Test
    fun `lastIncentivePullTimestamp setter writes a long`() {
        dao.lastIncentivePullTimestamp = 99L

        verify { editor.putLong("last incentive update timestamp ", 99L) }
    }

    @Test
    fun `lastAshaPullTimestamp getter uses konstants default`() {
        every {
            prefs.getLong("last asha update timestamp ", Konstants.defaultTimeStamp)
        } returns 111L

        assertEquals(111L, dao.lastAshaPullTimestamp)
    }

    @Test
    fun `lastAshaPullTimestamp setter writes a long`() {
        dao.lastAshaPullTimestamp = 222L

        verify { editor.putLong("last asha update timestamp ", 222L) }
    }

    // ---------------------------------------------------------------
    // Plain user profile strings
    // ---------------------------------------------------------------

    @Test
    fun `saveUserGender and getUserGender round trip through PREF_USER_GENDER`() {
        dao.saveUserGender("FEMALE")
        every { prefs.getString("PREF_USER_GENDER", null) } returns "FEMALE"

        verify { editor.putString("PREF_USER_GENDER", "FEMALE") }
        assertEquals("FEMALE", dao.getUserGender())
    }

    @Test
    fun `saveUserDob and getUserDob round trip through PREF_USER_DOB`() {
        dao.saveUserDob("01-01-1990")
        every { prefs.getString("PREF_USER_DOB", null) } returns "01-01-1990"

        verify { editor.putString("PREF_USER_DOB", "01-01-1990") }
        assertEquals("01-01-1990", dao.getUserDob())
    }

    @Test
    fun `saveUserMobile and getUserMobile round trip through PREF_USER_MOBILE`() {
        dao.saveUserMobile("9999999999")
        every { prefs.getString("PREF_USER_MOBILE", null) } returns "9999999999"

        verify { editor.putString("PREF_USER_MOBILE", "9999999999") }
        assertEquals("9999999999", dao.getUserMobile())
    }

    @Test
    fun `saveEmployeeId and getEmployeeId round trip through PREF_EMPID`() {
        dao.saveEmployeeId("EMP-1")
        every { prefs.getString("PREF_EMPID", null) } returns "EMP-1"

        verify { editor.putString("PREF_EMPID", "EMP-1") }
        assertEquals("EMP-1", dao.getEmployeeId())
    }

    @Test
    fun `saveUserEmail and getUserEmail round trip through PREF_USER_EMAIL`() {
        dao.saveUserEmail("a@b.com")
        every { prefs.getString("PREF_USER_EMAIL", null) } returns "a@b.com"

        verify { editor.putString("PREF_USER_EMAIL", "a@b.com") }
        assertEquals("a@b.com", dao.getUserEmail())
    }

    @Test
    fun `getUserEmail returns null when never stored`() {
        every { prefs.getString("PREF_USER_EMAIL", null) } returns null

        assertNull(dao.getUserEmail())
    }

    // ---------------------------------------------------------------
    // Supervisor details
    // ---------------------------------------------------------------

    @Test
    fun `saveSupervisorName and getSupervisorName round trip`() {
        dao.saveSupervisorName("Sup One")
        every { prefs.getString("PREF_SUPERVISOR_NAME", null) } returns "Sup One"

        verify { editor.putString("PREF_SUPERVISOR_NAME", "Sup One") }
        assertEquals("Sup One", dao.getSupervisorName())
    }

    @Test
    fun `saveSupervisorName accepts null`() {
        dao.saveSupervisorName(null)

        verify { editor.putString("PREF_SUPERVISOR_NAME", null) }
    }

    @Test
    fun `saveSupervisorEmpID and getSupervisorEmpID round trip`() {
        dao.saveSupervisorEmpID("SUP-9")
        every { prefs.getString("PREF_SUPERVISOR_EMPID", null) } returns "SUP-9"

        verify { editor.putString("PREF_SUPERVISOR_EMPID", "SUP-9") }
        assertEquals("SUP-9", dao.getSupervisorEmpID())
    }

    @Test
    fun `saveSupervisorContact and getSupervisorContact round trip`() {
        dao.saveSupervisorContact("8888888888")
        every { prefs.getString("PREF_SUPERVISOR_Contact", null) } returns "8888888888"

        verify { editor.putString("PREF_SUPERVISOR_Contact", "8888888888") }
        assertEquals("8888888888", dao.getSupervisorContact())
    }

    @Test
    fun `saveSupervisorId and getSupervisorId round trip`() {
        dao.saveSupervisorId(42)
        every { prefs.getInt("PREF_SUPERVISOR_ID", -1) } returns 42

        verify { editor.putInt("PREF_SUPERVISOR_ID", 42) }
        assertEquals(42, dao.getSupervisorId())
    }

    @Test
    fun `saveSupervisorDistrict and getSupervisorDistrict round trip`() {
        dao.saveSupervisorDistrict("Kamrup")
        every { prefs.getString("PREF_SUPERVISOR_DISTRICT", "") } returns "Kamrup"

        verify { editor.putString("PREF_SUPERVISOR_DISTRICT", "Kamrup") }
        assertEquals("Kamrup", dao.getSupervisorDistrict())
    }

    @Test
    fun `saveSupervisorBlock and getSupervisorBlock round trip`() {
        dao.saveSupervisorBlock("Block1")
        every { prefs.getString("PREF_SUPERVISOR_BLOCK", "") } returns "Block1"

        verify { editor.putString("PREF_SUPERVISOR_BLOCK", "Block1") }
        assertEquals("Block1", dao.getSupervisorBlock())
    }

    @Test
    fun `saveSupervisorState and getSupervisorState round trip`() {
        dao.saveSupervisorState("Assam")
        every { prefs.getString("PREF_SUPERVISOR_STATE", "") } returns "Assam"

        verify { editor.putString("PREF_SUPERVISOR_STATE", "Assam") }
        assertEquals("Assam", dao.getSupervisorState())
    }

    @Test
    fun `saveSupervisorSubcenter and getSupervisorSubcenter round trip`() {
        dao.saveSupervisorSubcenter("SC-1")
        every { prefs.getString("PREF_SUPERVISOR_SUBCENTER", "") } returns "SC-1"

        verify { editor.putString("PREF_SUPERVISOR_SUBCENTER", "SC-1") }
        assertEquals("SC-1", dao.getSupervisorSubcenter())
    }

    @Test
    fun `saveSupervisorFacilityType and getSupervisorFacilityType round trip`() {
        dao.saveSupervisorFacilityType("PHC")
        every { prefs.getString("PREF_SUPERVISOR_FACILITY_TYPE", "") } returns "PHC"

        verify { editor.putString("PREF_SUPERVISOR_FACILITY_TYPE", "PHC") }
        assertEquals("PHC", dao.getSupervisorFacilityType())
    }

    // ---------------------------------------------------------------
    // Facility / designation / location
    // ---------------------------------------------------------------

    @Test
    fun `saveFacilityId and getFacilityId round trip`() {
        dao.saveFacilityId(5)
        every { prefs.getInt("facilityId", -1) } returns 5

        verify { editor.putInt("facilityId", 5) }
        assertEquals(5, dao.getFacilityId())
    }

    @Test
    fun `saveDesignationId and getDesignationId round trip`() {
        dao.saveDesignationId(3)
        every { prefs.getInt("designation_id", -1) } returns 3

        verify { editor.putInt("designation_id", 3) }
        assertEquals(3, dao.getDesignationId())
    }

    @Test
    fun `saveLocationType and getLocationType round trip`() {
        dao.saveLocationType("RURAL")
        every { prefs.getString("location_type", "") } returns "RURAL"

        verify { editor.putString("location_type", "RURAL") }
        assertEquals("RURAL", dao.getLocationType())
    }

    @Test
    fun `getLocationType returns empty string when stored value is null`() {
        every { prefs.getString("location_type", "") } returns null

        assertEquals("", dao.getLocationType())
    }

    @Test
    fun `saveDistrict and getDistrict round trip`() {
        dao.saveDistrict("Kamrup")
        every { prefs.getString("district", "") } returns "Kamrup"

        verify { editor.putString("district", "Kamrup") }
        assertEquals("Kamrup", dao.getDistrict())
    }

    @Test
    fun `getDistrict returns empty string when stored value is null`() {
        every { prefs.getString("district", "") } returns null

        assertEquals("", dao.getDistrict())
    }

    @Test
    fun `saveBlock and getBlock round trip`() {
        dao.saveBlock("Block1")
        every { prefs.getString("block", "") } returns "Block1"

        verify { editor.putString("block", "Block1") }
        assertEquals("Block1", dao.getBlock())
    }

    @Test
    fun `getBlock returns empty string when stored value is null`() {
        every { prefs.getString("block", "") } returns null

        assertEquals("", dao.getBlock())
    }

    @Test
    fun `saveState and getState round trip`() {
        dao.saveState("Assam")
        every { prefs.getString("state", "") } returns "Assam"

        verify { editor.putString("state", "Assam") }
        assertEquals("Assam", dao.getState())
    }

    @Test
    fun `getState returns empty string when stored value is null`() {
        every { prefs.getString("state", "") } returns null

        assertEquals("", dao.getState())
    }

    // ---------------------------------------------------------------
    // CHO / ANM lists
    // ---------------------------------------------------------------

    @Test
    fun `saveChoList and getChoList round trip`() {
        dao.saveChoList("[{\"id\":1}]")
        every { prefs.getString("CHO_LIST", "") } returns "[{\"id\":1}]"

        verify { editor.putString("CHO_LIST", "[{\"id\":1}]") }
        assertEquals("[{\"id\":1}]", dao.getChoList())
    }

    @Test
    fun `getChoList returns empty string when stored value is null`() {
        every { prefs.getString("CHO_LIST", "") } returns null

        assertEquals("", dao.getChoList())
    }

    @Test
    fun `saveAnmList and getAnmList round trip`() {
        dao.saveAnmList("[{\"id\":2}]")
        every { prefs.getString("ANM_LIST", "") } returns "[{\"id\":2}]"

        verify { editor.putString("ANM_LIST", "[{\"id\":2}]") }
        assertEquals("[{\"id\":2}]", dao.getAnmList())
    }

    @Test
    fun `getAnmList returns empty string when stored value is null`() {
        every { prefs.getString("ANM_LIST", "") } returns null

        assertEquals("", dao.getAnmList())
    }
}
