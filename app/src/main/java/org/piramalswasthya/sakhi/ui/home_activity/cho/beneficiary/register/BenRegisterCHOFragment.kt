package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.register

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
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber


@AndroidEntryPoint
class BenRegisterCHOFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: BenRegisterCHOViewModel by viewModels()

    private var micClickedElementId: Int = -1

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val formattedValue = value/*.substring(0,50)*/.uppercase()
        val listIndex =
            viewModel.updateValueByIdAndReturnListIndex(micClickedElementId, formattedValue)
        listIndex.takeIf { it >= 0 }?.let {
            binding.form.rvInputForm.adapter?.notifyItemChanged(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                when (index) {
                    Konstants.micClickIndex -> {
                        micClickedElementId = formId
                        sttContract.launch(Unit)
                    }

                    else -> {
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }
                }
            }, isEnabled = true
        )
        binding.tvBenName.visibility = View.GONE
        binding.tvAgeGender.visibility = View.GONE
        binding.btnSubmit.isEnabled = true
        binding.form.rvInputForm.adapter = adapter
        lifecycleScope.launch {
            viewModel.formList.collect {
                if (it.isNotEmpty())
                    adapter.submitList(it)
            }
        }

        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = resources.getString(R.string.beneficiary_registration)
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = "2"
        }
        binding.btnSubmit.setOnClickListener {
            saveBeneficiary()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                BenRegisterCHOViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                    viewModel.resetState()
                }

                BenRegisterCHOViewModel.State.SAVE_FAILED -> {
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

    private fun saveBeneficiary() {
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
                3 -> notifyItemChanged(viewModel.getIndexOfAge())
                8 -> notifyItemChanged(viewModel.getIndexOfDob())
            }
        }
    }
}