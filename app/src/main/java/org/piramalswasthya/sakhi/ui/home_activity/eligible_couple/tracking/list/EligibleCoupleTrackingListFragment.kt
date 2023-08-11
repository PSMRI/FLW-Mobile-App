package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.ECTrackingListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding

@AndroidEntryPoint
class EligibleCoupleTrackingListFragment : Fragment() {

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: EligibleCoupleTrackingListViewModel by viewModels()

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
        val benAdapter = ECTrackingListAdapter(
            ECTrackingListAdapter.ECTrackListClickListener(addNewTrack = {
                findNavController().navigate(
                    EligibleCoupleTrackingListFragmentDirections.actionEligibleCoupleTrackingListFragmentToEligibleCoupleTrackingFormFragment(
                        it
                    )
                )
            }, showAllTracks = {
                Toast.makeText(context, " Yet to impleteme.t", Toast.LENGTH_SHORT).show()
            })
//            BenListAdapterForForm.ClickListener(
//                {
//                    Toast.makeText(context, "Ben : $it clicked", Toast.LENGTH_SHORT).show()
//                },
//                { hhId, benId ->
//                    findNavController().navigate(
//                        EligibleCoupleTrackingListFragmentDirections.actionEligibleCoupleTrackingListFragmentToEligibleCoupleTrackingFormFragment(
//                            benId
//                        )
//                    )
//                }), "Track"
        )
        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.benList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
//                viewModel.updateFilledStatus(it)
                benAdapter.submitList(it)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}