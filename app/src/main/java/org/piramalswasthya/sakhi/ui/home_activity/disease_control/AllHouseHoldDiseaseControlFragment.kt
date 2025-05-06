package org.piramalswasthya.sakhi.ui.home_activity.disease_control

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.HouseHoldListAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentAllHouseHoldDiseaseControlBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity


@AndroidEntryPoint
class AllHouseHoldDiseaseControlFragment : Fragment() {

    private var _binding: FragmentAllHouseHoldDiseaseControlBinding? = null

    private val binding: FragmentAllHouseHoldDiseaseControlBinding
        get() = _binding!!


    private val viewModel: AllHouseHoldDiseaseControlViewModel by viewModels()

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        binding.searchView.setText(value)
        binding.searchView.setSelection(value.length)
        viewModel.filterText(value)
    }

    var isDisease = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllHouseHoldDiseaseControlBinding.inflate(layoutInflater, container, false)
        viewModel.checkDraft()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val householdAdapter = HouseHoldListAdapter(isDisease,HouseHoldListAdapter.HouseholdClickListener({

        }, {
            if(viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()){
                findNavController().navigate(
                    AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToMalariaSuspectedListFragment(
                        it,1, viewModel.diseaseType
                    )
                )
            } else if (viewModel.diseaseType == IconDataset.Disease.AES_JE.toString()){
                findNavController().navigate(
                    AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToAESSuspectedListFragment(
                        it,3, viewModel.diseaseType
                    )
                )
            }
            else if (viewModel.diseaseType == IconDataset.Disease.KALA_AZAR.toString()){
                findNavController().navigate(
                    AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToKalaAzarSuspectedListFragment(
                        it,2, viewModel.diseaseType
                    )
                )
            }
            else if (viewModel.diseaseType == IconDataset.Disease.FILARIA.toString()){
                findNavController().navigate(
                    AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToFilariaSuspectedListFragment(
                        it,4, viewModel.diseaseType
                    )
                )
            }
            else if (viewModel.diseaseType == IconDataset.Disease.LEPROSY.toString()){
                findNavController().navigate(
                    AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToLeprosySuspectedListFragment(
                        it,5, viewModel.diseaseType
                    )
                )
            }

        }, {

        }))
        binding.rvAny.adapter = householdAdapter

        lifecycleScope.launch {
            viewModel.householdList.collect {
                if (it.isEmpty()) binding.flEmpty.visibility = View.VISIBLE
                else binding.flEmpty.visibility = View.GONE
                householdAdapter.submitList(it)
            }
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
            if (b) (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else (searchView as EditText).removeTextChangedListener(searchTextWatcher)

        }
    }
    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                viewModel.diseaseType
            )
        }
    }
}