package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.fpot

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
import org.piramalswasthya.sakhi.databinding.FragmentFpotBinding
import org.piramalswasthya.sakhi.work.WorkerUtils

@AndroidEntryPoint
class FpotFragment : Fragment() {

    private var _binding : FragmentFpotBinding? = null
    private val binding : FragmentFpotBinding
        get() = _binding!!

    private val viewModel: FpotViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFpotBinding.inflate(layoutInflater, container, false)
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
        binding.btnHbncSubmit.setOnClickListener {
            if (validate()) viewModel.submitForm()
        }
        viewModel.exists.observe(viewLifecycleOwner) {exists ->
            val adapter = FormInputAdapterOld(isEnabled = !exists)
            binding.hbncForm.rvInputForm.adapter = adapter
            if (exists) {
                binding.btnHbncSubmit.visibility = View.GONE
                viewModel.setExistingValues()
            }
            else {
                viewModel.address.observe(viewLifecycleOwner) {
                    viewModel.setAddress(it, adapter)
                }
            }
            lifecycleScope.launch {
                adapter.submitList(viewModel.getFirstPage())
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                FpotViewModel.State.LOADING -> {
                    binding.hbncForm.rvInputForm.visibility = View.GONE
                    binding.btnHbncSubmit.visibility = View.GONE
                    binding.cvPatientInformation.visibility = View.GONE
                    binding.pbHbnc.visibility = View.VISIBLE
                }
                FpotViewModel.State.SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerD2dSyncWorker(requireContext())
                }
                FpotViewModel.State.FAIL -> {
                    binding.hbncForm.rvInputForm.visibility = View.VISIBLE
                    binding.btnHbncSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbHbnc.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.saving_mdsr_to_database_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    binding.hbncForm.rvInputForm.visibility = View.VISIBLE
                    binding.btnHbncSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbHbnc.visibility = View.GONE
                }

            }
        }
    }

    fun validate(): Boolean {
//        val result = binding.pmjayForm.rvInputForm.adapter?.let {
//            (it as FormInputAdapter).validateInput()
//        }
//        Timber.d("Validation : $result")
//        return if (result == -1)
//            true
//        else {
//            if (result != null) {
//                binding.pmjayForm.rvInputForm.scrollToPosition(result)
//            }
//            false
//        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}