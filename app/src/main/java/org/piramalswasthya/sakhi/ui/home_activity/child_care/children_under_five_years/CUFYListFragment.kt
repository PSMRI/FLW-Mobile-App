package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years

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
import org.piramalswasthya.sakhi.adapters.ChildrenUnderFiveYearsAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentChildrenUnderFiveYearsListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYFormSyncWorker

@AndroidEntryPoint
class CUFYListFragment : Fragment() {

    private var _binding: FragmentChildrenUnderFiveYearsListBinding? = null

    private val binding: FragmentChildrenUnderFiveYearsListBinding
        get() = _binding!!

    private val viewModel: CUFYListViewModel by viewModels()
    private lateinit var benAdapter: ChildrenUnderFiveYearsAdapter

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        binding.searchView.setText(value)
        binding.searchView.setSelection(value.length)
        viewModel.filterText(value)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildrenUnderFiveYearsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CUFYFormSyncWorker.enqueue(requireContext())
        benAdapter = ChildrenUnderFiveYearsAdapter(
            ChildrenUnderFiveYearsAdapter.ChildListClickListener { benId, hhId, type ->
                findNavController().navigate(
                    CUFYListFragmentDirections.actionChildrenUnderFiveYearListFragmentToChildrenUnderFiveYearFormFragment(
                        benId,
                        hhId,
                        type
                    )
                )
            }
        )
        binding.rvAny.adapter = benAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.benList.collect { list ->
                binding.flEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                benAdapter.submitList(list)
            }
        }

        binding.ibSearch.visibility = View.VISIBLE
        binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Need to implementation when it's need
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Need to implementation when it's need
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
                R.drawable.ic__adolescent,
                getString(R.string.children_under_five_years)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}