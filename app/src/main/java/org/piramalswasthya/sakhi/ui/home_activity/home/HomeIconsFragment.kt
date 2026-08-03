package org.piramalswasthya.sakhi.ui.home_activity.home

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/** Small, generic playback position shown in RESUME. Not health/category data. */
private const val GENERIC_RESUME_PROGRESS = 40

/** Debounce window so rapid taps on the strip produce a single callback. */
private const val RECAP_CLICK_DEBOUNCE_MS = 600L

/** Debug preview token meaning "derive the state from local recap data". */
private const val MONTHLY_RECAP_PREVIEW_AUTO = "auto"

@AndroidEntryPoint
class HomeIconsFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    @Inject
    lateinit var pref: PreferenceDao

    private var _binding: RvIconGridBinding? = null
    private val binding: RvIconGridBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireActivity() })

    private val recapStripViewModel: MonthlyRecapStripViewModel by viewModels()

    /** Guards the recap entrance so it plays at most once per view lifecycle. */
    private var recapEntrancePlayed = false

    /** Timestamp of the last accepted strip click, for debouncing. */
    private var lastRecapClickMs = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = RvIconGridBinding.inflate(layoutInflater, container, false)
        recapEntrancePlayed = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHomeIconRvAdapter()
        setUpMonthlyRecapStrip()
    }

    /**
     * Strip state now derives from local recap persistence via
     * [MonthlyRecapStripViewModel]: availability (fail-closed; release stays
     * unavailable until real rollout integration) + snapshot status
     * (none/NOT_STARTED -> READY, IN_PROGRESS -> RESUME, COMPLETED -> REPLAY).
     *
     * Debug builds keep a forced-preview override: any token other than "auto"
     * in monthly_recap_debug.xml renders that state directly for visual testing.
     */
    private fun setUpMonthlyRecapStrip() {
        if (BuildConfig.DEBUG) {
            val token = getString(R.string.monthly_recap_preview_state)
                .trim().lowercase(Locale.ROOT)
            if (token != MONTHLY_RECAP_PREVIEW_AUTO) {
                renderMonthlyRecapStrip(MonthlyRecapStripState.fromToken(token))
                return
            }
        }
        recapStripViewModel.stripState.observe(viewLifecycleOwner) { state ->
            renderMonthlyRecapStrip(state)
        }
    }

    /**
     * Re-evaluates recap visibility each time the dashboard becomes visible: a
     * background sync may have landed the previous month's records after the
     * first (hidden) check, and the strip must not stay stale until restart.
     */
    override fun onResume() {
        super.onResume()
        recapStripViewModel.refresh()
    }

    private fun renderMonthlyRecapStrip(state: MonthlyRecapStripState) {
        val strip = binding.monthlyRecapStrip
        val card = strip.root
        val style = styleFor(state)

        if (!style.visible) {
            // HIDDEN: no view, no click target, no accessibility focus, no animation.
            card.animate().cancel()
            card.setOnClickListener(null)
            card.isClickable = false
            card.isFocusable = false
            card.visibility = View.GONE
            return
        }

        // Resolve strip text through the app's CURRENT language explicitly. The
        // ambient context is not reliably localised on newer APIs, so — mirroring
        // the app's own pattern (e.g. HRPPregnantTrackViewModel) — we build a
        // Resources for the selected language. This makes the strip title, message,
        // CTA, badges, content-description and month name follow the app language.
        val locale = Locale(pref.getCurrentLanguage().symbol)
        val res = localizedResources(locale)

        val monthLabel = MonthlyRecapMonth.previousMonthLabel(locale)
        strip.recapMonthLabel.text = monthLabel
        strip.recapMonthLabel.visibility = if (monthLabel.isBlank()) View.GONE else View.VISIBLE

        strip.recapTitle.text = res.getString(R.string.monthly_recap_title)
        strip.recapMessage.text = res.getString(style.messageRes)
        strip.recapCta.text = res.getString(style.ctaRes)
        strip.recapBadgeNew.text = res.getString(R.string.monthly_recap_badge_new)
        strip.recapBadgeDone.text = res.getString(R.string.monthly_recap_badge_done)

        strip.recapBadgeNew.visibility = if (style.showNewBadge) View.VISIBLE else View.GONE
        strip.recapBadgeDone.visibility = if (style.showCompletedBadge) View.VISIBLE else View.GONE

        if (style.showProgress) {
            // Generic playback position only — never category/health information.
            strip.recapProgress.progress = GENERIC_RESUME_PROGRESS
            strip.recapProgress.visibility = View.VISIBLE
        } else {
            strip.recapProgress.visibility = View.GONE
        }

        card.contentDescription = res.getString(style.contentDescriptionRes)
        card.isClickable = true
        card.isFocusable = true
        card.visibility = View.VISIBLE
        card.setOnClickListener { onRecapStripClicked(state) }

        setUpRecapCharacterAnimation()
        playRecapEntrance(state, style)
    }

    /**
     * The card character is a looping Lottie of the ASHA-didi — the same character
     * who narrates the recap, so the card and the story feel like one experience.
     * Reduced-motion devices get a single static frame and never play.
     */
    private fun setUpRecapCharacterAnimation() {
        val character = _binding?.monthlyRecapStrip?.recapCharacter ?: return
        if (recapAnimationsEnabled(requireContext())) {
            character.playAnimation()
        } else {
            character.progress = 0f
        }
    }

    private fun onRecapStripClicked(state: MonthlyRecapStripState) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRecapClickMs < RECAP_CLICK_DEBOUNCE_MS) return
        lastRecapClickMs = now
        when (state) {
            // Every visible state opens playback directly. The recap is narrated in
            // the language the ASHA already uses the app in, so there is nothing to
            // ask her first. The ViewModel resumes an IN_PROGRESS story at its stored
            // scene; READY and REPLAY start at scene 0.
            MonthlyRecapStripState.READY,
            MonthlyRecapStripState.RESUME,
            MonthlyRecapStripState.REPLAY -> navigateToRecapPlayback()

            MonthlyRecapStripState.HIDDEN -> Unit
        }
    }

    /** Opens the recap playback screen exactly once per accepted tap. */
    private fun navigateToRecapPlayback() {
        navigateFromHome { it.navigate(HomeFragmentDirections.actionNavHomeToMonthlyRecapPlaybackFragment()) }
    }

    private inline fun navigateFromHome(navigate: (androidx.navigation.NavController) -> Unit) {
        val navController = findNavController()
        try {
            // Only navigate from home; prevents duplicate destinations on stale taps.
            if (navController.currentDestination?.id == R.id.homeFragment) navigate(navController)
        } catch (e: Exception) {
            Timber.e(e, "Monthly Recap navigation failed")
        }
    }

    /**
     * Warm one-shot entrance for the character, tied to this view's lifecycle and
     * played at most once. READY gets the full slide + settle + sparkles; RESUME a
     * gentler slide; REPLAY stays static. When animators are disabled the layout is
     * already in its final resting state, so nothing needs to run.
     */
    private fun playRecapEntrance(state: MonthlyRecapStripState, style: MonthlyRecapStripStyle) {
        if (recapEntrancePlayed) return
        recapEntrancePlayed = true
        if (!style.animateEntrance || !recapAnimationsEnabled(requireContext())) return

        val strip = binding.monthlyRecapStrip
        val character = strip.recapCharacter
        val text = strip.recapTextColumn
        val isReady = state == MonthlyRecapStripState.READY

        // She arrives first, walking in from the start edge...
        character.translationX = -dp(if (isReady) 56f else 32f)
        character.alpha = 0f
        character.animate()
            .translationX(0f)
            .alpha(1f)
            .setStartDelay(80L)
            .setDuration(if (isReady) 480L else 360L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { onRecapEntranceSettled(isReady) }
            .start()

        // ...and the words settle in just behind her, so the card reads as one
        // thing arriving rather than a character bolted onto a static banner.
        text.translationY = dp(10f)
        text.alpha = 0f
        text.animate()
            .translationY(0f)
            .alpha(1f)
            .setStartDelay(200L)
            .setDuration(380L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun onRecapEntranceSettled(isReady: Boolean) {
        val strip = _binding?.monthlyRecapStrip ?: return
        if (isReady) {
            // Restrained celebratory settle.
            strip.recapCharacter.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(130L)
                .withEndAction {
                    val settled = _binding?.monthlyRecapStrip?.recapCharacter ?: return@withEndAction
                    settled.animate().scaleX(1f).scaleY(1f).setDuration(130L).start()
                }
                .start()
        }
    }

    private fun dp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

    /** Resources bound to [locale] so strip text follows the app's selected language. */
    private fun localizedResources(locale: Locale): Resources {
        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(locale)
        return requireContext().createConfigurationContext(config).resources
    }

    private fun setUpHomeIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(IconGridAdapter.GridIconClickListener { navDirections ->
            val navController = findNavController()

            try {
                val currentDestinationId = navController.currentDestination?.id

                // Skip navigation if already on destination
                if (currentDestinationId == R.id.villageLevelFormsFragment &&
                    navDirections == HomeFragmentDirections.actionNavHomeToVillageLevelFormsFragment()
                ) {
                    Timber.d("Already at destination: skipping navigation.")
                    return@GridIconClickListener
                }

                navController.navigate(navDirections)
            } catch (e: Exception) {
                Timber.e(e, "Navigation failed")
            }
           // findNavController().navigate(it)
        }, viewModel.scope)
        binding.rvIconGrid.adapter = rvAdapter
        viewModel.devModeEnabled.observe(viewLifecycleOwner) {
            Timber.d("update called!~~ $it")
            rvAdapter.submitList(iconDataset.getHomeIconDataset(resources))
        }

    }

    override fun onDestroyView() {
        // Cancel any recap entrance animation + stop the Lottie so neither touches
        // a destroyed view.
        _binding?.monthlyRecapStrip?.recapCharacter?.apply {
            animate().cancel()
            cancelAnimation()
        }
        _binding?.monthlyRecapStrip?.recapTextColumn?.animate()?.cancel()
        super.onDestroyView()
        _binding = null
    }
}