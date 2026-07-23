package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Extra coverage for JsonAdapters.kt mappers/helpers not exercised by
 * DtoToCacheMappingTest or ScreeningDtoMappingTest:
 *  - ABHAGeneratedDTO.toCache()
 *  - LeprosyFollowUpDTO.toCache()
 *  - the package-level date helpers getLongFromDate / getLongFromDateMultipleSupport / convertIsoDateToMillis
 */
class JsonAdaptersTest {

    // ---------------- ABHAGeneratedDTO.toCache() ----------------

    private fun abhaDto() = ABHAGeneratedDTO(
        beneficiaryID = 10L,
        beneficiaryRegID = 20L,
        benName = "Asha",
        createdBy = "creator",
        message = "created",
        txnId = "txn-1",
        providerServiceMapId = 7
    )

    @Test fun `ABHAGeneratedDTO toCache maps identifiers and names`() {
        val cache = abhaDto().toCache()
        assertEquals(10L, cache.beneficiaryID)
        assertEquals(20L, cache.beneficiaryRegID)
        assertEquals("Asha", cache.benName)
        assertEquals("txn-1", cache.txnId)
        assertEquals("creator", cache.createdBy)
        assertEquals(7, cache.providerServiceMapId)
    }

    @Test fun `ABHAGeneratedDTO toCache carries abha flags and optional surname`() {
        val cache = abhaDto().copy(
            benSurname = "Devi",
            healthId = "hid",
            healthIdNumber = "hidn",
            isNewAbha = true
        ).toCache()
        assertEquals("Devi", cache.benSurname)
        assertEquals("hid", cache.healthId)
        assertEquals("hidn", cache.healthIdNumber)
        assertTrue(cache.isNewAbha)
    }

    @Test fun `ABHAGeneratedDTO toCache defaults isNewAbha false`() {
        assertEquals(false, abhaDto().toCache().isNewAbha)
    }

    // ---------------- LeprosyFollowUpDTO.toCache() ----------------

    private fun leprosyFollowUpDto() = LeprosyFollowUpDTO(
        benId = 55L,
        visitNumber = 3,
        createdBy = "creator",
        createdDate = "2023-01-10",
        modifiedBy = "modifier",
        lastModDate = "2023-01-11"
    )

    @Test fun `LeprosyFollowUpDTO toCache maps ids and visit`() {
        val cache = leprosyFollowUpDto().toCache()
        assertEquals(55L, cache.benId)
        assertEquals(3, cache.visitNumber)
    }

    @Test fun `LeprosyFollowUpDTO toCache sets synced state and audit fields`() {
        val cache = leprosyFollowUpDto().toCache()
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertEquals("creator", cache.createdBy)
        assertEquals("modifier", cache.modifiedBy)
    }

    @Test fun `LeprosyFollowUpDTO toCache passes through clinical fields`() {
        val cache = leprosyFollowUpDto().copy(
            treatmentStatus = "Ongoing",
            leprosyStatus = "Positive",
            typeOfLeprosy = "MB",
            referredTo = 2,
            referToName = "PHC"
        ).toCache()
        assertEquals("Ongoing", cache.treatmentStatus)
        assertEquals("Positive", cache.leprosyStatus)
        assertEquals("MB", cache.typeOfLeprosy)
        assertEquals(2, cache.referredTo)
        assertEquals("PHC", cache.referToName)
    }

    @Test fun `LeprosyFollowUpDTO toCache parses valid date to positive long`() {
        assertTrue(leprosyFollowUpDto().toCache().createdDate > 0L)
    }

    // ---------------- getLongFromDate ----------------

    @Test fun `getLongFromDate returns zero for null`() {
        assertEquals(0L, getLongFromDate(null))
    }

    @Test fun `getLongFromDate returns zero for blank`() {
        assertEquals(0L, getLongFromDate("   "))
    }

    @Test fun `getLongFromDate returns zero for literal null string`() {
        assertEquals(0L, getLongFromDate("null"))
        assertEquals(0L, getLongFromDate("NULL"))
    }

    @Test fun `getLongFromDate parses yyyy-MM-dd to positive long`() {
        assertTrue(getLongFromDate("2023-06-15") > 0L)
    }

    @Test fun `getLongFromDate parses long format fallback`() {
        assertTrue(getLongFromDate("Jun 5, 2023 3:30:00 PM") > 0L)
    }

    @Test fun `getLongFromDate returns zero for unparseable`() {
        assertEquals(0L, getLongFromDate("not-a-date"))
    }

    // ---------------- getLongFromDateMultipleSupport ----------------

    @Test fun `getLongFromDateMultipleSupport returns null for null`() {
        assertNull(getLongFromDateMultipleSupport(null))
    }

    @Test fun `getLongFromDateMultipleSupport returns null for blank`() {
        assertNull(getLongFromDateMultipleSupport(""))
    }

    @Test fun `getLongFromDateMultipleSupport returns null for epoch sentinel`() {
        assertNull(getLongFromDateMultipleSupport("1970-01-01"))
    }

    @Test fun `getLongFromDateMultipleSupport parses iso date`() {
        assertNotNull(getLongFromDateMultipleSupport("2023-06-15"))
    }

    @Test fun `getLongFromDateMultipleSupport parses slash date`() {
        assertNotNull(getLongFromDateMultipleSupport("15/06/2023"))
    }

    @Test fun `getLongFromDateMultipleSupport returns null for garbage`() {
        assertNull(getLongFromDateMultipleSupport("garbage"))
    }

    // ---------------- convertIsoDateToMillis ----------------

    @Test fun `convertIsoDateToMillis returns zero for null`() {
        assertEquals(0L, convertIsoDateToMillis(null))
    }

    @Test fun `convertIsoDateToMillis returns zero for empty`() {
        assertEquals(0L, convertIsoDateToMillis(""))
    }

    @Test fun `convertIsoDateToMillis parses valid iso offset date`() {
        assertTrue(convertIsoDateToMillis("2024-01-01T00:00:00.000+05:30") > 0L)
    }

    @Test fun `convertIsoDateToMillis returns zero for garbage`() {
        assertEquals(0L, convertIsoDateToMillis("garbage"))
    }
}
