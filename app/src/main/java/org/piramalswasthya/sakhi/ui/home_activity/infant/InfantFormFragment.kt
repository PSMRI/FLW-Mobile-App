package org.piramalswasthya.sakhi.ui.home_activity.infant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker

@AndroidEntryPoint
class InfantFormFragment : Fragment() {

    private var _binding: FragmentInfantFormBinding? = null
    private val binding get() = _binding!!
    private val args: InfantFormFragmentArgs by navArgs()
    private val viewModel: HBNCFormViewModel by viewModels()
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
        Toast.makeText(requireContext(), " Infant $benId and hhId: $hhId", Toast.LENGTH_LONG).show()


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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncedVisitList.collectLatest {
                    val cards = viewModel.getVisitCardList()

                    binding.recyclerVisitCards.layoutManager =
                        GridLayoutManager(requireContext(), 3)
                    binding.recyclerVisitCards.adapter = VisitCardAdapter(cards) { card ->
                        val action = InfantFormFragmentDirections
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
                R.drawable.ic__child,
                getString(R.string.hbnc_day_list)
            )
        }
    }
}
