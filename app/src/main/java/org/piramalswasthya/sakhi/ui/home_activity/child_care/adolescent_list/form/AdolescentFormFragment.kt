package org.piramalswasthya.sakhi.ui.home_activity.adolescent_health

import android.os.Bundle
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
import org.piramalswasthya.sakhi.databinding.FragmentAdolescentHealthBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.form.AdolescentHealthFormViewModel
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class AdolescentHealthFormFragment : Fragment() {

    private var _binding: FragmentAdolescentHealthBinding? = null
    private val binding: FragmentAdolescentHealthBinding
        get() = _binding!!

    private val viewModel: AdolescentHealthFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdolescentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    }, isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty()) {
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
            submitAdolescentHealthForm()
        }

        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AdolescentHealthFormViewModel.State.SAVING -> {
                    binding.btnSubmit.isEnabled = false
                    Toast.makeText(context, "Saving...", Toast.LENGTH_SHORT).show()
                }
                AdolescentHealthFormViewModel.State.SAVE_SUCCESS -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().popBackStack()
                }
                AdolescentHealthFormViewModel.State.SAVE_FAILED -> {
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(context, "Save Failed!", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun submitAdolescentHealthForm() {
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
                R.drawable.ic__adolescent, // Add a relevant icon
                "Adolescent Health Form"
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}