    package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.national_deworming_day

    import android.os.Build
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.annotation.RequiresApi
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.Lifecycle
    import androidx.lifecycle.lifecycleScope
    import androidx.lifecycle.repeatOnLifecycle
    import androidx.navigation.fragment.findNavController
    import dagger.hilt.android.AndroidEntryPoint
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.launch
    import org.piramalswasthya.sakhi.R
    import org.piramalswasthya.sakhi.adapters.DewormingAdapter
    import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
    import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
    import org.piramalswasthya.sakhi.utils.Log

    @AndroidEntryPoint
    class DewormingListFragment : Fragment() {
        companion object {
            fun newInstance() = DewormingListFragment()
        }

        private val viewModel: DewormingViewModel by viewModels()
        private var _binding: FragmentVhndListBinding? = null
        private val binding: FragmentVhndListBinding
            get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View {
            _binding = FragmentVhndListBinding.inflate(inflater, container, false)
            return binding.root
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.btnNextPage.visibility = View.VISIBLE
            val dewormingAdapter = DewormingAdapter(
                clickListener = DewormingAdapter.DewormingClickListener { id ->
                    findNavController().navigate(
                        DewormingListFragmentDirections.actionDewormingListFragmentToDewormingFormFragment(
                            id
                        )
                    )
                }
            )


            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.isCurrentMonthFormFilled.collect { statusMap ->
                        val isDEWORMINGDisabled = statusMap["DEWORMING"] == true
                        binding.btnNextPage.isEnabled = !isDEWORMINGDisabled
                    }
                }
            }


            binding.rvAny.adapter = dewormingAdapter
            binding.btnNextPage.text = getString(R.string.ndd_list_title)
            binding.btnNextPage.setOnClickListener {
                findNavController().navigate(R.id.action_DewormingListFragment_to_DewormingFormFragment)
            }
            lifecycleScope.launch {
                viewModel.allDewormingList.collect {
                    if (it.isEmpty())
                        binding.flEmpty.visibility = View.VISIBLE
                    else
                        binding.flEmpty.visibility = View.GONE
                    dewormingAdapter.submitList(it)
                }
            }
        }

        override fun onStart() {
            super.onStart()
            activity?.let {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__village_level_form,
                    getString(R.string.deworming_list_title)
                )
            }
        }
//
//        override fun onDestroyView() {
//            super.onDestroyView()
//            _binding = null
//        }
    }