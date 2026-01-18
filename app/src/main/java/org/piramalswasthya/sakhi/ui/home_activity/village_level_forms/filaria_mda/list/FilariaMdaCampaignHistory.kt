package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.filaria_mda.list

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
import org.piramalswasthya.sakhi.adapters.PHCAdapter
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FilariaMdaCampaignAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.form.FilariaMDAFormViewModel
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.filaria_mda.FilariaMdaFormCampaignViewModel
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.phc_review_meeting.PHCReviewListFragement
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.phc_review_meeting.PHCReviewListFragementDirections
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.phc_review_meeting.PHCReviewViewModel
import kotlin.getValue

@AndroidEntryPoint
class FilariaMdaCampaignHistory : Fragment() {
    companion object {
        fun newInstance() = FilariaMdaCampaignHistory()
    }

    private val viewModel: FilariaMdaFormCampaignViewModel by viewModels()
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
        binding.mdaTitle.visibility = View.VISIBLE
        val pHCAdapter = FilariaMdaCampaignAdapter(
            clickListener = FilariaMdaCampaignAdapter.MdaClickListener { id ->
                findNavController().navigate(
                    FilariaMdaCampaignHistoryDirections.actionMdaCampagaignHisoryFragmentToMdaFormCampaign(
                        id
                    )
                )
            })
        binding.rvAny.adapter = pHCAdapter
        binding.btnNextPage.text = resources.getString(R.string.add_mda_filaria_form_title)
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_mdaCampagaignHisoryFragment_to_mdaFormCampaign)
        }
       /* viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isCurrentMonthFormFilled.collect { statusMap ->
                    val isPHCDisabled = statusMap["PHC"] == true
                    binding.btnNextPage.isEnabled = isPHCDisabled
                }
            }
        }*/

        viewModel.bottleList.observe(viewLifecycleOwner) { list ->

            if (!list.isNullOrEmpty()) {
                  binding.flEmpty.visibility = View.GONE
                    pHCAdapter.submitList(list)
            } else {
                binding.flEmpty.visibility = View.VISIBLE
            }
        }
        viewModel.loadBottleData()



    }

    override fun onStart() {
        super.onStart()

      val title =  resources.getString(R.string.history_of_mda_form_campaign)
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                title
            )
        }
    }
}