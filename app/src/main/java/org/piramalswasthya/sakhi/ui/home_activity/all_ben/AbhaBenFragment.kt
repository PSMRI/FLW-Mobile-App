package org.piramalswasthya.sakhi.ui.home_activity.all_ben

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.AlertFilterBinding
import org.piramalswasthya.sakhi.databinding.FragmentAbhaBenBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import org.piramalswasthya.sakhi.utils.RoleConstants
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AbhaBenFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentAbhaBenBinding? = null

    private val binding: FragmentAbhaBenBinding
        get() = _binding!!

    val args: AllBenFragmentArgs by lazy {
        AllBenFragmentArgs.fromBundle(requireArguments())
    }

    private lateinit var benNewAdapter: BenListAdapter
    private lateinit var benOldAdapter: BenListAdapter

    private var isNewListEmpty = true
    private var isOldListEmpty = true

    private var selectedAbha = Abha.ALL

    private val viewModel: AllBenViewModel by viewModels()
    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        binding.searchView.setText(value)
        binding.searchView.setSelection(value.length)
        viewModel.filterText(value)
    }

    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    enum class Abha {
        ALL,
        WITH,
        WITHOUT,
        AGE_ABOVE_30
    }

    private val filterAlert by lazy {
        val filterAlertBinding = AlertFilterBinding.inflate(layoutInflater, binding.root, false)
        filterAlertBinding.rgAbha.setOnCheckedChangeListener { _, i ->
            Timber.d("RG Gender selected id : $i")
            selectedAbha = when (i) {
                filterAlertBinding.rbAll.id -> Abha.ALL
                filterAlertBinding.rbWith.id -> Abha.WITH
                filterAlertBinding.rbWithout.id -> Abha.WITHOUT
                filterAlertBinding.rbAgeAboveThirty.id -> Abha.AGE_ABOVE_30
                else -> Abha.ALL
            }

        }

        filterAlertBinding.tvRch.visibility = View.GONE
        filterAlertBinding.cbRch.visibility = View.GONE

        val alert = MaterialAlertDialogBuilder(requireContext()).setView(filterAlertBinding.root)
            .setOnCancelListener {
            }.create()

        filterAlertBinding.btnOk.setOnClickListener {
            when (selectedAbha) {
                Abha.WITH -> viewModel.filterType(1)
                Abha.WITHOUT -> viewModel.filterType(2)
                Abha.AGE_ABOVE_30 -> viewModel.filterType(3)
                else -> viewModel.filterType(0)
            }
            alert.cancel()
        }
        filterAlertBinding.btnCancel.setOnClickListener {
            alert.cancel()
        }

        alert
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbhaBenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ibFilter.setOnClickListener {
            filterAlert.show()
        }

        binding.ibDownload.setOnClickListener {
            viewModel.downloadCsv(requireContext())
        }

        if (args.source == 1 || args.source == 2 || args.source == 3) {
            binding.ibFilter.visibility = View.GONE
            binding.ibDownload.visibility = View.VISIBLE
        }

        benNewAdapter = BenListAdapter(
            clickListener = BenListAdapter.BenClickListener(
                { _,_, _, _ ->

//                        findNavController().navigate(
//                            AllBenFragmentDirections.actionAllBenFragmentToNewBenRegFragment(
//                                hhId = hhId,
//                                benId = benId,
//                                relToHeadId = relToHeadId,
//                                gender = 0
//
//                            )
//                        )

                },
                clickedWifeBen = {
                        _, _, _, _ ->

                    if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                        /*
                        * Currently No Implementation Required
                        * */
                    }
                },
                clickedHusbandBen = {
                        item, hhId, benId, relToHeadId ->

                    if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                        /*
                        *      * Currently No Implementation Required
                        * */
                    }
                },
                clickedChildben = {_, _, _, _ ->

                },
                {_, _->

                },
                { item,benId, hhId ->
                    checkAndGenerateABHA(benId)
                },
                {item, benId, hhId, isViewMode, isIFA ->
                },
                {
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.setData(Uri.parse("tel:${it.mobileNo}"))
                        startActivity(callIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        activity?.let {
                            (it as HomeActivity).askForPermissions()
                        }
                        Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
                    }
                },{

                }

                ),
            showBeneficiaries = true,
            showRegistrationDate = true,
            showSyncIcon = true,
            showAbha = true,
            showCall = true,
            pref = prefDao,
            context = requireActivity()

        )
        binding.rvNewAbha.adapter = benNewAdapter

        benOldAdapter = BenListAdapter(
            clickListener = BenListAdapter.BenClickListener(
                { item,hhId, benId, relToHeadId ->

                        findNavController().navigate(
                            AllBenFragmentDirections.actionAllBenFragmentToNewBenRegFragment(
                                hhId = hhId,
                                benId = benId,
                                relToHeadId = relToHeadId,
                                isAddSpouse = 0,
                                gender = 0

                            )
                        )

                },
                clickedWifeBen = {item, hhId, benId, relToHeadId ->
                },
                clickedHusbandBen = {
                        item,  hhId, benId, relToHeadId ->

                },
                clickedChildben = {
                        item, hhId, benId, relToHeadId ->
                },
                {item,hhid->
                },
                { item,benId, hhId ->
                    checkAndGenerateABHA(benId)
                },
                { item,benId, hhId, isViewMode, isIFA ->
                },
                {
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.setData(Uri.parse("tel:${it.mobileNo}"))
                        startActivity(callIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        activity?.let {
                            (it as HomeActivity).askForPermissions()
                        }
                        Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
                    }
                },{

                }

            ),
            showBeneficiaries = true,
            showRegistrationDate = true,
            showSyncIcon = true,
            showAbha = true,
            showCall = true,
            pref = prefDao,
            context = requireActivity()
        )
        binding.rvExistingAbha.adapter = benOldAdapter

        binding.ibSearch.visibility = View.VISIBLE
        binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                /*
                 * Currently No Implementation Required
                 * */
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                /*
                  * Currently No Implementation Required
                  * */
            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.filterText(p0?.toString() ?: "")
            }

        }
        binding.searchView.setOnFocusChangeListener { searchView, b ->
            if (b)
                (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else
                (searchView as EditText).removeTextChangedListener(searchTextWatcher)

        }

        viewModel.abha.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    abhaDisclaimer.setMessage(it)
                    abhaDisclaimer.show()
                }
            }
        }

        viewModel.benRegId.observe(viewLifecycleOwner) {
            if (it != null) {
                val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("benId", viewModel.benId.value)
                intent.putExtra("benRegId", it)
                requireActivity().startActivity(intent)
                viewModel.resetBenRegId()
            }

        }
    }

    private fun checkAndGenerateABHA(benId: Long) {
        lifecycleScope.launch {
            if (viewModel.getBenFromId(benId) == 0L) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Alert!")
                    .setMessage("Please wait for the record to sync and try again.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                viewModel.fetchAbha(benId)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__ben,
                    title = if (args.source == 1) {
                        getString(R.string.icon_title_abhas)
                    } else if (args.source == 2) {
                        getString(R.string.icon_title_rchs)
                    } else {
                        getString(R.string.icon_title_ben)
                    }
                )
            } else {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__ben,
                    title = if (args.source == 1) {
                        getString(R.string.icon_title_abhas)
                    } else if (args.source == 2) {
                        getString(R.string.icon_title_rchs)
                    } else {
                        getString(R.string.icon_title_ben)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}