package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.track

import android.os.Bundle
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
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.BaseFormFragment
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class HRPNonPregnantTrackFragment : BaseFormFragment() {

    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: HRPNonPregnantTrackViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) { recordExists ->
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
                    if (it.isNotEmpty())
                        adapter.submitList(it)
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
            submitNonPregTrackingForm()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                HRPNonPregnantTrackViewModel.State.SAVE_SUCCESS -> {
                    setFormAsClean()
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                    viewModel.resetState()
                }

                HRPNonPregnantTrackViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.something_wend_wong_contact_testing),
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {}
            }
        }

    }

    private fun submitNonPregTrackingForm() {
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
                1 -> notifyItemChanged(viewModel.getIndexOfLmp())
                2 -> {
                    notifyItemChanged(viewModel.getIndexOfAnemia())
                    notifyItemChanged(viewModel.getIndexOfRisk())
                }

                4, 5, 6 -> {
                    notifyItemChanged(viewModel.getIndexOfAncLabel())
                    notifyItemChanged(viewModel.getIndexOfRisk())
                }

                17 -> {
                    notifyItemChanged(viewModel.getIndexOfRbg())
                    notifyItemChanged(viewModel.getIndexOfFbg())
                    notifyItemChanged(viewModel.getIndexOfPpbg())
                }

                22 -> {
                    notifyItemChanged(viewModel.getIndexOfIfaQuantity())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__follow_up_high_risk_non_preg,
                getString(R.string.follow_up_non_pregnant)
            )
        }
    }

    override fun saveDraft() {
        viewModel.saveForm()
    }
}
