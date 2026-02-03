package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_age_more_15

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
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
import org.piramalswasthya.sakhi.databinding.FragmentInputFormPageHhBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_age_more_15.NewBenRegG15ViewModel.State
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.BaseFormFragment
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class NewBenRegG15Fragment : BaseFormFragment() {

    private var _binding: FragmentInputFormPageHhBinding? = null

    private val binding: FragmentInputFormPageHhBinding
        get() = _binding!!

    private val viewModel: NewBenRegG15ViewModel by viewModels()
    private var micClickedElementId: Int = -1
    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val formattedValue = value/*.substring(0,50)*/.uppercase()
        val listIndex =
            viewModel.updateValueByIdAndReturnListIndex(micClickedElementId, formattedValue)
        listIndex.takeIf { it >= 0 }?.let {
            binding.inputForm.rvInputForm.adapter?.notifyItemChanged(it)
        }
        setFormAsDirty()
    }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) {
                requestLocationPermission()
            } else findNavController().navigateUp()
        }

    private var latestTmpUri: Uri? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    viewModel.setImageUriToFormElement(uri)

                    binding.inputForm.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapter
                        adapter.notifyItemChanged(0)
                    }
                    Timber.d("Image saved at @ $uri")
                    setFormAsDirty()
                }
            }
        }


    private fun showSettingsAlert() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())

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
            findNavController().navigateUp()
            dialog.cancel()
        }
        alertDialog.show()
    }

    private val errorAlert by lazy {
        MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.error_input))
            //.setMessage("Do you want to continue with previous form, or create a new form and discard the previous form?")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }

            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputFormPageHhBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.currentPage.collect {
                binding.tvTitle.text = when (it) {
                    1 -> resources.getString(R.string.beneficiary_details_ben)
                    2 -> resources.getString(R.string.id_details)
                    3 -> resources.getString(R.string.reproductive_status_ben)
                    else -> null
                }
            }
        }
        lifecycleScope.launch {
            viewModel.prevPageButtonVisibility.collect {
                binding.btnPrev.visibility = if (it) View.VISIBLE else View.INVISIBLE

            }
        }
        lifecycleScope.launch {
            viewModel.nextPageButtonVisibility.collect {
                binding.btnNext.visibility = if (it) View.VISIBLE else View.INVISIBLE

            }
        }
        lifecycleScope.launch {
            viewModel.submitPageButtonVisibility.collect {
                binding.btnSubmitForm.visibility = if (it) View.VISIBLE else View.INVISIBLE

            }
        }
        binding.btnPrev.setOnClickListener {
            goToPrevPage()
        }
        binding.btnNext.setOnClickListener {
            goToNextPage()
        }
        binding.btnSubmitForm.setOnClickListener {
            submitBenForm()
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
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
                                setFormAsDirty()
                            }
                        }

                    }, isEnabled = !recordExists
                    )
                binding.inputForm.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        Timber.d("Collecting $it")
                        if (it.isNotEmpty())
                            adapter.submitList(it)
                    }
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                }

                State.SAVING -> {
                    binding.clContent.visibility = View.GONE
                    binding.rlSaving.visibility = View.VISIBLE
                }

                State.SAVE_SUCCESS -> {
                    setFormAsClean()
                    binding.clContent.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigate(NewBenRegG15FragmentDirections.actionNewBenRegG15FragmentToHomeFragment())
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(

                        context,
                        resources.getString(R.string.something_wend_wong_contact_testing),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.clContent.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                }
                State.DRAFT_SAVED -> {
                    setFormAsClean()
                    binding.clContent.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Draft saved successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                errorAlert.setMessage(it)
                errorAlert.show()
                viewModel.resetErrorMessage()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun hardCodedListUpdate(formId: Int) {
        binding.inputForm.rvInputForm.adapter?.apply {
            when (formId) {
                6 -> {
                    notifyItemChanged(5)
                    notifyItemChanged(viewModel.getIndexOfAgeAtMarriage())
                }

                5 -> {
                    notifyItemChanged(4)
                }

                7 -> {
                    notifyItemChanged(viewModel.getIndexOfMaritalStatus())
                    notifyItemChanged(viewModel.getIndexOfRelationToHead())
                }

                8 -> {
                    notifyDataSetChanged()

                }

                28 -> notifyItemChanged(1)

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

    private fun goToPrevPage() {
        lifecycleScope.launch {
            viewModel.goToPreviousPage()
        }
    }

    private fun goToNextPage() {
        if (validateCurrentPage()) viewModel.goToNextPage()
    }


    override fun onStart() {
        super.onStart()
        requestLocationPermission()
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

    private fun validateCurrentPage(): Boolean {
        val result = binding.inputForm.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1) true
        else {
            if (result != null) {
                binding.inputForm.rvInputForm.scrollToPosition(result)
            }
            false
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
