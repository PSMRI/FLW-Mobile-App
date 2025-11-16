package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.confirmed.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FollowUpDatesAdapter
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentLeprosyFromBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class LeprosyConfirmedFromFragment : Fragment() {
    private var _binding: FragmentLeprosyFromBinding? = null

    private val viewModel: LeprosyConfirmedFromViewModel by viewModels()

    private lateinit var followUpAdapter: FollowUpDatesAdapter


    private val binding: FragmentLeprosyFromBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLeprosyFromBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSubmit.visibility = View.VISIBLE

        setupFollowUpRecyclerView()


        viewModel.followUpDates.observe(viewLifecycleOwner) { followUps ->
            followUpAdapter.submitList(followUps)

            if (followUps.isNotEmpty()) {
                binding.rvFollowUpDates.visibility = View.VISIBLE
            } else {
                binding.rvFollowUpDates.visibility = View.GONE
            }
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->


            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)

                    }, isEnabled = true
                )
                binding.btnSubmit.isEnabled = true
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
            submitMalariaScreeningForm()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                LeprosyConfirmedFromViewModel.State.SAVE_SUCCESS -> {

                    Toast.makeText(requireContext(), "Follow-up saved successfully", Toast.LENGTH_SHORT).show()

                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                LeprosyConfirmedFromViewModel.State.VISIT_COMPLETED -> {
                    Toast.makeText(requireContext(), "Visit completed! Starting next visit screening.", Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }
                LeprosyConfirmedFromViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(requireContext(), "Failed to save follow-up", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupFollowUpRecyclerView() {
        followUpAdapter = FollowUpDatesAdapter()
        binding.rvFollowUpDates.apply {
            adapter = followUpAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }



    private fun submitLeprosyFollowUpForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun setMessage(message: Int) {
        Toast.makeText(
            requireContext(),
            resources.getString(message),Toast.LENGTH_SHORT
        ).show()
    }
    private fun submitMalariaScreeningForm() {
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
    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ncd,
                getString(R.string.leprosy_confirmed_form)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}