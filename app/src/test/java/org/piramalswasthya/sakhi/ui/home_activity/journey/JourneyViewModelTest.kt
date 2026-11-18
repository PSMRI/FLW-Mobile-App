package org.piramalswasthya.sakhi.ui.home_activity.journey

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.badges.BadgeRepository
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BadgeStateCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.notifications.ActivityClassifier
import org.piramalswasthya.sakhi.notifications.FormSaveInterceptor

@OptIn(ExperimentalCoroutinesApi::class)
class JourneyViewModelTest : BaseViewModelTest() {

    private lateinit var dao: EveningNotifDao
    private lateinit var interceptor: FormSaveInterceptor
    private lateinit var badgeRepository: BadgeRepository
    private lateinit var pref: PreferenceDao

    private fun card(index: Int, progress: Long?, target: Long?) = BadgeRepository.BadgeCard(
        definition = BadgeDefinitions.ALL[index],
        state = progress?.let {
            BadgeStateCache(1, BadgeDefinitions.ALL[index].id, 0, it, target ?: 0, lastEvaluatedAt = 0)
        },
        earnedLevels = 0,
        timesEarned = 0
    )

    @Before
    override fun setUp() {
        super.setUp()
        dao = mockk()
        interceptor = mockk()
        badgeRepository = mockk()
        pref = mockk()
        coEvery { interceptor.captureToday() } returns Unit
        coEvery { dao.countForDay(ActivityClassifier.dateKey(System.currentTimeMillis())) } returns 3
        coEvery { dao.lifetimeCount() } returns 120L
        every { pref.getLoggedInUser() } returns mockk<User>().also { every { it.name } returns "Meena" }
        every { badgeRepository.shelf } returns flowOf(
            listOf(
                card(0, null, null), // no state → 0
                card(1, 1, 4), // 0.25
                card(2, 3, 3), // 1.0
                card(3, 1, 0), // target coerced to 1 → 1.0
                card(4, 2, 5) // 0.4
            )
        )
    }

    @Test
    fun `loads today's count, lifetime total and the top three badges`() = runTest(testDispatcher) {
        val vm = JourneyViewModel(dao, interceptor, badgeRepository, pref)
        val top = mutableListOf<List<BadgeRepository.BadgeCard>>()
        vm.topBadges.observeForever { top += it }
        advanceUntilIdle()

        assertEquals("Meena", vm.userName)
        assertEquals(3, vm.todayCount.value)
        assertEquals(120L, vm.lifetimeTotal.value)
        assertEquals(
            listOf(BadgeDefinitions.ALL[2].id, BadgeDefinitions.ALL[3].id, BadgeDefinitions.ALL[4].id),
            top.last().map { it.definition.id }
        )
        coVerify { interceptor.captureToday() }
    }

    @Test
    fun `user name falls back to blank`() = runTest(testDispatcher) {
        every { pref.getLoggedInUser() } returns null
        assertEquals("", JourneyViewModel(dao, interceptor, badgeRepository, pref).userName)

        every { pref.getLoggedInUser() } throws IllegalStateException("prefs")
        assertEquals("", JourneyViewModel(dao, interceptor, badgeRepository, pref).userName)
        advanceUntilIdle()
    }
}
