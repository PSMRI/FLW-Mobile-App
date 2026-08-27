package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import java.text.SimpleDateFormat
import java.util.Locale

class HomeVisitDomainFromEntityTest {

    private fun entity(
        visitDate: String = "2026-04-14",
        isSynced: Boolean = false,
        createdAt: Long = 1_600_000_000_000L
    ) = ANCFormResponseJsonEntity(
        id = 5,
        benId = 10L,
        visitDay = "Day1",
        visitDate = visitDate,
        formId = "anc_form",
        version = 1,
        formDataJson = "{}",
        isSynced = isSynced,
        createdAt = createdAt
    )

    @Test fun `fromEntity maps id benId and visitNumber`() {
        val domain = HomeVisitDomain.fromEntity(entity(), visitNumber = 3)
        assertEquals(5, domain.id)
        assertEquals(10L, domain.benId)
        assertEquals(3, domain.visitNumber)
    }

    @Test fun `fromEntity parses valid visitDate into epoch millis`() {
        val domain = HomeVisitDomain.fromEntity(entity(visitDate = "2026-04-14"), visitNumber = 1)
        val expected = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2026-04-14")!!.time
        assertEquals(expected, domain.visitDate)
        assertEquals("2026-04-14", domain.visitDateString)
    }

    @Test fun `fromEntity falls back to createdAt when visitDate is unparsable`() {
        val domain = HomeVisitDomain.fromEntity(
            entity(visitDate = "not-a-date", createdAt = 1_650_000_000_000L),
            visitNumber = 1
        )
        assertEquals(1_650_000_000_000L, domain.visitDate)
        assertEquals("not-a-date", domain.visitDateString)
    }

    @Test fun `fromEntity falls back to createdAt when visitDate is blank`() {
        val domain = HomeVisitDomain.fromEntity(
            entity(visitDate = "", createdAt = 1_700_000_000_000L),
            visitNumber = 1
        )
        assertEquals(1_700_000_000_000L, domain.visitDate)
    }

    @Test fun `fromEntity maps synced state to SYNCED`() {
        val domain = HomeVisitDomain.fromEntity(entity(isSynced = true), visitNumber = 1)
        assertEquals(SyncState.SYNCED, domain.syncState)
        assertTrue(domain.isSynced)
    }

    @Test fun `fromEntity maps unsynced state to UNSYNCED`() {
        val domain = HomeVisitDomain.fromEntity(entity(isSynced = false), visitNumber = 1)
        assertEquals(SyncState.UNSYNCED, domain.syncState)
        assertEquals(false, domain.isSynced)
    }

    @Test fun `fromEntity copies formDataJson`() {
        val domain = HomeVisitDomain.fromEntity(entity(), visitNumber = 1)
        assertEquals("{}", domain.formDataJson)
    }
}
