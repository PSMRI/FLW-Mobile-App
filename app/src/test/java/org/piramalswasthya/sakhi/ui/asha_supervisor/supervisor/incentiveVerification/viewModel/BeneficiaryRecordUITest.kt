package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BeneficiaryRecordUITest {

    private fun buildRecord(id: Long = 1L) = BeneficiaryRecordUI(
        id = id,
        activityId = 10L,
        ashaId = 5,
        benId = 100L,
        amount = 50L,
        name = "Ben1",
        startDate = "2026-01-01",
        activityDec = "desc",
        groupName = "grp",
        approvalStatus = 101,
        rchId = "rch1",
        abhaNumber = "abha1",
        isClaimed = true,
        verifiedByUserName = "sup"
    )

    @Test
    fun `BeneficiaryRecordUI exposes all constructor fields`() {
        val record = buildRecord()

        assertEquals(1L, record.id)
        assertEquals(10L, record.activityId)
        assertEquals(5, record.ashaId)
        assertEquals(100L, record.benId)
        assertEquals(50L, record.amount)
        assertEquals("Ben1", record.name)
        assertEquals("2026-01-01", record.startDate)
        assertEquals("desc", record.activityDec)
        assertEquals("grp", record.groupName)
        assertEquals(101, record.approvalStatus)
        assertEquals("rch1", record.rchId)
        assertEquals("abha1", record.abhaNumber)
        assertEquals(true, record.isClaimed)
        assertEquals("sup", record.verifiedByUserName)
    }

    @Test
    fun `BeneficiaryRecordUI supports nullable optional fields`() {
        val record = BeneficiaryRecordUI(
            id = 1L,
            activityId = 10L,
            ashaId = 5,
            benId = 100L,
            amount = 50L,
            name = null,
            startDate = null,
            activityDec = null,
            groupName = null,
            approvalStatus = null,
            rchId = null,
            abhaNumber = null,
            isClaimed = null,
            verifiedByUserName = null
        )

        assertEquals(null, record.name)
        assertEquals(null, record.approvalStatus)
        assertEquals(null, record.isClaimed)
    }

    @Test
    fun `BeneficiaryRecordUI equals hashCode toString and copy behave per data class contract`() {
        val first = buildRecord()
        val same = buildRecord()
        val different = buildRecord(id = 2L)

        assertEquals(first, same)
        assertEquals(first.hashCode(), same.hashCode())
        assertNotEquals(first, different)
        assertTrue(first.toString().contains("Ben1"))

        val copied = first.copy(name = "Ben2")
        assertEquals("Ben2", copied.name)
        assertEquals(first.id, copied.id)
    }

    @Test
    fun `BeneficiaryRecordUI component functions destructure in declared order`() {
        val record = buildRecord()

        val (id, activityId, ashaId, benId, amount, name, startDate, activityDec, groupName,
            approvalStatus, rchId, abhaNumber, isClaimed, verifiedByUserName) = record

        assertEquals(record.id, id)
        assertEquals(record.activityId, activityId)
        assertEquals(record.ashaId, ashaId)
        assertEquals(record.benId, benId)
        assertEquals(record.amount, amount)
        assertEquals(record.name, name)
        assertEquals(record.startDate, startDate)
        assertEquals(record.activityDec, activityDec)
        assertEquals(record.groupName, groupName)
        assertEquals(record.approvalStatus, approvalStatus)
        assertEquals(record.rchId, rchId)
        assertEquals(record.abhaNumber, abhaNumber)
        assertEquals(record.isClaimed, isClaimed)
        assertEquals(record.verifiedByUserName, verifiedByUserName)
    }

    @Test
    fun `BeneficiaryUiState Loading is a singleton object`() {
        assertEquals(BeneficiaryUiState.Loading, BeneficiaryUiState.Loading)
    }

    @Test
    fun `BeneficiaryUiState Success exposes records and supports equality`() {
        val records = listOf(buildRecord())
        val success = BeneficiaryUiState.Success(records)
        val sameSuccess = BeneficiaryUiState.Success(records)

        assertEquals(records, success.records)
        assertEquals(success, sameSuccess)
        assertEquals(success.hashCode(), sameSuccess.hashCode())
        assertTrue(success.toString().contains("Success"))
    }

    @Test
    fun `BeneficiaryUiState Error exposes message and supports equality`() {
        val error = BeneficiaryUiState.Error("boom")
        val sameError = BeneficiaryUiState.Error("boom")

        assertEquals("boom", error.message)
        assertEquals(error, sameError)
        assertNotEquals(error, BeneficiaryUiState.Error("other"))
    }
}
