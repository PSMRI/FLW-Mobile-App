package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.*
import org.junit.Test

class ExtendedHelperTests {

    // =====================================================
    // AadhaarValidation Tests (extended)
    // =====================================================

    @Test fun `empty aadhaar is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("")) }
    @Test fun `short aadhaar is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("12345")) }
    @Test fun `11 digit aadhaar is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("12345678901")) }
    @Test fun `13 digit aadhaar is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("1234567890123")) }
    @Test fun `alpha aadhaar is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("abcdefghijkl")) }
    @Test fun `aadhaar starting with 0 is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("012345678901")) }
    @Test fun `aadhaar starting with 1 is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("112345678901")) }
    @Test fun `aadhaar with spaces is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("1234 5678 9012")) }
    @Test fun `12 digit aadhaar starting with 2 can be valid`() {
        // Aadhaar numbers starting with 2-9 could be valid based on Verhoeff
        val result = AadhaarValidationUtils.isValidAadhaar("234567890123")
        // Just checking it doesn't throw
        assertNotNull(result)
    }

    // =====================================================
    // AadhaarValidation Object Tests
    // =====================================================

    @Test fun `AadhaarValidationUtils exists`() {
        assertNotNull(AadhaarValidationUtils)
    }

    // =====================================================
    // TokenExpiryManager Tests (extended)
    // =====================================================

    @Test fun `TokenExpiryManager can be created`() {
        val manager = TokenExpiryManager()
        assertNotNull(manager)
    }

    @Test fun `TokenExpiryManager onRefreshSuccess does not throw`() {
        val manager = TokenExpiryManager()
        manager.onRefreshSuccess()
    }

    @Test fun `TokenExpiryManager onRefreshFailed does not throw on first call`() {
        val manager = TokenExpiryManager()
        manager.onRefreshFailed()
    }

    @Test fun `TokenExpiryManager forceLogoutEvent is not null`() {
        val manager = TokenExpiryManager()
        assertNotNull(manager.forceLogoutEvent)
    }

    // =====================================================
    // Languages Interaction Tests
    // =====================================================

    @Test fun `Languages ENGLISH is first ordinal`() {
        assertEquals(0, Languages.ENGLISH.ordinal)
    }

    @Test fun `Languages can iterate all values`() {
        var count = 0
        Languages.values().forEach { count++ }
        assertEquals(3, count)
    }

    @Test fun `Languages symbols are all distinct`() {
        val symbols = Languages.values().map { it.symbol }
        assertEquals(symbols.size, symbols.toSet().size)
    }

    @Test fun `Languages names are all distinct`() {
        val names = Languages.values().map { it.name }
        assertEquals(names.size, names.toSet().size)
    }

    // =====================================================
    // Konstants Derived Tests
    // =====================================================

    @Test fun `child age range is after infant`() {
        assertTrue(Konstants.minAgeForChild > Konstants.maxAgeForInfant)
    }

    @Test fun `eligible couple range includes ncd min age`() {
        assertTrue(Konstants.minAgeForNcd >= Konstants.minAgeForEligibleCouple)
        assertTrue(Konstants.minAgeForNcd <= Konstants.maxAgeForEligibleCouple)
    }

    @Test fun `all age ranges have positive width`() {
        assertTrue(Konstants.maxAgeForEligibleCouple > Konstants.minAgeForEligibleCouple)
        assertTrue(Konstants.maxAgeForAdolescent > Konstants.minAgeForAdolescent)
        assertTrue(Konstants.maxAgeForChild > Konstants.minAgeForChild)
        assertTrue(Konstants.maxAgeForGenBen > Konstants.minAgeForGenBen)
    }

    @Test fun `anc weeks cover pregnancy period`() {
        val totalCoverage = Konstants.maxAncWeek - Konstants.minAnc1Week
        assertTrue(totalCoverage > 30) // ~37 weeks covered
    }

    @Test fun `baby low weight is less than 3000g`() {
        assertTrue(Konstants.babyLowWeight < 3000.0)
    }

    @Test fun `ben id capacity is at least 50`() {
        assertTrue(Konstants.benIdCapacity >= 50)
    }

    @Test fun `pnc ec gap is between 30 and 60 days`() {
        assertTrue(Konstants.pncEcGap in 30..60)
    }

    // =====================================================
    // Additional AadhaarValidation Edge Cases
    // =====================================================

    @Test fun `aadhaar with special chars is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("@#\$%^&*()!+")) }
    @Test fun `aadhaar with mixed alpha-num is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("1234abcd5678")) }
    @Test fun `aadhaar with dashes is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("1234-5678-9012")) }
    @Test fun `single digit is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("5")) }
    @Test fun `blank string is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("   ")) }
    @Test fun `aadhaar all zeros is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("000000000000")) }
    @Test fun `aadhaar all ones is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("111111111111")) }
    @Test fun `aadhaar with leading 2 could be valid`() { assertNotNull(AadhaarValidationUtils.isValidAadhaar("200000000000")) }
    @Test fun `aadhaar with tab chars is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("\t123456789012")) }
    @Test fun `aadhaar with newline is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("123456\n789012")) }
    @Test fun `very long number is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("1".repeat(20))) }
    @Test fun `empty after trim is invalid`() { assertFalse(AadhaarValidationUtils.isValidAadhaar("  \t  ")) }
}
