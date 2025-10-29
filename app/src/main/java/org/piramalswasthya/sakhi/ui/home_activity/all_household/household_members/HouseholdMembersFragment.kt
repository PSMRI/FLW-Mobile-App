package org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject

@AndroidEntryPoint
class HouseholdMembersFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: HouseholdMembersViewModel by viewModels()

    var showAbha = false
    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private val hhId by lazy {
        HouseholdMembersFragmentArgs.fromBundle(requireArguments()).hhId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE
        binding.llSearch.visibility = View.GONE
        if (viewModel.isFromDisease == 1 && viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()) {
            binding.switchButton.visibility = View.VISIBLE
            showAbha = false
        } else if(viewModel.isFromDisease == 1) {
            binding.switchButton.visibility = View.GONE
            showAbha = false
        } else {
            binding.switchButton.visibility = View.GONE
            showAbha = true
        }
        binding.switchButton.text = if (binding.switchButton.isChecked) "ON" else "OFF"
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            binding.switchButton.text = if (isChecked) "ON" else "OFF"
        }

        val benAdapter = BenListAdapter(
            clickListener = BenListAdapter.BenClickListener(
                { hhId, benId, relToHeadId ->
                    if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                        if (viewModel.isFromDisease == 0) {
                            findNavController().navigate(
                                HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(
                                    hhId = hhId,
                                    benId = benId,
                                    gender = 0,
                                    relToHeadId = relToHeadId
                                )

                            )
                        } else {
                            if (viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()) {

                                findNavController().navigate(
                                    HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToMalariaFormFragment(
                                        benId = benId,
                                    )

                                )
                            } else if (viewModel.diseaseType == IconDataset.Disease.KALA_AZAR.toString()) {
                                findNavController().navigate(
                                    HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToKalaAzarFormFragment(
                                        benId = benId,
                                    )

                                )
                            }
                        }
                    }
                },
                {
                },
                { benId, hhId ->
                    if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                        checkAndGenerateABHA(benId)
                    }
                },
                { benId, hhId ->
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
                }
            ),
            showSyncIcon = true,
            showAbha = showAbha,
            showRegistrationDate = true,
            showCall = true,
            pref = prefDao
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

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__hh,
                    getString(R.string.household_members)
                )
            } else {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__hh,
                    getString(R.string.household_members)
                )
            }
        }
    }


    private fun checkAndGenerateABHA(benId: Long) {
        viewModel.fetchAbha(benId)
    }


}