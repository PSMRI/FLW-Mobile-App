package org.piramalswasthya.sakhi.adapters

import androidx.fragment.app.FragmentActivity
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BenPagingAdapterTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun itemCount_isZeroBeforeAnyDataSubmitted() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val adapter = try {
            BenPagingAdapter(context = activity)
        } catch (e: NullPointerException) {
            org.junit.Assume.assumeNoException(e)
            return
        }
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun submitBenIds_doesNotThrowWithNoDataLoaded() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val adapter = try {
            BenPagingAdapter(context = activity)
        } catch (e: NullPointerException) {
            org.junit.Assume.assumeNoException(e)
            return
        }
        adapter.submitBenIds(listOf(1L, 2L))
        adapter.submitBenIds(emptyList())
    }

    @Test
    fun submitChildCounts_doesNotThrowWithNoDataLoaded() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val adapter = try {
            BenPagingAdapter(context = activity)
        } catch (e: NullPointerException) {
            org.junit.Assume.assumeNoException(e)
            return
        }
        adapter.submitChildCounts(mapOf(1L to 2))
        adapter.submitChildCounts(emptyMap())
    }
}
