package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.AlertConsentBinding
import org.piramalswasthya.sakhi.databinding.FragmentNewBenRegTypeBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import org.piramalswasthya.sakhi.work.WorkerUtils

@AndroidEntryPoint
class NewBenRegTypeFragment : Fragment() {


    private var _binding : FragmentNewBenRegTypeBinding? = null

    private val binding  : FragmentNewBenRegTypeBinding
        get() = _binding!!


    private val hhId by lazy {
        NewBenRegTypeFragmentArgs.fromBundle(requireArguments()).hhId
    }

    private var hasDraftKid = false
    private var hasDraftGen = false


    private val consentAlert by lazy {
        val alertBinding = AlertConsentBinding.inflate(layoutInflater,binding.root,false)
        alertBinding.textView4.text = getString(R.string.consent_alert_title)
        alertBinding.checkBox.text = getString(R.string.consent_text)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(false)
            .create()
        alertBinding.btnNegative.setOnClickListener {
            alertDialog.dismiss()
            findNavController().navigateUp()
        }
        alertBinding.btnPositive.setOnClickListener {
            if(alertBinding.checkBox.isChecked) {
                alertDialog.dismiss()
                alertBinding.checkBox.isChecked = false
                viewModel.setConsentAgreed()
            }
            else
                Toast.makeText(context,resources.getString(R.string.please_tick_the_checkbox), Toast.LENGTH_SHORT).show()
        }
        alertDialog
    }

    private val draftLoadAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.incomplete_form_found))
            .setMessage(resources.getString(R.string.do_you_want_to_continue_with_previous_form_or_create_a_new_form_and_discard_the_previous_form))
            .setPositiveButton(resources.getString(R.string.open_draft)){
                    dialog,_->
                val isKid = binding.rgBenType.checkedRadioButtonId==binding.rbKidPath.id
                viewModel.navigateToNewBenRegistration(hhId,false, isKid)
                dialog.dismiss()

            }
            .setNegativeButton(resources.getString(R.string.create_new)) { dialog, _ ->
                val isKid = binding.rgBenType.checkedRadioButtonId == binding.rbKidPath.id
                viewModel.navigateToNewBenRegistration(hhId, true, isKid)
                dialog.dismiss()
            }
            .create()
    }

    private val viewModel: NewBenRegTypeViewModel by viewModels()

    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })

//    private val onBackPressedCallback by lazy {
//        object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                Toast.makeText(context, "Back Pressed", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewBenRegTypeBinding.inflate(layoutInflater)
        viewModel.checkDraft(hhId)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.isConsentAgreed)
            consentAlert.show()
//        activity?.onBackPressedDispatcher?.addCallback(
//            viewLifecycleOwner, onBackPressedCallback
//        )

        viewModel.hasDraftForKid.observe(viewLifecycleOwner) {
            this.hasDraftKid = it
        }
        viewModel.hasDraftForGen.observe(viewLifecycleOwner) {
            this.hasDraftGen = it
        }

        lifecycleScope.launch {
            homeViewModel.numBenIdsAvail.collect {
                when (it) {
                    in 1..Konstants.benIdWorkerTriggerLimit -> {
                        binding.errorText.text =
                            resources.getString(R.string.warning_id_running_low_connect_to_internet_at_the_earliest)
                        WorkerUtils.triggerGenBenIdWorker(requireContext())

                    }
                    0 -> {
                        binding.btnContinue.visibility = View.GONE
                        binding.errorText.text =
                            resources.getString(R.string.error_no_more_ben_ids_available_connect_to_internet_to_get_some)
                        WorkerUtils.triggerGenBenIdWorker(requireContext())
                    }
                    else -> {
                        binding.btnContinue.visibility = View.VISIBLE
                        binding.errorText.text = null
                    }
                }

            }
        }

        viewModel.navigateToNewBenKidRegistration.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    NewBenRegTypeFragmentDirections.actionNewBenRegTypeFragmentToNewBenRegL15Fragment(
                        hhId
                    )
                )
                viewModel.navigateToBenKidRegistrationCompleted()
            }
        }
        viewModel.navigateToNewBenGenRegistration.observe(viewLifecycleOwner){
            if(it) {
                findNavController().navigate(
                    NewBenRegTypeFragmentDirections.actionNewBenRegTypeFragmentToNewBenRegG15Fragment(
                        hhId
                    )
                )
                viewModel.navigateToBenGenRegistrationCompleted()
            }
        }

        binding.btnContinue.setOnClickListener {
            when (binding.rgBenType.checkedRadioButtonId) {
                R.id.rb_kid_path -> {
                    if(hasDraftKid)
                        draftLoadAlert.show()
                    else
                        viewModel.navigateToNewBenRegistration(hhId=hhId, delete = false, isKid = true)
                }
                R.id.rb_adult_path -> {
                    if(hasDraftGen)
                        draftLoadAlert.show()
                    else
                        viewModel.navigateToNewBenRegistration(hhId=hhId, delete = false, isKid = false)
                }
                else -> Toast.makeText(
                    context,
                    resources.getString(R.string.please_select_type_of_beneficiary),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}