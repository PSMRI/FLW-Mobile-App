package org.piramalswasthya.sakhi.ui.asha_facilitator_activity.facilitator_home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FacilitatorHomePageAdapter
import org.piramalswasthya.sakhi.databinding.FragmentFacilitatorHomeBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.ui.asha_facilitator_activity.FacilitatorsHomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.EnableDevModeBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import timber.log.Timber

@AndroidEntryPoint
class FacilitatorHomeFragment : Fragment() {

    companion object {
        var numViewCopies = 0
        var numCopies = 0
    }

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.exit_application))
            .setMessage(resources.getString(R.string.do_you_want_to_exit_application))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                activity?.finish()
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private var _binding: FragmentFacilitatorHomeBinding? = null
    private val binding: FragmentFacilitatorHomeBinding
        get() = _binding!!
    private val viewModel: HomeViewModel by viewModels({ requireActivity() })
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!exitAlert.isShowing)
                    exitAlert.show()

            }
        }
    }

    private val enableDevMode: EnableDevModeBottomSheetFragment by lazy {
        EnableDevModeBottomSheetFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        numCopies++
        Timber.d("onCreate() called! $numCopies")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        numViewCopies++
        _binding = FragmentFacilitatorHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated() called! $numViewCopies")
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        setUpViewPager()

    }

    private fun setUpViewPager() {
        binding.vp2Home.adapter = FacilitatorHomePageAdapter(this)
        TabLayoutMediator(binding.tlHomeViewpager, binding.vp2Home) { tab, position ->
            tab.text = when (position) {
                0 -> requireActivity().getString(R.string.dashboard)
                1 -> requireActivity().getString(R.string.menu_home_home)
                else -> "NA"
            }
            if (position == 1) {
                tab.view.setOnLongClickListener {
                    if (viewModel.getDebMode()) {
                        viewModel.setDevMode(false)
                        Toast.makeText(context, "Dev Mode Disabled!", Toast.LENGTH_LONG).show()

                    } else {
                        if (!enableDevMode.isVisible)
                            enableDevMode.show(childFragmentManager, "DEV_MODE")
                    }
                    true
                }
            }
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        (activity as FacilitatorsHomeActivity?)?.let { facilitatorhomeActivity ->
            facilitatorhomeActivity.addClickListenerToHomepageActionBarTitle()
            viewModel.locationRecord?.village?.let {
                facilitatorhomeActivity.updateActionBar(
                    R.drawable.ic_home, when (viewModel.currentLanguage) {
                        Languages.ENGLISH -> it.name
                        Languages.HINDI -> it.nameHindi ?: it.name
                        Languages.ASSAMESE -> it.nameAssamese ?: it.name
                    }
                )
                facilitatorhomeActivity.setHomeMenuItemVisibility(false)
            }
            binding.vp2Home.setCurrentItem(1, false)
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as FacilitatorsHomeActivity?)?.let {
            it.setHomeMenuItemVisibility(true)
            it.removeClickListenerToHomepageActionBarTitle()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        numViewCopies--
        Timber.d("onDestroyView() called! $numViewCopies")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        numCopies--
        Timber.d("onDestroy() called! $numCopies")

    }
}