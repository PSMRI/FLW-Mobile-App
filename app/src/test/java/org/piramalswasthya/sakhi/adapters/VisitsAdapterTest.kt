package org.piramalswasthya.sakhi.adapters

import org.junit.Assert.assertEquals
import org.junit.Test

class VisitsAdapterTest {

    @Test
    fun itemCount_matchesVisitNumbersSize() {
        val adapter = VisitsAdapter(listOf(1, 2, 3)) {}
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = VisitsAdapter(emptyList()) {}
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun itemCount_isOneForSingleItemList() {
        val adapter = VisitsAdapter(listOf(5)) {}
        assertEquals(1, adapter.itemCount)
    }
}
