package org.piramalswasthya.sakhi.adapters

import org.junit.Assert.assertEquals
import org.junit.Test

class GetBenPageNumberAdapterTest {

    @Test
    fun itemCount_equalsMaxPageNumber() {
        val adapter = GetBenPageNumberAdapter(5, GetBenPageNumberAdapter.PageClickListener { })
        assertEquals(5, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForZeroMaxPageNumber() {
        val adapter = GetBenPageNumberAdapter(0, GetBenPageNumberAdapter.PageClickListener { })
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun pageClickListener_invokesLambdaWithPage() {
        var captured: Int? = null
        val listener = GetBenPageNumberAdapter.PageClickListener { page -> captured = page }
        listener.onClickedPage(3)
        assertEquals(3, captured)
    }
}
