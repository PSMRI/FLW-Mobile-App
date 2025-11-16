package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.LeprosyMemberListAdapter
import org.piramalswasthya.sakhi.adapters.VisitsAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.databinding.LayoutVisitsBottomSheetBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithLeprosyScreeningDomain
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.kala_azar.list.KalaAzarSuspectedListFragmentDirections

@AndroidEntryPoint
class LeprosySuspectedListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: LeprosySuspectedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE
        binding.llSearch.visibility = View.GONE

        val benAdapter = LeprosyMemberListAdapter(
            clickListener = LeprosyMemberListAdapter.ClickListener(
                clickedForm = { hhId, benId ->
                    findNavController().navigate(
                        LeprosySuspectedListFragmentDirections.actionLeprosySuspectedListFragmentToLeprosyFormFragment(
                            benId = benId
                        )
                    )
                },
                clickedVisits = { benWithLeprosy ->
                    showVisitsBottomSheet(benWithLeprosy)
                }
            )
        )
        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.allBenList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }




    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hh,
                getString(R.string.icon_title_leprosy)
            )
        }
    }


    private fun showVisitsBottomSheet(benWithLeprosy: BenWithLeprosyScreeningDomain) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = LayoutVisitsBottomSheetBinding.inflate(layoutInflater)

        val currentVisitNumber = benWithLeprosy.leprosy?.currentVisitNumber ?: 1
        val visitNumbers = (1 until currentVisitNumber).toList()

        println("DEBUG: Visit Numbers list = $visitNumbers")

        if (visitNumbers.isEmpty()) {
            binding.rvVisits.visibility = View.GONE
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else {
            binding.rvVisits.visibility = View.VISIBLE
            binding.tvEmptyMessage.visibility = View.GONE

            binding.rvVisits.layoutManager = LinearLayoutManager(requireContext())

            val visitsAdapter = VisitsAdapter(visitNumbers) { visitNumber ->
                println("DEBUG: Visit $visitNumber clicked")
                navigateToLeprosyVisitFragment(benWithLeprosy.ben.benId, visitNumber)
                bottomSheetDialog.dismiss()
            }

            binding.rvVisits.adapter = visitsAdapter
        }

        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()

        binding.rvVisits.post {
            println("DEBUG: RecyclerView child count = ${binding.rvVisits.childCount}")
            println("DEBUG: RecyclerView adapter item count = ${binding.rvVisits.adapter?.itemCount}")
        }
    }

    private fun navigateToLeprosyVisitFragment(benId: Long, visitNumber: Int) {
        findNavController().navigate(
            LeprosySuspectedListFragmentDirections.actionLeprosySuspectedListFragmentToLeprosyVisitFragment(
                benId = benId,
                visitNumber = visitNumber
            )
        )
    }

}




