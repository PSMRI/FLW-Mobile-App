package org.piramalswasthya.sakhi.ui.home_activity.all_ben

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import org.piramalswasthya.sakhi.databinding.AlertFilterBinding
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchAndToggleRvButtonBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_household.AllHouseholdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import timber.log.Timber

@AndroidEntryPoint
class AllBenFragment : Fragment() {

    private var _binding: FragmentDisplaySearchAndToggleRvButtonBinding? = null

    private val binding: FragmentDisplaySearchAndToggleRvButtonBinding
        get() = _binding!!

    val args: AllBenFragmentArgs by lazy {
        AllBenFragmentArgs.fromBundle(requireArguments())
    }

    private lateinit var benAdapter: BenListAdapter

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
        WITHOUT
    }

    private val filterAlert by lazy {
        val filterAlertBinding = AlertFilterBinding.inflate(layoutInflater, binding.root, false)
        filterAlertBinding.rgAbha.setOnCheckedChangeListener { radioGroup, i ->
            Timber.d("RG Gender selected id : $i")
            selectedAbha = when (i) {
                filterAlertBinding.rbAll.id -> Abha.ALL
                filterAlertBinding.rbWith.id -> Abha.WITH
                filterAlertBinding.rbWithout.id -> Abha.WITHOUT
                else -> Abha.ALL
            }

        }

        filterAlertBinding.tvRch.visibility = View.GONE
        filterAlertBinding.cbRch.visibility = View.GONE

        val alert = MaterialAlertDialogBuilder(requireContext()).setView(filterAlertBinding.root)
            .setOnCancelListener {
            }.create()

        filterAlertBinding.btnOk.setOnClickListener {
            if (selectedAbha == Abha.WITH) {
                viewModel.filterType(1)
            } else if (selectedAbha == Abha.WITHOUT) {
                viewModel.filterType(2)
            }  else {
                viewModel.filterType(0)
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
        _binding = FragmentDisplaySearchAndToggleRvButtonBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE

        binding.ibFilter.setOnClickListener {
            filterAlert.show()
        }

        if (args.source == 1 || args.source == 2) {
            binding.ibFilter.visibility = View.GONE
        }

        benAdapter = BenListAdapter(
            clickListener = BenListAdapter.BenClickListener(
                { hhId, benId, relToHeadId ->

                    findNavController().navigate(
                        AllBenFragmentDirections.actionAllBenFragmentToNewBenRegFragment(
                            hhId = hhId,
                            benId = benId,
                            relToHeadId = relToHeadId,
                            gender = 0

                        )
                    )
                },
                {
                },
                { benId, hhId ->
                    checkAndGenerateABHA(benId)
                },

                ),
            showAbha = true,
            showSyncIcon = true,
            showBeneficiaries = true,
            showRegistrationDate = true
        )
        binding.rvAny.adapter = benAdapter
        lifecycleScope.launch {
            viewModel.benList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }

        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment())
        }
        binding.ibSearch.visibility = View.VISIBLE
        binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

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
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ben,
                title = if (args.source == 1) {
                    getString(R.string.icon_title_abha)
                } else if (args.source == 2) {
                    getString(R.string.icon_title_rch)
                } else {
                    getString(R.string.icon_title_ben)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}