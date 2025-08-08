package org.piramalswasthya.sakhi.ui.home_activity.asha_profile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.HouseHoldListAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentAshaProfileBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_household.AllHouseholdFragmentDirections
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class AshaProfileFragment : Fragment() {

    private var _binding: FragmentAshaProfileBinding? = null
    private val binding: FragmentAshaProfileBinding
        get() = _binding!!
    private val viewModel: AshaProfileViewModel by viewModels()

    private var latestTmpUri: Uri? = null

    @Inject
    lateinit var prefDao: PreferenceDao


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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAshaProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->

            notIt?.let { recordExists ->
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                binding.addHousehold.visibility = if (recordExists) View.VISIBLE else View.GONE
                binding.rvAny.visibility = if (recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    },
                    imageClickListener = FormInputAdapter.ImageClickListener {
                        viewModel.setCurrentImageFormId(it)
                        takeImage()
                    },
                     isEnabled = !recordExists,

                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())
                            adapter.submitList(it)

                    }
                }
            }
        }

        binding.addHousehold.setOnClickListener {
            findNavController().navigate(
                AshaProfileFragmentDirections.actionAshaProfileFragmentToNewHouseholdFragment(

                )
            )
        }

        val householdAdapter = HouseHoldListAdapter(false, prefDao, HouseHoldListAdapter.HouseholdClickListener({
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                findNavController().navigate(
                    AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment(
                        it
                    )
                )
            }
        }, {
//            val bundle = Bundle()
//            bundle.putLong("hhId", it)
//            bundle.putString("diseaseType", "No")
//            bundle.putInt("fromDisease", 0)
//            findNavController().navigate(R.id.householdMembersFragments, bundle)
            findNavController().navigate(
                AllHouseholdFragmentDirections.actionAllHouseholdFragmentToHouseholdMembersFragment(
                    it,0,"No"
                )
            )
        }, {
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                if (it.numMembers == 0) {
                    findNavController().navigate(
                        AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewBenRegFragment(
                            it.hhId,
                            18
                        )
                    )
                }
            }

        }))
        binding.rvAny.adapter = householdAdapter



        lifecycleScope.launch {
            viewModel.householdList.collect {
                householdAdapter.submitList(it)
            }
        }


        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
        binding.btnSubmit.setOnClickListener {
            if (validate()) viewModel.saveForm()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                AshaProfileViewModel.State.IDLE -> {
                }

                AshaProfileViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                AshaProfileViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save Successful", Toast.LENGTH_LONG).show()
                    viewModel.setRecordExist(true)
                }

                AshaProfileViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }
    }




    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)

                }

                4, 5 -> {
                    notifyDataSetChanged()
                }

            }
        }
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__menopause,
                getString(R.string.asha_profile)
            )
        }
    }


    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1)
            true
        else {
            if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
            false
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

}