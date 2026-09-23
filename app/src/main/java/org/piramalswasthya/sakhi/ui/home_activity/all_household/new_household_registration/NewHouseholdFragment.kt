package org.piramalswasthya.sakhi.ui.home_activity.all_household.new_household_registration

import android.Manifest.permission
import android.app.AlertDialog
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.AlertConsentBinding
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_household.new_household_registration.NewHouseholdViewModel.State
import timber.log.Timber


private const val ABHA_ALREADY_EXISTS_CODE = 5001

@AndroidEntryPoint
class NewHouseholdFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null

    private val binding: FragmentNewFormBinding
        get() = _binding!!


    private val viewModel: NewHouseholdViewModel by viewModels()

    private val isMitaninFlavor = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)

    private val gateSubmitOnAbhaFetch: Boolean
        get() = isMitaninFlavor && viewModel.isNewRegistration

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) {
                requestLocationPermission()
            } else{
                if (isAdded) findNavController().navigateUp()
            }

        }

    private var micClickedElementId: Int = -1
    private var editMode: Boolean = false
    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val formattedValue = value/*.substring(0,50)*/.uppercase()
        val listIndex =
            viewModel.updateValueByIdAndReturnListIndex(micClickedElementId, formattedValue)
        listIndex.takeIf { it >= 0 }?.let {
            binding.form.rvInputForm.adapter?.notifyItemChanged(it)
        }
    }


    private fun showSettingsAlert() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        // Setting Dialog Title
        alertDialog.setTitle(resources.getString(R.string.enable_gps))

        // Setting Dialog Message
        alertDialog.setMessage(resources.getString(R.string.gps_is_not_enabled_do_you_want_to_go_to_settings_menu))

        // On pressing Settings button
        alertDialog.setPositiveButton(resources.getString(R.string.settings)) { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        // on pressing cancel button
        alertDialog.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialog, _ ->
            findNavController().navigateUp()
            dialog.cancel()
        }
        alertDialog.show()
    }

    private val consentAlert by lazy {
        val alertBinding = AlertConsentBinding.inflate(layoutInflater, binding.root, false)
        alertBinding.textView4.text = resources.getString(R.string.consent_alert_title)
        alertBinding.scrollableText.text =if (isMitaninFlavor){
            resources.getString(R.string.mitanin_consent_text)
        }else{
            resources.getString(R.string.consent_text)
        }
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(false)
            .create()
        alertBinding.scrollableText.setOnClickListener {
            alertBinding.checkBox.isChecked = !alertBinding.checkBox.isChecked
        }
        alertBinding.btnNegative.setOnClickListener {
            alertDialog.dismiss()
            findNavController().navigateUp()
        }
        alertBinding.btnPositive.setOnClickListener {
            if (alertBinding.checkBox.isChecked) {
                viewModel.setConsentAgreed()
                //   requestLocationPermission()
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
    private val nextScreenAlert by lazy {

        MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.add_head_of_family))
            .setCancelable(false)
//            .setMessage(str)
            .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->

                if (isAdded) {
                    findNavController().navigate(
                        NewHouseholdFragmentDirections.actionNewHouseholdFragmentToNewBenRegFragment(
                            viewModel.getHHId(),
                            18
                        )
                    )

                }

                dialog.dismiss()
            }.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                if (isAdded)  findNavController().navigateUp()
                dialog.dismiss()
            }.create()
    }


    private fun showCreateNewAbhaDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.abha_not_created_title))
            .setMessage(getString(R.string.abha_not_created_message))
            .setPositiveButton(getString(R.string.yes_dialog)) { dialog, _ ->
                findNavController().popBackStack(R.id.homeFragment, false)
                startActivity(Intent(requireActivity(), AbhaIdActivity::class.java))
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setSubmitEnabled(enabled: Boolean) {
        val b = _binding ?: return
        b.btnSubmit.isEnabled = enabled
        val tint = if (enabled) {
            com.google.android.material.color.MaterialColors.getColor(
                b.btnSubmit, com.google.android.material.R.attr.colorPrimary
            )
        } else {
            ContextCompat.getColor(requireContext(), R.color.md_theme_light_ongray)
        }
        b.btnSubmit.backgroundTintList = android.content.res.ColorStateList.valueOf(tint)
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvPatientInformation.visibility = View.GONE

        viewModel.abhaUserDetails.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val member = result.data.firstOrNull { it.hasUsableData() }
                    if (isMitaninFlavor && member != null) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.prefillFromAyushmanCard(member)
                            (_binding?.form?.rvInputForm?.adapter as? FormInputAdapter)
                                ?.notifyDataSetChanged()
                           // if (gateSubmitOnAbhaFetch) setSubmitEnabled(true)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.abha_no_valid_details),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    viewModel.clearAbhaUserDetails()
                }

                is NetworkResult.Error -> {
                    if (result.code == ABHA_ALREADY_EXISTS_CODE) {
                        showCreateNewAbhaDialog()
                    } else {
                        val errorMessage = result.message.takeIf { it.isNotBlank() }
                            ?: getString(R.string.abha_no_valid_details)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                    viewModel.clearAbhaUserDetails()
                }

                NetworkResult.NetworkError -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.clearAbhaUserDetails()
                }

                null -> Unit
            }
        }

        viewModel.readRecord.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (!recordExists) View.VISIBLE else View.GONE
               // if (gateSubmitOnAbhaFetch && !recordExists) setSubmitEnabled(false)
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        when (index) {
                            Konstants.micClickIndex -> {
                                micClickedElementId = formId
                                sttContract.launch(Unit)
                            }

                            else -> {
                                viewModel.updateListOnValueChanged(formId, index)
                            }
                        }
                    },
                    sendOtpClickListener = FormInputAdapter.SendOtpClickListener { clickedFormId, _, _, _, _, _, _ ->
                        if (clickedFormId == viewModel.getAbhaSubmitBtnId()) {
                            val abhaId = viewModel.getAbhaCardInput().orEmpty()
                            if (abhaId.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.enter_abha_id_ayushman_card_number),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                viewModel.getUserDetailsByAyushmanAbhaCardNo(abhaId)
                            }
                        }
                    },
                    isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())

                            adapter.submitList(it)
                    }
                }
            }
        }
        binding.fabEdit.setOnClickListener {
            editMode = true
            viewModel.setRecordExists(false)
        }
        binding.btnSubmit.setOnClickListener {
            submitHouseholdForm()
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
                    if (!editMode) {
                        nextScreenAlert.setMessage(
                            resources.getString(
                                R.string.add_head_of_family_message,
                                viewModel.getHoFName()
                            )
                        )
                        nextScreenAlert.show()
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


    }

    private fun submitHouseholdForm() {
        activity?.currentFocus?.clearFocus()
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hh,
                getString(R.string.frag_nhhr_title)
            )
        }
        viewModel.readRecord.observe(viewLifecycleOwner) {
            if (!it && !viewModel.getIsConsentAgreed()) consentAlert.show()
        }


    }

    private fun requestLocationPermission() {
        val locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            requestLocationPermission.launch(permission.ACCESS_FINE_LOCATION)
        else
            if (!isGPSEnabled)
                showSettingsAlert()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (consentAlert.isShowing) consentAlert.dismiss()
        if (nextScreenAlert.isShowing) nextScreenAlert.dismiss()
        _binding = null
    }
}
