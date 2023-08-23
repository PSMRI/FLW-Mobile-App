package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.hbyc.form

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
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.FormInputAdapterOld
import org.piramalswasthya.sakhi.databinding.FragmentHbycBinding
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class HbycFragment : Fragment() {

    private var _binding : FragmentHbycBinding? = null
    private val binding : FragmentHbycBinding
        get() = _binding!!

    private val viewModel: HbycViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHbycBinding.inflate(layoutInflater, container, false)
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
        binding.btnHbycSubmit.setOnClickListener {
            if (validate()) viewModel.submitForm()
        }
        viewModel.exists.observe(viewLifecycleOwner) {exists ->
            val adapter = FormInputAdapterOld(isEnabled = !exists)
            binding.hbycForm.rvInputForm.adapter = adapter
            if (exists) {
                binding.btnHbycSubmit.visibility = View.GONE
                viewModel.setExistingValues()
            }
            else {
                viewModel.address.observe(viewLifecycleOwner) {
                    viewModel.setAutoPopulatedValues(it, adapter)
                }
            }
            lifecycleScope.launch {
                adapter.submitList(viewModel.getFirstPage())
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                HbycViewModel.State.LOADING -> {
                    binding.hbycForm.rvInputForm.visibility = View.GONE
                    binding.btnHbycSubmit.visibility = View.GONE
                    binding.cvPatientInformation.visibility = View.GONE
                    binding.pbHbyc.visibility = View.VISIBLE
                }
                HbycViewModel.State.SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerD2dSyncWorker(requireContext())
                }
                HbycViewModel.State.FAIL -> {
                    binding.hbycForm.rvInputForm.visibility = View.VISIBLE
                    binding.btnHbycSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbHbyc.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.saving_mdsr_to_database_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    binding.hbycForm.rvInputForm.visibility = View.VISIBLE
                    binding.btnHbycSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbHbyc.visibility = View.GONE
                }

            }
        }
    }

    fun validate(): Boolean {
        val result = binding.hbycForm.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1)
            true
        else {
            if (result != null) {
                binding.hbycForm.rvInputForm.scrollToPosition(result)
            }
            false
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}