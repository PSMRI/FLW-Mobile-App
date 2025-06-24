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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
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
                Toast.makeText(context, "Tracking is done", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
                viewModel.resetState()
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                HRPPregnantTrackViewModel.State.SAVE_SUCCESS -> {
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
                3 -> notifyItemChanged(viewModel.getIndexOfPregInduced())
                4 -> notifyItemChanged(viewModel.getIndexOfGest())
                5 -> notifyItemChanged(viewModel.getIndexOfHypothyroidism())
                6 -> notifyItemChanged(viewModel.getIndexOfPolyhydromnios())
                7 -> notifyItemChanged(viewModel.getIndexOfOligohydromnios())
                8 -> notifyItemChanged(viewModel.getIndexOfAntepartum())
                9 -> notifyItemChanged(viewModel.getIndexOfMalPre())
                10 -> notifyItemChanged(viewModel.getIndexOfHiv())
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


}