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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenChildImmunizationListAdapter
import org.piramalswasthya.sakhi.adapters.ImmunizationBirthDoseCategoryAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentChildImmunizationListBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject

@AndroidEntryPoint
class ChildImmunizationListFragment : Fragment(),ImmunizationBirthDoseCategoryAdapter.CategoryClickListener{

    private var _binding: FragmentChildImmunizationListBinding? = null
    private val binding: FragmentChildImmunizationListBinding
        get() = _binding!!


    private val viewModel: ChildImmunizationListViewModel by viewModels()

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        binding.searchView.setText(value)
        binding.searchView.setSelection(value.length)
        viewModel.setSearchText(value)
    }

    private val bottomSheet: ChildImmunizationVaccineBottomSheetFragment by lazy { ChildImmunizationVaccineBottomSheetFragment() }
    private val filterBottomSheet: ChildImmunizationFilterBottomSheetFragment by lazy { ChildImmunizationFilterBottomSheetFragment() }
    private var isBottomSheetShowing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildImmunizationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f is ChildImmunizationVaccineBottomSheetFragment) isBottomSheetShowing = false
            }
        }, false)

        binding.rvCat.adapter = ImmunizationBirthDoseCategoryAdapter(viewModel.categoryData(),this,viewModel)

        binding.rvList.adapter =
            BenChildImmunizationListAdapter(BenChildImmunizationListAdapter.VaccinesClickListener {
                if (!isBottomSheetShowing) {
                    isBottomSheetShowing = true
                    viewModel.updateBottomSheetData(it)
                    bottomSheet.show(childFragmentManager, "ImM")
                }
            })


        lifecycleScope.launch {
            viewModel.immunizationBenList.collect {
                if (it.isEmpty()){
                    binding.flEmpty.visibility = View.VISIBLE
                    binding.rvList.visibility = View.GONE
                } else{
                    binding.flEmpty.visibility = View.GONE
                    binding.rvList.visibility = View.VISIBLE
                    binding.rvList.apply {
                        (adapter as BenChildImmunizationListAdapter).submitList(it.sortedByDescending { it.ben.regDate })
                    }
                }

            }
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.setSearchText(p0?.toString() ?: "")
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

        binding.tvSelectedFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }

        viewModel.selectedFilter.observe(viewLifecycleOwner){
            if (it!=null){
                binding.tvSelectedFilter.text = it
            }
        }

        binding.cardDueThisMonth.setOnClickListener {
            viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.DUE_THIS_MONTH)
        }
        binding.cardNextMonth.setOnClickListener {
            viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.NEXT_MONTH)
        }

        lifecycleScope.launch {
            viewModel.dueThisMonthCount.collect { binding.tvDueThisMonthCount.text = "$it" }
        }
        lifecycleScope.launch {
            viewModel.nextMonthCount.collect { binding.tvNextMonthCount.text = "$it" }
        }
        lifecycleScope.launch {
            viewModel.resultMeta.collect { binding.tvResultMeta.text = it }
        }
        lifecycleScope.launch {
            viewModel.isFiltered.collect { filtered ->
                binding.tvEmptyContent.setText(
                    if (filtered) R.string.imm_no_match_for_filters
                    else R.string.no_records_found
                )
                renderQuickFilterCards()
            }
        }

        renderQuickFilterCards()
    }

    /**
     * Active card is filled with its accent, idle card is outlined in it - the styling the
     * FLW-1144 prototype specifies. Done in code because both states share one view.
     */
    private fun renderQuickFilterCards() {
        val selected = viewModel.selectedQuickFilter
        paintQuickFilterCard(
            card = binding.cardDueThisMonth,
            label = binding.tvDueThisMonthLabel,
            count = binding.tvDueThisMonthCount,
            accent = ContextCompat.getColor(requireContext(), R.color.imm_due_this_month_accent),
            active = selected == ChildImmunizationListViewModel.QuickFilter.DUE_THIS_MONTH,
        )
        paintQuickFilterCard(
            card = binding.cardNextMonth,
            label = binding.tvNextMonthLabel,
            count = binding.tvNextMonthCount,
            accent = ContextCompat.getColor(requireContext(), R.color.imm_next_month_accent),
            active = selected == ChildImmunizationListViewModel.QuickFilter.NEXT_MONTH,
        )
    }

    private fun paintQuickFilterCard(
        card: MaterialCardView,
        label: TextView,
        count: TextView,
        @ColorInt accent: Int,
        active: Boolean,
    ) {
        val idleBg = ContextCompat.getColor(requireContext(), R.color.imm_filter_card_idle_bg)
        val onAccent = ContextCompat.getColor(requireContext(), android.R.color.white)
        card.setCardBackgroundColor(if (active) accent else idleBg)
        card.strokeColor = accent
        card.isChecked = active
        val textColor = if (active) onAccent else accent
        label.setTextColor(textColor)
        count.setTextColor(textColor)
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__immunization,
                getString(R.string.child_immunization_list)
            )
        }
    }

    override fun onClicked(catDataList: String) {
        viewModel.setDoseStage(viewModel.toCategory(catDataList))
    }


}