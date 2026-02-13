package org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil
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

    fun showSoftDeleteDialog(benBasicDomain: BenBasicDomain) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Beneficiary")
            .setMessage("Are you sure you want to delete ${benBasicDomain.benFullName}")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deActivateBeneficiary(benBasicDomain)
            }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.dismiss() }
            .show()
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
                { item,hhId, benId, relToHeadId ->
                    val isAsha = prefDao.getLoggedInUser()?.role.equals("asha", true)
                    val canNavigate = !item.isDeactivate

                    when {
                        isAsha && viewModel.isFromDisease == 0 && canNavigate -> {
                            findNavController().navigate(
                                HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(
                                    hhId = hhId,
                                    benId = benId,
                                    gender = 0,
                                    isAddSpouse = 0,
                                    relToHeadId = relToHeadId
                                )

                            )
                        }

                        isAsha && viewModel.isFromDisease != 0 &&
                                viewModel.diseaseType == IconDataset.Disease.MALARIA.toString() &&
                                canNavigate -> {
                            findNavController().navigate(
                                HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToMalariaFormFragment(
                                    benId = benId,
                                )

                            )
                        }

                        isAsha && viewModel.isFromDisease != 0 &&
                                viewModel.diseaseType == IconDataset.Disease.KALA_AZAR.toString() &&
                                canNavigate -> {
                            findNavController().navigate(
                                HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToKalaAzarFormFragment(
                                    benId = benId,
                                )

                            )
                        }
                    }

                },
                clickedWifeBen = { item, hhId, benId, relToHeadId ->

                    when {
                        prefDao.getLoggedInUser()?.role.equals("asha", true) &&
                                !item.isDeactivate &&
                                viewModel.isFromDisease == 0 -> {

                            findNavController().navigate(
                                HouseholdMembersFragmentDirections
                                    .actionHouseholdMembersFragmentToNewBenRegFragment(
                                        hhId = hhId,
                                        benId = 0,
                                        gender = 2,
                                        selectedBenId = benId,
                                        isAddSpouse = 1,
                                        relToHeadId = HelperUtil.getFemaleRelationId(relToHeadId)
                                    )
                            )
                        }

                        prefDao.getLoggedInUser()?.role.equals("asha", true) &&
                                !item.isDeactivate &&
                                viewModel.isFromDisease != 0 &&
                                viewModel.diseaseType == IconDataset.Disease.MALARIA.toString() -> {

                            findNavController().navigate(
                                HouseholdMembersFragmentDirections
                                    .actionHouseholdMembersFragmentToMalariaFormFragment(
                                        benId = benId
                                    )
                            )
                        }

                        prefDao.getLoggedInUser()?.role.equals("asha", true) &&
                                !item.isDeactivate &&
                                viewModel.isFromDisease != 0 &&
                                viewModel.diseaseType == IconDataset.Disease.KALA_AZAR.toString() -> {

                            findNavController().navigate(
                                HouseholdMembersFragmentDirections
                                    .actionHouseholdMembersFragmentToKalaAzarFormFragment(
                                        benId = benId
                                    )
                            )
                        }

                        else -> {
                            // No action required
                        }
                    }

                },
                clickedHusbandBen = {
                        item,hhId, benId, relToHeadId ->
                    if (!item.isDeactivate){
                        if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                            if (viewModel.isFromDisease == 0) {
                                findNavController().navigate(
                                    HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(
                                        hhId = hhId,
                                        benId = 0,
                                        gender = 1,
                                        selectedBenId = benId,
                                        isAddSpouse = 1,
                                        relToHeadId = HelperUtil.getMaleRelationId(relToHeadId)
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
                    }

                },
                clickedChildben = { item, hhId, benId, relToHeadId ->

                    if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                        if (!item.isDeactivate){
                            if (viewModel.isFromDisease == 0) {
                                findNavController().navigate(
                                    HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment(
                                        hhId = hhId,
                                        benId = 0,
                                        gender = 0,
                                        selectedBenId = benId,
                                        isAddSpouse = 0,
                                        relToHeadId = HelperUtil.getFemaleRelationId(relToHeadId)
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
                    }
                },
                {item, hhid->
                },
                { item,benId, hhId ->
                    if (!item.isDeactivate && prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                        checkAndGenerateABHA(benId)
                    }
                },
                { item,benId, hhId, isViewMode, isIFA ->
                },
                {
                    if(!it.isDeactivate){
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

                },{

                    viewLifecycleOwner.lifecycleScope.launch {
                        val isHof = viewModel.isHOF(it)
                        if (isHof) {
                            if (viewModel.canDeleteHoF(it.hhId)) {
                                showSoftDeleteDialog(it)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Head of Family cannot be deleted when other members exist.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            showSoftDeleteDialog(it)
                        }
                    }
                }
            ),
            showRegistrationDate = true,
            showSyncIcon = true,
            showAbha = showAbha,
            showCall = true,
            pref = prefDao,
            context = requireActivity(),
            isSoftDeleteEnabled = true
        )
        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.benListWithChildren.collect {
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