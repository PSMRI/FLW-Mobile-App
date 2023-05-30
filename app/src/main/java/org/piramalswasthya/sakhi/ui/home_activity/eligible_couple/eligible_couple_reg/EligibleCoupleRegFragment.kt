package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.eligible_couple_reg

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import org.piramalswasthya.sakhi.adapters.FormInputAdapterOld
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.death_reports.mdsr.MdsrObjectViewModel
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class EligibleCoupleRegFragment : Fragment() {

    private var _binding : FragmentNewFormBinding? = null
    private val binding : FragmentNewFormBinding
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

//        viewModel.benName.observe(viewLifecycleOwner) {
//            binding.tvBenName.text = it
//        }
//        viewModel.benAgeGender.observe(viewLifecycleOwner) {
//            binding.tvAgeGender.text = it
//        }
//        binding.btnSubmit.setOnClickListener{
//            if(validate()) viewModel.submitForm()
//        }
//        viewModel.exists.observe(viewLifecycleOwner) { exists ->
//            val adapter = FormInputAdapterOld(isEnabled = !exists)
//            binding.form.rvInputForm.adapter = adapter
//            if (exists) {
//                binding.btnSubmit.visibility = View.GONE
////                binding.mdsrForm.inputForm.rvInputForm.apply {
////                    isClickable = false
////                    isFocusable = false
////                }
//                viewModel.setExistingValues()
//            } else {
//
//                viewModel.address.observe(viewLifecycleOwner) {
//                    viewModel.setAddress(it, adapter)
//                }
//            }
//            lifecycleScope.launch{
//                adapter.submitList(viewModel.getFirstPage(adapter))
//            }
//        }
//        viewModel.state.observe(viewLifecycleOwner) {
//            when(it) {
//                MdsrObjectViewModel.State.LOADING -> {
//                    binding.cvPatientInformation.visibility = View.GONE
//                    binding.form.rvInputForm.visibility = View.GONE
//                    binding.btnSubmit.visibility = View.GONE
//                    binding.pbForm.visibility = View.VISIBLE
//                }
//                MdsrObjectViewModel.State.SUCCESS -> {
//                    findNavController().navigateUp()
//                    WorkerUtils.triggerD2dSyncWorker(requireContext())
//                }
//                MdsrObjectViewModel.State.FAIL -> {
//                    binding.cvPatientInformation.visibility = View.VISIBLE
//                    binding.form.rvInputForm.visibility = View.VISIBLE
//                    binding.btnSubmit.visibility = View.VISIBLE
//                    binding.pbForm.visibility = View.GONE
//                    Toast.makeText(context, "Saving Mdsr to database Failed!", Toast.LENGTH_LONG).show()
//                }
//                else -> {
//                    binding.cvPatientInformation.visibility = View.VISIBLE
//                    binding.form.rvInputForm.visibility = View.VISIBLE
//                    binding.btnSubmit.visibility = View.VISIBLE
//                    binding.pbForm.visibility = View.GONE
//                }
//            }
//        }
    }



    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapterOld).validateInput()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}