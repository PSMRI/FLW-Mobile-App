package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.phc_review_meeting

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import org.piramalswasthya.sakhi.adapters.PHCAdapter
import org.piramalswasthya.sakhi.adapters.VHNCAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class PHCReviewListFragement : Fragment() {
    companion object {
        fun newInstance() = PHCReviewListFragement()
    }

    private val viewModel: PHCReviewViewModel by viewModels()
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
        val pHCAdapter = PHCAdapter(
        clickListener = PHCAdapter.PHCClickListener { id ->
            findNavController().navigate(
                PHCReviewListFragementDirections.actionPHCReviewListFragementToPHCReviewFormFragement(
                    id
                )
            )
        })
        binding.rvAny.adapter = pHCAdapter
        binding.btnNextPage.text=getString(R.string.icon_title_phc)
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_PHCReviewListFragement_to_PHCReviewFormFragement)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isCurrentMonthFormFilled.collect { statusMap ->
                    val isPHCDisabled = statusMap["PHC"] == true
                    binding.btnNextPage.isEnabled = !isPHCDisabled
                }
            }
        }
        lifecycleScope.launch {
            viewModel.allPHCCList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                pHCAdapter.submitList(it)
            }
        }


    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.icon_title_phc_list)
            )
        }
    }
}