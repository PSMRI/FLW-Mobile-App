package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.track

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
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.register.BenRegisterCHOFragmentDirections
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class HRPPregnantTrackFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: HRPPregnantTrackViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) {
            val adapter = FormInputAdapter(
                formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                    viewModel.updateListOnValueChanged(formId, index)
                    hardCodedListUpdate(formId)
                }, isEnabled = !it
            )
            binding.btnSubmit.isEnabled = !it
            binding.form.rvInputForm.adapter = adapter
            lifecycleScope.launch {
                viewModel.formList.collect { list ->
                    if (list.isNotEmpty())
                        adapter.submitList(list)

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
            submitTrackingForm()
        }

        viewModel.trackingDone.observe(viewLifecycleOwner) {
            trackingDone -> if (trackingDone) {
            Toast.makeText(context, "Tracking is done", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            viewModel.resetState()
        }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                HRPPregnantTrackViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(context, "Save Successful!!!", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                    viewModel.resetState()
                }

                HRPPregnantTrackViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(
                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                }
                else -> {}
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }
    private fun submitTrackingForm() {
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


    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> notifyItemChanged(1)
                2 -> notifyItemChanged(2)
                3 -> notifyItemChanged(3)
                4 -> notifyItemChanged(4)
                5 -> notifyItemChanged(5)
                6 -> notifyItemChanged(6)
                7 -> notifyItemChanged(7)
                8 -> notifyItemChanged(8)
                9 -> notifyItemChanged(9)
                10 -> notifyItemChanged(10)
            }
        }
    }

}