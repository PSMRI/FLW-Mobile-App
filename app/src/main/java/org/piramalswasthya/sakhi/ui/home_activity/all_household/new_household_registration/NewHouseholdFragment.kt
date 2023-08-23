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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.AlertConsentBinding
import org.piramalswasthya.sakhi.databinding.FragmentInputFormPageHhBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.home_activity.all_household.new_household_registration.NewHouseholdViewModel.State
import timber.log.Timber


@AndroidEntryPoint
class NewHouseholdFragment : Fragment() {

    private var _binding: FragmentInputFormPageHhBinding? = null

    private val binding: FragmentInputFormPageHhBinding
        get() = _binding!!


    private val viewModel: NewHouseholdViewModel by viewModels()

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) {
                requestLocationPermission()
            } else
                findNavController().navigateUp()
        }

    private var micClickedElementId: Int = -1
    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val formattedValue = value/*.substring(0,50)*/.uppercase()
        val listIndex =
            viewModel.updateValueByIdAndReturnListIndex(micClickedElementId, formattedValue)
        listIndex.takeIf { it >= 0 }?.let {
            binding.inputForm.rvInputForm.adapter?.notifyItemChanged(it)
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
        alertBinding.checkBox.text = resources.getString(R.string.consent_text)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(false)
            .create()
        alertBinding.btnNegative.setOnClickListener {
            alertDialog.dismiss()
            findNavController().navigateUp()
        }
        alertBinding.btnPositive.setOnClickListener {
            if (alertBinding.checkBox.isChecked) {
                viewModel.setConsentAgreed()
                requestLocationPermission()
                alertDialog.dismiss()
            } else
                Toast.makeText(context, resources.getString(R.string.please_tick_the_checkbox), Toast.LENGTH_SHORT).show()
        }
        alertDialog
    }


//    private val pageChangeCallback: OnPageChangeCallback by lazy {
//        object : OnPageChangeCallback() {
//
//            override fun onPageSelected(i: Int) {
//                onPageChange(i)
//
//            }
//        }
//    }

//    private fun onPageChange(i: Int) {
//        Timber.d("OnPageChange $i called with mTab ${viewModel.mTabPosition}")
//        if (i == viewModel.mTabPosition) {
//            return
//        }
//        if (i < viewModel.mTabPosition)
//            viewModel.setMTabPosition(i)
//        else {
//            val validated =
//                validateFormForPage(i)
//            if (validated) {
//                viewModel.setMTabPosition(i)
//                if(viewModel.recordExists.value==false){
//                    if(viewModel.mTabPosition ==1 || viewModel.mTabPosition==2)
//                        viewModel.saveForm()
//                    when (viewModel.mTabPosition) {
//                        1 -> {
//                            viewModel.saveForm()
//                        }
//                        2 -> {
//                            viewModel.saveForm()
//                        }
//                    }
//                }
//            }
//        }
//        binding.vp2Nhhr.currentItem = viewModel.mTabPosition
//        //binding.tlNhhr.setScrollPosition(mTabPosition, 0f, false)
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
//                if(viewModel.recordExists.value==false)
//                    binding.btnSubmitForm.visibility = View.VISIBLE
//            }
//        }
//    }

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputFormPageHhBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.tlNhhr.apply {
//            addTab(newTab().also {
//                it.text = "Family Details"
//                it.view.isEnabled = false
//            })
//            addTab(newTab().also {
//                it.text = "Household Details"
//                it.view.isEnabled = false
//            })
//            addTab(newTab().also {
//                it.text = "Household Amenities"
//                it.view.isEnabled = false
//            })
//        }
        lifecycleScope.launch {
            viewModel.currentPage.collect {
                binding.tvTitle.text = when (it) {
                    1 -> resources.getString(R.string.nhhr_title_page_1)
                    2 -> resources.getString(R.string.nhhr_title_page_2)
                    3 -> resources.getString(R.string.nhhr_title_page_3)
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
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                if (viewModel.currentPage.value == 3 && !recordExists) binding.btnSubmitForm.visibility = View.VISIBLE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        when (index) {
                            Konstants.micClickIndex -> {
                                micClickedElementId = formId
                                sttContract.launch(Unit)
                            }

                            else -> {
                                viewModel.updateListOnValueChanged(formId, index)
//                                hardCodedListUpdate(formId)
                            }
                        }
                    },
                    isEnabled = !recordExists
                )
                binding.inputForm.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())

                            adapter.submitList(it)
                    }
                }
            }
        }
        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExists(false)
        }
//        binding.vp2Nhhr.adapter = NewHouseholdPagerAdapter(this)
//        binding.vp2Nhhr.isUserInputEnabled = false
//        when (viewModel.mTabPosition) {
//            0 -> {
//                binding.btnPrev.visibility = View.GONE
//                binding.btnNext.visibility = View.VISIBLE
//            }
//            1 -> {
//                binding.btnNext.visibility = View.VISIBLE
//                binding.btnPrev.visibility = View.VISIBLE
//            }
//            2 -> {
//                binding.btnPrev.visibility = View.VISIBLE
//                binding.btnNext.visibility = View.GONE
//            }
//        }

//        binding.tlNhhr.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                tab?.position?.takeIf {viewModel.tabSelectedPosition(it+1)  }?.let {
//                    goToNextPage()
//                }?:
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//
//            }
//
//        })
//        TabLayoutMediator(binding.tlNhhr, binding.vp2Nhhr) { tab, position ->
//            tab.text = when (position) {
//                0 -> "Family Details"
//                1 -> "Household Details"
//                2 -> "Household Amenities"
//                else -> "NA"
//            }
//            tab.view.isClickable = false
//        }.attach()

        binding.btnPrev.setOnClickListener {
            goToPrevPage()
        }
        binding.btnNext.setOnClickListener {
            goToNextPage()
        }
        binding.btnSubmitForm.setOnClickListener {
            submitHouseholdForm()
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
                    findNavController().navigate(
                        NewHouseholdFragmentDirections.actionNewHouseholdFragmentToNewBenRegTypeFragment(
                            viewModel.getHHId()
                        )
                    )
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
            }
        }


    }

    private fun submitHouseholdForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun goToPrevPage() {
        viewModel.goToPreviousPage()
    }

    private fun goToNextPage() {
        if (validateCurrentPage())
            viewModel.goToNextPage()
    }

    override fun onStart() {
        super.onStart()
        viewModel.recordExists.observe(viewLifecycleOwner) {
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
        _binding = null
    }
}
