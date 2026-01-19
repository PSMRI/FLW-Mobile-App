package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_confirmed.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.TbConfirmedListAdapter
import org.piramalswasthya.sakhi.adapters.TbScreeningListAdapter
import org.piramalswasthya.sakhi.adapters.TbSuspectedListAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_confirmed.from.TBConfirmedViewModel
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.list.TBScreeningListFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_suspected.list.TBSuspectedListViewModel
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class TBConfirmedListFragment : Fragment() {


    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: TBConfirmedListViewModel by viewModels()

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
        val benAdapter = TbConfirmedListAdapter(
            TbConfirmedListAdapter.ClickListener { hhId, benId ->
                findNavController().navigate(
                    TBConfirmedListFragmentDirections.actionTBConfirmedListFragmentToTBConfirmedFormFragment(
                    benId
                    )
                )

            },
            pref = prefDao
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
                    R.drawable.ic__ncd,
                    getString(R.string.tb_confirmed_list)
                )
            } else {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__ncd,
                    getString(R.string.tb_confirmed_list)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
    override fun onDestroy() {
        super.onDestroy()
    }
}