package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_confirmed.from

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
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.TBFollowUpDatesAdapter
import org.piramalswasthya.sakhi.databinding.FragmentLeprosyFromBinding
import org.piramalswasthya.sakhi.ui.common.attachAdapterUnsavedGuard
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.WorkerUtils

@AndroidEntryPoint
class TBConfirmedFormFragment : Fragment() {


    private var _binding: FragmentLeprosyFromBinding? = null
    private val binding: FragmentLeprosyFromBinding
        get() = _binding!!

    private val viewModel: TBConfirmedViewModel by viewModels()
    private lateinit var followUpAdapter: TBFollowUpDatesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeprosyFromBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        if (formId == 1|| formId ==2 || formId==4 || formId ==8 || formId==10) {
                            (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
                        }
                    }

                )
                binding.btnSubmit.isEnabled = true
                binding.form.rvInputForm.adapter = adapter
                attachAdapterUnsavedGuard(adapter)
                lifecycleScope.launch {
                    viewModel.formList.collect { list ->
                        if (list.isNotEmpty()) adapter.submitList(list)
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
            submitTBSuspectedForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                TBConfirmedViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.tb_tracking_submitted), Toast.LENGTH_SHORT
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }
    }


    private fun submitTBSuspectedForm() {
        viewModel.saveForm()


    }

    private fun setupFollowUpRecyclerView() {
        followUpAdapter = TBFollowUpDatesAdapter()
        binding.rvFollowUpDates.apply {
            adapter = followUpAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun showAlerts() {
       /* val alert = viewModel.getAlerts()
        alert?.let {
            tbSuspectedAlert.setMessage(it)
            tbSuspectedAlert.show()
        }*/
    }



    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ncd,
                getString(R.string.tb_confirmed_form)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}