package org.piramalswasthya.sakhi.ui.home_activity.incentives

/**
 * When the Claim / Reclaim button is offered for a selected month.
 *
 * Extracted from [IncentivesFragment] so the rule can be unit tested without a Fragment: it was
 * previously inline inside the "Fetch" click listener and therefore only reachable on a device.
 * Pure — no Android, no BuildConfig — so the caller passes `isMitaninVariant` in.
 *
 * Month indices are 0-based to match `Calendar.MONTH` and the `R.array.months` ordering
 * (index 0 = January), which is how [IncentivesFragment] resolves the spinner selection.
 */
object IncentiveClaimWindow {

    /**
     * @param isMitaninVariant   BuildConfig.FLAVOR contains "mitanin" (mitanin / mitaninStag /
     *                           mitaninUat). Every other flavor — saksham, sakshamStag,
     *                           sakshamUat (Utprerona), niramay, xushrukha — takes the
     *                           unchanged legacy branch.
     * @param selectedMonthIndex 0-based month picked in the spinner.
     * @param selectedYear       year picked in the spinner.
     * @param currentMonthIndex  0-based `Calendar.MONTH` for today.
     * @param currentYear        `Calendar.YEAR` for today.
     * @param currentDayOfMonth  `Calendar.DAY_OF_MONTH` for today.
     */
    fun isClaimAllowed(
        isMitaninVariant: Boolean,
        selectedMonthIndex: Int,
        selectedYear: Int,
        currentMonthIndex: Int,
        currentYear: Int,
        currentDayOfMonth: Int
    ): Boolean {
        val isExactlyLastMonth =
            (selectedYear == currentYear && selectedMonthIndex == currentMonthIndex - 1) ||
                    (currentMonthIndex == 0 && selectedMonthIndex == 11 && selectedYear == currentYear - 1)

        val isSelectedPreviousMonth = (selectedYear < currentYear) ||
                (selectedYear == currentYear && selectedMonthIndex < currentMonthIndex)

        return if (isMitaninVariant) {
            // ── FLW-1177: kept for reuse once the pilot ends and permanent cut-offs are decided ──
            // Pre-FLW-1177 Mitanin rule: month M was claimed on days 1-3 of M+1, extended to day 5
            // only when something in M had been rejected. Restore this branch (and delete the one
            // below) when the state confirms the permanent cut-off dates. `isRejectedClaim` would
            // have to be passed in again for it.
//            val mitaninClaimWindowEnd = if (isRejectedClaim) 5 else 3
//            isExactlyLastMonth && currentDayOfMonth in 1..mitaninClaimWindowEnd

            // FLW-1177 interim pilot rule (BRD 164692128 §21, Chhattisgarh pilot, 2-3 months).
            // The claim still targets the PREVIOUS month — a Mitanin claims month M's work, and
            // MT/ANM review it, during M+1. What FLW-1177 removes is only the day-of-month
            // cut-off: Claim and Reclaim are now offered on every day of M+1, not just days 1-3
            // (or 1-5 after a rejection). A rejected activity therefore has the whole of M+1 to
            // be corrected and re-approved; once M+1 ends the server takes it to Unclaimed (106)
            // and it never rolls over into the month after.
            //
            // Deliberately NOT `selectedMonthIndex == currentMonthIndex`: the current month is
            // still in progress and is claimed next month, so offering it here would both hide
            // last month's rejected claims and let a month be claimed before it has finished.
            isExactlyLastMonth
        } else {
            // Unchanged for saksham / sakshamStag / sakshamUat (Utprerona) / niramay / xushrukha.
            // Do not fold this into the Mitanin branch — FLW-1177 is a Chhattisgarh pilot rule.
            when {
                isExactlyLastMonth -> currentDayOfMonth <= 12
                isSelectedPreviousMonth -> true
                else -> false
            }
        }
    }
}
