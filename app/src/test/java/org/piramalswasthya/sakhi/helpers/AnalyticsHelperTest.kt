package org.piramalswasthya.sakhi.helpers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnalyticsHelperTest {

    private lateinit var context: Context
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var analyticsHelper: AnalyticsHelper

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        firebaseAnalytics = mockk(relaxed = true)

        mockkStatic("com.google.firebase.analytics.ktx.AnalyticsKt")
        every { Firebase.analytics } returns firebaseAnalytics

        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putInt(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putDouble(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putLong(any(), any()) } just Runs
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } just Runs

        analyticsHelper = AnalyticsHelper(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `firebaseAnalytics property resolves from the Firebase ktx entry point`() {
        assertEquals(firebaseAnalytics, analyticsHelper.firebaseAnalytics)
    }

    @Test
    fun `setUserId delegates to firebaseAnalytics`() {
        analyticsHelper.setUserId("user-42")
        verify { firebaseAnalytics.setUserId("user-42") }
    }

    @Test
    fun `setUserProperty delegates to firebaseAnalytics`() {
        analyticsHelper.setUserProperty("role", "asha")
        verify { firebaseAnalytics.setUserProperty("role", "asha") }
    }

    @Test
    fun `logEvent with a bundle forwards the name and bundle`() {
        val bundle = mockk<Bundle>()
        analyticsHelper.logEvent("screen_view", bundle)
        verify { firebaseAnalytics.logEvent("screen_view", bundle) }
    }

    @Test
    fun `logEvent without a bundle defaults to null`() {
        analyticsHelper.logEvent("app_open")
        verify { firebaseAnalytics.logEvent("app_open", null) }
    }

    @Test
    fun `logEvent with json puts every supported value type into the bundle`() {
        val json = JSONObject()
            .put("name", "asha")
            .put("count", 5)
            .put("ratio", 2.5)
            .put("bigNumber", 123456789012345L)
            .put("active", true)

        analyticsHelper.logEvent("custom_event", json)

        verify { anyConstructed<Bundle>().putString("name", "asha") }
        verify { anyConstructed<Bundle>().putInt("count", 5) }
        verify { anyConstructed<Bundle>().putDouble("ratio", 2.5) }
        verify { anyConstructed<Bundle>().putLong("bigNumber", 123456789012345L) }
        verify { anyConstructed<Bundle>().putBoolean("active", true) }
        verify { firebaseAnalytics.logEvent("custom_event", any()) }
    }

    @Test
    fun `logEvent with json falls back to string conversion for unsupported types`() {
        val nested = JSONObject().put("inner", "value")
        val json = JSONObject().put("payload", nested)

        analyticsHelper.logEvent("nested_event", json)

        verify { anyConstructed<Bundle>().putString("payload", nested.toString()) }
    }

    @Test
    fun `logEvent with a null json still logs an empty bundle`() {
        analyticsHelper.logEvent("no_params_event", null as JSONObject?)
        verify { firebaseAnalytics.logEvent("no_params_event", any()) }
    }

    @Test
    fun `logCustomTimestampEvent puts the timestamp under a suffixed key`() {
        analyticsHelper.logCustomTimestampEvent("sync_started", 1700000000000L)

        verify { anyConstructed<Bundle>().putLong("sync_started_time", 1700000000000L) }
        verify { firebaseAnalytics.logEvent("sync_started", any()) }
    }

    @Test
    fun `logApiCall records a successful call without optional fields`() {
        analyticsHelper.logApiCall("syncData", 250L, isSuccess = true)

        verify { anyConstructed<Bundle>().putString("api_name", "syncData") }
        verify { anyConstructed<Bundle>().putLong("response_time_ms", 250L) }
        verify { anyConstructed<Bundle>().putString("status", "success") }
        verify(exactly = 0) { anyConstructed<Bundle>().putInt("response_code", any()) }
        verify(exactly = 0) { anyConstructed<Bundle>().putString("error_message", any()) }
        verify { firebaseAnalytics.logEvent("api_call_event", any()) }
    }

    @Test
    fun `logApiCall records a failed call with response code and error message`() {
        analyticsHelper.logApiCall(
            endpoint = "pushData",
            durationMs = 900L,
            isSuccess = false,
            responseCode = 500,
            errorMessage = "server exploded"
        )

        verify { anyConstructed<Bundle>().putString("status", "failure") }
        verify { anyConstructed<Bundle>().putInt("response_code", 500) }
        verify { anyConstructed<Bundle>().putString("error_message", "server exploded") }
    }

    @Test
    fun `logApiCall truncates a long error message to 100 characters`() {
        val longMessage = "e".repeat(150)
        val captured = slot<String>()
        every { anyConstructed<Bundle>().putString("error_message", capture(captured)) } just Runs

        analyticsHelper.logApiCall(
            endpoint = "pushData",
            durationMs = 900L,
            isSuccess = false,
            errorMessage = longMessage
        )

        assertEquals(100, captured.captured.length)
    }
}
