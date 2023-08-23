package org.piramalswasthya.sakhi.ui.home_activity.all_household

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
import org.piramalswasthya.sakhi.adapters.HouseHoldListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class AllHouseholdFragment : Fragment() {

    private var _binding : FragmentDisplaySearchRvButtonBinding? = null

    private val binding  : FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: AllHouseholdViewModel by viewModels()


    private var hasDraft = false

    private val draftLoadAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.incomplete_form_found))
            .setMessage(resources.getString(R.string.do_you_want_to_continue_with_previous_form_or_create_a_new_form_and_discard_the_previous_form))
            .setPositiveButton(resources.getString(R.string.open_draft)){
                    dialog,_->
                viewModel.navigateToNewHouseholdRegistration(false)
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.create_new)){
                    dialog,_->
                viewModel.navigateToNewHouseholdRegistration(true)
                dialog.dismiss()
            }
            .create()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater,container, false)
        viewModel.checkDraft()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        activity?.let{
            (it as HomeActivity).updateActionBar(R.drawable.ic__hh)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.text = resources.getString(R.string.btn_text_frag_home_nhhr)
        binding.tvEmptyContent.text = resources.getString(R.string.no_records_found_hh)
        val householdAdapter = HouseHoldListAdapter(HouseHoldListAdapter.HouseholdClickListener(
            {
            findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment(it))
        },
            {
            findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToHouseholdMembersFragment(it))
        },
            {
            findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewBenRegTypeFragment(it))
        }
        ))
        binding.rvAny.adapter = householdAdapter

        lifecycleScope.launch {
            viewModel.householdList.collect{
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                householdAdapter.submitList(it)
            }
        }


        viewModel.hasDraft.observe(viewLifecycleOwner){
            hasDraft = it
        }
        viewModel.navigateToNewHouseholdRegistration.observe(viewLifecycleOwner){
            if(it) {
                findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment())
                viewModel.navigateToNewHouseholdRegistrationCompleted()
            }
        }

        binding.btnNextPage.setOnClickListener {
            if (hasDraft)
                draftLoadAlert.show()
            else
                viewModel.navigateToNewHouseholdRegistration(false)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}