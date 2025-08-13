package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import org.piramalswasthya.sakhi.adapters.AncAbortionListAdapter
import org.piramalswasthya.sakhi.adapters.AncVisitListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class AbortionListFragment : Fragment() {

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: AbortionListViewModel by viewModels()


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
        val benAdapter = AncAbortionListAdapter(
            AncAbortionListAdapter.AbortionListClickListener(showVisits = {
                viewModel.updateSelectedBenId(it)

            },
                addVisit = { benId, visitNumber ->
//                    findNavController().navigate(
//                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(
//                            benId, visitNumber
//                        )
//                    )
                },
//                pmsma = { benId, hhId ->
//                    findNavController().navigate(
//                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
//                            benId, hhId
//                        )
//                    )
//                }
            )
        )
        binding.rvAny.adapter = benAdapter
        lifecycleScope.launch {
            viewModel.allAbortionList.collect {

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
                viewModel.setSearchQuery(p0?.toString() ?: "")
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
                R.drawable.ic__anc_visit,
                getString(R.string.icon_title_pmt)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}