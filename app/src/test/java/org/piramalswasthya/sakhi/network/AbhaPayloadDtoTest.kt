package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbhaPayloadDtoTest {

    @Test
    fun `AbhaTokenRequest holds explicit credentials and copies independently`() {
        val request = AbhaTokenRequest(
            clientId = "client-1",
            clientSecret = "secret-1",
            grantType = "client_credentials"
        )
        assertEquals("client-1", request.clientId)
        assertEquals("secret-1", request.clientSecret)
        assertEquals("client_credentials", request.grantType)

        val rotated = request.copy(clientSecret = "secret-2")
        assertEquals("client-1", rotated.clientId)
        assertEquals("secret-2", rotated.clientSecret)
        assertNotEquals(request, rotated)
        assertEquals(request, request.copy())
        assertEquals(request.hashCode(), request.copy().hashCode())
        assertTrue(request.toString().contains("client-1"))
    }

    @Test
    fun `AbhaTokenRequest uses default grantType when omitted`() {
        val request = AbhaTokenRequest(
            clientId = "client-1",
            clientSecret = "secret-1"
        )
        assertEquals("client_credentials", request.grantType)
    }

    @Test
    fun `AbhaTokenResponse exposes token lifetimes and differs only in the last field`() {
        val response = AbhaTokenResponse(
            accessToken = "access",
            expiresIn = 300,
            refreshExpiresIn = 1800,
            refreshToken = "refresh",
            tokenType = "Bearer"
        )
        assertEquals("access", response.accessToken)
        assertEquals(300, response.expiresIn)
        assertEquals(1800, response.refreshExpiresIn)
        assertEquals("refresh", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertNotEquals(response, response.copy(tokenType = "Basic"))
        assertEquals(response, response.copy())
        assertTrue(response.toString().contains("Bearer"))
    }

    @Test
    fun `AbhaGenerateAadhaarOtpRequest mutable login fields round trip`() {
        val request = AbhaGenerateAadhaarOtpRequest(
            txnId = "txn-1",
            scope = listOf("abha-enrol"),
            loginHint = "aadhaar",
            loginId = "encrypted-aadhaar",
            otpSystem = "aadhaar"
        )
        assertEquals("txn-1", request.txnId)
        assertEquals(listOf("abha-enrol"), request.scope)
        assertEquals("aadhaar", request.loginHint)

        request.loginId = "new-encrypted"
        request.otpSystem = "abdm"
        assertEquals("new-encrypted", request.loginId)
        assertEquals("abdm", request.otpSystem)
        assertNotEquals(
            request,
            request.copy(scope = listOf("abha-enrol", "mobile-verify"))
        )
    }

    @Test
    fun `AadhaarVerifyBioRequest mutable biometric fields round trip`() {
        val request = AadhaarVerifyBioRequest(
            aadhaar = "1234",
            bioType = "FMR",
            pid = "pid-blob"
        )
        assertEquals("1234", request.aadhaar)
        assertEquals("FMR", request.bioType)
        assertEquals("pid-blob", request.pid)

        request.aadhaar = "5678"
        request.bioType = "FIR"
        request.pid = "pid-blob-2"
        assertEquals("5678", request.aadhaar)
        assertEquals("FIR", request.bioType)
        assertEquals("pid-blob-2", request.pid)
        assertEquals(request, request.copy())
        assertEquals(request.hashCode(), request.copy().hashCode())
        assertNotEquals(request, request.copy(pid = "other"))
    }

    @Test
    fun `AbhaResendAadhaarOtpRequest and otp responses carry transaction ids`() {
        val resend = AbhaResendAadhaarOtpRequest(txnId = "txn-2")
        val aadhaarOtp = AbhaGenerateAadhaarOtpResponse(txnId = "txn-3")
        val aadhaarOtpV2 = AbhaGenerateAadhaarOtpResponseV2(
            txnId = "txn-4",
            mobileNumber = "9999900000",
            message = "sent"
        )
        val mobileOtpRequest = AbhaGenerateMobileOtpRequest(mobile = "9999900000", txnId = "txn-5")
        val mobileOtpResponse = AbhaGenerateMobileOtpResponse(txnId = "txn-6")
        val verifyMobileResponse = AbhaVerifyMobileOtpResponse(txnId = "txn-7")

        assertEquals("txn-2", resend.txnId)
        assertEquals("txn-3", aadhaarOtp.txnId)
        assertEquals("txn-4", aadhaarOtpV2.txnId)
        assertEquals("9999900000", aadhaarOtpV2.mobileNumber)
        assertEquals("sent", aadhaarOtpV2.message)
        assertEquals("9999900000", mobileOtpRequest.mobile)
        assertEquals("txn-5", mobileOtpRequest.txnId)
        assertEquals("txn-6", mobileOtpResponse.txnId)
        assertEquals("txn-7", verifyMobileResponse.txnId)
        assertNotEquals(aadhaarOtp, aadhaarOtp.copy(txnId = "other"))
        assertNotEquals(aadhaarOtpV2, aadhaarOtpV2.copy(message = "resent"))
        assertEquals(mobileOtpRequest, mobileOtpRequest.copy())
    }

    @Test
    fun `AbhaCheckAndGenerateMobileOtpResponse distinguishes linked and unlinked mobiles`() {
        val linked = AbhaCheckAndGenerateMobileOtpResponse(mobileLinked = true, txnId = "txn-8")
        val unlinked = linked.copy(mobileLinked = false)
        assertTrue(linked.mobileLinked)
        assertEquals("txn-8", linked.txnId)
        assertEquals(false, unlinked.mobileLinked)
        assertNotEquals(linked, unlinked)
        assertEquals(linked, linked.copy())
        assertTrue(linked.toString().contains("txn-8"))
    }

    @Test
    fun `AbhaVerifyAadhaarOtpRequest nests auth data and consent`() {
        val otp = Otp(
            timeStamp = "2023-01-01T10:00:00",
            txnId = "txn-9",
            otpValue = "123456",
            mobile = "9999911111"
        )
        val request = AbhaVerifyAadhaarOtpRequest(
            authData = AuthData(authMethods = listOf("otp"), otp = otp),
            consent = Consent(code = "abha-enrollment", version = "1.4")
        )
        assertEquals(listOf("otp"), request.authData.authMethods)
        assertEquals("123456", request.authData.otp.otpValue)
        assertEquals("abha-enrollment", request.consent.code)
        assertEquals("1.4", request.consent.version)

        otp.otpValue = "654321"
        otp.timeStamp = "2023-01-01T10:05:00"
        otp.mobile = "9999922222"
        assertEquals("654321", request.authData.otp.otpValue)
        assertEquals("2023-01-01T10:05:00", otp.timeStamp)
        assertEquals("9999922222", otp.mobile)
        assertEquals("txn-9", otp.txnId)
        assertNotEquals(request, request.copy(consent = Consent("other", "1.4")))
    }

    @Test
    fun `AbhaVerifyMobileOtpRequest nests auth data two and otp two`() {
        val otp2 = Otp2(timeStamp = "2023-02-01T09:00:00", txnId = "txn-10", otpValue = "111111")
        val request = AbhaVerifyMobileOtpRequest(
            scope = listOf("abha-enrol", "mobile-verify"),
            authData = AuthData2(authMethods = listOf("otp"), otp = otp2)
        )
        assertEquals(listOf("abha-enrol", "mobile-verify"), request.scope)
        assertEquals(listOf("otp"), request.authData.authMethods)
        assertEquals("111111", request.authData.otp.otpValue)
        assertEquals("txn-10", otp2.txnId)

        otp2.otpValue = "222222"
        otp2.timeStamp = "2023-02-01T09:10:00"
        assertEquals("222222", request.authData.otp.otpValue)
        assertEquals("2023-02-01T09:10:00", otp2.timeStamp)
        assertEquals(otp2, otp2.copy())
        assertEquals(otp2.hashCode(), otp2.copy().hashCode())
        assertNotEquals(otp2, otp2.copy(otpValue = "333333"))
        assertTrue(otp2.toString().contains("txn-10"))
    }

    @Test
    fun `LoginVerifyOtpRequest nests auth data three and otp three`() {
        val otp3 = Otp3(txnId = "txn-11", otpValue = "444444")
        val request = LoginVerifyOtpRequest(
            scope = listOf("abha-login", "mobile-verify"),
            authData = AuthData3(authMethods = listOf("otp"), otp = otp3)
        )
        assertEquals(2, request.scope.size)
        assertEquals("444444", request.authData.otp.otpValue)
        assertEquals(listOf("otp"), request.authData.authMethods)

        otp3.otpValue = "555555"
        assertEquals("555555", request.authData.otp.otpValue)
        assertEquals("txn-11", otp3.txnId)
        assertNotEquals(otp3, otp3.copy(txnId = "txn-12"))
        assertEquals(otp3, otp3.copy())
    }

    @Test
    fun `LoginGenerateOtpRequest mutable login id and response pairing`() {
        val request = LoginGenerateOtpRequest(
            scope = listOf("abha-login"),
            loginHint = "mobile",
            loginId = "encrypted-mobile",
            otpSystem = "abdm",
            txnId = ""
        )
        assertEquals(listOf("abha-login"), request.scope)
        assertEquals("mobile", request.loginHint)
        assertEquals("abdm", request.otpSystem)
        assertEquals("", request.txnId)

        request.loginId = "encrypted-mobile-2"
        assertEquals("encrypted-mobile-2", request.loginId)

        val response = LoginGenerateOtpResponse(txnId = "txn-13", message = "otp sent")
        assertEquals("txn-13", response.txnId)
        assertEquals("otp sent", response.message)
        assertNotEquals(response, response.copy(message = "otp resent"))
        assertNotEquals(request, request.copy(txnId = "txn-13"))
    }

    @Test
    fun `SearchAbhaRequest mutable mobile and response holds matching abha entries`() {
        val request = SearchAbhaRequest(scope = listOf("search-abha"), mobile = "9999933333")
        assertEquals(listOf("search-abha"), request.scope)
        request.mobile = "9999944444"
        assertEquals("9999944444", request.mobile)

        val first = Abha(index = 0, ABHANumber = "11-1111-1111-1111", name = "Asha", gender = "F")
        val second = Abha(index = 1, ABHANumber = "22-2222-2222-2222", name = "Meena", gender = "F")
        val response = SearchAbhaResponse(txnId = "txn-14", ABHA = listOf(first, second))

        assertEquals("txn-14", response.txnId)
        assertEquals(2, response.ABHA.size)
        assertEquals(0, first.index)
        assertEquals("11-1111-1111-1111", first.ABHANumber)
        assertEquals("Asha", first.name)
        assertEquals("F", first.gender)
        assertEquals(1, second.index)
        assertNotEquals(first, second)
        assertNotEquals(first, first.copy(gender = "M"))
        assertEquals(first, first.copy())
        assertEquals(first.hashCode(), first.copy().hashCode())
        assertNotEquals(response, response.copy(ABHA = listOf(first)))
    }

    @Test
    fun `SearchAbhaResponse tolerates an empty abha list`() {
        val response = SearchAbhaResponse(txnId = "txn-15", ABHA = emptyList())
        assertTrue(response.ABHA.isEmpty())
        assertEquals("txn-15", response.txnId)
        assertTrue(response.toString().contains("txn-15"))
    }

    @Test
    fun `CreateAbhaIdRequest tolerates null optional profile fields`() {
        val request = CreateAbhaIdRequest(
            email = null,
            firstName = null,
            healthId = null,
            lastName = null,
            middleName = null,
            password = null,
            profilePhoto = null,
            txnId = "txn-16"
        )
        assertNull(request.email)
        assertNull(request.firstName)
        assertNull(request.healthId)
        assertNull(request.lastName)
        assertNull(request.middleName)
        assertNull(request.password)
        assertNull(request.profilePhoto)
        assertEquals("txn-16", request.txnId)
        assertEquals(request, request.copy())
        assertNotEquals(request, request.copy(txnId = "txn-17"))
    }

    @Test
    fun `CreateAbhaIdRequest differing only in the last field is unequal after all others compare equal`() {
        val request = CreateAbhaIdRequest(
            email = "a@b.com",
            firstName = "Asha",
            lastName = "Devi",
            middleName = "K",
            healthId = "asha@abdm",
            password = "secret",
            profilePhoto = "photo",
            txnId = "txn-18"
        )
        assertEquals("a@b.com", request.email)
        assertEquals("Asha", request.firstName)
        assertEquals("Devi", request.lastName)
        assertEquals("K", request.middleName)
        assertEquals("asha@abdm", request.healthId)
        assertEquals("secret", request.password)
        assertEquals("photo", request.profilePhoto)
        assertNotEquals(request, request.copy(txnId = "txn-19"))
        assertEquals(request.hashCode(), request.copy().hashCode())
        assertTrue(request.toString().contains("Asha"))
    }

    @Test
    fun `CreateAbhaIdGovRequest carries demographic and location codes`() {
        val request = CreateAbhaIdGovRequest(
            aadharNumber = 123456789012L,
            benefitName = "PMJAY",
            consentHealthId = true,
            dateOfBirth = "1990-05-04",
            gender = "F",
            name = "Asha Devi",
            stateCode = 22,
            districtCode = 401
        )
        assertEquals(123456789012L, request.aadharNumber)
        assertEquals("PMJAY", request.benefitName)
        assertTrue(request.consentHealthId)
        assertEquals("1990-05-04", request.dateOfBirth)
        assertEquals("F", request.gender)
        assertEquals("Asha Devi", request.name)
        assertEquals(22, request.stateCode)
        assertEquals(401, request.districtCode)

        assertNotEquals(request, request.copy(districtCode = 402))
        assertNotEquals(request, request.copy(consentHealthId = false))
        assertEquals(request, request.copy())
        assertEquals(request.hashCode(), request.copy().hashCode())
        assertTrue(request.toString().contains("PMJAY"))
    }

    @Test
    fun `AbhaPublicCertificateResponse and code responses expose lookup values`() {
        val cert = AbhaPublicCertificateResponse(
            publicKey = "-----BEGIN PUBLIC KEY-----",
            encryptionAlgorithm = "RSA"
        )
        assertEquals("-----BEGIN PUBLIC KEY-----", cert.publicKey)
        assertEquals("RSA", cert.encryptionAlgorithm)
        assertNotEquals(cert, cert.copy(encryptionAlgorithm = "AES"))

        val district = DistrictCodeResponse(code = "401", name = "Raipur")
        val state = StateCodeResponse(code = "22", name = "Chhattisgarh", districts = listOf(district))
        assertEquals("22", state.code)
        assertEquals("Chhattisgarh", state.name)
        assertEquals(1, state.districts?.size)
        assertEquals("401", district.code)
        assertEquals("Raipur", district.name)
        assertNotEquals(district, district.copy(name = "Durg"))
        assertEquals(district, district.copy())
    }

    @Test
    fun `StateCodeResponse tolerates a null district list`() {
        val state = StateCodeResponse(code = "23", name = "Madhya Pradesh", districts = null)
        assertNull(state.districts)
        assertEquals("23", state.code)
        assertNotEquals(state, state.copy(districts = emptyList()))
        assertEquals(state.hashCode(), state.copy().hashCode())
    }

    @Test
    fun `MapHIDtoBeneficiary mutable provider and profile fields round trip`() {
        val profile = ABHAProfile(firstName = "Asha", lastName = "Devi", ABHANumber = "11-1111", photo = "photo-blob")
        val map = MapHIDtoBeneficiary(
            beneficiaryRegID = 500L,
            beneficiaryID = 501L,
            healthId = "asha@abdm",
            healthIdNumber = "11-1111-1111-1111",
            providerServiceMapId = null,
            createdBy = null,
            message = null,
            txnId = null,
            ABHAProfile = null,
            isNew = null
        )
        assertEquals(500L, map.beneficiaryRegID)
        assertEquals(501L, map.beneficiaryID)
        assertEquals("asha@abdm", map.healthId)
        assertEquals("11-1111-1111-1111", map.healthIdNumber)
        assertNull(map.providerServiceMapId)
        assertNull(map.ABHAProfile)
        assertNull(map.isNew)

        map.providerServiceMapId = 12
        map.createdBy = "asha1"
        map.message = "mapped"
        map.txnId = "txn-20"
        map.ABHAProfile = profile
        map.isNew = true

        assertEquals(12, map.providerServiceMapId)
        assertEquals("asha1", map.createdBy)
        assertEquals("mapped", map.message)
        assertEquals("txn-20", map.txnId)
        assertNotNull(map.ABHAProfile)
        assertEquals("Asha", map.ABHAProfile?.firstName)
        assertEquals("Devi", map.ABHAProfile?.lastName)
        assertEquals("11-1111", map.ABHAProfile?.ABHANumber)
        assertEquals("photo-blob", map.ABHAProfile?.photo)
        assertEquals(true, map.isNew)
        assertNotEquals(map, map.copy(isNew = false))
        assertEquals(map, map.copy())
    }
}
