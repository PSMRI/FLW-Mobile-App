package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenChildImmunizationListAdapter
import org.piramalswasthya.sakhi.adapters.ImmunizationBenListAdapter
//import org.piramalswasthya.sakhi.adapters.ImmunizationBenListAdapter
import org.piramalswasthya.sakhi.adapters.ImmunizationBirthDoseCategoryAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentChildImmunizationListBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ChildImmunizationListFragment : Fragment(),ImmunizationBirthDoseCategoryAdapter.CategoryClickListener {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentChildImmunizationListBinding? = null
    private val binding: FragmentChildImmunizationListBinding
        get() = _binding!!


    private val viewModel: ChildImmunizationListViewModel by viewModels()
    private var catTxt = ""

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        binding.searchView.setText(value)
        binding.searchView.setSelection(value.length)
        viewModel.filterText(value)
    }

    private val bottomSheet: ChildImmunizationVaccineBottomSheetFragment by lazy { ChildImmunizationVaccineBottomSheetFragment() }
    private val filterBottomSheet: ChildImmunizationFilterBottomSheetFragment by lazy { ChildImmunizationFilterBottomSheetFragment() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildImmunizationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCat.adapter = ImmunizationBirthDoseCategoryAdapter(viewModel.categoryData(),this,viewModel)

        binding.rvList.adapter =
            BenChildImmunizationListAdapter(BenChildImmunizationListAdapter.VaccinesClickListener {
                viewModel.updateBottomSheetData(it)
                if (!bottomSheet.isVisible)
                    bottomSheet.show(childFragmentManager, "ImM")
            })

//        ImmunizationBenListAdapter(
//            ImmunizationBenListAdapter.VaccinesClickListener(clickedVaccine = {
//                viewModel.updateBottomSheetData(it)
//                if (!bottomSheet.isVisible)
//                    bottomSheet.show(childFragmentManager, "ImM")
//            }, callBen = {
//                try {
//                    val callIntent = Intent(Intent.ACTION_CALL)
//                    callIntent.setData(Uri.parse("tel:${it.mobileNo}"))
//                    startActivity(callIntent)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    activity?.let {
//                        (it as HomeActivity).askForPermissions()
//                    }
//                    Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
//                }
//            })
//        )


        lifecycleScope.launch {
            viewModel.immunizationBenList.collect {
               // Timber.d("Collecting list : $it")

                binding.rvList.apply {
                    (adapter as BenChildImmunizationListAdapter).submitList(it.sortedByDescending { it.ben.regDate })
                }
            }
        }

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

        binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }

        binding.ivFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as SupervisorActivity).updateActionBar(
                R.drawable.ic__immunization,
                getString(R.string.child_immunization_list)
            )
//            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
//                (it as HomeActivity).updateActionBar(
//                    R.drawable.ic__immunization,
//                    getString(R.string.child_immunization_list)
//                )
//            } else {
//                (it as SupervisorActivity).updateActionBar(
//                    R.drawable.ic__immunization,
//                    getString(R.string.child_immunization_list)
//                )
//            }
        }
    }

    override fun onClicked(catDataList: String) {

        if (catDataList.contains("ALL")) {
            viewModel.filterText("")
        }
        else {
            catTxt = catDataList
            viewModel.filterText(catTxt)

        }
    }


}