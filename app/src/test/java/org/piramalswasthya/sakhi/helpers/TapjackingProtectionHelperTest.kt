package org.piramalswasthya.sakhi.helpers

import android.app.Activity
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TapjackingProtectionHelperTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `applyWindowSecurity does not touch the window in a debug build`() {
        val activity: Activity = mockk(relaxed = true)

        TapjackingProtectionHelper.applyWindowSecurity(activity)

        verify(exactly = 0) { activity.window }
    }

    @Test
    fun `enableTouchFiltering marks the content view to filter touches when obscured`() {
        val activity: Activity = mockk(relaxed = true)
        val rootView: View = mockk(relaxed = true)
        every { activity.findViewById<View>(android.R.id.content) } returns rootView
        mockkStatic(Settings::class)
        every { Settings.canDrawOverlays(activity) } returns false

        TapjackingProtectionHelper.enableTouchFiltering(activity)

        verify { rootView.filterTouchesWhenObscured = true }
    }

    @Test
    fun `enableTouchFiltering warns the user when an overlay is detected`() {
        val activity: Activity = mockk(relaxed = true)
        val rootView: View = mockk(relaxed = true)
        every { activity.findViewById<View>(android.R.id.content) } returns rootView
        mockkStatic(Settings::class)
        every { Settings.canDrawOverlays(activity) } returns true
        mockkStatic(Toast::class)
        val toast: Toast = mockk(relaxed = true)
        every { Toast.makeText(activity, any<String>(), Toast.LENGTH_LONG) } returns toast

        TapjackingProtectionHelper.enableTouchFiltering(activity)

        verify { toast.show() }
    }

    @Test
    fun `handleFilteredTouch allows touches when event is null`() {
        val activity: Activity = mockk(relaxed = true)

        val allowed = TapjackingProtectionHelper.handleFilteredTouch(activity, null)

        assertTrue(allowed)
    }

    @Test
    fun `handleFilteredTouch blocks and warns when the window is obscured`() {
        val activity: Activity = mockk(relaxed = true)
        val event: MotionEvent = mockk()
        every { event.flags } returns MotionEvent.FLAG_WINDOW_IS_OBSCURED
        mockkStatic(Toast::class)
        val toast: Toast = mockk(relaxed = true)
        every { Toast.makeText(activity, any<String>(), Toast.LENGTH_SHORT) } returns toast

        val allowed = TapjackingProtectionHelper.handleFilteredTouch(activity, event)

        assertFalse(allowed)
        verify { toast.show() }
    }

    @Test
    fun `handleFilteredTouch allows touches when the window is not obscured`() {
        val activity: Activity = mockk(relaxed = true)
        val event: MotionEvent = mockk()
        every { event.flags } returns 0

        val allowed = TapjackingProtectionHelper.handleFilteredTouch(activity, event)

        assertTrue(allowed)
    }

    @Test
    fun `isTouchAllowed returns false and warns when the window is obscured`() {
        val activity: Activity = mockk(relaxed = true)
        val event: MotionEvent = mockk()
        every { event.flags } returns MotionEvent.FLAG_WINDOW_IS_OBSCURED
        mockkStatic(Toast::class)
        val toast: Toast = mockk(relaxed = true)
        every { Toast.makeText(activity, any<String>(), Toast.LENGTH_SHORT) } returns toast

        val allowed = TapjackingProtectionHelper.isTouchAllowed(activity, event)

        assertFalse(allowed)
        verify { toast.show() }
    }

    @Test
    fun `isTouchAllowed returns true when event has no obscured flag`() {
        val activity: Activity = mockk(relaxed = true)
        val event: MotionEvent = mockk()
        every { event.flags } returns 0

        val allowed = TapjackingProtectionHelper.isTouchAllowed(activity, event)

        assertTrue(allowed)
    }

    @Test
    fun `isTouchAllowed returns true when event is null`() {
        val activity: Activity = mockk(relaxed = true)

        val allowed = TapjackingProtectionHelper.isTouchAllowed(activity, null)

        assertTrue(allowed)
    }
}
