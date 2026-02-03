package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.hbyc.form

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
import org.piramalswasthya.sakhi.databinding.FragmentHbycBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.BaseFormFragment
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class HbycFragment : BaseFormFragment() {

    private var _binding: FragmentHbycBinding? = null
    private val binding: FragmentHbycBinding
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
        viewModel.exists.observe(viewLifecycleOwner) { exists ->
            val adapter = FormInputAdapter(
                formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                    viewModel.updateListOnValueChanged(formId, index)
                    hardCodedListUpdate(formId)
                    setFormAsDirty()
                }, isEnabled = !exists
            )
            binding.btnHbycSubmit.isEnabled = !exists
            binding.hbycForm.rvInputForm.adapter = adapter
            lifecycleScope.launch {
                viewModel.formList.collect { list ->
                    if (list.isNotEmpty())
                        adapter.submitList(list)

                }
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
                    setFormAsClean()
                    findNavController().navigateUp()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
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
                HbycViewModel.State.DRAFT_SAVED -> {
                    setFormAsClean()
                    binding.hbycForm.rvInputForm.visibility = View.VISIBLE
                    binding.btnHbycSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbHbyc.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Draft saved successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
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

    private fun hardCodedListUpdate(formId: Int) {
        binding.hbycForm.rvInputForm.adapter?.apply {
            when (formId) {
                // if required
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__child,
                getString(R.string.hbyc)
            )
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun saveDraft() {
        viewModel.saveDraft()
    }
}