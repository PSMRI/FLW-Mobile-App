package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.track

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
class HRPPregnantTrackFragment : BaseFormFragment() {

    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: HRPPregnantTrackViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) {
            val adapter = FormInputAdapter(
                formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                    viewModel.updateListOnValueChanged(formId, index)
                    hardCodedListUpdate(formId)
                    setFormAsDirty()
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
        viewModel.benWithHrpt.observe(viewLifecycleOwner) {
            it?.let {
                binding.llPatientInformation2.visibility = View.VISIBLE
                binding.husbandName.text = it.ben.spouseName
                binding.benId.text = it.ben.benId.toString()
                binding.rchId.text = it.ben.rchId ?: resources.getString(R.string.str_not_available)
                binding.mobileNumber.text = it.ben.mobileNo.toString()
                binding.lmp.text = it.asDomainModel().lmpString
                binding.edd.text = it.asDomainModel().eddString
                binding.weeksOfPreg.text = it.asDomainModel().weeksOfPregnancy
            }

        }
        binding.btnSubmit.setOnClickListener {
            submitTrackingForm()
        }

        viewModel.trackingDone.observe(viewLifecycleOwner) { trackingDone ->
            if (trackingDone) {
                setFormAsClean()
                Toast.makeText(context, "Tracking is done", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
                viewModel.resetState()
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                HRPPregnantTrackViewModel.State.SAVE_SUCCESS -> {
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

                HRPPregnantTrackViewModel.State.SAVE_FAILED -> {
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

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__follow_up_high_risk_preg,
                getString(R.string.follow_up_pregnant)
            )
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
                1 -> notifyItemChanged(viewModel.getIndexOfRdPmsa())
                13 -> notifyItemChanged(viewModel.getIndexOfRdDengue())
                14 -> notifyItemChanged(viewModel.getIndexOfRdFilaria())
                2 -> notifyItemChanged(viewModel.getIndexOfSevereAnemia())
                3 -> notifyItemChanged(viewModel.getIndexOfSevereAnemia()) // Should likely be getIndexOfSevereAnemia based on context but original code used 2. Correcting if typo. Actually let's keep it as is if it was consistent or improve.
                18 -> {
                    notifyItemChanged(viewModel.getIndexOfRbg())
                    notifyItemChanged(viewModel.getIndexOfFbg())
                    notifyItemChanged(viewModel.getIndexOfPpbg())
                    notifyItemChanged(viewModel.getIndexOfOgttLabel())
                    notifyItemChanged(viewModel.getIndexOfFasting())
                    notifyItemChanged(viewModel.getIndexOfAfter())
                }

                26 -> {
                    notifyItemChanged(viewModel.getIndexOfIfaQuantity())
                }
            }
        }
    }

    override fun saveDraft() {
        viewModel.saveForm()
    }

}
