package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.RecapScene
import org.piramalswasthya.sakhi.helpers.RecapStoryGate
import org.piramalswasthya.sakhi.model.MonthlyRecapCache
import org.piramalswasthya.sakhi.model.RecapStatus
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo

/**
 * Phase 7 — playback ViewModel: story loading, the empty-month Closed state,
 * RESUME restoration (Room + SavedStateHandle), ordered progress writes and
 * replay.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MonthlyRecapPlaybackViewModelTest : BaseViewModelTest() {

    private lateinit var storyGate: RecapStoryGate
    private lateinit var recapRepo: MonthlyRecapRepo

    private val story = listOf(
        RecapScene(RecapScene.Type.WELCOME, "welcome", lottieRawRes = null),
        RecapScene(RecapScene.Type.CATEGORY, "you did 5", lottieRawRes = 1, categoryId = "NCD", count = 5),
        RecapScene(RecapScene.Type.CATEGORY, "you did 2", lottieRawRes = 2, categoryId = "HOUSEHOLD", count = 2),
        RecapScene(RecapScene.Type.CATEGORY, "you did 9", lottieRawRes = 3, categoryId = "IMMUNIZATION", count = 9),
        RecapScene(RecapScene.Type.GOODBYE, "bye", lottieRawRes = 7),
    )

    @Before
    override fun setUp() {
        super.setUp()
        storyGate = mockk()
        recapRepo = mockk(relaxed = true)
        snapshot(status = RecapStatus.NOT_STARTED, progressScene = 0)
    }

    /** Stubs the stored snapshot the ViewModel restores from. */
    private fun snapshot(status: RecapStatus, progressScene: Int) {
        val cache = mockk<MonthlyRecapCache>(relaxed = true)
        every { cache.status } returns status.name
        every { cache.progressScene } returns progressScene
        coEvery { recapRepo.getOrCreateCurrentRecap() } returns cache
    }

    private fun viewModel(saved: SavedStateHandle = SavedStateHandle()) =
        MonthlyRecapPlaybackViewModel(storyGate, recapRepo, saved)

    private fun playing(vm: MonthlyRecapPlaybackViewModel) =
        vm.state.value as MonthlyRecapPlaybackViewModel.UiState.Playing

    // ---------------------------------------------------------------- loading

    @Test
    fun `story present moves to Playing, marks started and records total scenes`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(story, playing(vm).scenes)
        coVerify(exactly = 1) { recapRepo.onPlaybackOpened(story.size) }
    }

    @Test
    fun `empty story closes without touching playback status`() = runTest {
        coEvery { storyGate.composeStory() } returns emptyList()
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.state.value is MonthlyRecapPlaybackViewModel.UiState.Closed)
        coVerify(exactly = 0) { recapRepo.onPlaybackOpened(any()) }
        coVerify(exactly = 0) { recapRepo.markCompleted() }
    }

    // ---------------------------------------------------------------- resume

    @Test
    fun `fresh recap starts at scene 0`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(0, playing(vm).startSceneIndex)
    }

    @Test
    fun `close on scene 3 then reopen resumes at scene 3 from Room`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        snapshot(status = RecapStatus.IN_PROGRESS, progressScene = 3)
        val vm = viewModel() // fresh ViewModel = process death / fresh open
        advanceUntilIdle()
        assertEquals(3, playing(vm).startSceneIndex)
    }

    @Test
    fun `saved state wins over Room for same-session recreation`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        snapshot(status = RecapStatus.IN_PROGRESS, progressScene = 1)
        val vm = viewModel(SavedStateHandle(mapOf("recap_playback_scene_index" to 4)))
        advanceUntilIdle()
        assertEquals(4, playing(vm).startSceneIndex)
    }

    @Test
    fun `completed recap replays from scene 0, ignoring stored progress`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        snapshot(status = RecapStatus.COMPLETED, progressScene = 3)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(0, playing(vm).startSceneIndex)
    }

    @Test
    fun `stored index beyond the story is clamped into range`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        snapshot(status = RecapStatus.IN_PROGRESS, progressScene = 99)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(story.lastIndex, playing(vm).startSceneIndex)
    }

    @Test
    fun `negative stored index is clamped to zero`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        snapshot(status = RecapStatus.IN_PROGRESS, progressScene = -5)
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(0, playing(vm).startSceneIndex)
    }

    // ---------------------------------------------------------------- writes

    @Test
    fun `scene progress mirrors into the snapshot and goodbye completes it`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSceneShown(1, story[1])
        advanceUntilIdle()
        coVerify(exactly = 1) { recapRepo.updateSafeProgress(1) }
        coVerify(exactly = 0) { recapRepo.markCompleted() }

        vm.onSceneShown(4, story[4])
        advanceUntilIdle()
        coVerify(exactly = 1) { recapRepo.updateSafeProgress(4) }
        coVerify(exactly = 1) { recapRepo.markCompleted() }
    }

    @Test
    fun `rapid scene changes write progress in the order shown`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSceneShown(1, story[1])
        vm.onSceneShown(2, story[2])
        vm.onSceneShown(3, story[3])
        advanceUntilIdle()

        // The serialized writer must commit in tap order — 3 lands last.
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            recapRepo.updateSafeProgress(1)
            recapRepo.updateSafeProgress(2)
            recapRepo.updateSafeProgress(3)
        }
    }

    @Test
    fun `saved state tracks the last shown scene for rotation`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        val saved = SavedStateHandle()
        val vm = viewModel(saved)
        advanceUntilIdle()
        vm.onSceneShown(2, story[2])
        advanceUntilIdle()
        assertEquals(2, saved.get<Int>("recap_playback_scene_index"))
    }

    @Test
    fun `replay resets the saved scene index`() = runTest {
        coEvery { storyGate.composeStory() } returns story
        val saved = SavedStateHandle()
        val vm = viewModel(saved)
        advanceUntilIdle()
        vm.onSceneShown(3, story[3])
        advanceUntilIdle()
        vm.onReplay()
        assertEquals(0, saved.get<Int>("recap_playback_scene_index"))
    }
}
