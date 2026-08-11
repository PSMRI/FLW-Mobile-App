package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Tests for the pure mapper functions in PNC.kt:
 *  - PNCVisitCache.asDomainModel()
 *  - PNCVisitCache.asNetworkModel()
 *  - PNCNetwork.asCacheModel()
 *  - BenWithDoAndPncCache.asBasicDomainModelForPNC()
 */
class PncMappingTest {

    private fun benBasic(benId: Long = 1L) = BenBasicCache(
        benId = benId,
        hhId = 1L,
        regDate = 1_600_000_000_000L,
        benName = "Jane",
        benSurname = "Doe",
        gender = Gender.FEMALE,
        dob = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365L * 25),
        relToHeadId = 2,
        mobileNo = 9998887771L,
        hrpStatus = false,
        syncState = SyncState.SYNCED,
        reproductiveStatusId = 2,
        lastMenstrualPeriod = null,
        isKid = false,
        immunizationStatus = false,
        villageId = 1,
        abhaId = null,
        isNewAbha = false,
        cbacFilled = false,
        cbacSyncState = SyncState.SYNCED,
        cdrFilled = false,
        cdrSyncState = SyncState.SYNCED,
        mdsrFilled = false,
        mdsrSyncState = SyncState.SYNCED,
        pmsmaSyncState = SyncState.SYNCED,
        pmsmaFilled = false,
        hbncFilled = false,
        hbycFilled = false,
        pwrFilled = false,
        pwrSyncState = SyncState.SYNCED,
        doSyncState = SyncState.SYNCED,
        irSyncState = SyncState.SYNCED,
        crSyncState = SyncState.SYNCED,
        ecrFilled = false,
        ectFilled = false,
        tbsnFilled = false,
        tbsnSyncState = SyncState.SYNCED,
        tbspFilled = false,
        tbspSyncState = SyncState.SYNCED,
        hrppaFilled = false,
        hrpnpaFilled = false,
        hrpmbpFilled = false,
        hrptFilled = false,
        hrptrackingDone = false,
        hrnptrackingDone = false,
        hrnptFilled = false,
        hrppaSyncState = SyncState.SYNCED,
        hrpnpaSyncState = SyncState.SYNCED,
        hrpmbpSyncState = SyncState.SYNCED,
        hrptSyncState = SyncState.SYNCED,
        hrnptSyncState = SyncState.SYNCED,
        isDelivered = true,
        pwHrp = false,
        irFilled = false,
        isMdsr = false,
        crFilled = false,
        doFilled = true,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = true
    )

    private fun deliveryOutcome(
        benId: Long = 1L,
        isActive: Boolean = true,
        dateOfDelivery: Long? = null
    ) = DeliveryOutcomeCache(
        benId = benId,
        isActive = isActive,
        dateOfDelivery = dateOfDelivery,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.SYNCED
    )

    private fun pncRecord(
        benId: Long = 1L,
        pncPeriod: Int,
        pncDate: Long,
        syncState: SyncState = SyncState.SYNCED
    ) = PNCVisitCache(
        benId = benId,
        pncPeriod = pncPeriod,
        isActive = true,
        pncDate = pncDate,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = syncState
    )

    private fun cache(
        benId: Long = 1L,
        pncPeriod: Int = 1,
        isActive: Boolean = true,
        pncDate: Long = 1_600_000_000_000L
    ) = PNCVisitCache(
        benId = benId,
        pncPeriod = pncPeriod,
        isActive = isActive,
        pncDate = pncDate,
        createdBy = "creator",
        updatedBy = "updater",
        syncState = SyncState.UNSYNCED
    )

    // ---------------------------------------------------------------
    // PNCVisitCache.asDomainModel()
    // ---------------------------------------------------------------

    @Test
    fun `asDomainModel maps benId visitNumber and syncState`() {
        val domain = cache(benId = 9L, pncPeriod = 7).asDomainModel()

        assertEquals(9L, domain.benId)
        assertEquals(7, domain.visitNumber)
        assertEquals(SyncState.UNSYNCED, domain.syncState)
    }

    // ---------------------------------------------------------------
    // PNCVisitCache.asNetworkModel()
    // ---------------------------------------------------------------

    @Test
    fun `asNetworkModel maps core fields`() {
        val net = cache(benId = 12L, pncPeriod = 3, isActive = true).copy(
            id = 5L,
            ifaTabsGiven = 30,
            contraceptionMethod = "IUCD",
            referralFacility = "PHC",
            motherDeath = false
        ).asNetworkModel()

        assertEquals(5L, net.id)
        assertEquals(12L, net.benId)
        assertEquals(3, net.pncPeriod)
        assertTrue(net.isActive)
        assertEquals(30, net.ifaTabsGiven)
        assertEquals("IUCD", net.contraceptionMethod)
        assertEquals("PHC", net.referralFacility)
        assertNotNull(net.pncDate)
        assertNotNull(net.createdDate)
    }

    // ---------------------------------------------------------------
    // PNCNetwork.asCacheModel()
    // ---------------------------------------------------------------

    @Test
    fun `PNCNetwork asCacheModel maps core fields and sets synced`() {
        val net = PNCNetwork(
            id = 1L,
            benId = 77L,
            pncPeriod = 6,
            isActive = true,
            pncDate = "2024-01-15",
            ifaTabsGiven = 45,
            anyContraceptionMethod = true,
            contraceptionMethod = "Condom",
            sterilisationDate = "2024-01-10",
            otherPpcMethod = null,
            anyDangerSign = null,
            motherDangerSign = null,
            otherDangerSign = null,
            referralFacility = "CHC",
            motherDeath = false,
            deathDate = null,
            causeOfDeath = null,
            otherDeathCause = null,
            placeOfDeath = null,
            otherPlaceOfDeath = null,
            remarks = "ok",
            createdBy = "c",
            createdDate = "2024-01-15",
            updatedBy = "u",
            updatedDate = "2024-01-16"
        )

        val cache = net.asCacheModel()

        assertEquals(77L, cache.benId)
        assertEquals(6, cache.pncPeriod)
        assertTrue(cache.isActive)
        assertEquals(45, cache.ifaTabsGiven)
        assertEquals("Condom", cache.contraceptionMethod)
        assertEquals("CHC", cache.referralFacility)
        assertEquals("ok", cache.remarks)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------------------------------------------------------
    // Branch-variant coverage: mother-death path, no-contraception path,
    // and differing pncPeriod / isActive on PNCNetwork.asCacheModel.
    // ---------------------------------------------------------------

    @Test
    fun `asNetworkModel mother-death path`() {
        val net = cache(benId = 21L, pncPeriod = 42).copy(
            id = 9L,
            motherDeath = true,
            referralFacility = "DH"
        ).asNetworkModel()
        assertEquals(9L, net.id)
        assertEquals(21L, net.benId)
        assertEquals(42, net.pncPeriod)
        assertNotNull(net.pncDate)
    }

    @Test
    fun `asDomainModel inactive`() {
        val domain = cache(benId = 3L, pncPeriod = 21, isActive = false).asDomainModel()
        assertEquals(3L, domain.benId)
        assertEquals(21, domain.visitNumber)
    }

    @Test
    fun `PNCNetwork asCacheModel no-contraception mother-death`() {
        val net = PNCNetwork(
            id = 2L,
            benId = 78L,
            pncPeriod = 1,
            isActive = false,
            pncDate = "2024-02-15",
            ifaTabsGiven = 0,
            anyContraceptionMethod = false,
            contraceptionMethod = null,
            sterilisationDate = "",
            otherPpcMethod = null,
            anyDangerSign = "Yes",
            motherDangerSign = "Fever",
            otherDangerSign = null,
            referralFacility = "DH",
            motherDeath = true,
            deathDate = "2024-02-20",
            causeOfDeath = "Sepsis",
            otherDeathCause = null,
            placeOfDeath = "Hospital",
            otherPlaceOfDeath = null,
            remarks = "critical",
            createdBy = "c",
            createdDate = "2024-02-15",
            updatedBy = "u",
            updatedDate = "2024-02-16"
        )
        val cache = net.asCacheModel()
        assertEquals(78L, cache.benId)
        assertEquals(1, cache.pncPeriod)
        assertEquals("critical", cache.remarks)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------------------------------------------------------
    // BenWithDoAndPncCache.asBasicDomainModelForPNC()
    // ---------------------------------------------------------------

    @Test
    fun `asBasicDomainModelForPNC with no active delivery outcome allows immediate fill`() {
        val benWithDoAndPnc = BenWithDoAndPncCache(
            ben = benBasic(),
            deliveryOutcomeCache = listOf(deliveryOutcome(isActive = false, dateOfDelivery = null)),
            savedPncRecords = emptyList()
        )

        val domain = benWithDoAndPnc.asBasicDomainModelForPNC()

        assertEquals("", domain.deliveryDate)
        assertTrue(domain.allowFill)
        assertEquals(0L, domain.pncDate)
        assertTrue(domain.savedPncRecords.isEmpty())
        assertNull(domain.syncState)
        assertEquals(1L, domain.ben.benId)
    }

    @Test
    fun `asBasicDomainModelForPNC computes days since delivery and formats delivery date`() {
        val deliveryMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val benWithDoAndPnc = BenWithDoAndPncCache(
            ben = benBasic(),
            deliveryOutcomeCache = listOf(deliveryOutcome(isActive = true, dateOfDelivery = deliveryMillis)),
            savedPncRecords = emptyList()
        )

        val domain = benWithDoAndPnc.asBasicDomainModelForPNC()

        val expectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(deliveryMillis))
        assertEquals(expectedDate, domain.deliveryDate)
        assertTrue(domain.allowFill)
        assertEquals(0L, domain.pncDate)
    }

    @Test
    fun `asBasicDomainModelForPNC picks active delivery outcome and reports synced when all records synced`() {
        val deliveryMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val benWithDoAndPnc = BenWithDoAndPncCache(
            ben = benBasic(benId = 5L),
            deliveryOutcomeCache = listOf(
                deliveryOutcome(benId = 5L, isActive = false, dateOfDelivery = null),
                deliveryOutcome(benId = 5L, isActive = true, dateOfDelivery = deliveryMillis)
            ),
            savedPncRecords = listOf(
                pncRecord(benId = 5L, pncPeriod = 1, pncDate = 111L, syncState = SyncState.SYNCED),
                pncRecord(benId = 5L, pncPeriod = 3, pncDate = 333L, syncState = SyncState.SYNCED)
            )
        )

        val domain = benWithDoAndPnc.asBasicDomainModelForPNC()

        val expectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(deliveryMillis))
        assertEquals(expectedDate, domain.deliveryDate)
        assertEquals(333L, domain.pncDate)
        assertTrue(domain.allowFill)
        assertEquals(SyncState.SYNCED, domain.syncState)
        assertEquals(2, domain.savedPncRecords.size)
    }

    @Test
    fun `asBasicDomainModelForPNC disallows fill when max pnc period reached and reports unsynced`() {
        val deliveryMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(42)
        val benWithDoAndPnc = BenWithDoAndPncCache(
            ben = benBasic(benId = 8L),
            deliveryOutcomeCache = listOf(
                deliveryOutcome(benId = 8L, isActive = true, dateOfDelivery = deliveryMillis)
            ),
            savedPncRecords = listOf(
                pncRecord(benId = 8L, pncPeriod = 42, pncDate = 999L, syncState = SyncState.UNSYNCED)
            )
        )

        val domain = benWithDoAndPnc.asBasicDomainModelForPNC()

        assertFalse(domain.allowFill)
        assertEquals(999L, domain.pncDate)
        assertEquals(SyncState.UNSYNCED, domain.syncState)
    }

    @Test
    fun `asNetworkModel maps null deathDate to null`() {
        val net = cache(benId = 15L, pncPeriod = 5).copy(deathDate = null).asNetworkModel()

        assertNull(net.deathDate)
    }

    // ---------------------------------------------------------------
    // Generated members / property accessors for PNCVisitCache & PNCNetwork
    // ---------------------------------------------------------------

    private fun sweep(obj: Any) {
        obj.javaClass.methods
            .filter { (it.name.startsWith("get") || it.name.startsWith("is")) && it.parameterCount == 0 }
            .forEach { getter ->
                runCatching {
                    val value = getter.invoke(obj)
                    val setterName = "set" + getter.name.removePrefix("get").removePrefix("is")
                    obj.javaClass.methods
                        .firstOrNull { it.name == setterName && it.parameterCount == 1 }
                        ?.invoke(obj, value)
                }
            }
        obj.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(obj) } }
    }

    @Test
    fun `PNCVisitCache default constructor, accessors and generated members`() {
        val visit = cache(benId = 40L, pncPeriod = 4)

        assertNotNull(visit)
        sweep(visit)
        assertEquals(visit, visit.copy())
        assertEquals(visit.hashCode(), visit.copy().hashCode())
        assertTrue(visit.toString().contains("PNCVisitCache"))
    }

    @Test
    fun `PNCNetwork accessors and generated members`() {
        val net = PNCNetwork(
            id = 3L,
            benId = 88L,
            pncPeriod = 2,
            isActive = true,
            pncDate = "2024-03-01",
            ifaTabsGiven = 10,
            anyContraceptionMethod = false,
            contraceptionMethod = null,
            sterilisationDate = "2024-03-05",
            otherPpcMethod = null,
            anyDangerSign = null,
            motherDangerSign = null,
            otherDangerSign = null,
            referralFacility = "PHC",
            motherDeath = false,
            deathDate = null,
            causeOfDeath = null,
            otherDeathCause = null,
            placeOfDeath = null,
            otherPlaceOfDeath = null,
            remarks = null,
            createdBy = "c",
            createdDate = "2024-03-01",
            updatedBy = "u",
            updatedDate = "2024-03-02"
        )

        sweep(net)
        assertEquals(net, net.copy())
        assertEquals(net.hashCode(), net.copy().hashCode())
        assertTrue(net.toString().contains("PNCNetwork"))
    }

    @Test
    fun `PNCNetwork asCacheModel maps delivery discharge summaries`() {
        val net = PNCNetwork(
            id = 4L,
            benId = 90L,
            pncPeriod = 1,
            isActive = true,
            pncDate = "2024-04-01",
            ifaTabsGiven = 5,
            anyContraceptionMethod = null,
            contraceptionMethod = null,
            sterilisationDate = "2024-04-02",
            otherPpcMethod = null,
            anyDangerSign = null,
            motherDangerSign = null,
            otherDangerSign = null,
            referralFacility = null,
            motherDeath = false,
            deathDate = null,
            causeOfDeath = null,
            otherDeathCause = null,
            placeOfDeath = null,
            otherPlaceOfDeath = null,
            remarks = null,
            deliveryDischargeSummary1 = "s1",
            deliveryDischargeSummary2 = "s2",
            deliveryDischargeSummary3 = "s3",
            deliveryDischargeSummary4 = "s4",
            createdBy = "c",
            createdDate = "2024-04-01",
            updatedBy = "u",
            updatedDate = "2024-04-02"
        )

        val cache = net.asCacheModel()

        assertEquals("s1", cache.deliveryDischargeSummary1)
        assertEquals("s2", cache.deliveryDischargeSummary2)
        assertEquals("s3", cache.deliveryDischargeSummary3)
        assertEquals("s4", cache.deliveryDischargeSummary4)
    }

    @Test
    fun `BenWithDoAndPncCache equals hashCode and copy behave as data class`() {
        val a = BenWithDoAndPncCache(
            ben = benBasic(),
            deliveryOutcomeCache = emptyList(),
            savedPncRecords = emptyList()
        )
        val b = a.copy()

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("BenWithDoAndPncCache"))
    }
}
