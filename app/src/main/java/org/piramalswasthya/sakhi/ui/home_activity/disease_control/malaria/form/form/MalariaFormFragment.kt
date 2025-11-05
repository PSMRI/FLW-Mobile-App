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
import org.piramalswasthya.sakhi.databinding.FragmentMaleriaFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class MalariaFormFragment : Fragment() {
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

            } else {

                binding.fabEdit.visibility = View.GONE
            }
            binding.btnSubmit.visibility = if (notIt) View.GONE else View.VISIBLE
            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
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
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
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
                    if (viewModel.isDeath) {
                        setMessage(R.string.ben_marked_death)

                    } else {
                        setMessage(R.string.malaria_submitted)

                    }

                    WorkerUtils.triggerAmritPushWorker(requireContext())
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

}