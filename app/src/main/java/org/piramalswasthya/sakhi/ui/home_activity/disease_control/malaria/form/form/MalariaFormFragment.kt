package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.form

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.MalariaMemberListAdapter
import org.piramalswasthya.sakhi.adapters.VisitsListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentMaleriaFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.BaseFormFragment
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list.MalariaSuspectedListFragmentDirections
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class MalariaFormFragment : BaseFormFragment() {
    private var _binding: FragmentMaleriaFormBinding? = null

    private val viewModel: MalariaFormViewModel by viewModels()

    private val binding: FragmentMaleriaFormBinding
        get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMaleriaFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            if (viewModel.isSuspected) {
                viewModel.isBeneficaryStatusDeath.observe(viewLifecycleOwner){ it
                    binding.fabEdit.visibility = if(notIt && !it && viewModel.isSuspected) View.VISIBLE else View.GONE
                }

            } else if(viewModel.isnotConfirmed) {
                viewModel.isBeneficaryStatusDeath.observe(viewLifecycleOwner){ it
                    binding.fabEdit.visibility = if(notIt && !it && viewModel.isnotConfirmed) View.VISIBLE else View.GONE
                }
            } else {

                binding.fabEdit.visibility = View.GONE
            }
            binding.btnSubmit.visibility = if (notIt) View.GONE else View.VISIBLE
            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                        setFormAsDirty()
                    }, isEnabled = !recordExists
                )
                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty()) {
                            adapter.notifyItemChanged(viewModel.getIndexOfDate())
                            adapter.submitList(it)
                        }

                    }
                }
            }
        }

        val visitAdapter = VisitsListAdapter()
        binding.visitsRV.adapter = visitAdapter



        binding.llPatientInformation3.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.allVisitsList.collect {
                visitAdapter.submitList(it)
            }
        }
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.visitNo.observe(viewLifecycleOwner) {
            binding.number.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            submitMalariaScreeningForm()
        }
        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                MalariaFormViewModel.State.SAVE_SUCCESS -> {
                    setFormAsClean()
                    if (viewModel.isDeath) {
                        setMessage(R.string.ben_marked_death)

                    } else {
                        setMessage(R.string.malaria_submitted)

                    }

                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }
                MalariaFormViewModel.State.DRAFT_SAVED -> {
                    setFormAsClean()
                    Toast.makeText(
                        context,
                        "Draft saved successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }
    }

    private fun setMessage(message: Int) {
        Toast.makeText(
            requireContext(),
            resources.getString(message),Toast.LENGTH_SHORT
        ).show()
    }


    private fun submitMalariaScreeningForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
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
                R.drawable.ic__ncd,
                getString(R.string.maleria_screening_form)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                7,8,9,10,11,12,13,14,15,17,19,20 -> {
                    notifyDataSetChanged()

                }

            }
        }
    }

    override fun saveDraft() {
        viewModel.saveDraft()
    }

}
