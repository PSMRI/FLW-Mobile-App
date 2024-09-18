package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.NCDCategoryAdapter
import org.piramalswasthya.sakhi.adapters.NcdCbacBenListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNcdEligibleListBinding
import org.piramalswasthya.sakhi.model.getDateStrFromLong
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber

@AndroidEntryPoint
class NcdEligibleListFragment : Fragment() , NCDCategoryAdapter.ClickListener {



    private val binding: FragmentNcdEligibleListBinding by lazy {
        FragmentNcdEligibleListBinding.inflate(layoutInflater)
    }

    private val viewModel: NcdEligibleListViewModel by viewModels()

    private val bottomSheet: NcdBottomSheetFragment by lazy { NcdBottomSheetFragment() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNextPage.visibility = View.GONE


        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, viewModel.yearsList())
        binding.tilRvDropdown.setAdapter(adapter)

        binding.rvCat.adapter = NCDCategoryAdapter(viewModel.categoryData(),this,viewModel)

        val benAdapter =
            NcdCbacBenListAdapter(
                clickListener = NcdCbacBenListAdapter.CbacFormClickListener(
                    clickedView = {
                        Timber.d("ClickListener Triggered!")
                        viewModel.setSelectedBenId(it)
                        if (!bottomSheet.isVisible)
                            bottomSheet.show(
                                childFragmentManager,
                                resources.getString(R.string.cbac)
                            )
                    },
                    clickedNew = { benId, nextFillDateMillis ->
                        if (nextFillDateMillis != null) {
                            Toast.makeText(
                                context,
                                "Available on ${getDateStrFromLong(nextFillDateMillis)}",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            findNavController().navigate(
                                NcdEligibleListFragmentDirections.actionNcdEligibleListFragmentToCbacFragment(
                                    benId = benId,
                                    ashaId = viewModel.getAshaId()
                                )
                            )
                        }

                    })
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
        lifecycleScope.launch {
            viewModel.ncdDetails.collect {
                Timber.d("Collecting Ncd Details : $it")
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
        binding.tilRvDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem != "Select Years") {
                    viewModel.filterText(selectedItem)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Timber.d("Called here!")
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
                R.drawable.ic__ncd_list,
                getString(R.string.ncd_eligible_list)
            )
        }
    }

    override fun onClicked(catDataList: String) {
        viewModel.selectedText = catDataList
    }

}