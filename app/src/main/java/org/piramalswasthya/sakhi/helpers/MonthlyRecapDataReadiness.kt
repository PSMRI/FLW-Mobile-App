package org.piramalswasthya.sakhi.helpers

import org.piramalswasthya.sakhi.model.RecapFreezeBlocker

/**
 * Whether this install's local database can be trusted for a recap month.
 *
 * [isFullPullComplete] mirrors PreferenceDao's one-shot download flag.
 * [localDataSince] is the earliest moment this install can be considered to have
 * been accumulating the ASHA's records (see PreferenceDao.recapLocalDataSince).
 */
data class RecapDataReadiness(
    val isFullPullComplete: Boolean,
    val localDataSince: Long,
)

/**
 * THE single decision point for whether a freshly calculated payload may be FROZEN.
 *
 * Why this exists — the recap counts rows in the device's own Room database. For a
 * phone in continuous use that is complete by construction, because the ASHA
 * created every row on it. It is NOT complete when the install is newer than the
 * month being summarised, or when the one-shot server download has not finished:
 * the pull is insert-if-absent, silently skips records whose parent row is missing,
 * and never re-runs, so a gap is permanent.
 *
 * Every failure mode here loses rows and none can add them, so an ungated freeze
 * would under-credit an ASHA's work — the worst possible bug in a feature whose
 * only job is to motivate her. Blocking is therefore fail-closed: hide this month
 * and re-evaluate on the next open, rather than freeze a number we cannot stand behind.
 *
 * Deliberately NOT covered: a download that finished but silently dropped records on
 * a device old enough to pass [RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH].
 * `isFullPullComplete` is set unconditionally by the terminal sync worker and the
 * pull workers treat skipped rows as success, so no on-device flag can detect that.
 * It is handled operationally instead — pilot allowlist, plus the diagnostics written
 * alongside the payload.
 *
 * Pure and side-effect free so every branch is unit-testable on the JVM.
 */
fun recapFreezeBlocker(
    readiness: RecapDataReadiness,
    windowStartMillis: Long,
    hasCountableWork: Boolean,
): RecapFreezeBlocker = when {
    !readiness.isFullPullComplete -> RecapFreezeBlocker.FULL_PULL_INCOMPLETE

    // <= 0 means "unknown", which fails closed rather than comparing as very old.
    readiness.localDataSince <= 0L ||
            readiness.localDataSince > windowStartMillis ->
        RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH

    !hasCountableWork -> RecapFreezeBlocker.NO_COUNTABLE_WORK

    else -> RecapFreezeBlocker.NONE
}
