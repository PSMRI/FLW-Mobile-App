package org.piramalswasthya.sakhi.model.dynamicEntity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard

class MoreDynamicEntitiesTest {

    // =====================================================
    // FilariaMDAFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `FilariaMDAFormResponseJsonEntity creation`() {
        val e = FilariaMDAFormResponseJsonEntity(hhId = 1L, visitDate = "d", visitMonth = "2026-04", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(1L, e.hhId)
        assertEquals("2026-04", e.visitMonth)
    }
    @Test fun `FilariaMDAFormResponseJsonEntity default id 0`() {
        val e = FilariaMDAFormResponseJsonEntity(hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, e.id)
    }
    @Test fun `FilariaMDAFormResponseJsonEntity default isSynced`() {
        val e = FilariaMDAFormResponseJsonEntity(hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }
    @Test fun `FilariaMDAFormResponseJsonEntity syncedAt null`() {
        val e = FilariaMDAFormResponseJsonEntity(hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertNull(e.syncedAt)
    }
    @Test fun `FilariaMDAFormResponseJsonEntity copy`() {
        val e = FilariaMDAFormResponseJsonEntity(hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(99L, e.copy(hhId = 99L).hhId)
    }
    @Test fun `FilariaMDAFormResponseJsonEntity same key fields`() {
        val a = FilariaMDAFormResponseJsonEntity(id = 1, hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(1, a.id)
        assertEquals(1L, a.hhId)
        assertEquals("f", a.formId)
    }

    // =====================================================
    // FilariaMDACampaignFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `FilariaMDACampaignFormResponseJsonEntity creation`() {
        val e = FilariaMDACampaignFormResponseJsonEntity(visitDate = "d", visitYear = "2026", formId = "f", version = 1, formDataJson = "{}")
        assertEquals("2026", e.visitYear)
    }
    @Test fun `FilariaMDACampaignFormResponseJsonEntity default syncState`() {
        val e = FilariaMDACampaignFormResponseJsonEntity(visitDate = "d", visitYear = "y", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(SyncState.UNSYNCED, e.syncState)
    }
    @Test fun `FilariaMDACampaignFormResponseJsonEntity syncState mutable`() {
        val e = FilariaMDACampaignFormResponseJsonEntity(visitDate = "d", visitYear = "y", formId = "f", version = 1, formDataJson = "{}")
        e.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, e.syncState)
    }
    @Test fun `FilariaMDACampaignFormResponseJsonEntity copy`() {
        val e = FilariaMDACampaignFormResponseJsonEntity(visitDate = "d", visitYear = "y", formId = "f", version = 1, formDataJson = "{}")
        assertEquals("2027", e.copy(visitYear = "2027").visitYear)
    }

    // =====================================================
    // MosquitoNetFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `MosquitoNetFormResponseJsonEntity creation`() {
        val e = MosquitoNetFormResponseJsonEntity(hhId = 1L, formId = "f", version = 1, visitDate = "d", formDataJson = "{}")
        assertEquals(1L, e.hhId)
    }
    @Test fun `MosquitoNetFormResponseJsonEntity default id 0`() {
        val e = MosquitoNetFormResponseJsonEntity(hhId = 1L, formId = "f", version = 1, visitDate = "d", formDataJson = "{}")
        assertEquals(0, e.id)
    }
    @Test fun `MosquitoNetFormResponseJsonEntity default isSynced`() {
        val e = MosquitoNetFormResponseJsonEntity(hhId = 1L, formId = "f", version = 1, visitDate = "d", formDataJson = "{}")
        assertFalse(e.isSynced)
    }
    @Test fun `MosquitoNetFormResponseJsonEntity copy`() {
        val e = MosquitoNetFormResponseJsonEntity(hhId = 1L, formId = "f", version = 1, visitDate = "d", formDataJson = "{}")
        assertEquals(50L, e.copy(hhId = 50L).hhId)
    }
    @Test fun `MosquitoNetFormResponseJsonEntity equals`() {
        val a = MosquitoNetFormResponseJsonEntity(id = 1, hhId = 1L, formId = "f", version = 1, visitDate = "d", formDataJson = "{}")
        val b = MosquitoNetFormResponseJsonEntity(id = 1, hhId = 1L, formId = "f", version = 1, visitDate = "d", formDataJson = "{}")
        assertEquals(a, b)
    }

    // =====================================================
    // BenIfaFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `BenIfaFormResponseJsonEntity creation`() {
        val e = BenIfaFormResponseJsonEntity(benId = 1L, hhId = 2L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(1L, e.benId)
        assertEquals(2L, e.hhId)
    }
    @Test fun `BenIfaFormResponseJsonEntity default id 0`() {
        val e = BenIfaFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, e.id)
    }
    @Test fun `BenIfaFormResponseJsonEntity default isSynced`() {
        val e = BenIfaFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }
    @Test fun `BenIfaFormResponseJsonEntity copy`() {
        val e = BenIfaFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(77L, e.copy(benId = 77L).benId)
    }

    // =====================================================
    // EyeSurgeryFormResponseJsonEntity Tests
    // =====================================================

    @Test fun `EyeSurgeryFormResponseJsonEntity creation`() {
        val e = EyeSurgeryFormResponseJsonEntity(benId = 1L, hhId = 2L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(1L, e.benId)
        assertEquals("m", e.visitMonth)
    }
    @Test fun `EyeSurgeryFormResponseJsonEntity default id 0`() {
        val e = EyeSurgeryFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertEquals(0, e.id)
    }
    @Test fun `EyeSurgeryFormResponseJsonEntity default isSynced`() {
        val e = EyeSurgeryFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertFalse(e.isSynced)
    }
    @Test fun `EyeSurgeryFormResponseJsonEntity copy`() {
        val e = EyeSurgeryFormResponseJsonEntity(benId = 1L, hhId = 1L, visitDate = "d", visitMonth = "m", formId = "f", version = 1, formDataJson = "{}")
        assertEquals("new_f", e.copy(formId = "new_f").formId)
    }

    // =====================================================
    // VisitCard Tests
    // =====================================================

    @Test fun `VisitCard creation`() {
        val vc = VisitCard("3 Months", "14-04-2026", true, false, false)
        assertEquals("3 Months", vc.visitDay)
        assertEquals("14-04-2026", vc.visitDate)
        assertTrue(vc.isCompleted)
        assertFalse(vc.isEditable)
        assertFalse(vc.isBabyDeath)
    }
    @Test fun `VisitCard incomplete`() {
        val vc = VisitCard("6 Months", "-", false, true, false)
        assertFalse(vc.isCompleted)
        assertTrue(vc.isEditable)
    }
    @Test fun `VisitCard with baby death`() {
        val vc = VisitCard("9 Months", "d", true, false, true)
        assertTrue(vc.isBabyDeath)
    }
    @Test fun `VisitCard copy`() {
        val vc = VisitCard("3 Months", "d", false, false, false)
        assertEquals(true, vc.copy(isCompleted = true).isCompleted)
    }
    @Test fun `VisitCard equals`() {
        val a = VisitCard("3 Months", "d", true, false, false)
        val b = VisitCard("3 Months", "d", true, false, false)
        assertEquals(a, b)
    }
    @Test fun `VisitCard not equals`() {
        val a = VisitCard("3 Months", "d", true, false, false)
        val b = VisitCard("6 Months", "d", true, false, false)
        assertNotEquals(a, b)
    }
}
