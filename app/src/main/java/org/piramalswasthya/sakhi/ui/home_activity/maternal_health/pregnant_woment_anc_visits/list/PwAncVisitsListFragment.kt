package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.AncVisitListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class PwAncVisitsListFragment : Fragment() {

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    val args: PwAncVisitsListFragmentArgs by lazy {
        PwAncVisitsListFragmentArgs.fromBundle(requireArguments())
    }

    private val viewModel: PwAncVisitsListViewModel by viewModels()

    private val bottomSheet: AncBottomSheetFragment by lazy { AncBottomSheetFragment() }

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
        val benAdapter = AncVisitListAdapter(
            AncVisitListAdapter.PregnancyVisitClickListener(showVisits = {
                viewModel.updateBottomSheetData(it)
                if (!bottomSheet.isVisible)
                    bottomSheet.show(childFragmentManager, "ANC")
            },
                addVisit = { benId, visitNumber ->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(
                            benId, visitNumber
                        )
                    )
                },
                pmsma = { benId, hhId ->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
                            benId, hhId
                        )
                    )
                },
                callBen = {
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.setData(Uri.parse("tel:${it.ben.mobileNo}"))
                        startActivity(callIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        activity?.let {
                            (it as HomeActivity).askForPermissions()
                        }
                        Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
                    }
                }), true
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