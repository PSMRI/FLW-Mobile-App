package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.aadhaar_num_asha

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.activity_contracts.RDServiceCapturePIDContract
import org.piramalswasthya.sakhi.activity_contracts.RDServiceInfoContract
import org.piramalswasthya.sakhi.activity_contracts.RDServiceInitContract
import org.piramalswasthya.sakhi.databinding.FragmentAadhaarNumberAshaBinding
import org.piramalswasthya.sakhi.helpers.AadhaarValidationUtils
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.network.AadhaarVerifyBioRequest
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern


@AndroidEntryPoint
class AadhaarNumberAshaFragment : Fragment() {

    private var isPasswordVisible:Boolean = false

    private var _binding: FragmentAadhaarNumberAshaBinding? = null
    private val binding: FragmentAadhaarNumberAshaBinding
        get() = _binding!!

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private val viewModel: AadhaarNumberAshaViewModel by viewModels()

    private val aadhaarDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.individual_s_consent_for_creation_of_abha_number))
            .setMessage(resources.getString(R.string.aadhar_disclaimer_consent_text))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private val rdServiceCapturePIDContract =
        registerForActivityResult(RDServiceCapturePIDContract()) {
            Toast.makeText(requireContext(), "pid captured $it", Toast.LENGTH_SHORT).show()
            binding.pid.text = Gson().toJson(
                AadhaarVerifyBioRequest(
                    binding.tietAadhaarNumber.text.toString(),
                    "FMR", it.toString()
                )
            )
            viewModel.verifyBio(binding.tietAadhaarNumber.text.toString(), it)
            binding.pid.text = Gson().toJson(viewModel.responseData)
        }

    private val rdServiceDeviceInfoContract = registerForActivityResult(RDServiceInfoContract()) {
        binding.pid.text = Gson().toJson(
            AadhaarVerifyBioRequest(
                binding.tietAadhaarNumber.toString(),
                "FMR", it.toString()
            )
        )
        viewModel.verifyBio(binding.tietAadhaarNumber.text.toString(), it)
    }
    private val rdServiceInitContract = registerForActivityResult(RDServiceInitContract()) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAadhaarNumberAshaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isValidAadhaar = false
        var isValidMobile = false

        parentViewModel.verificationType.observe(viewLifecycleOwner) {
            when (it) {
                "OTP" -> binding.btnVerifyAadhaar.text = resources.getString(R.string.generate_otp)
                "FP" -> {
                    checkApp()
                    binding.btnVerifyAadhaar.text = resources.getString(R.string.validate_fp)
                }
            }
        }

        val intent = requireActivity().intent

        val benId = intent.getLongExtra("benId", 0)
        val benRegId = intent.getLongExtra("benRegId", 0)

        if (benId > 0) {
            viewModel.getBen(benId)
        }
        binding.btnVerifyAadhaar.setOnClickListener {
            verifyAadhaar()
        }

        viewModel.ben.observe(viewLifecycleOwner) {
            it?.let {
                binding.benNameTitle.visibility = View.VISIBLE
                binding.benName.visibility = View.VISIBLE
                binding.benName.text =it
                   // String.format("%s%s%s", getString(R.string.generating_abha_for), "\n", it)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.SUCCESS) {
                viewModel.resetState()
            }
            parentViewModel.setAbhaMode(AadhaarIdViewModel.Abha.CREATE)
            parentViewModel.setState(it)
        }

        viewModel.mobileNumber.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setMobileNumber(it)
            }
        }

        viewModel.txnId.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setTxnId(it)
            }
        }
        viewModel.otpMobileNumberMessage.observe(viewLifecycleOwner) {
            it?.let {
                parentViewModel.setOTPMsg(it)
            }
        }

        binding.aadharConsentCheckBox.setOnCheckedChangeListener { _, ischecked ->
            binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile && ischecked
        }

        binding.aadharDisclaimer.setOnClickListener {
            aadhaarDisclaimer.show()
        }

        binding.tietAadhaarNumber.setTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if(s.toString().isNullOrBlank()){
                    binding.tvErrorText.visibility = View.GONE
                    binding.tvErrorText.text = ""
                    binding.ivValidAadhaar.setImageResource(R.drawable.ic_check_circle_grey)
                }else if(AadhaarValidationUtils.isValidAadhaar(s.toString())){
                    binding.tvErrorText.visibility = View.GONE
                    binding.tvErrorText.text = ""
                    binding.ivValidAadhaar.setImageResource(R.drawable.ic_check_circle_green)
                }else{
                    binding.tvErrorText.visibility = View.VISIBLE
                    binding.tvErrorText.text = getString(R.string.str_invalid_aadhaar_no)
                    binding.ivValidAadhaar.setImageResource(R.drawable.ic_check_circle_grey)
                }

                isValidAadhaar = (s != null) && AadhaarValidationUtils.isValidAadhaar(s.toString())
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && binding.aadharConsentCheckBox.isChecked
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        /*binding.tietAadhaarNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {

                if(s.toString().isNullOrBlank()){
                    binding.tvErrorText.visibility = View.GONE
                    binding.tvErrorText.text = ""
                }else if(AadhaarValidationUtils.isValidAadhaar(s.toString())){
                    binding.tvErrorText.visibility = View.GONE
                    binding.tvErrorText.text = ""
                }else{
                    binding.tvErrorText.visibility = View.VISIBLE
                    binding.tvErrorText.text = getString(R.string.str_invalid_aadhaar_no)
                }

                isValidAadhaar = (s != null) && AadhaarValidationUtils.isValidAadhaar(s.toString())
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && binding.aadharConsentCheckBox.isChecked
            }

        })*/

        binding.tietMobileNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if((s != null) && isValidMobileNumber(s.toString())){
                    binding.tvErrorTextMobile.visibility = View.GONE
                    binding.tvErrorTextMobile.text = ""
                    binding.ivValidMobile.setImageResource(R.drawable.ic_check_circle_green)

                }else{
                    binding.tvErrorTextMobile.visibility = View.VISIBLE
                    binding.tvErrorTextMobile.text = getString(R.string.str_invalid_mobile_no)
                    binding.ivValidMobile.setImageResource(R.drawable.ic_check_circle_grey)
                }
                isValidMobile = (s != null) && isValidMobileNumber(s.toString())
                if (isValidMobile)
                    parentViewModel.setMobileNumber(s.toString())
                binding.btnVerifyAadhaar.isEnabled = isValidAadhaar && isValidMobile
                        && binding.aadharConsentCheckBox.isChecked
            }

        })

        // observing error message from parent and updating error text field
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorText.visibility = View.VISIBLE
                binding.tvErrorText.text = it
                viewModel.resetErrorMessage()
            }
        }

        binding.ivShowText.setOnClickListener {
            if (isPasswordVisible) {
                binding.tietAadhaarNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                binding.ivShowText.setBackgroundResource(R.drawable.ic_show)
            }else{
                binding.tietAadhaarNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                binding.ivShowText.setBackgroundResource(R.drawable.ic_hide)
            }
            isPasswordVisible = !isPasswordVisible
            binding.tietAadhaarNumber.setSelection(binding.tietAadhaarNumber.text?.length!!)

        }
    }

    private fun verifyAadhaar() {
        Toast.makeText(requireContext(), parentViewModel.verificationType.value, Toast.LENGTH_SHORT)
            .show()
        parentViewModel.setAadhaarNumber(binding.tietAadhaarNumber.text.toString())
        when (parentViewModel.verificationType.value) {
            "OTP" -> viewModel.generateOtpClicked(binding.tietAadhaarNumber.text.toString())
            "FP" -> rdServiceCapturePIDContract.launch(Unit)
        }
    }

    fun isValidMobileNumber(str: String?): Boolean {
        val regex = "(\\+91|0)?[1-9][0-9]{9}"
        val p: Pattern = Pattern.compile(regex)
        if (str == null) {
            return false
        }
        val m: Matcher = p.matcher(str)
        return m.matches()
    }

    private fun checkApp() {

    }
}