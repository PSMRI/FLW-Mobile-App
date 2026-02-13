package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.InfantListAdapter
import org.piramalswasthya.sakhi.adapters.NcdReferListAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.InfantListFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.filter.NCDReferTypeFilter
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class NcdRefferedList : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    private val binding: FragmentDisplaySearchRvButtonBinding by lazy {
        FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater)
    }

    private val filterBottomSheet: NCDReferTypeFilter by lazy { NCDReferTypeFilter() }


    private val viewModel: NcdRefferedListViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNextPage.visibility = View.GONE
        binding.filterText.visibility = View.VISIBLE

        val benAdapter =
            NcdReferListAdapter(viewModel.userName, NcdReferListAdapter.NcdReferallickListener { benId ,hhId->
                findNavController().navigate(
                    NcdRefferedListDirections
                        .actionNcdRefferedListToNCDReferalFormFragment(
                            benId = benId,
                            hhId = hhId
                        )
                )
            },true)
        binding.rvAny.adapter = benAdapter



        binding.ivFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }

        binding.tvSelectedFilter.setOnClickListener {
            if (!filterBottomSheet.isVisible)
                filterBottomSheet.show(childFragmentManager, "ImM")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedFilter.collect { value ->
                    binding.tvSelectedFilter.text = value
                }
            }
        }


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
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__ncd_priority,
                    getString(R.string.ncd_refer_list)
                )
            } else {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__ncd_priority,
                    getString(R.string.ncd_refer_list)
                )
            }
        }
    }

}