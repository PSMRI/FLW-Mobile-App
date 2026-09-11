package org.piramalswasthya.sakhi.badges.domain

import java.security.MessageDigest

/**
 * Stable, non-identifying discriminators for badges that can be earned more than once at the
 * same level (Badge LLD §4).
 *
 * Two awards of the same badge and level are only different because of what they were for: a
 * quarter, or a beneficiary. The server needs to be able to tell them apart — otherwise a
 * quarterly badge earned in two quarters collapses to one row on upload and cannot be
 * restored — but it must not learn who the beneficiary was.
 *
 * Quarter keys are not about anyone, so they travel as they are. Beneficiary ids are hashed
 * here, once, at the point the award is created, so the identifier is never written to a
 * local table either and there is no second form of the same award to reconcile later. The
 * digest is deterministic, so the same beneficiary produces the same key on every device and
 * after every reinstall, which is exactly what makes the uniqueness constraint work.
 *
 * Not a security boundary: a beneficiary id drawn from a small space could be recovered by
 * an attacker who already had both the id list and the database. It is a data-minimisation
 * measure — the identifier does not leave the phone — and the health tables it came from are
 * already on the same device behind the same lock.
 */
object AwardKeys {

    /**
     * The key for a per-case award. Truncated to 32 hex characters: collision risk over one
     * ASHA's caseload is negligible, and the column it lands in is bounded.
     */
    fun forCase(rawCaseId: String): String {
        if (rawCaseId.isBlank()) return ""
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(rawCaseId.trim().toByteArray(Charsets.UTF_8))
        return buildString(KEY_CHARS) {
            for (byte in digest) {
                if (length >= KEY_CHARS) break
                append(HEX[(byte.toInt() shr 4) and 0xF])
                append(HEX[byte.toInt() and 0xF])
            }
        }
    }

    private const val KEY_CHARS = 32
    private val HEX = "0123456789abcdef".toCharArray()
}
