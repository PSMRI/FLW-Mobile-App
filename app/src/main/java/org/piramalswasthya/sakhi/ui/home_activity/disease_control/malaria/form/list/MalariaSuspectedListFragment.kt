package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.MalariaMemberListAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity


@AndroidEntryPoint
class MalariaSuspectedListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: MalariaSuspectedViewModel by viewModels()

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
        binding.llSearch.visibility = View.GONE
        if (viewModel.isFromDisease == 1 && viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()) {
            binding.switchButton.visibility = View.VISIBLE
        } else if(viewModel.isFromDisease == 1) {
            binding.switchButton.visibility = View.GONE
        } else {
            binding.switchButton.visibility = View.GONE
        }
        binding.switchButton.text = if (binding.switchButton.isChecked) "ON" else "OFF"
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            binding.switchButton.text = if (isChecked) "ON" else "OFF"
        }

        val benAdapter = MalariaMemberListAdapter(
            clickListener = MalariaMemberListAdapter.ClickListener { hhId, benId ->
                findNavController().navigate(
                    MalariaSuspectedListFragmentDirections.actionMalariaSuspectedListFragmentToMalariaFormFragment(
                        benId = benId
                    )
                )
            },
        )
        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.allBenList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }


    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hh,
                getString(R.string.icon_title_maleria)
            )
        }
    }





}