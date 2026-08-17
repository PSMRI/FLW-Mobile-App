package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import android.util.Base64
import java.security.MessageDigest

object WadhGenerator {

    /**
     * Generates the "wadh" (Wrapper API Data Hash) value required for
     * UIDAI EKYC / Face Auth PID Options.
     *
     * Formula (per UIDAI EKYC API 2.5 spec):
     *   wadh = BASE64( SHA256( ver + ra + rc + lr + de + pfr ) )
     *
     * @param ver EKYC API version, e.g. "2.5"
     * @param ra  Resident authentication type: "F"=finger, "I"=iris,
     *            "O"=OTP, "P"=face. Must match fCount/iCount/pCount used
     *            in your PID options.
     * @param rc  Resident consent — must be "Y" (any other value is
     *            rejected by UIDAI).
     * @param lr  Local language response required? "Y" or "N".
     * @param de  Who decrypts biometric data: "N" = KUA decrypts,
     *            "Y" = ASA decrypts.
     * @param pfr Print format response — "Y" to include PDF, else "N".
     */
    fun generateWadh(
        ver: String = "2.5",
        ra: String = "P",   // "P" = face auth
        rc: String = "Y",
        lr: String = "N",
        de: String = "N",
        pfr: String = "N"
    ): String {
        val concatenated = ver + ra + rc + lr + de + pfr

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(concatenated.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}

// Usage for Face Auth flow:
// val wadh = WadhGenerator.generateWadh(ver = "2.5", ra = "P", rc = "Y", lr = "N", de = "N", pfr = "N")
// val pidXml = buildPidOptionsXml(wadh, 10000)