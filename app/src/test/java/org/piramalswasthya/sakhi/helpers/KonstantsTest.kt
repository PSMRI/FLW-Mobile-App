package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class KonstantsTest {

    // =====================================================
    // Age Range Constants Tests
    // =====================================================

    @Test
    fun `eligible couple age range is 15 to 49`() {
        assertEquals(15, Konstants.minAgeForEligibleCouple)
        assertEquals(49, Konstants.maxAgeForEligibleCouple)
    }

    @Test
    fun `reproductive age range is 15 to 49`() {
        assertEquals(15, Konstants.minAgeForReproductiveAge)
        assertEquals(49, Konstants.maxAgeForReproductiveAge)
    }

    @Test
    fun `ncd min age is 30`() {
        assertEquals(30, Konstants.minAgeForNcd)
    }

    @Test
    fun `adolescent age range is 10 to 14`() {
        assertEquals(10, Konstants.minAgeForAdolescent)
        assertEquals(14, Konstants.maxAgeForAdolescent)
    }

    @Test
    fun `adolescent list max age is 19`() {
        assertEquals(19, Konstants.maxAgeForAdolescentlist)
    }

    @Test
    fun `infant max age is 61 days`() {
        assertEquals(61, Konstants.maxAgeForInfant)
    }

    @Test
    fun `child age range is 92 to 456 days`() {
        assertEquals(92, Konstants.minAgeForChild)
        assertEquals(456, Konstants.maxAgeForChild)
    }

    @Test
    fun `gen ben age range is 15 to 99`() {
        assertEquals(15, Konstants.minAgeForGenBen)
        assertEquals(99, Konstants.maxAgeForGenBen)
    }

    @Test
    fun `cdr max age is 14`() {
        assertEquals(14, Konstants.maxAgeForCdr)
    }

    @Test
    fun `min marriage age is 12`() {
        assertEquals(12, Konstants.minAgeForMarriage)
    }

    // =====================================================
    // Duration Constants Tests
    // =====================================================

    @Test
    fun `cbac filing interval is 365 days`() {
        assertEquals(TimeUnit.DAYS.toMillis(365), Konstants.minMillisBwtweenCbacFiling)
    }

    @Test
    fun `non follow up duration is 90 days`() {
        assertEquals(TimeUnit.DAYS.toMillis(90), Konstants.nonFollowUpDuration)
    }

    // =====================================================
    // ANC Week Constants Tests
    // =====================================================

    @Test
    fun `anc1 week range is 5 to 12`() {
        assertEquals(5, Konstants.minAnc1Week)
        assertEquals(12, Konstants.maxAnc1Week)
    }

    @Test
    fun `anc2 week range is 14 to 27`() {
        assertEquals(14, Konstants.minAnc2Week)
        assertEquals(27, Konstants.maxAnc2Week)
    }

    @Test
    fun `anc3 week range is 28 to 35`() {
        assertEquals(28, Konstants.minAnc3Week)
        assertEquals(35, Konstants.maxAnc3Week)
    }

    @Test
    fun `anc4 week range is 36 to 40`() {
        assertEquals(36, Konstants.minAnc4Week)
        assertEquals(40, Konstants.maxAnc4Week)
    }

    @Test
    fun `max anc week is 42`() {
        assertEquals(42, Konstants.maxAncWeek)
    }

    @Test
    fun `anc week ranges do not overlap`() {
        assertTrue(Konstants.maxAnc1Week < Konstants.minAnc2Week)
        assertTrue(Konstants.maxAnc2Week < Konstants.minAnc3Week)
        assertTrue(Konstants.maxAnc3Week < Konstants.minAnc4Week)
    }

    // =====================================================
    // Business Constants Tests
    // =====================================================

    @Test
    fun `baby low weight threshold is 2500 grams`() {
        assertEquals(2500.0, Konstants.babyLowWeight, 0.0)
    }

    @Test
    fun `pnc ec gap is 45 days`() {
        assertEquals(45L, Konstants.pncEcGap)
    }

    @Test
    fun `ben id capacity is 100`() {
        assertEquals(100, Konstants.benIdCapacity)
    }

    @Test
    fun `ben id worker trigger limit is less than capacity`() {
        assertTrue(Konstants.benIdWorkerTriggerLimit < Konstants.benIdCapacity)
    }

    @Test
    fun `min week to show delivered is 23`() {
        assertEquals(23, Konstants.minWeekToShowDelivered)
    }

    // =====================================================
    // Extended Age Relationship Tests
    // =====================================================

    @Test fun `eligible couple min is same as reproductive min`() {
        assertEquals(Konstants.minAgeForEligibleCouple, Konstants.minAgeForReproductiveAge)
    }

    @Test fun `eligible couple max is same as reproductive max`() {
        assertEquals(Konstants.maxAgeForEligibleCouple, Konstants.maxAgeForReproductiveAge)
    }

    @Test fun `ncd min age is greater than adolescent max`() {
        assertTrue(Konstants.minAgeForNcd > Konstants.maxAgeForAdolescentlist)
    }

    @Test fun `adolescent range is subset of adolescent list range`() {
        assertTrue(Konstants.minAgeForAdolescent >= 10)
        assertTrue(Konstants.maxAgeForAdolescent <= Konstants.maxAgeForAdolescentlist)
    }

    @Test fun `infant max age is less than child min age`() {
        assertTrue(Konstants.maxAgeForInfant < Konstants.minAgeForChild)
    }

    @Test fun `gen ben max age is 99`() {
        assertEquals(99, Konstants.maxAgeForGenBen)
    }

    @Test fun `marriage min age is less than eligible couple min`() {
        assertTrue(Konstants.minAgeForMarriage < Konstants.minAgeForEligibleCouple)
    }

    // =====================================================
    // Extended ANC Week Tests
    // =====================================================

    @Test fun `anc1 week range is valid`() {
        assertTrue(Konstants.minAnc1Week < Konstants.maxAnc1Week)
    }

    @Test fun `anc2 week range is valid`() {
        assertTrue(Konstants.minAnc2Week < Konstants.maxAnc2Week)
    }

    @Test fun `anc3 week range is valid`() {
        assertTrue(Konstants.minAnc3Week < Konstants.maxAnc3Week)
    }

    @Test fun `anc4 week range is valid`() {
        assertTrue(Konstants.minAnc4Week < Konstants.maxAnc4Week)
    }

    @Test fun `max anc week is greater than anc4 max`() {
        assertTrue(Konstants.maxAncWeek >= Konstants.maxAnc4Week)
    }

    @Test fun `anc1 starts at week 5`() {
        assertEquals(5, Konstants.minAnc1Week)
    }

    @Test fun `anc4 ends at week 40`() {
        assertEquals(40, Konstants.maxAnc4Week)
    }

    // =====================================================
    // Extended Business Logic Tests
    // =====================================================

    @Test fun `baby low weight is positive`() {
        assertTrue(Konstants.babyLowWeight > 0)
    }

    @Test fun `pnc ec gap is positive`() {
        assertTrue(Konstants.pncEcGap > 0)
    }

    @Test fun `ben id capacity is positive`() {
        assertTrue(Konstants.benIdCapacity > 0)
    }

    @Test fun `ben id worker trigger limit is positive`() {
        assertTrue(Konstants.benIdWorkerTriggerLimit > 0)
    }

    @Test fun `cbac filing interval is positive`() {
        assertTrue(Konstants.minMillisBwtweenCbacFiling > 0)
    }

    @Test fun `non follow up duration is positive`() {
        assertTrue(Konstants.nonFollowUpDuration > 0)
    }

    @Test fun `cbac interval is greater than follow up duration`() {
        assertTrue(Konstants.minMillisBwtweenCbacFiling > Konstants.nonFollowUpDuration)
    }

    @Test fun `min week to show delivered is before anc4 max`() {
        assertTrue(Konstants.minWeekToShowDelivered < Konstants.maxAnc4Week)
    }
}
