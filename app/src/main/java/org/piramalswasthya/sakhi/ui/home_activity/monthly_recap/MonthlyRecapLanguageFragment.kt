package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentMonthlyRecapLanguageBinding
import org.piramalswasthya.sakhi.model.MonthlyRecapLanguage
import org.piramalswasthya.sakhi.ui.home_activity.home.recapAnimationsEnabled

/** Debounce window so rapid taps produce a single selection callback. */
private const val LANG_CLICK_DEBOUNCE_MS = 600L

/**
 * Monthly Recap language selection.
 *
 * ASHA-didi welcomes the ASHA and presents two equal, explicitly tappable
 * options: Hindi and Assamese. The choice is persisted to the local recap
 * snapshot (recap-only — the global Sakhi language is never changed) and
 * survives rotation, process death and restart. Language selection does NOT
 * mark the recap as started; back returns to the dashboard with the strip in
 * READY. Playback opens in a later Phase.
 */
@AndroidEntryPoint
class MonthlyRecapLanguageFragment : Fragment() {

    private var _binding: FragmentMonthlyRecapLanguageBinding? = null
    private val binding: FragmentMonthlyRecapLanguageBinding
        get() = _binding!!

    private val viewModel: MonthlyRecapLanguageViewModel by viewModels()

    private var selectionMade = false
    private var lastClickMs = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyRecapLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recapLangCardHindi.setOnClickListener { onLanguageChosen(MonthlyRecapLanguage.HINDI) }
        binding.recapLangCardAssamese.setOnClickListener { onLanguageChosen(MonthlyRecapLanguage.ASSAMESE) }
        // Defer until the view is attached, otherwise TalkBack can miss the announcement.
        view.post { view.announceForAccessibility(getString(R.string.monthly_recap_lang_screen_cd)) }

        // The ASHA always makes an explicit, fresh choice: the screen never
        // pre-selects (no auto-select from a previously stored choice or from the
        // app language). The tap still persists the recap-only language downstream.
        setUpWelcomeDidi()
    }

    /**
     * The welcome character is a looping Lottie (waves, blinks, greets). We drive
     * playback from code so reduced-motion is respected: when animations are off we
     * show a single static frame and never play.
     */
    private fun setUpWelcomeDidi() {
        if (!recapAnimationsEnabled(requireContext())) {
            binding.recapLangDidi.progress = 0f // static friendly frame, no motion
            return
        }
        playWelcomeEntrance()
    }

    // ---------------------------------------------------------------- selection

    private fun onLanguageChosen(language: MonthlyRecapLanguage) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickMs < LANG_CLICK_DEBOUNCE_MS) return
        lastClickMs = now
        if (selectionMade) return
        selectionMade = true

        applySelectionVisuals(
            language,
            announce = true,
            bounce = recapAnimationsEnabled(requireContext())
        )
        // Single isolated callback: persists recap-only language to the local
        // snapshot (idempotent get-or-create underneath). No global language
        // change, no playback navigation (later Phase).
        viewModel.onLanguageSelected(language)
    }

    /** One clear visual confirmation: highlight the choice, quiet the other. */
    private fun applySelectionVisuals(
        language: MonthlyRecapLanguage,
        announce: Boolean,
        bounce: Boolean,
    ) {
        val hindi = language == MonthlyRecapLanguage.HINDI
        val selectedCard = if (hindi) binding.recapLangCardHindi else binding.recapLangCardAssamese
        val otherCard = if (hindi) binding.recapLangCardAssamese else binding.recapLangCardHindi

        selectedCard.strokeColor =
            MaterialColors.getColor(selectedCard, com.google.android.material.R.attr.colorPrimary)
        selectedCard.strokeWidth = dp(2.5f).toInt()
        otherCard.alpha = 0.45f
        selectedCard.isClickable = false
        otherCard.isClickable = false
        otherCard.isEnabled = false

        val confirmation = getString(
            if (hindi) R.string.monthly_recap_lang_selected_hi
            else R.string.monthly_recap_lang_selected_as
        )
        binding.recapLangConfirmation.text = confirmation
        binding.recapLangConfirmation.visibility = View.VISIBLE
        if (announce) binding.recapLangConfirmation.announceForAccessibility(confirmation)

        // Didi shares the joy: one small, restrained happy bounce.
        if (bounce) {
            binding.recapLangDidi.animate()
                .scaleX(1.05f).scaleY(1.05f).setDuration(140L)
                .withEndAction {
                    _binding?.recapLangDidi?.animate()
                        ?.scaleX(1f)?.scaleY(1f)?.setDuration(140L)?.start()
                }
                .start()
        }
    }

    // ---------------------------------------------------------------- entrance

    /**
     * One-shot entrance: the character fades + scales in, then her Lottie loop
     * (wave / blink / greet) starts; the instruction and language cards follow with
     * a short stagger. Taps work from the first frame.
     */
    private fun playWelcomeEntrance() {
        val b = binding

        b.recapLangDidi.apply { alpha = 0f; scaleX = 0.85f; scaleY = 0.85f; translationY = dp(16f) }
        val followers = listOf(
            b.recapLangInstructionHi, b.recapLangInstructionAs, b.recapLangOptionsRow
        )
        followers.forEach { it.alpha = 0f; it.translationY = dp(16f) }

        b.recapLangDidi.animate()
            .alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
            .setStartDelay(60L)
            .setDuration(340L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { _binding?.recapLangDidi?.playAnimation() }
            .start()

        followers.forEachIndexed { index, viewToShow ->
            viewToShow.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(260L + index * 80L)
                .setDuration(300L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    // ---------------------------------------------------------------- lifecycle

    override fun onDestroyView() {
        _binding?.let { b ->
            b.recapLangDidi.animate().cancel()
            b.recapLangDidi.pauseAnimation()
            b.recapLangInstructionHi.animate().cancel()
            b.recapLangInstructionAs.animate().cancel()
            b.recapLangOptionsRow.animate().cancel()
        }
        super.onDestroyView()
        _binding = null
    }

    private fun dp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}
