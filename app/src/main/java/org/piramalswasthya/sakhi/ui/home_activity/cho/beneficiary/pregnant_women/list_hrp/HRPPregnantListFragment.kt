package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.list_hrp

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
import org.piramalswasthya.sakhi.adapters.HRPTListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class HRPPregnantListFragment : Fragment() {

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: HRPPregnantListViewModel by viewModels()

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val lowerValue = value.lowercase()
        binding.searchView.setText(lowerValue)
        binding.searchView.setSelection(lowerValue.length)
        viewModel.filterText(lowerValue)
    }

    private val bottomSheet: HRPPregnantTrackBottomSheet by lazy { HRPPregnantTrackBottomSheet() }
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
        val benAdapter = HRPTListAdapter(
            clickListener = HRPTListAdapter.HRPTClickListener(
                {
                },
                { _, benId ->
                    findNavController().navigate(
                        HRPPregnantListFragmentDirections.actionHRPPregnantListFragmentToHRPPregnantTrackFragment(
                            benId = benId,
                            trackId = 0
                        )
                    )
                },
                { _, benId ->
                    viewModel.benId = benId
                    if (!bottomSheet.isVisible)
                        bottomSheet.show(
                            childFragmentManager,
                            resources.getString(R.string.follow_up)
                        )
                }
            ),
            formButtonText = arrayOf(
                resources.getString(R.string.follow_up),
                resources.getString(R.string.history)
            )
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
                R.drawable.ic__high_risk_preg,
                getString(R.string.high_risk_list_pw)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}