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
}
