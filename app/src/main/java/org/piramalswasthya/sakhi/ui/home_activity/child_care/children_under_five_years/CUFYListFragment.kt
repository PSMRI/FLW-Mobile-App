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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.CUFYAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.databinding.FragmentChildrenUnderFiveYearsListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class CUFYListFragment : Fragment() {

    private var _binding: FragmentChildrenUnderFiveYearsListBinding? = null

    private val binding: FragmentChildrenUnderFiveYearsListBinding
        get() = _binding!!

    private val viewModel: CUFYListViewModel by viewModels()
    private lateinit var benAdapter: CUFYAdapter

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
        _binding = FragmentChildrenUnderFiveYearsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        benAdapter = CUFYAdapter(
            CUFYAdapter.ChildListClickListener { benId, hhId,dob, type ->
                showOptionsBottomSheet(benId, hhId, dob,type)
            }
        )

        binding.rvAny.adapter = benAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.benList.collect { list ->
                binding.flEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.benListWithSamStatus.collect { benList ->
                    benAdapter.submitList(benList)
                }
            }
        }


        lifecycleScope.launch {
            viewModel.startSamStatusUpdates()
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
            if (b)
                (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else
                (searchView as EditText).removeTextChangedListener(searchTextWatcher)

        }

    }

    private fun showOptionsBottomSheet(benId: Long, hhId: Long, dob: Long, type : String) {
        val bottomSheet = CUFYBottomSheetFragment.newInstance(benId, hhId, dob,type)
        bottomSheet.show(childFragmentManager, bottomSheet.tag)

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