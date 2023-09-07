package org.piramalswasthya.sakhi.ui.home_activity.all_ben

import android.app.AlertDialog
import android.content.Intent
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
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_household.AllHouseholdFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel

@AndroidEntryPoint
class AllBenFragment : Fragment() {

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null

    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!


    private val viewModel: AllBenViewModel by viewModels()

    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE
        val benAdapter = BenListAdapter(
            clickListener = BenListAdapter.BenClickListener(
                { hhId, benId, isKid ->

                    findNavController().navigate(
                        AllBenFragmentDirections.actionAllBenFragmentToNewBenRegFragment(
                            hhId = hhId,
                            benId = benId,
                            relToHeadId = -1,
                            gender = 0

                        )
                    )

//                    findNavController().navigate(
//                        if (isKid) AllBenFragmentDirections.actionAllBenFragmentToNewBenRegL15Fragment(
//                            hhId,
//                            benId
//                        )
//                        else
//                            AllBenFragmentDirections.actionAllBenFragmentToNewBenRegG15Fragment(
//                                hhId,
//                                benId
//                            )
//                    )
                },
                {
                    findNavController().navigate(
                        AllBenFragmentDirections.actionAllBenFragmentToNewBenRegTypeFragment(
                            it
                        )
                    )
                },
                { benId, hhId ->
                    checkAndGenerateABHA(benId)
                },

                ), showAbha = true
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

        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment())
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

        viewModel.abha.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    abhaDisclaimer.setMessage(it)
                    abhaDisclaimer.show()
                }
            }
        }

        viewModel.benRegId.observe(viewLifecycleOwner) {
            if (it != null) {
                val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("benId", viewModel.benId.value)
                intent.putExtra("benRegId", it)
                requireActivity().startActivity(intent)
                viewModel.resetBenRegId()
            }

        }
    }

    private fun checkAndGenerateABHA(benId: Long) {
        viewModel.fetchAbha(benId)
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(R.drawable.ic__ben)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}