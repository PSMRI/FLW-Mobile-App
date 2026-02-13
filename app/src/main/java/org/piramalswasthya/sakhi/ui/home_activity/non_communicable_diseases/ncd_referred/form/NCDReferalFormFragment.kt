package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form

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
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class NCDReferalFormFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: NcdRefferalFormViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.benId.text = viewModel.benId.toString()
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    }, isEnabled = !recordExists

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
        binding.btnSubmit.setOnClickListener {
            if (validateCurrentPage()) {
                viewModel.saveForm()
            }
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                NcdRefferalFormViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.submit), Toast.LENGTH_SHORT
                    ).show()

                    val referral = viewModel.referalCache
                    val json = Gson().toJson(referral)
                    findNavController()
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("REFERRAL_RESULT", json)
                    findNavController()
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("REFERRAL_DONE", viewModel.referraltype)
                    findNavController().navigateUp()
                }

                else -> {}
            }
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ncd_list,
                getString(R.string.ncd_refer_form)
            )
        }
    }



}