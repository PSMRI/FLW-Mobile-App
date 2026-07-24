package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.piramalswasthya.sakhi.helpers.RecapScene
import org.piramalswasthya.sakhi.helpers.RecapStoryGate
import org.piramalswasthya.sakhi.model.RecapStatus
import org.piramalswasthya.sakhi.model.recapStatus
import org.piramalswasthya.sakhi.model.safeProgressScene
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import javax.inject.Inject

/**
 * Phase 7 — drives the personalised recap playback.
 *
 * Resume model (currentScene semantics): the snapshot's progressScene stores
 * the scene BEING SHOWN, and reopening starts AT that scene. Restoration
 * sources, in priority order:
 *  1. [SavedStateHandle] — same-session recreation (rotation, UI restart);
 *  2. Room (progressScene) — true process death or a fresh open of an
 *     IN_PROGRESS recap ("Continue Recap" resumes where she left off);
 *  3. 0 — first open, or a COMPLETED recap being replayed.
 *
 * Write ordering: scene-progress writes are serialized through a fair mutex
 * acquired in main-thread launch order, so rapid next/back taps can never let
 * an older write land last (the stored index always matches the last scene
 * shown). On open, the story length is recorded (totalScenes) so clamping and
 * resume validate against the real story.
 *
 * EMPTY story → [UiState.Closed]: the fragment exits immediately — the recap
 * "does not open at all" for a month with nothing to celebrate (user decision;
 * the strip is normally already hidden by then, this is the second lock).
 */
@HiltViewModel
class MonthlyRecapPlaybackViewModel @Inject constructor(
    private val storyGate: RecapStoryGate,
    private val recapRepo: MonthlyRecapRepo,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        /** [startSceneIndex] is the validated resume target (0 for fresh/replay). */
        data class Playing(val scenes: List<RecapScene>, val startSceneIndex: Int) : UiState()
        object Closed : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    /** Fair FIFO lock: progress writes commit in the exact order scenes were shown. */
    private val progressWriteMutex = Mutex()

    init {
        viewModelScope.launch {
            val scenes = storyGate.composeStory()
            if (scenes.isEmpty()) {
                _state.value = UiState.Closed
                return@launch
            }
            val snapshot = recapRepo.getOrCreateCurrentRecap()
            val startIndex = when {
                // 1. Same-session recreation (rotation): exact scene, pre-validation.
                savedStateHandle.contains(KEY_SCENE_INDEX) ->
                    savedStateHandle.get<Int>(KEY_SCENE_INDEX) ?: 0
                // 2. Process death / fresh open of an in-progress story: the Room
                //    index, already coerced against the stored totalScenes.
                snapshot?.recapStatus() == RecapStatus.IN_PROGRESS -> snapshot.safeProgressScene()
                // 3. Fresh start or replay of a completed story.
                else -> 0
            }.coerceIn(0, scenes.lastIndex)
            recapRepo.onPlaybackOpened(totalScenes = scenes.size)
            _state.value = UiState.Playing(scenes, startIndex)
        }
    }

    /** Called by the fragment whenever a scene becomes current. */
    fun onSceneShown(index: Int, scene: RecapScene) {
        savedStateHandle[KEY_SCENE_INDEX] = index
        viewModelScope.launch {
            progressWriteMutex.withLock {
                recapRepo.updateSafeProgress(index)
                if (scene.type == RecapScene.Type.GOODBYE) recapRepo.markCompleted()
            }
        }
    }

    /** Replay restarts the story UI-side; the COMPLETED status is preserved. */
    fun onReplay() {
        savedStateHandle[KEY_SCENE_INDEX] = 0
    }

    private companion object {
        const val KEY_SCENE_INDEX = "recap_playback_scene_index"
    }
}
