package org.piramalswasthya.sakhi.ui.asha_facilitator_activity.facilitator_home.form

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.IconAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.FragmentFacilitatorFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FacilitatorFormFragment : Fragment() {

    private var _binding: FragmentFacilitatorFormBinding? = null
    private val binding: FragmentFacilitatorFormBinding
        get() = _binding!!

    private val viewModel: FacilitatorFormViewModel by viewModels({requireActivity()})

    @Inject
    lateinit var iconDataset: IconDataset

    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFacilitatorFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = recordExists
                )
                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())
                            adapter.submitList(it)

                    }
                }
            }
        }
        setUpHomeIconRvAdapter()

    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)

                }
                4, 5 -> {
                    notifyItemChanged(4)
                }

            }
        }
    }

    private fun setUpHomeIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            2
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconAdapter(IconAdapter.GridIconClickListener {
            findNavController().navigate(it)
        }, homeViewModel.scope)
        binding.rvIconGrid.adapter = rvAdapter
        homeViewModel.devModeEnabled.observe(viewLifecycleOwner) {
            Timber.d("update called!~~ $it")
            rvAdapter.submitList(iconDataset.getHomeIconDataset(resources))
        }

    }
}