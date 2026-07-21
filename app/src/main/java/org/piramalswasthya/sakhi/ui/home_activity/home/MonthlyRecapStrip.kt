package org.piramalswasthya.sakhi.ui.home_activity.home

import android.content.Context
import android.provider.Settings
import androidx.annotation.StringRes
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.RecapStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pure mapping from (availability, local snapshot status) to the dashboard strip
 * state. Fail-closed: unavailable always wins, regardless of snapshot state.
 *
 * - unavailable/unknown            -> HIDDEN
 * - no snapshot / NOT_STARTED      -> READY
 * - IN_PROGRESS                    -> RESUME
 * - COMPLETED                      -> REPLAY
 */
fun stripStateFor(available: Boolean, status: RecapStatus?): MonthlyRecapStripState =
    when {
        !available -> MonthlyRecapStripState.HIDDEN
        status == RecapStatus.IN_PROGRESS -> MonthlyRecapStripState.RESUME
        status == RecapStatus.COMPLETED -> MonthlyRecapStripState.REPLAY
        else -> MonthlyRecapStripState.READY
    }

/**
 * True when system animator-duration scale is non-zero. Shared by the strip and
 * the recap screens so reduced-motion devices get the static final layout.
 */
fun recapAnimationsEnabled(context: Context): Boolean =
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) != 0f

/**
 * UI-only, privacy-safe representation of the Monthly Recap dashboard strip.
 *
 * This deliberately contains NO beneficiary data, medical information, category
 * counts, names or database entities — only which visible state the strip is in.
 * Business/eligibility data (Room, APIs, snapshots) is a later Phase and must map
 * into one of these states before reaching the UI.
 */
enum class MonthlyRecapStripState {
    HIDDEN,
    READY,
    RESUME,
    REPLAY;

    companion object {
        /** Maps a debug/config token to a state; anything unknown is safely [HIDDEN]. */
        fun fromToken(token: String?): MonthlyRecapStripState =
            when (token?.trim()?.lowercase(Locale.ROOT)) {
                "ready" -> READY
                "resume" -> RESUME
                "replay" -> REPLAY
                else -> HIDDEN
            }
    }
}

/**
 * Pure, privacy-safe description of how a [MonthlyRecapStripState] should render.
 *
 * Holds only string resource ids and presentation flags, so the mapping in
 * [styleFor] can be unit-tested without an Android [android.content.Context].
 */
data class MonthlyRecapStripStyle(
    val visible: Boolean,
    @StringRes val messageRes: Int,
    @StringRes val ctaRes: Int,
    @StringRes val contentDescriptionRes: Int,
    val showNewBadge: Boolean,
    val showCompletedBadge: Boolean,
    val showProgress: Boolean,
    val animateEntrance: Boolean,
)

/** Deterministic, privacy-safe mapping from state to presentation. Pure and testable. */
fun styleFor(state: MonthlyRecapStripState): MonthlyRecapStripStyle = when (state) {
    MonthlyRecapStripState.HIDDEN -> MonthlyRecapStripStyle(
        visible = false,
        messageRes = 0,
        ctaRes = 0,
        contentDescriptionRes = 0,
        showNewBadge = false,
        showCompletedBadge = false,
        showProgress = false,
        animateEntrance = false,
    )

    MonthlyRecapStripState.READY -> MonthlyRecapStripStyle(
        visible = true,
        messageRes = R.string.monthly_recap_ready_message,
        ctaRes = R.string.monthly_recap_view_recap,
        contentDescriptionRes = R.string.monthly_recap_cd_ready,
        showNewBadge = true,
        showCompletedBadge = false,
        showProgress = false,
        animateEntrance = true,
    )

    MonthlyRecapStripState.RESUME -> MonthlyRecapStripStyle(
        visible = true,
        messageRes = R.string.monthly_recap_resume_message,
        ctaRes = R.string.monthly_recap_continue_recap,
        contentDescriptionRes = R.string.monthly_recap_cd_resume,
        showNewBadge = false,
        showCompletedBadge = false,
        showProgress = true,
        animateEntrance = true,
    )

    MonthlyRecapStripState.REPLAY -> MonthlyRecapStripStyle(
        visible = true,
        messageRes = R.string.monthly_recap_replay_message,
        ctaRes = R.string.monthly_recap_watch_again,
        contentDescriptionRes = R.string.monthly_recap_cd_replay,
        showNewBadge = false,
        showCompletedBadge = true,
        showProgress = false,
        animateEntrance = false,
    )
}

/**
 * Derives the localised name of the immediately previous completed calendar month.
 *
 * [now] is injectable so year rollover (January -> previous December) is unit-testable
 * without changing the device clock. This is intentionally a tiny utility, not the
 * full RecapClock architecture, which arrives with real recap calculation later.
 */
object MonthlyRecapMonth {
    fun previousMonthLabel(locale: Locale, now: Calendar = Calendar.getInstance()): String {
        val cal = now.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        val label = SimpleDateFormat("MMMM", locale).format(cal.time)
        return label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}
