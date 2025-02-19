package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.impl.utils.INITIAL_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.ChildImmunizationVaccineAdapter
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.ImmunizationCategoryAdapter
import org.piramalswasthya.sakhi.adapters.ImmunizationVaccineAdapter
import org.piramalswasthya.sakhi.databinding.FragmentImmunizationFormBinding
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.VaccineCategoryDomain
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form.ImmunizationFormViewModel.State
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list.ChildImmunizationListFragmentDirections
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class ImmunizationFormFragment : Fragment() {
    //  private var _binding: FragmentNewFormBinding? = null

//    private val binding: FragmentNewFormBinding
//        get() = _binding!!

    private var _binding: FragmentImmunizationFormBinding? = null
    private val binding: FragmentImmunizationFormBinding
        get() = _binding!!


    private val viewModel: ImmunizationFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        _binding = FragmentImmunizationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }

        binding.llPatientInformation2.visibility = View.VISIBLE
        //     binding.llBenPwrTrackingDetails2.visibility = View.GONE
        binding.llBenPwrTrackingDetails3.visibility = View.GONE
        viewModel.benRegCache.observe(viewLifecycleOwner) {
            binding.tvTitleHusband.text = resources.getString(R.string.mother_s_name)
            binding.husbandName.text = it.motherName

            binding.tvTitleMobileNumber.text = resources.getString(R.string.father_s_name)
            binding.mobileNumber.text = it.fatherName

            binding.tvTitleBeneficiaryId.text = resources.getString(R.string.date_of_birth)
            binding.benId.text = getDateFromLong(it.dob)

        }
        binding.btnSubmit.setOnClickListener {
            submitImmForm()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                }

                State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.something_wend_wong_contact_testing),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }

//        binding.fabEdit.setOnClickListener {
//            viewModel.updateRecordExists(false)
//        }


        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                //    binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    isEnabled = !recordExists
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

        binding.rvImmCat.adapter =
            ChildImmunizationVaccineAdapter(ChildImmunizationVaccineAdapter.ImmunizationClickListener { item ->
                if (item.isSwitchChecked) {
                    viewModel.vaccinationDoneList.add(item)
                } else {
                    viewModel.vaccinationDoneList.removeIf { it ->
                        it.vaccineName == item.vaccineName
                    }
                }
            })

        lifecycleScope.launch {
            viewModel.bottomSheetContent.collect {
                it?.let {
                    submitListToVaccinationRv(it)
                }
            }
        }
    }

    lateinit var list: List<VaccineDomain>
    private fun submitListToVaccinationRv(detail: ImmunizationDetailsDomain) {
        list =
            detail.vaccineStateList.filter { it.vaccineCategory.name == viewModel.vaccineCategory }
        (_binding?.rvImmCat?.adapter as ChildImmunizationVaccineAdapter?)?.submitList(list)
    }


    private fun submitImmForm() {
        if (validateCurrentPage()) {
            //viewModel.saveForm()
            viewModel.saveImmunization()

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
                R.drawable.ic__immunization,
                getString(R.string.immunization)
            )
        }
    }

    fun getDateFromLong(dateLong: Long): String? {
        if (dateLong == 0L) return null
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateLong
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return f.format(cal.time)
    }

}