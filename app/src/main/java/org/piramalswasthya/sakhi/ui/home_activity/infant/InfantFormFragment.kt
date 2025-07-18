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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.VisitCardAdapter
import org.piramalswasthya.sakhi.databinding.FragmentInfantFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc.HBNCFormViewModel
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker

@AndroidEntryPoint
class InfantFormFragment : Fragment() {

    private var _binding: FragmentInfantFormBinding? = null
    private val binding get() = _binding!!
    private val args: InfantFormFragmentArgs by navArgs()
    private val viewModel: HBNCFormViewModel by viewModels()
    val benId = 0L
    val hhId = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfantFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        FormSyncWorker.enqueue(requireContext())

//        val benId = args.benId
//        val hhId = args.hhId


        // Load infant and synced visits
        viewModel.loadInfant(benId, hhId)
        viewModel.loadSyncedVisitList(benId)

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("form_submitted")
            ?.observe(viewLifecycleOwner) { submitted ->
                if (submitted == true) {
                    // 🔄 Refresh your visit list
                    viewModel.loadInfant(benId, hhId)
                    viewModel.loadSyncedVisitList(benId)

                    // 🧹 Clear the flag
                    savedStateHandle.remove<Boolean>("form_submitted")
                }
            }

        // Show infant details
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.infant.collectLatest { infant ->
                Log.d("InfantForm", "👶 Infant loaded: $infant")
                infant?.let {
                    binding.tvRchId?.text = it.benId.toString()
                }
            }
        }

        // Observe visit list and show visit cards
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncedVisitList.collectLatest {
                    Log.d("InfantForm", "📄 Synced visits changed")

                    val cards = viewModel.getVisitCardList()
                    Log.d("InfantForm", "✅ VisitCards: $cards")

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

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__child,
                getString(R.string.hbnc_day_list)
            )
        }
    }
}
