package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.infant_reg.list

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.InfantRegistrationAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.EcFilterBottomSheetFragment

@AndroidEntryPoint
class InfantRegListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!
    private val viewModel: InfantRegListViewModel by viewModels()

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val lowerValue = value.lowercase()
        binding.searchView.setText(lowerValue)
        binding.searchView.setSelection(lowerValue.length)
        viewModel.filterText(lowerValue)
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
        binding.filterText.visibility = View.VISIBLE
        binding.tvSelectedFilter.text = getString(R.string.filter_newest_first)
        binding.ivFilter.setOnClickListener {
            EcFilterBottomSheetFragment(viewModel.getCurrentSort()) { selected ->
                viewModel.setSortFilter(selected)
                binding.tvSelectedFilter.text = when (selected) {
                    EcFilterType.NEWEST_FIRST   -> getString(R.string.filter_newest_first)
                    EcFilterType.OLDEST_FIRST   -> getString(R.string.filter_oldest_first)
                    EcFilterType.AGE_WISE       -> getString(R.string.filter_age_wise)
                    EcFilterType.SYNCING_FIRST  -> getString(R.string.filter_syncing_first)
                    EcFilterType.UNSYNCED_FIRST -> getString(R.string.filter_unsynced_first)
                }
            }.show(childFragmentManager, "ECFilter")
        }
        val benAdapter = InfantRegistrationAdapter(
            InfantRegistrationAdapter.ClickListener { benId, babyIndex ->
                findNavController().navigate(
                    InfantRegListFragmentDirections.actionInfantRegListFragmentToInfantRegFragment(
                        benId,
                        babyIndex
                    )
                )
            }
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
    }

    override fun onStart() {
        super.onStart()
        val title = if (viewModel.onlyLowBirthWeight)
            getString(R.string.low_birth_weight_babies)
        else
            getString(R.string.infant_reg_list)
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__infant_registration,
                title
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}