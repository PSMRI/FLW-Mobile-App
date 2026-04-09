package org.piramalswasthya.sakhi.utils

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

/**
 * A NestedScrollView that safely handles the case where onSizeChanged() tries to scroll
 * to a focused view that is no longer a descendant of this scroll view.
 *
 * This can occur when a RecyclerView inside the scroll view has its adapter replaced while
 * an EditText inside the RecyclerView has focus. RecyclerView internally detaches views
 * before removing them, leaving a stale focused-view reference in the parent chain.
 * When the scroll view resizes (e.g., keyboard dismissal), onSizeChanged() calls
 * offsetDescendantRectToMyCoords() with the detached view, throwing
 * IllegalArgumentException: "parameter must be a descendant of this view".
 */
class SafeNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        try {
            super.onSizeChanged(w, h, oldw, oldh)
        } catch (e: IllegalArgumentException) {
            // Swallow: the focused child is no longer a descendant (detached during
            // RecyclerView layout). The scroll-to-focused-child behavior is skipped,
            // which is acceptable — the view is being replaced anyway.
        }
    }
}
