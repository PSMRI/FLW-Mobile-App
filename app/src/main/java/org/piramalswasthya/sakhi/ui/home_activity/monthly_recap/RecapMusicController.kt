package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import org.piramalswasthya.sakhi.R
import timber.log.Timber

/**
 * Soft looping background music for the recap playback (pilot).
 *
 * Design: the controller is "dumb" — it just makes the music audible or silent
 * on request. The fragment decides WHEN via one rule: music is audible only when
 * the story is playing (not story-paused) AND not muted AND the app is
 * foregrounded. That single rule keeps every case consistent:
 * - Pause button → pauses story AND music together;
 * - Music button → silences ONLY the music, story keeps playing;
 * - background → music silences immediately, resumes on return (if it should).
 *
 * The track is prepared once and loops CONTINUOUSLY across scenes (it is never
 * restarted per message). Low volume with a gentle 1s fade. Fully fail-safe:
 * every audio call is guarded, so a missing/corrupt file can never crash or
 * block the recap — the story just plays without music.
 *
 * Self-contained (this class + a few fragment wiring lines) so it can be
 * reverted cleanly if not wanted.
 */
class RecapMusicController(private val context: Context) {

    private var player: MediaPlayer? = null
    private var fade: ValueAnimator? = null
    private var currentVolume = 0f

    /** Create + loop the track, held silent until [setAudible] turns it up. */
    fun prepare() {
        if (player != null) return
        try {
            player = MediaPlayer.create(context.applicationContext, R.raw.recap_bg_music)?.apply {
                isLooping = true
                setVolume(0f, 0f)
                start()
            }
        } catch (e: Exception) {
            Timber.w(e, "Recap music failed to prepare")
            player = null
        }
    }

    /**
     * Make the music audible or silent. [fade] = smooth 1s ramp (buttons); pass
     * false for an immediate cut (going to background, where the screen is gone).
     * When turning silent, the player is paused after reaching zero.
     */
    fun setAudible(audible: Boolean, fade: Boolean) {
        val p = player ?: return
        val target = if (audible) TARGET_VOLUME else 0f
        try {
            if (audible && !p.isPlaying) p.start()
            if (fade) {
                fadeTo(target) { if (!audible) runCatching { p.pause() } }
            } else {
                cancelFade()
                currentVolume = target
                p.setVolume(target, target)
                if (!audible) p.pause()
            }
        } catch (e: Exception) {
            Timber.w(e, "Recap music setAudible failed")
        }
    }

    fun release() {
        cancelFade()
        runCatching { player?.release() }
        player = null
        currentVolume = 0f
    }

    private fun fadeTo(target: Float, onEnd: (() -> Unit)? = null) {
        val p = player ?: return
        cancelFade()
        fade = ValueAnimator.ofFloat(currentVolume, target).apply {
            duration = FADE_MS
            addUpdateListener { anim ->
                val v = anim.animatedValue as Float
                currentVolume = v
                runCatching { p.setVolume(v, v) }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            start()
        }
    }

    private fun cancelFade() {
        fade?.cancel()
        fade = null
    }

    private companion object {
        /** Background level. 25% for pilot audibility (esp. on emulators). */
        const val TARGET_VOLUME = 0.25f
        const val FADE_MS = 1_000L
    }
}
