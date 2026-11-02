package org.piramalswasthya.sakhi.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import timber.log.Timber

/**
 * Navigation helpers that drop a navigation request the NavController can no longer serve instead
 * of crashing with
 * `IllegalArgumentException: Navigation action/destination ... cannot be found from the current
 * destination`.
 *
 * The trigger is a stale/queued click: the first tap navigates away, and a second tap — already
 * posted to the main thread by a still-attached view — reaches the same handler after the
 * NavController has moved on. Fragments hosted as ViewPager2 pages (e.g. `IncentiveDashboardFragment`
 * inside `SupervisorHomeFragment`) are especially prone to it, because their views stay alive while
 * the host destination sits on the back stack, and they navigate using the *host* destination's
 * actions.
 *
 * Two independent guards, because they cover different windows:
 *  - the [Fragment] overloads additionally require the fragment to be RESUMED, so a click runnable
 *    that runs after `onSaveInstanceState` can't commit a transaction into a saved-state
 *    FragmentManager (`IllegalStateException`);
 *  - the [NavController] overloads verify the action/destination is still reachable, which is what
 *    catches the same-frame double tap (the source fragment is still RESUMED at that point).
 *
 * Use the [Fragment] overloads from click handlers; use the [NavController] ones for programmatic
 * navigation that must run while the fragment isn't resumed.
 */

/** Navigates only if this fragment is resumed and [directions]' action is still reachable. */
@MainThread
fun Fragment.safeNavigate(directions: NavDirections) {
    if (!isResumedForNavigation("action")) return
    navControllerOrNull()?.safeNavigate(directions)
}

/** Navigates only if this fragment is resumed and [destinationId] isn't already current. */
@MainThread
fun Fragment.safeNavigate(@IdRes destinationId: Int, args: Bundle? = null) {
    if (!isResumedForNavigation("destination")) return
    navControllerOrNull()?.safeNavigate(destinationId, args)
}

@MainThread
fun NavController.safeNavigate(directions: NavDirections) {
    // No current destination => graph not set yet; nothing can be navigated (and `graph` would throw).
    val current = currentDestination ?: return
    if (current.getAction(directions.actionId) == null) {
        Timber.w(
            "safeNavigate: action ${idName(directions.actionId)} not reachable from " +
                    "${idName(current.id)}; ignoring (stale click?)"
        )
        return
    }
    try {
        navigate(directions)
    } catch (e: RuntimeException) {
        Timber.e(e, "safeNavigate: navigate(${idName(directions.actionId)}) failed")
    }
}

@MainThread
fun NavController.safeNavigate(@IdRes destinationId: Int, args: Bundle? = null) {
    // Already there — a repeated tap would otherwise push a duplicate copy on the back stack.
    val current = currentDestination ?: return
    if (current.id == destinationId) return
    // Resolution is left to NavController (it also searches nested graphs); the catch below turns
    // an unreachable destination into a dropped request instead of a crash.
    try {
        navigate(destinationId, args)
    } catch (e: RuntimeException) {
        Timber.e(e, "safeNavigate: navigate(${idName(destinationId)}) failed")
    }
}

/**
 * A click handler can run after the fragment has been stopped (queued `View.PerformClick`), at
 * which point committing a fragment transaction throws. RESUMED is the only state a real user tap
 * can originate from.
 */
private fun Fragment.isResumedForNavigation(what: String): Boolean {
    val resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    if (!resumed) {
        Timber.w("safeNavigate: dropping $what from non-resumed ${javaClass.simpleName}")
    }
    return resumed
}

private fun Fragment.navControllerOrNull(): NavController? = try {
    findNavController()
} catch (e: IllegalStateException) {
    Timber.w(e, "safeNavigate: no NavController for ${javaClass.simpleName}")
    null
}

/** Resource entry name for logging; falls back to the raw id when it can't be resolved. */
private fun NavController.idName(@IdRes id: Int): String = try {
    context.resources.getResourceEntryName(id)
} catch (e: Exception) {
    id.toString()
}
