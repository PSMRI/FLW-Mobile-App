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
import org.piramalswasthya.sakhi.databinding.FragmentInputFormPageHhBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_age_more_15.NewBenRegG15ViewModel.State
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class NewBenRegG15Fragment : Fragment() {

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
//                    context?.openFileOutput("Hello", Context.MODE_PRIVATE)?.use {
//                        context?.contentResolver?.openInputStream(uri)!!
//                        it.write(byteArray.)
//                    }

                    binding.inputForm.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapter
                        adapter.notifyItemChanged(0)
                    }
                    Timber.d("Image saved at @ $uri")
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

//    private val pageChangeCallback: ViewPager2.OnPageChangeCallback by lazy {
//        object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(i: Int) {
//                onPageChange(i)
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputFormPageHhBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.btnSubmitForm.text = context?.getString(R.string.btn_submit)
//        binding.vp2Nhhr.adapter = NewBenGenPagerAdapter(
//            mutableListOf(
//                "Ben Details",
//                "ID Details"
//            ), this
//        )
//        when (viewModel.mTabPosition) {
//            0 -> {
//                binding.btnPrev.visibility = View.GONE
//                binding.btnNext.visibility = View.VISIBLE
//            }
//            1 -> {
//                binding.btnPrev.visibility = View.VISIBLE
//                binding.btnNext.visibility = View.GONE
//            }
//        }
//
//        when (viewModel.mTabPosition) {
//            0 -> {
//                binding.btnPrev.visibility = View.GONE
//                binding.btnNext.visibility = View.VISIBLE
//                binding.btnSubmitForm.visibility = View.GONE
//            }
//            1 -> {
//                binding.btnNext.visibility = View.VISIBLE
//                binding.btnPrev.visibility = View.VISIBLE
//                binding.btnSubmitForm.visibility = View.GONE
//            }
//            2 -> {
//                binding.btnPrev.visibility = View.VISIBLE
//                binding.btnNext.visibility = View.GONE
//                binding.btnSubmitForm.visibility = View.VISIBLE
//            }
//        }
//        TabLayoutMediator(binding.tlNhhr, binding.vp2Nhhr) { tab, position ->
//            tab.text = (binding.vp2Nhhr.adapter as NewBenGenPagerAdapter).getPageName(position)
//            tab.view.isClickable = false
//        }.attach()
        lifecycleScope.launch {
            viewModel.currentPage.collect {
                binding.tvTitle.text = when (it) {
                    1 -> resources.getString(R.string.beneficiary_details_ben)
                    2 -> resources.getString(R.string.id_details)
                    3 -> resources.getString(R.string.reproductive_status_ben)
                    else -> null
                }
//                binding.tlNhhr.selectTab(binding.tlNhhr.getTabAt(it - 1), true)
//                when (it) {
//                    1 -> {
//                        binding.btnPrev.visibility = View.INVISIBLE
//                        binding.btnNext.visibility = View.VISIBLE
//                        binding.btnSubmitForm.visibility = View.INVISIBLE
//                    }
//                    2 -> {
//                        binding.btnNext.visibility = View.VISIBLE
//                        binding.btnPrev.visibility = View.VISIBLE
//                        binding.btnSubmitForm.visibility = View.INVISIBLE
//                    }
//                    3 -> {
//                        binding.btnPrev.visibility = View.VISIBLE
//                        binding.btnNext.visibility = View.INVISIBLE
//                        binding.btnSubmitForm.visibility = View.VISIBLE
//                    }
//                }
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
                    binding.clContent.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
                    Toast.makeText(context, resources.getString(R.string.save_successful), Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigate(NewBenRegG15FragmentDirections.actionNewBenRegG15FragmentToHomeFragment())
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, resources.getString(R.string.something_wend_wong_contact_testing), Toast.LENGTH_LONG
                    ).show()
                    binding.clContent.visibility = View.VISIBLE
                    binding.rlSaving.visibility = View.GONE
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

//        viewModel.hasReproductiveStatus.observe(viewLifecycleOwner) {
//            val adapter = (binding.vp2Nhhr.adapter as NewBenGenPagerAdapter)
//            if (it) adapter.addPage("Reproductive Status")
//            else if (adapter.itemCount == 3) adapter.removePage(2)
//        }

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
                    /*viewModel.getIndexOfFatherName().takeIf { it > 0 }?.let {
                        Timber.d("Update Father index called! $it")
                        notifyItemChanged(it)
                    }
                    viewModel.getIndexOfMotherName().takeIf { it > 0 }?.let {
                        Timber.d("Update Mother index called! $it")
                        notifyItemChanged(it)
                    }
                    viewModel.getIndexOfSpouseName().takeIf { it > 0 }?.let {
                        Timber.d("Update Spouse index called! $it")
                        notifyItemChanged(it)
                    }*/

                }

//                27 ->
//                    viewModel.getIndexOfExpectedDateOfDelivery().takeIf { it > 0 }?.let {
//                        notifyItemChanged(it)
//                    }

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
        viewModel.goToPreviousPage()
    }

    private fun goToNextPage() {
        if (validateCurrentPage()) viewModel.goToNextPage()
    }


    override fun onStart() {
        super.onStart()
        requestLocationPermission()
    }


//    private fun onPageChange(i: Int) {
//        if (i == viewModel.mTabPosition) {
//            return
//        }
//        if (i < viewModel.mTabPosition) viewModel.setMTabPosition(i)
//        else {
//            val validated = validateFormForPage(i)
//            if (validated) {
//                viewModel.setMTabPosition(i)
////                when (viewModel.mTabPosition) {
////                    1 -> {
////                        viewModel.persistRegisterRecord()
////                    }
////                    2 -> {
////                        viewModel.persistSecondPage()
////                    }
////                }
//            }
//        }
//        binding.vp2Nhhr.currentItem = viewModel.mTabPosition
//        if (binding.vp2Nhhr.adapter?.itemCount == 2) {
//            when (viewModel.mTabPosition) {
//                0 -> {
//                    binding.btnPrev.visibility = View.GONE
//                    binding.btnNext.visibility = View.VISIBLE
//                    binding.btnSubmitForm.visibility = View.GONE
//                }
//                1 -> {
//                    binding.btnPrev.visibility = View.VISIBLE
//                    binding.btnNext.visibility = View.GONE
//                    if (viewModel.recordExists.value == false) binding.btnSubmitForm.visibility =
//                        View.VISIBLE
//                }
//            }
//        } else {
//            when (viewModel.mTabPosition) {
//                0 -> {
//                    binding.btnPrev.visibility = View.GONE
//                    binding.btnNext.visibility = View.VISIBLE
//                    binding.btnSubmitForm.visibility = View.GONE
//
//                }
//                1 -> {
//                    binding.btnNext.visibility = View.VISIBLE
//                    binding.btnPrev.visibility = View.VISIBLE
//                    binding.btnSubmitForm.visibility = View.GONE
//
//                }
//                2 -> {
//                    binding.btnPrev.visibility = View.VISIBLE
//                    binding.btnNext.visibility = View.GONE
//                    if (viewModel.recordExists.value == false) binding.btnSubmitForm.visibility =
//                        View.VISIBLE
//
//                }
//            }
//        }
//    }

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

//    private fun validateFormForPage(i: Int): Boolean {
//        val currentItem = "f${viewModel.mTabPosition}"
//        Timber.d(
//            "item :Current mTab position : $currentItem toChange position $i \n Fragment : ${
//                childFragmentManager.findFragmentByTag(
//                    currentItem
//                )
//            }"
//        )
//        return (childFragmentManager.findFragmentByTag(currentItem) as NewBenRegG15ObjectFragment).validate()
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}