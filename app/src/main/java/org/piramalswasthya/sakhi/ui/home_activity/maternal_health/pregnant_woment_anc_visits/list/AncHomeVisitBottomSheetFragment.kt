package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.AncHomeVisitAdapter
import org.piramalswasthya.sakhi.adapters.AncVisitAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.BottomSheetAncBinding
import org.piramalswasthya.sakhi.databinding.BottomSheetAncHomeVisitBinding
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class AncHomeVisitBottomSheetFragment : BottomSheetDialogFragment () {

    private val viewModel: AncHomeVisitViewModel by viewModels()
    private lateinit var adapter: AncHomeVisitAdapter
    private var beneficiaryId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        adapter = AncHomeVisitAdapter(
            clickListener = AncHomeVisitAdapter.HomeVisitClickListener { homeVisit ->

                findNavController().navigate(
                    PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncCounsellingFormFragment(
                        homeVisit.benId,homeVisit.visitNumber+1,true,homeVisit.visitDateString
                    )
                )
            }
        )

        val binding = BottomSheetAncHomeVisitBinding.inflate(inflater, container, false)
        binding.rvAncHomeVisit.adapter = adapter
        binding.rvAncHomeVisit.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        beneficiaryId = arguments?.getLong("benId") ?: 0

        if (beneficiaryId > 0) {
            viewModel.loadHomeVisits(beneficiaryId)

            viewModel.homeVisits.observe(viewLifecycleOwner) { visits ->
                adapter.submitList(visits)

                if (visits.isEmpty()) {
                    showEmptyState()
                }
            }
        }
    }



    private fun showEmptyState() {

    }


}