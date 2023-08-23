package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class EligibleCoupleRegFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!


    private val viewModel: EligibleCoupleRegViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())

                            adapter.submitList(it)

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
            submitEligibleCoupleForm()
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EligibleCoupleRegViewModel.State.SAVE_SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                }

                else -> {}
            }
        }
    }

    private fun submitEligibleCoupleForm() {
        if (validate()) {
            viewModel.saveForm()
        }
    }

    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1)
            true
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
                17 -> {
                    notifyItemChanged(viewModel.getIndexOfAge1())
                    notifyItemChanged(viewModel.getIndexOfGap1())
                }

                22 -> {
                    notifyItemChanged(viewModel.getIndexOfAge2())
                    notifyItemChanged(viewModel.getIndexOfGap2())
                }

                27 -> {
                    notifyItemChanged(viewModel.getIndexOfAge3())
                    notifyItemChanged(viewModel.getIndexOfGap3())
                }

                32 -> {
                    notifyItemChanged(viewModel.getIndexOfAge4())
                    notifyItemChanged(viewModel.getIndexOfGap4())
                }

                37 -> {
                    notifyItemChanged(viewModel.getIndexOfAge5())
                    notifyItemChanged(viewModel.getIndexOfGap5())
                }

                42 -> {
                    notifyItemChanged(viewModel.getIndexOfAge6())
                    notifyItemChanged(viewModel.getIndexOfGap6())
                }

                47 -> {
                    notifyItemChanged(viewModel.getIndexOfAge7())
                    notifyItemChanged(viewModel.getIndexOfGap7())
                }

                52 -> {
                    notifyItemChanged(viewModel.getIndexOfAge8())
                    notifyItemChanged(viewModel.getIndexOfGap8())
                }

                57 -> {
                    notifyItemChanged(viewModel.getIndexOfAge9())
                    notifyItemChanged(viewModel.getIndexOfGap9())
                }

                12 -> {
                    notifyItemChanged(viewModel.getIndexOfLiveChildren())
                    notifyItemChanged(viewModel.getIndexOfMaleChildren())
                    notifyItemChanged(viewModel.getIndexOfFeMaleChildren())
                }

                19, 24, 29, 34, 39, 44, 49, 54, 59 -> {
                    notifyItemChanged(viewModel.getIndexOfMaleChildren())
                    notifyItemChanged(viewModel.getIndexOfFeMaleChildren())
                }

                13 -> {
                    notifyItemChanged(viewModel.getIndexOfChildren())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}