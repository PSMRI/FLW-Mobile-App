package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.vulnerable_population.list

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
import org.piramalswasthya.sakhi.adapters.VulnerableListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class VulnerablePopulationListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    @Inject
    lateinit var prefDao: PreferenceDao

    private val viewModel: VulnerablePopulationViewModel by viewModels()

    private val filterBottomSheet: VulnerableFilterBottomSheet by lazy { VulnerableFilterBottomSheet() }

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
        val benAdapter = VulnerableListAdapter(
            VulnerableListAdapter.ClickListener { hhId, benId ->
                if (findNavController().currentDestination?.id == R.id.vulnerablePopulationListFragment) {
                    findNavController().navigate(
                        VulnerablePopulationListFragmentDirections.actionVulnerablePopulationListFragmentToTBScreeningFormFragment(
                            benId = benId
                        )
                    )
                }
            },
            viewModel
        )
        binding.rvAny.adapter = benAdapter

        viewLifecycleOwner.lifecycleScope.launch {
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

        binding.ivFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }

        binding.ivFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }

        binding.tvSelectedFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }

        viewModel.selectedFilter.observe(viewLifecycleOwner){
            if (it!=null){
                binding.tvSelectedFilter.text = it
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
                R.drawable.ic_crash,
                getString(R.string.icon_title_ncd_vulnerable_treatment)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



}