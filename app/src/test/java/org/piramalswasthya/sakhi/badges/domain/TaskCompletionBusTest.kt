package org.piramalswasthya.sakhi.badges.domain

import androidx.room.InvalidationTracker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.InAppDb

class TaskCompletionBusTest {

    private val tracker = mockk<InvalidationTracker>(relaxed = true)
    private val db = mockk<InAppDb>().also { every { it.invalidationTracker } returns tracker }
    private val evaluator = mockk<BadgeEvaluator>()
    private val bus = TaskCompletionBus(db, evaluator)

    @Test
    fun `start observes mapped tables and evaluates once`() {
        coEvery { evaluator.evaluateAll() } just runs
        val observer = slot<InvalidationTracker.Observer>()
        every { tracker.addObserver(capture(observer)) } just runs

        bus.start()
        bus.start() // idempotent

        verify(exactly = 1) { tracker.addObserver(any()) }
        coVerify(timeout = 3_000, exactly = 1) { evaluator.evaluateAll() }

        // a table invalidation publishes another (debounced) evaluation
        observer.captured.onInvalidated(setOf("HBNC"))
        coVerify(timeout = 3_000, exactly = 2) { evaluator.evaluateAll() }

        bus.stop()
        verify { tracker.removeObserver(observer.captured) }
    }

    @Test
    fun `evaluation failures are contained and stop is idempotent`() {
        coEvery { evaluator.evaluateAll() } throws IllegalStateException("boom")

        bus.start()
        coVerify(timeout = 3_000) { evaluator.evaluateAll() }
        bus.publish()

        bus.stop()
        bus.stop()
        verify(exactly = 1) { tracker.removeObserver(any()) }
    }

    @Test
    fun `tracker failures do not prevent startup`() {
        coEvery { evaluator.evaluateAll() } just runs
        every { tracker.addObserver(any()) } throws IllegalStateException("closed")

        bus.start()
        coVerify(timeout = 3_000) { evaluator.evaluateAll() }
        bus.stop()

        verify(exactly = 0) { tracker.removeObserver(any()) }
        assertTrue(BadgeFactsReader.MAPPED_TABLES.isNotEmpty())
    }

    @Test
    fun `remove failures are swallowed`() {
        coEvery { evaluator.evaluateAll() } just runs
        every { tracker.removeObserver(any()) } throws IllegalStateException("closed")

        bus.start()
        bus.stop()
        bus.start()
        bus.stop()

        verify(exactly = 2) { tracker.addObserver(any()) }
    }
}
