package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.form

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class TBScreeningFormFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: TBScreeningFormViewModel by viewModels()

    var referralForReason = "Suspected TB case"
    var referType = "TB"

    private val tbSuspectedAlert by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.tb_screening))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private val referToHwcFacilityAlert by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.tb_screening))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.yes)) {dialog, _ ->
             findNavController().navigate(TBScreeningFormFragmentDirections.actionTBScreeningFormFragmentToNcdReferForm(viewModel.benId, referral = referralForReason, referralType = referType))
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private val tbSuspectedFamilyAlert by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.tb_screening))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
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
            submitTBScreeningForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                TBScreeningFormViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.tb_screening_submitted), Toast.LENGTH_SHORT
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("REFERRAL_DONE")
            ?.observe(viewLifecycleOwner) { typeName ->

                val type = TBScreeningFormViewModel.ReferralType.valueOf(typeName)
                viewModel.markReferralCompleted(type)
                viewModel.saveForm()
                findNavController().currentBackStackEntry?.savedStateHandle?.set("REFERRAL_DONE",null)
            }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("REFERRAL_RESULT")
            ?.observe(viewLifecycleOwner) { json ->
                val referral = Gson().fromJson(json, ReferalCache::class.java)
                viewModel.addReferral(referral)
                findNavController().currentBackStackEntry?.savedStateHandle?.set("REFERRAL_RESULT",null)

            }
    }

    private fun submitTBScreeningForm() {
        if (validateCurrentPage()) {
            viewModel.getAlerts()
            if (viewModel.referToHwcFacility.isNullOrBlank()){
                viewModel.saveForm()
            }else{
                showAlerts()
            }
        }
    }

    private fun showAlerts() {
        viewModel.getAlerts()
        viewModel.suspectedTB?.let {
            tbSuspectedAlert.setMessage(it)
            tbSuspectedAlert.show()
        }

        viewModel.suspectedTBFamily?.let {
            tbSuspectedFamilyAlert.setMessage(it)
            tbSuspectedFamilyAlert.show()
        }
        viewModel.referToHwcFacility?.let {
            referToHwcFacilityAlert.setMessage(it)
            referToHwcFacilityAlert.show()
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
                getString(R.string.tb_screening_form)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}