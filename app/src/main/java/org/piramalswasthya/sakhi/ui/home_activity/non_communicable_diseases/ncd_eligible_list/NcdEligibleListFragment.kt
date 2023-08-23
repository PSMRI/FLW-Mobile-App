package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

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
import org.piramalswasthya.sakhi.adapters.NcdCbacBenListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber

@AndroidEntryPoint
class NcdEligibleListFragment : Fragment() {


    private val binding: FragmentDisplaySearchRvButtonBinding by lazy {
        FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater)
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

        val benAdapter =
            NcdCbacBenListAdapter(
                clickListener = NcdCbacBenListAdapter.CbacFormClickListener(
                    clickedView = {
                        Timber.d("ClickListener Triggered!")
                        viewModel.setSelectedBenId(it)
                        if (!bottomSheet.isVisible)
                            bottomSheet.show(childFragmentManager, resources.getString(R.string.cbac))
                    },
                    clickedNew = {
                        findNavController().navigate(
                            NcdEligibleListFragmentDirections.actionNcdEligibleListFragmentToCbacFragment(
                                benId = it,
                                ashaId = viewModel.getAshaId()
                            )
                        )
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
            (it as HomeActivity).updateActionBar(R.drawable.ic__ncd_list)
        }
    }

}