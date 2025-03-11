package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.InfantListAdapter
import org.piramalswasthya.sakhi.databinding.AlertFilterBinding
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchAndToggleRvButtonBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class InfantListFragment : Fragment() {

    private var _binding: FragmentDisplaySearchAndToggleRvButtonBinding? = null
    private val binding: FragmentDisplaySearchAndToggleRvButtonBinding
        get() = _binding!!

    private val viewModel: InfantListViewModel by viewModels()

    private var showRchRecords = false
    private lateinit var benAdapter: InfantListAdapter

    private val filterAlert by lazy {
        val filterAlertBinding = AlertFilterBinding.inflate(layoutInflater, binding.root, false)
        filterAlertBinding.rgAbha.setOnCheckedChangeListener { radioGroup, i ->

        }

        filterAlertBinding.cbRch.setOnCheckedChangeListener { compoundButton, b ->
            showRchRecords = b
        }

        filterAlertBinding.tvAbha.visibility = View.GONE
        filterAlertBinding.rgAbha.visibility = View.GONE

        val alert = MaterialAlertDialogBuilder(requireContext()).setView(filterAlertBinding.root)
            .setOnCancelListener {
            }.create()

        filterAlertBinding.btnOk.setOnClickListener {
            viewModel.filterType(showRchRecords.toString())
            alert.cancel()
        }
        filterAlertBinding.btnCancel.setOnClickListener {
            alert.cancel()
        }

        alert
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchAndToggleRvButtonBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE

        binding.ibFilter.setOnClickListener {
            filterAlert.show()
        }

        benAdapter = InfantListAdapter(
            InfantListAdapter.InfantListClickListener { benId, hhId ->
                findNavController().navigate(
                    InfantListFragmentDirections.actionInfantListFragmentToHbncDayListFragment(
                        benId = benId,
                        hhId = hhId
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
                R.drawable.ic__infant,
                getString(R.string.child_care_icon_title_infant_list)
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}