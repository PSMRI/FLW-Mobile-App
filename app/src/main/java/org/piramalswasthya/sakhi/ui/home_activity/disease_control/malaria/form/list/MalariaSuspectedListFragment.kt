package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.IrsRoundListAdapter
import org.piramalswasthya.sakhi.adapters.MalariaMemberListAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.form.MalariaFormViewModel
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber


@AndroidEntryPoint
class MalariaSuspectedListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: MalariaSuspectedViewModel by viewModels()
    private val irsViewModel: MalariaIRSViewModel by viewModels()
    private val irsListViewmodel: IRSRoundListViewModel by viewModels()

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
        if (viewModel.isFromDisease == 1 && viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()) {
            binding.switchButton.visibility = View.VISIBLE
            binding.switchBtnMosquito.visibility = View.VISIBLE
        } else if(viewModel.isFromDisease == 1) {
            binding.switchButton.visibility = View.GONE
            binding.switchBtnMosquito.visibility = View.GONE
        } else {
            binding.switchButton.visibility = View.GONE
            binding.switchBtnMosquito.visibility = View.GONE
        }
        binding.switchButton.text = if (binding.switchButton.isChecked) "IRS ON" else "IRS OFF"
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            binding.switchButton.text = if (isChecked) "IRS ON" else "IRS OFF"
            binding.llContent.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.switchBtnMosquito.text = if (binding.switchBtnMosquito.isChecked) "Mosquito Net ON" else "Mosquito Net OFF"
        binding.switchBtnMosquito.setOnCheckedChangeListener { _, isChecked ->
            binding.switchBtnMosquito.text = if (isChecked) "Mosquito Net ON" else "Mosquito Net OFF"
            binding.llContent.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val benAdapter = MalariaMemberListAdapter(
            clickListener = MalariaMemberListAdapter.ClickListener { hhId, benId ->
                findNavController().navigate(
                    MalariaSuspectedListFragmentDirections.actionMalariaSuspectedListFragmentToMalariaFormFragment(
                        benId = benId
                    )
                )
            },
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

        irsViewModel.checkSubmitButtonVisibility()

        lifecycleScope.launchWhenStarted {
            irsViewModel.isSubmitVisible.collect { isVisible ->
                binding.btnSubmit.isEnabled = if (isVisible) true else false
            }
        }


        val irsListAdapter = IrsRoundListAdapter()
        binding.irsAny.adapter = irsListAdapter
        lifecycleScope.launch {
            irsListViewmodel.allBenList.collect {
                irsListAdapter.submitList(it)
            }
        }

                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        irsViewModel.updateListOnValueChanged(formId, index)
                    }, isEnabled = true
                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    irsViewModel.formList.collect {
                        if (it.isNotEmpty()) {
                            adapter.notifyItemChanged(irsViewModel.getIndexOfDate())
                            adapter.submitList(it)
                        }

                    }

        }

        binding.btnSubmit.setOnClickListener {
            submitIRSScreeningForm()
        }

        irsViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                MalariaIRSViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.irs_submitted), Toast.LENGTH_SHORT
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }
    }

    private fun submitIRSScreeningForm() {
        irsViewModel.saveForm()
    }

    private fun validateCurrentPage(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1) true
        else {
            if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hh,
                getString(R.string.icon_title_maleria)
            )
        }
    }
}