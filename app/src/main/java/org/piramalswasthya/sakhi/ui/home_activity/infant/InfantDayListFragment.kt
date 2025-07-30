package org.piramalswasthya.sakhi.ui.home_activity.infant

import android.os.Bundle
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
import org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.InfantListViewModel
import org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc.HBNCFormViewModel
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker

@AndroidEntryPoint
class InfantDayListFragment : Fragment() {

    private var _binding: FragmentInfantFormBinding? = null
    private val binding get() = _binding!!
    private val args: InfantDayListFragmentArgs by navArgs()
    private val viewModel: HBNCFormViewModel by viewModels()
    var dob=0L
    private val infantListViewModel: InfantListViewModel by viewModels()
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

        viewModel.loadInfant(benId, hhId)
        viewModel.loadSyncedVisitList(benId)

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("form_submitted")
            ?.observe(viewLifecycleOwner) { submitted ->
                if (submitted == true) {
                    viewModel.loadInfant(benId, hhId)
                    viewModel.loadSyncedVisitList(benId)
                    savedStateHandle.remove<Boolean>("form_submitted")
                }
            }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.infant.collectLatest { infant ->
                infant?.let {
                    binding.tvRchId?.text = it.benId.toString()
                }
            }
        }
        infantListViewModel.getDobByBenIdAsync(benId) { dobMillis ->
            if (dobMillis != null) {
                dob=dobMillis
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncedVisitList.collectLatest {
                    val cards = viewModel.getVisitCardList(dob)

                    binding.recyclerVisitCards.layoutManager =
                        GridLayoutManager(requireContext(), 2)
                    binding.recyclerVisitCards.adapter = VisitCardAdapter(cards) { card ->
                        val action = InfantDayListFragmentDirections
                            .actionInfantFormFragmentToHbncFormFragment(
                                benId,hhId,
                                visitDay = card.visitDay,
                                isViewMode = !card.isEditable,
                                formId = HBNC_FORM_ID
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
                R.drawable.ic__infant,
                getString(R.string.hbnc_day_list)
            )
        }
    }
}
