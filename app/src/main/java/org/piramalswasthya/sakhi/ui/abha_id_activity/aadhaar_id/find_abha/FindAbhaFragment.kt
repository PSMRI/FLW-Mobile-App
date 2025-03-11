package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.find_abha

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentFindAbhaBinding
import org.piramalswasthya.sakhi.network.Abha
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel


@AndroidEntryPoint
class FindAbhaFragment : Fragment() {

    private var _binding: FragmentFindAbhaBinding? = null
    private val binding: FragmentFindAbhaBinding
        get() = _binding!!

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private val viewModel: FindAbhaViewModel by viewModels()

    private lateinit var adapterType: ArrayAdapter<String>

    private var abhaList = mutableListOf<String>()
    private var abhaData = mutableListOf<Abha>()

    private var selectedAbhaIndex = 0

    private val aadhaarDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.individual_s_consent_for_creation_of_abha_number))
            .setMessage(resources.getString(R.string.aadhar_disclaimer_consent_text))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindAbhaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isValidMobile = false
        var isValidAbha = false

        val intent = requireActivity().intent

        val benId = intent.getLongExtra("benId", 0)
        val benRegId = intent.getLongExtra("benRegId", 0)

        if (benId > 0) {
            viewModel.getBen(benId)
        }

        binding.btnSearchAbha.setOnClickListener {
            binding.abhaDropdown.setText("")
            binding.abhaDropdown.setAdapter(null)
            abhaList.clear()
            abhaData.clear()
            selectedAbhaIndex = 0
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            searchAbha()
        }

        binding.btnGenerateOtp.setOnClickListener {
            viewModel.generateOtpClicked(selectedAbhaIndex.toString())
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                viewModel.resetState()
            }
//            parentViewModel.setState(it)
        }

        viewModel.fnlState.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                viewModel.resetState()
            }
            parentViewModel.setAbhaMode(AadhaarIdViewModel.Abha.SEARCH)
            parentViewModel.setState(it)
        }

        viewModel.fnlTxnId.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setTxnId(it)
            }
        }

        viewModel.abha.observe(viewLifecycleOwner) {
            it?.let {
                binding.tilSelectAbha.isEnabled = true
                abhaData.addAll(it)
                it.forEach { abha ->
                    abhaList.add(abha.name)
                }
                adapterType = ArrayAdapter<String>(
                    requireContext(),
                    R.layout.dropdown_item_abha,
                    abhaList
                )
                binding.abhaDropdown.setAdapter(adapterType)
            }
        }

        binding.aadharConsentCheckBox.setOnCheckedChangeListener { _, ischecked ->
            binding.btnGenerateOtp.isEnabled = isValidAbha && isValidMobile && ischecked
        }

        binding.aadharDisclaimer.setOnClickListener {
            aadhaarDisclaimer.show()
        }

        binding.tietMobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                isValidMobile = (s != null) && (s.length == 10)
                if (isValidMobile) {
                    parentViewModel.setMobileNumber(s.toString())
                    binding.btnSearchAbha.isEnabled = isValidMobile
//                    binding.btnSearchAbha.isEnabled = isValidAbha && isValidMobile
//                            && binding.aadharConsentCheckBox.isChecked
                }
            }

        })

        (binding.tilSelectAbha.getEditText() as AutoCompleteTextView).onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                selectedAbhaIndex = abhaData[position].index
                isValidAbha = true
            }

        // observing error message from parent and updating error text field
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorTextAbha.visibility = View.VISIBLE
                binding.tvErrorTextAbha.text = it
                viewModel.resetErrorMessage()
            }
        }
    }

    private fun searchAbha() {
        viewModel.searchAbhaClicked(binding.tietMobileNumber.text.toString())
    }

}