package org.piramalswasthya.sakhi.ui.design

/**
 * Redesign switches for the risky shell changes (REDESIGN_IMPLEMENTATION_PLAN.md §2.4 / step 8).
 * Compile-time constants so QA can compare builds and roll back by flipping one value.
 */
object UiFlags {
    /**
     * Bottom navigation Today · Modules · Sync · Profile on HomeActivity (Figma 02 / 03 / 08 / 09).
     * OFF by default: turning it on is a product decision (plan §7 item 3 proposes ON for UAT only).
     * When ON, the drawer stays available: the Profile tab opens it ("Profile & more").
     */
    const val BOTTOM_NAV = false
}
