package org.piramalswasthya.sakhi.helpers

import org.piramalswasthya.sakhi.model.FamilyMember
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transient in-memory hand-off for ABHA/Ayushman details fetched during household registration.
 *
 * The Head-of-Family [FamilyMember] fetched while creating a household is published here keyed by
 * that household's final (frozen) id once the household is saved, and consumed exactly once when the
 * HoF beneficiary registration opens for the same household id.
 *
 * Keying by the unique household id guarantees the data can never be applied to a different
 * household; consume-on-read prevents stale reuse. Not persisted — if the process dies before the
 * HoF is registered, the entry is lost and the HoF form falls back to manual entry.
 */
@Singleton
class HofAbhaPrefillCache @Inject constructor() {

    private val pending = ConcurrentHashMap<Long, FamilyMember>()

    /** Stores [member] for a saved household. No-op for an unfrozen draft id (0). */
    fun put(householdId: Long, member: FamilyMember) {
        if (householdId != 0L) pending[householdId] = member
    }

    /** Returns and removes the member stored for [householdId], or null if none. */
    fun consume(householdId: Long): FamilyMember? = pending.remove(householdId)
}