package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_non_eligible_list

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject

@AndroidEntryPoint
class NcdNonEligibleListFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private val binding: FragmentDisplaySearchRvButtonBinding by lazy {
        FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater)
    }
    private val viewModel: NcdNonEligibleListViewModel by viewModels()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNextPage.visibility = View.GONE
        val benAdapter = BenListAdapter(
            showBeneficiaries = true,
            pref = prefDao,
            context = requireActivity()
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
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic_ncd_noneligible,
                getString(R.string.ncd_non_eligible_list)
            )
        }
    }
}