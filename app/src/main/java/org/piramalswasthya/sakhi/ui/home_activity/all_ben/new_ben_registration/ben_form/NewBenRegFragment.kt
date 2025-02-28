package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.AlertConsentBinding
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.NewBenRegViewModel.State
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class NewBenRegFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: NewBenRegViewModel by viewModels()

    private var micClickedElementId: Int = -1
    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val formattedValue = value/*.substring(0,50)*/.uppercase()
        val listIndex =
            viewModel.updateValueByIdAndReturnListIndex(micClickedElementId, formattedValue)
        listIndex.takeIf { it >= 0 }?.let {
            binding.form.rvInputForm.adapter?.notifyItemChanged(it)
        }
    }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) {
                requestLocationPermission()
            } else findNavController().navigateUp()
        }


    private var latestTmpUri: Uri? = null

    var isValidOtp = false

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    viewModel.setImageUriToFormElement(uri)

                    binding.form.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapter
                        adapter.notifyItemChanged(0)
                    }
                    Timber.d("Image saved at @ $uri")
                }
            }
        }


    private fun showAddSpouseAlert() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext()).setCancelable(false)

        // Setting Dialog Title
        alertDialog.setTitle("Add Spouse")

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to add ${viewModel.getBenName()}'s ${if (viewModel.getBenGender() == Gender.MALE) "Wife" else "Husband"}?")

        // On pressing Settings button
        alertDialog.setPositiveButton(
            "Ok"
        ) { dialog, _ ->
            val spouseGender = if (viewModel.getBenGender() == Gender.FEMALE) 1 else 2
            findNavController().navigate(
                NewBenRegFragmentDirections.actionNewBenRegFragmentSelf(
                    hhId = viewModel.hhId,
                    gender = spouseGender,
                    relToHeadId = if (spouseGender == 1) 5 else 4
                )
            )
            dialog.dismiss()
        }

        // on pressing cancel button
        alertDialog.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialog, _ ->
            try {
                findNavController().navigateUp()
            } catch (e:Exception){
                dialog.cancel()
            }
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun showSettingsAlert() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext())

        // Setting Dialog Title
        alertDialog.setTitle(resources.getString(R.string.enable_gps))

        // Setting Dialog Message
        alertDialog.setMessage(resources.getString(R.string.gps_is_not_enabled_do_you_want_to_go_to_settings_menu))

        // On pressing Settings button
        alertDialog.setPositiveButton(
            resources.getString(R.string.settings)
        ) { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        // on pressing cancel button
        alertDialog.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialog, _ ->
            try {
                findNavController().navigateUp()
            } catch (e:Exception) {
                dialog.cancel()
            }

            dialog.cancel()
        }
        alertDialog.show()
    }

    private val consentAlert by lazy {
        val alertBinding = AlertConsentBinding.inflate(layoutInflater, binding.root, false)
        alertBinding.textView4.text = resources.getString(R.string.consent_alert_title)
        alertBinding.checkBox.text = resources.getString(R.string.consent_text)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(false)
            .create()
        alertBinding.btnNegative.setOnClickListener {
            alertDialog.dismiss()
            try {
                findNavController().navigateUp()
            }catch (e:Exception){
                alertDialog.dismiss()
            }

        }
        alertBinding.btnPositive.setOnClickListener {
            if (alertBinding.checkBox.isChecked) {
                viewModel.setConsentAgreed()
                //requestLocationPermission()
                alertDialog.dismiss()
            } else
                Toast.makeText(
                    context,
                    resources.getString(R.string.please_tick_the_checkbox),
                    Toast.LENGTH_SHORT
                ).show()
        }
        alertDialog
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvPatientInformation.visibility = View.GONE
        binding.btnSubmit.setOnClickListener {
            submitBenForm()
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter =
                    FormInputAdapter(imageClickListener = FormInputAdapter.ImageClickListener {
                        viewModel.setCurrentImageFormId(it)
                        takeImage()
                    }, formValueListener = FormInputAdapter.FormValueListener { formId, index ->
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

                    }, sendOtpClickListener = FormInputAdapter.SendOtpClickListener{formId, button, timerInsec, tilEditText, isEnabled, position, otpField ->
                       var tempContactNo = ""
                        lifecycleScope.launch {
                            viewModel.formList.collect {
                              tempContactNo = it[viewModel.getIndexofTempraryNumber()].value.toString()
                            }
                        }
                        if (button.text == "Resend OTP") {
                            viewModel.resendOtp(tempContactNo)
                        } else {
                            viewModel.sentOtp(tempContactNo)
                        }

                        button.isEnabled = !isEnabled
                        startTimer(timerInsec,button,position)
                        tilEditText.visibility = View.VISIBLE
                        otpField.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            }

                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            }

                            override fun afterTextChanged(s: Editable?) {
                                isValidOtp = (s != null) && (s.length == 4)
                                if (isValidOtp)
                                    viewModel.validateOtp(tempContactNo,s.toString().toInt(),requireActivity(),otpField,button)


                            }

                        })
                    },
                        isEnabled = !recordExists
                    )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        Timber.d("Collecting $it")
                        if (it.isNotEmpty())
                            adapter.submitList(it)
                    }
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.setUpPage()
                    }
                }
            }
        }


        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                }

                State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    if (viewModel.isHoFMarried()) {
                        showAddSpouseAlert()
                    } else {
                        findNavController().navigateUp()
                    }
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.something_wend_wong_contact_testing),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }

        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1008 -> {
                    notifyDataSetChanged()

                }

                8 -> {
                    notifyItemChanged(viewModel.getIndexOfAgeAtMarriage())
                    notifyItemChanged(5)
                    notifyItemChanged(6)
                }

                7 -> {
                    notifyItemChanged(4)
                    notifyItemChanged(viewModel.getIndexOfAgeAtMarriage())
                }

                5 -> {

                    notifyItemChanged(4)
                    notifyItemChanged(5)

                }

                9 -> {
                    viewModel.getIndexOfMaritalStatus().takeIf { it != -1 }?.let {
                        notifyItemChanged(it)
                    }
                }

                12 -> notifyDataSetChanged()
//notifyItemChanged(viewModel.getIndexOfContactNumber())
            }
        }
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePicture.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile(Konstants.tempBenImagePrefix, null, requireActivity().cacheDir)
                .apply {
                    createNewFile()
//                deleteOnExit()
                }
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun submitBenForm() {
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
    //    requestLocationPermission()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ben,
                getString(if (viewModel.isHoF) R.string.title_new_ben_reg_hof else R.string.title_new_ben_reg_non_hof)
            )
        }

        viewModel.recordExists.observe(viewLifecycleOwner) {
            if (!it && !viewModel.getIsConsentAgreed()) consentAlert.show()
        }
//        binding.vp2Nhhr.registerOnPageChangeCallback(pageChangeCallback)

    }

    private fun requestLocationPermission() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        else if (!isGPSEnabled) showSettingsAlert()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private lateinit var countDownTimer : CountDownTimer
    private var countdownTimers : HashMap<Int, CountDownTimer> = HashMap()

    private fun formatTimeInSeconds(millis: Long) : String {
        val seconds = millis / 1000
        return "${seconds} sec"
    }
    private fun startTimer(timerInSec: TextView, generateOtp: MaterialButton,position:Int) {
        countDownTimer =  object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerInSec.visibility = View.VISIBLE
                timerInSec.text = formatTimeInSeconds(millisUntilFinished)
            }
            override fun onFinish() {
                timerInSec.visibility = View.INVISIBLE
                timerInSec.text = ""
                generateOtp.isEnabled = true
                generateOtp.text = timerInSec.resources.getString(R.string.resend_otp)
            }
        }.start()

        countdownTimers[position] = countDownTimer

    }
}