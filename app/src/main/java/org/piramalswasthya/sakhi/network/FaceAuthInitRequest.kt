package org.piramalswasthya.sakhi.network

data class FaceAuthInitRequest(
    val scope: List<String> = listOf("abha-enrol", "face-auth")
)

data class FaceAuthInitResponse(
    val txnId: String,
    val message: String
)

data class CapturePIDRequest(
    val scope: List<String> = listOf("abha-enrol", "face-verify"),
    val txnId: String
//    val pid: String? = null
)

data class CapturePIDResponse(
    val status: String, // PENDING, VERIFIED, FAILED, COMPLETE
    val message: String,
    val txnId: String? = null
)

data class FaceEnrollmentRequest(
    val authData: FaceAuthData,
    val consent: AbhaConsent = AbhaConsent()
)

data class FaceAuthData(
    val authMethods: List<String> = listOf("face_auth"),
    val face: FaceBlock
)

data class FaceBlock(
    val txnId: String,
    var aadhaar: String, // encrypted in repo before sending, same pattern as other requests
    val mobile: String
)

data class AbhaConsent(
    val code: String = "abha-enrollment",
    val version: String = "1.4"
)