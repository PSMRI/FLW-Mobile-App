package org.piramalswasthya.sakhi.helpers
import android.app.Activity
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast

object TapjackingProtectionHelper {

    /**
     * Call this in Activity.onCreate() BEFORE setContentView()
     */
    fun applyWindowSecurity(activity: Activity) {

        // Prevent screenshot + some overlays
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    /**
     * Call this in Activity.onCreate() AFTER setContentView()
     * to block touch events when UI is obscured.
     */
    fun enableTouchFiltering(activity: Activity) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        rootView.filterTouchesWhenObscured = true

        // OPTIONAL — warn the user if overlays are active
        if (Settings.canDrawOverlays(activity)) {
            Toast.makeText(
                activity,
                "Screen overlay detected. Some actions may be restricted.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Call this in Activity.onFilterTouchEventForSecurity() override
     */
    fun handleFilteredTouch(activity: Activity, event: MotionEvent?): Boolean {
        if ((event?.flags ?: 0) and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0) {
            Toast.makeText(
                activity,
                "Screen overlay detected. Action blocked.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    fun isTouchAllowed(activity: Activity, event: MotionEvent?): Boolean {
        return if ((event?.flags ?: 0) and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0) {
            Toast.makeText(activity, "Screen overlay detected. Action blocked.", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

}
