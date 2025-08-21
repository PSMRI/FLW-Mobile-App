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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapterWithBgIcon
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
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
                val adapter = FormInputAdapterWithBgIcon(
                    formValueListener = FormInputAdapterWithBgIcon.FormValueListener { formId, index ->
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
            (it as FormInputAdapterWithBgIcon).validateInput(resources)
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
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge1())
                    notifyItemChanged(viewModel.getIndexOfGap1())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                22 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge2())
                    notifyItemChanged(viewModel.getIndexOfGap2())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                27 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge3())
                    notifyItemChanged(viewModel.getIndexOfGap3())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                32 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge4())
                    notifyItemChanged(viewModel.getIndexOfGap4())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                37 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge5())
                    notifyItemChanged(viewModel.getIndexOfGap5())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                42 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge6())
                    notifyItemChanged(viewModel.getIndexOfGap6())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                47 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge7())
                    notifyItemChanged(viewModel.getIndexOfGap7())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                52 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge8())
                    notifyItemChanged(viewModel.getIndexOfGap8())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                57 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge9())
                    notifyItemChanged(viewModel.getIndexOfGap9())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
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

                61, 62 -> {
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                }

                63, 64 -> {
                    notifyItemChanged(viewModel.getIndexOfPhysicalObservationLabel())
                }

                65, 66, 67, 68 -> {
                    notifyItemChanged(viewModel.getIndexOfObstetricHistoryLabel())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__eligible_couple,
                getString(R.string.eligible_couple_registration)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}