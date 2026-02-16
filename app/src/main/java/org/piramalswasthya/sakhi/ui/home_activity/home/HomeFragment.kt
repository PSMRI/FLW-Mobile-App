package org.piramalswasthya.sakhi.ui.home_activity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.HomePagerAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentHomeBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.Languages.ASSAMESE
import org.piramalswasthya.sakhi.helpers.Languages.ENGLISH
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.PullFromAmritWorker
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var formRepository: FormRepository
    @Inject
    lateinit var pref: PreferenceDao
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

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
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
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated() called! $numViewCopies")
//        if (!viewModel.isLocationSet()) {
//            findNavController().navigate(HomeFragmentDirections.actionNavHomeToServiceTypeFragment())
//        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
//        binding.btnNhhr.setOnClickListener {
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNewHouseholdFragment())
//        }

        setUpViewPager()
        setUpWorkerProgress()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentLang = pref.getCurrentLanguage()
          val langCode = currentLang.symbol
            formRepository.downloadAllFormsSchemas(langCode)
        }

    }

    private fun setUpWorkerProgress() {
        WorkManager.getInstance(requireContext())
            .getWorkInfosLiveData(WorkQuery.fromUniqueWorkNames(WorkerUtils.pushWorkerUniqueName, WorkerUtils.pullWorkerUniqueName, WorkerUtils.syncWorkerUniqueName))
            .observe(viewLifecycleOwner) { workInfoMutableList ->
                workInfoMutableList?.let { list ->
                    list.takeIf { it.isNotEmpty() }?.let { workInfoMutableList1 ->

                        workInfoMutableList1.filter { it.state == WorkInfo.State.RUNNING }.takeIf {
                            it.isNotEmpty()
                        }?.first()?.let {

                            val progressData = it.progress
                            val currentPage = progressData.getInt(PullFromAmritWorker.Progress, 0)
                            val totalPage = progressData.getInt(PullFromAmritWorker.NumPages, 0)
                            binding.llFullLoadProgress.visibility = View.VISIBLE
                            binding.tvLoadProgress.text = resources.getString(R.string.downloading)

                            if (totalPage > 0) {
                                if (binding.pbLoadProgress.isIndeterminate) {
                                    binding.pbLoadProgress.isIndeterminate = false
                                }
                                val p = (currentPage * 100) / totalPage
                                Timber.tag("Current Progress").v("$p")
                                binding.pbLoadProgress.progress = p
                                binding.tvLoadProgress.text = context?.getString(
                                    R.string.home_fragment_percent_download_text,
                                    p
                                )
                            }

                        } ?: run {
                            binding.llFullLoadProgress.visibility = View.GONE
                        }
                    }
                }
            }
    }

    private fun setUpViewPager() {

        binding.vp2Home.adapter = HomePagerAdapter(this)
        TabLayoutMediator(binding.tlHomeViewpager, binding.vp2Home) { tab, position ->
            tab.text = when (position) {
                0 -> requireActivity().getString(R.string.menu_home_scheduler)
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
        (activity as HomeActivity?)?.let { homeActivity ->
            homeActivity.addClickListenerToHomepageActionBarTitle()
            viewModel.locationRecord?.village?.let {
                homeActivity.updateActionBar(
                    R.drawable.ic_home, when (viewModel.currentLanguage) {
                        ENGLISH -> it.name
                        Languages.HINDI -> it.nameHindi ?: it.name
                        ASSAMESE -> it.nameAssamese ?: it.name
                    }
                )
                homeActivity.setHomeMenuItemVisibility(false)
            }
            binding.vp2Home.setCurrentItem(1, false)
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as HomeActivity?)?.let {
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