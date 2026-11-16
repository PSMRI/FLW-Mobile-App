package org.piramalswasthya.sakhi.ui.home_activity.home

import androidx.lifecycle.Observer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.helpers.MonthlyRecapAvailabilityProvider
import org.piramalswasthya.sakhi.helpers.RecapStoryGate
import org.piramalswasthya.sakhi.model.MonthlyRecapCache
import org.piramalswasthya.sakhi.model.RecapStatus
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo

@OptIn(ExperimentalCoroutinesApi::class)
class MonthlyRecapStripViewModelTest : BaseViewModelTest() {

    private lateinit var recapRepo: MonthlyRecapRepo
    private lateinit var availability: MonthlyRecapAvailabilityProvider
    private lateinit var storyGate: RecapStoryGate
    private lateinit var gamification: GamificationConfigProvider
    private val switches = MutableStateFlow(GamificationConfigProvider.State(monthlyRecap = true))

    @Before
    override fun setUp() {
        super.setUp()
        recapRepo = mockk()
        availability = mockk()
        storyGate = mockk()
        gamification = mockk()
        every { gamification.state } returns switches
        coEvery { storyGate.hasStory() } returns true
        coEvery { availability.isAvailable() } returns true
        coEvery { availability.isReplayAvailable() } returns false
        recap(null)
    }

    private fun recap(status: RecapStatus?) {
        val cache = status?.let { s ->
            mockk<MonthlyRecapCache>(relaxed = true).also { every { it.status } returns s.name }
        }
        every { recapRepo.observeCurrentRecap() } returns flowOf(cache)
    }

    private fun observedState(): Pair<MonthlyRecapStripViewModel, MutableList<MonthlyRecapStripState>> {
        val vm = MonthlyRecapStripViewModel(recapRepo, availability, storyGate, gamification)
        val seen = mutableListOf<MonthlyRecapStripState>()
        vm.stripState.observeForever(Observer { seen += it })
        return vm to seen
    }

    @Test
    fun `no snapshot yet is ready`() = runTest(testDispatcher) {
        val (_, seen) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.READY, seen.last())
    }

    @Test
    fun `in progress resumes and completed replays`() = runTest(testDispatcher) {
        recap(RecapStatus.IN_PROGRESS)
        val (_, resume) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.RESUME, resume.last())

        recap(RecapStatus.COMPLETED)
        val (_, replay) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.REPLAY, replay.last())
    }

    @Test
    fun `empty month hides the strip`() = runTest(testDispatcher) {
        coEvery { storyGate.hasStory() } returns false
        val (_, seen) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.HIDDEN, seen.last())
    }

    @Test
    fun `outside the window only a completed recap can be replayed`() = runTest(testDispatcher) {
        coEvery { availability.isAvailable() } returns false
        recap(RecapStatus.IN_PROGRESS)
        val (_, inProgress) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.HIDDEN, inProgress.last())

        recap(RecapStatus.COMPLETED)
        val (_, noReplay) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.HIDDEN, noReplay.last())

        coEvery { availability.isReplayAvailable() } returns true
        val (_, replay) = observedState()
        advanceUntilIdle()
        assertEquals(MonthlyRecapStripState.REPLAY, replay.last())
    }

    @Test
    fun `refresh and the recap switch re-check the current month`() = runTest(testDispatcher) {
        val (vm, _) = observedState()
        advanceUntilIdle()
        vm.refresh()
        advanceUntilIdle()
        switches.value = GamificationConfigProvider.State(monthlyRecap = false)
        advanceUntilIdle()

        verify(exactly = 3) { recapRepo.observeCurrentRecap() }
    }
}
