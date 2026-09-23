package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.model.BottleItem

class BottleAdapterTest {

    private fun bottle(srNo: Int = 1, bottleNumber: String = "B1", date: String = "01-01-2024") =
        BottleItem(srNo = srNo, bottleNumber = bottleNumber, dateOfProvision = date)

    @Test
    fun itemCount_matchesItemsSize() {
        val adapter = BottleAdapter(listOf(bottle(), bottle(srNo = 2)))
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = BottleAdapter(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun itemCount_isOneForSingleItemList() {
        val adapter = BottleAdapter(listOf(bottle()))
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun itemCount_unaffectedByShowAsVisitNumberFlag() {
        val adapter = BottleAdapter(listOf(bottle(), bottle(srNo = 2)), showAsVisitNumber = true)
        assertEquals(2, adapter.itemCount)
    }
}
