package org.piramalswasthya.sakhi.ui.home_activity.infant

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.VisitCardAdapter
import org.piramalswasthya.sakhi.databinding.FragmentInfantFormBinding
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard
import org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc.HBNCFormViewModel
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker

@AndroidEntryPoint
class InfantFormFragment : Fragment() {

    private var _binding: FragmentInfantFormBinding? = null
    private val binding get() = _binding!!
    private val args: InfantFormFragmentArgs by navArgs()

    private val viewModel: HBNCFormViewModel by viewModels()

    private val rchId = "1"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfantFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FormSyncWorker.enqueue(requireContext())
         val benId = args.benId
         val hhId = args.hhId
        viewModel.loadInfant(benId,hhId)

        // Observe infant basic info
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.infant.collectLatest { infant ->
                Log.d("InfantForm", "👶 Infant loaded: $infant")
                infant?.let {
//                    binding.tvName?.text = it.name ?: "Baby of ${it.motherName}"
//                    binding.tvDob?.text = it.dob ?: "-"
                    binding.tvRchId?.text = it.benId.toString() ?: "-"
                }
            }
        }

        // ✅ Observe visits and refresh on resume
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadSyncedVisitList(benId) // Load fresh every time fragment resumes

                viewModel.syncedVisitList.collectLatest { visits ->
                    Log.d("InfantForm", "📄 Synced visits: ${visits.size}")

                    val visitDays = listOf(
                        "1st Day", "3rd Day", "7th Day",
                        "14th Day", "21st Day", "28th Day", "42nd Day"
                    )

                    val completed = visits.map { it.visitDay }
                    val nextEditable = viewModel.getNextEligibleVisitDay()
                    val dob = viewModel.infant.value?.visitDay ?: "-"

                    val cards = visitDays.map { day ->
                        val visit = visits.find { it.visitDay == day }
                        val isCompleted = visit != null
                        val isEditable = when (day) {
                            nextEditable -> true
                            "14th Day", "21st Day", "28th Day" -> "7th Day" in completed && day !in completed
                            "42nd Day" -> "7th Day" in completed && "28th Day" in completed && day !in completed
                            else -> false
                        }

                        val visitDate = visit?.let {
                            try {
                                val json = JSONObject(it.formDataJson)
                                json.optString("visitDate", "-")
                            } catch (e: Exception) {
                                "-"
                            }
                        } ?: viewModel.calculateDueDate(dob, day) ?: "-"

                        VisitCard(
                            visitDay = day,
                            visitDate = visitDate,
                            isCompleted = isCompleted,
                            isEditable = isEditable
                        )
                    }

                    binding.recyclerVisitCards.layoutManager = GridLayoutManager(requireContext(), 3)
                    binding.recyclerVisitCards.adapter = VisitCardAdapter(cards) { card ->
                        val action = InfantFormFragmentDirections
                            .actionInfantFormFragmentToHbncFormFragment(
                                visitDay = card.visitDay,
                                isViewMode = !card.isEditable,
                                formId = "hbnc_form_001"
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
