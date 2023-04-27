package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.hbnc_form.part_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.hbnc_form.part_2.HbncPartIIViewModel.State
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class HbncPartIIFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!
    private val viewModel: HbncPartIIViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            if (validate()) viewModel.submitForm()
        }
        viewModel.exists.observe(viewLifecycleOwner) { exists ->
            val adapter = FormInputAdapter(
                imageClickListener = null,
                formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                    viewModel.updateListOnValueChanged(formId, index)
                },
                isEnabled = !exists
            )
            binding.form.rvInputForm.adapter = adapter
            if (exists) {
                binding.btnSubmit.visibility = View.GONE
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formList.flowWithLifecycle(
                    viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED
                ).collect { list ->
                    Timber.d("Collecting formList : ${list.map { it.id }}")
                    (binding.form.rvInputForm.adapter as FormInputAdapter?)?.submitList(list)
                }
            }
        }


        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                State.LOADING -> {
                    binding.form.rvInputForm.visibility = View.GONE
                    binding.btnSubmit.visibility = View.GONE
                    binding.cvPatientInformation.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }
                State.SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerD2dSyncWorker(requireContext())
                }
                State.FAIL -> {
                    binding.form.rvInputForm.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        context, "Saving hbnc part II to database Failed!", Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    binding.form.rvInputForm.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }

            }
        }
    }

    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}