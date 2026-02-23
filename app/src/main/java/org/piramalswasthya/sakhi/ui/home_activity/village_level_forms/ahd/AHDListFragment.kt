package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ahd

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
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.AHDAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class AHDListFragment : Fragment() {
    companion object {
        fun newInstance() = AHDListFragment()
    }

    private val viewModel: AHDViewModel by viewModels()
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
        val ahdAdapter = AHDAdapter(
            clickListener = AHDAdapter.AHDClickListener { id ->
                findNavController().navigate(
                    AHDListFragmentDirections.actionAHDListFragmentToAHDFormFragment(id)
                )
            }
        )
        binding.rvAny.adapter = ahdAdapter
        binding.btnNextPage.text = getString(R.string.icon_title_ahd)
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_AHDListFragment_to_AHDFormFragment)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isCurrentMonthFormFilled.collect { statusMap ->
                    val isAHDDisabled = statusMap["AHD"] == true
                    binding.btnNextPage.isEnabled = !isAHDDisabled
                }
            }
        }
        lifecycleScope.launch {
            viewModel.allAHDList.collect {
                binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                ahdAdapter.submitList(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.icon_title_ahd_list)
            )
        }
    }
}