package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentMonthlyRecapPlaybackBinding
import org.piramalswasthya.sakhi.helpers.RecapScene
import org.piramalswasthya.sakhi.ui.home_activity.home.recapAnimationsEnabled

/**
 * Phase 7 — the personalised Monthly Recap playback.
 *
 * Story-style auto-play: a slim segmented progress bar drives each scene
 * (welcome 3.5s, category scenes 6s). Every message scene pairs one sentence
 * with a DIFFERENT didi animation (the composer guarantees no consecutive
 * repeat); the speech bubble is visually attached to her, so the words feel
 * spoken. The goodbye scene never auto-advances — it offers Replay instead.
 *
 * Controls are BOTH gestural and visible: tap-left/tap-right/hold on the
 * overlay for people who know the story idiom, plus real Previous / Pause-Play /
 * Next buttons that TalkBack can find and describe (the overlay itself is
 * hidden from accessibility so the buttons are the single a11y path).
 *
 * Everything renders from the frozen snapshot: fully offline and identical on
 * every replay. Playback resumes at the scene supplied by the ViewModel
 * (SavedStateHandle for rotation, Room for process death / "Continue Recap").
 * Reduced-motion devices get static frames; the CountDownTimer is immune to
 * animator scale so the story still advances.
 */
@AndroidEntryPoint
class MonthlyRecapPlaybackFragment : Fragment() {

    private var _binding: FragmentMonthlyRecapPlaybackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MonthlyRecapPlaybackViewModel by viewModels()

    private var scenes: List<RecapScene> = emptyList()
    private var sceneIndex = 0
    private var segments: List<LinearProgressIndicator> = emptyList()

    private var sceneTimer: CountDownTimer? = null
    private var remainingMs = 0L
    private var currentSceneDurationMs = 0L
    private var touchDownAtMs = 0L
    private var motionEnabled = true

    /** True while the ASHA has explicitly paused via the Pause button. */
    private var userPaused = false

    /** Soft looping background music (pilot); null-safe if it fails to load. */
    private var music: RecapMusicController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyRecapPlaybackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        motionEnabled = recapAnimationsEnabled(requireContext())

        binding.recapPlaybackClose.setOnClickListener { findNavController().popBackStack() }
        binding.recapReplayButton.setOnClickListener { restartStory() }
        binding.recapPrevButton.setOnClickListener { onControlNavigate(-1) }
        binding.recapNextButton.setOnClickListener { onControlNavigate(+1) }
        binding.recapPauseButton.setOnClickListener { togglePause() }
        music = RecapMusicController(requireContext())
        binding.recapMusicToggle.setOnClickListener { toggleMusic() }
        updateMusicButton()
        setUpGestures()
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is MonthlyRecapPlaybackViewModel.UiState.Loading -> showLoading(true)

                        is MonthlyRecapPlaybackViewModel.UiState.Closed ->
                            findNavController().popBackStack()

                        is MonthlyRecapPlaybackViewModel.UiState.Playing -> {
                            if (scenes.isEmpty()) {
                                scenes = state.scenes
                                showLoading(false)
                                buildProgressSegments(scenes.size)
                                // Resume target: rotation → same scene; process death
                                // or "Continue Recap" → the scene stored in Room.
                                showScene(state.startSceneIndex, animate = motionEnabled)
                                // Prepare the loop once (plays continuously across all
                                // scenes, never restarts per message), then fade it in
                                // if it should currently be audible.
                                music?.prepare()
                                refreshMusic(fade = true)
                            }
                        }
                    }
                }
            }
        }
    }

    /** Blank-screen guard: a small spinner until the story is composed. */
    private fun showLoading(loading: Boolean) {
        val b = _binding ?: return
        b.recapLoading.visibility = if (loading) View.VISIBLE else View.GONE
        val storyVisibility = if (loading) View.INVISIBLE else View.VISIBLE
        b.recapProgressRow.visibility = storyVisibility
        b.recapPlaybackClose.visibility = storyVisibility
        b.recapControlsRow.visibility = storyVisibility // includes the music toggle
        // Keep the empty girl/bubble hidden until the first scene renders.
        b.recapPlaybackGirl.visibility = storyVisibility
        b.recapBubbleTail.visibility = storyVisibility
        b.recapBubbleCard.visibility = storyVisibility
    }

    // ------------------------------------------------------------ scene render

    private fun showScene(index: Int, animate: Boolean) {
        val b = _binding ?: return
        if (index !in scenes.indices) return
        sceneIndex = index
        val scene = scenes[index]
        viewModel.onSceneShown(index, scene)
        syncSegments(index)

        val isGoodbye = scene.type == RecapScene.Type.GOODBYE

        // Every scene uses the same girl + attached-bubble layout (user's "50/50"
        // balance): welcome carries the greeting animation with no count, category
        // scenes the achievement + count, goodbye the closing line + Replay.
        b.recapReplayButton.visibility = if (isGoodbye) View.VISIBLE else View.GONE
        // The finale has nothing left to auto-play: pause loses its meaning there.
        b.recapPauseButton.visibility = if (isGoodbye) View.INVISIBLE else View.VISIBLE
        b.recapNextButton.isEnabled = !isGoodbye
        b.recapNextButton.alpha = if (isGoodbye) 0.35f else 1f

        b.recapSceneText.text = emphasizedCount(scene) // count == null -> plain text
        scene.lottieRawRes?.let { res ->
            b.recapPlaybackGirl.setAnimation(res)
            if (motionEnabled) b.recapPlaybackGirl.playAnimation()
            else b.recapPlaybackGirl.progress = 0f // static friendly frame
        }
        if (animate) {
            fadeSlideIn(b.recapPlaybackGirl, translation = 0f)
            fadeSlideIn(b.recapBubbleTail)
            fadeSlideIn(b.recapBubbleCard)
            if (isGoodbye) fadeSlideIn(b.recapReplayButton)
        }
        b.root.announceForAccessibility(scene.text)
        startSceneTimer(scene)
    }

    /** Makes the achievement number pop inside the sentence (bold, accent, larger). */
    private fun emphasizedCount(scene: RecapScene): CharSequence {
        val count = scene.count?.toString() ?: return scene.text
        val start = scene.text.indexOf(count)
        if (start < 0) return scene.text
        val end = start + count.length
        return SpannableString(scene.text).apply {
            setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(requireContext(), R.color.recap_playback_count_accent)
                ),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun fadeSlideIn(view: View, translation: Float = 24f) {
        view.alpha = 0f
        view.translationY = translation
        view.animate().alpha(1f).translationY(0f)
            .setDuration(320L).setInterpolator(DecelerateInterpolator()).start()
    }

    // ------------------------------------------------------------ progress bar

    private fun buildProgressSegments(count: Int) {
        val b = _binding ?: return
        b.recapProgressRow.removeAllViews()
        segments = List(count) { i ->
            LinearProgressIndicator(requireContext()).apply {
                max = SEGMENT_MAX
                trackThickness = dpToPx(3f)
                trackCornerRadius = dpToPx(2f)
                trackColor = ContextCompat.getColor(context, R.color.recap_playback_progress_track)
                setIndicatorColor(
                    ContextCompat.getColor(context, R.color.recap_playback_progress_active)
                )
                layoutParams =
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginEnd = if (i == count - 1) 0 else dpToPx(6f)
                    }
            }.also { b.recapProgressRow.addView(it) }
        }
    }

    /** Past segments full, current + future reset (the timer fills the current). */
    private fun syncSegments(current: Int) {
        segments.forEachIndexed { i, segment ->
            segment.progress = if (i < current) SEGMENT_MAX else 0
        }
    }

    // ------------------------------------------------------------ auto-advance

    private fun startSceneTimer(scene: RecapScene) {
        cancelTimer()
        if (scene.type == RecapScene.Type.GOODBYE) {
            // The finale rests: full segment, no auto-advance — she chooses replay/close.
            segments.getOrNull(sceneIndex)?.progress = SEGMENT_MAX
            return
        }
        currentSceneDurationMs = readingDurationMs(scene.text)
        remainingMs = currentSceneDurationMs
        if (!userPaused) launchTimer(currentSceneDurationMs)
    }

    /**
     * Auto-advance time ADAPTS to sentence length so a short line isn't rushed
     * and a long Assamese line gets room to be read: a base plus per-word reading
     * time, clamped to a comfortable floor and ceiling. Word-splitting is done
     * without a regex (deliberately — an ICU regex crash bit us once). Pause +
     * manual Prev/Next remain the override, so a generous default is risk-free.
     */
    private fun readingDurationMs(text: String): Long {
        val words = text.split(' ', '\n', '\t', '\r', ' ').count { it.isNotBlank() }
        return (BASE_READ_MS + words * PER_WORD_MS).coerceIn(MIN_SCENE_MS, MAX_SCENE_MS)
    }

    private fun launchTimer(durationMs: Long) {
        remainingMs = durationMs
        sceneTimer = object : CountDownTimer(durationMs, TIMER_TICK_MS) {
            override fun onTick(msLeft: Long) {
                remainingMs = msLeft
                val done = currentSceneDurationMs - msLeft
                segments.getOrNull(sceneIndex)?.progress =
                    ((done.toFloat() / currentSceneDurationMs) * SEGMENT_MAX).toInt()
            }

            override fun onFinish() {
                segments.getOrNull(sceneIndex)?.progress = SEGMENT_MAX
                advance(1)
            }
        }.start()
    }

    private fun pauseTimer() {
        sceneTimer?.cancel()
        sceneTimer = null
    }

    private fun resumeTimer() {
        if (sceneTimer != null || scenes.isEmpty() || userPaused) return
        val scene = scenes.getOrNull(sceneIndex) ?: return
        if (scene.type == RecapScene.Type.GOODBYE) return
        if (remainingMs in 1..currentSceneDurationMs) launchTimer(remainingMs)
    }

    private fun cancelTimer() {
        sceneTimer?.cancel()
        sceneTimer = null
        remainingMs = 0L
    }

    private fun advance(delta: Int) {
        val next = sceneIndex + delta
        when {
            next < 0 -> showScene(0, animate = false) // back on first scene restarts it
            next in scenes.indices -> showScene(next, animate = motionEnabled)
            // past the goodbye scene: stay (she uses Replay or Close)
        }
    }

    private fun restartStory() {
        viewModel.onReplay()
        userPaused = false
        updatePauseButton()
        showScene(0, animate = motionEnabled)
    }

    // ------------------------------------------------------------ controls

    /** Explicit button navigation always resumes playing (never leaves it paused). */
    private fun onControlNavigate(delta: Int) {
        if (scenes.isEmpty()) return
        userPaused = false
        updatePauseButton()
        advance(delta)
    }

    private fun togglePause() {
        if (scenes.isEmpty()) return
        userPaused = !userPaused
        if (userPaused) {
            pauseTimer()
            if (motionEnabled) _binding?.recapPlaybackGirl?.pauseAnimation()
        } else {
            if (motionEnabled) _binding?.recapPlaybackGirl?.resumeAnimation()
            resumeTimer()
        }
        // Pause pauses the whole experience — music follows the story here.
        refreshMusic(fade = true)
        updatePauseButton()
    }

    private fun updatePauseButton() {
        val b = _binding ?: return
        b.recapPauseButton.setImageResource(
            if (userPaused) R.drawable.ic_recap_play else R.drawable.ic_recap_pause
        )
        b.recapPauseButton.contentDescription = getString(
            if (userPaused) R.string.monthly_recap_playback_play_cd
            else R.string.monthly_recap_playback_pause_cd
        )
    }

    /** Music on/off — silences ONLY the music; the story keeps auto-playing. */
    private fun toggleMusic() {
        viewModel.musicMuted = !viewModel.musicMuted
        refreshMusic(fade = true)
        updateMusicButton()
    }

    /** Music is audible only while the story is playing AND not muted. */
    private fun shouldMusicPlay(): Boolean = !userPaused && !viewModel.musicMuted

    private fun refreshMusic(fade: Boolean) {
        music?.setAudible(shouldMusicPlay(), fade)
    }

    private fun updateMusicButton() {
        val b = _binding ?: return
        val muted = viewModel.musicMuted
        b.recapMusicToggle.setImageResource(
            if (muted) R.drawable.ic_recap_music_off else R.drawable.ic_recap_music_on
        )
        b.recapMusicToggle.contentDescription = getString(
            if (muted) R.string.monthly_recap_music_off_cd
            else R.string.monthly_recap_music_on_cd
        )
    }

    // ------------------------------------------------------------ gestures

    /**
     * Convenience layer for people who know the story idiom: hold anywhere =
     * pause, quick tap right 2/3 = next, left 1/3 = back. Not the accessible
     * path — the visible buttons above are (this overlay is marked
     * importantForAccessibility="no").
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpGestures() {
        binding.recapTouchOverlay.setOnTouchListener { overlay, event ->
            if (scenes.isEmpty()) return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownAtMs = SystemClock.elapsedRealtime()
                    pauseTimer()
                    if (motionEnabled) binding.recapPlaybackGirl.pauseAnimation()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val heldMs = SystemClock.elapsedRealtime() - touchDownAtMs
                    if (motionEnabled && !userPaused) binding.recapPlaybackGirl.resumeAnimation()
                    if (heldMs < TAP_THRESHOLD_MS) {
                        if (event.x < overlay.width / 3f) advance(-1) else advance(1)
                    } else {
                        resumeTimer()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (motionEnabled && !userPaused) binding.recapPlaybackGirl.resumeAnimation()
                    resumeTimer()
                    true
                }

                else -> false
            }
        }
    }

    // ------------------------------------------------------------ lifecycle

    override fun onPause() {
        super.onPause()
        pauseTimer()
        _binding?.recapPlaybackGirl?.pauseAnimation()
        music?.setAudible(false, fade = false) // immediate silence on background
    }

    override fun onResume() {
        super.onResume()
        if (scenes.isEmpty()) return
        if (!userPaused) {
            if (motionEnabled) _binding?.recapPlaybackGirl?.resumeAnimation()
            resumeTimer()
        }
        // Restore music to whatever the current state dictates (silent if the
        // story is paused or the user muted it).
        refreshMusic(fade = true)
    }

    override fun onDestroyView() {
        cancelTimer()
        music?.release()
        music = null
        _binding?.let { b ->
            b.recapPlaybackGirl.animate().cancel()
            b.recapBubbleCard.animate().cancel()
            b.recapBubbleTail.animate().cancel()
            b.recapReplayButton.animate().cancel()
        }
        super.onDestroyView()
        _binding = null
    }

    private fun dpToPx(dp: Float): Int = (dp * resources.displayMetrics.density).toInt()

    private companion object {
        const val SEGMENT_MAX = 1000

        // Adaptive per-scene reading time: base + per-word, clamped. ~8s for a
        // short welcome, up to ~13s for a long Assamese achievement line.
        const val BASE_READ_MS = 3_500L
        const val PER_WORD_MS = 650L
        const val MIN_SCENE_MS = 7_000L
        const val MAX_SCENE_MS = 13_000L

        /**
         * 60ms ≈ 16 progress updates/second — visually smooth for a slim strip
         * while costing ~6x fewer callbacks than per-frame ticking on the
         * low-end handsets many ASHAs use.
         */
        const val TIMER_TICK_MS = 60L
        const val TAP_THRESHOLD_MS = 250L
    }
}
