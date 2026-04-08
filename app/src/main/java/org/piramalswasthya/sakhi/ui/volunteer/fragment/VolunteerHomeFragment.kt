package org.piramalswasthya.sakhi.ui.volunteer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.FragmentVolunteerHomeBinding
import org.piramalswasthya.sakhi.ui.volunteer.VolunteerActivity
import javax.inject.Inject

@AndroidEntryPoint
class VolunteerHomeFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    private var _binding: FragmentVolunteerHomeBinding? = null
    private val binding: FragmentVolunteerHomeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolunteerHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpIconRvAdapter()
        binding.fabAddBen.setOnClickListener {
            findNavController().navigate(
                VolunteerHomeFragmentDirections
                    .actionVolunteerHomeFragmentToNewBenRegFragment(
                        hhId = 0L,
                        relToHeadId = 18,
                        gender = 0
                    )
            )
        }
    }

    private fun setUpIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager

        val iconAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                findNavController().navigate(it)
            },
            viewLifecycleOwner.lifecycleScope
        )
        binding.rvIconGrid.adapter = iconAdapter
        iconAdapter.submitList(iconDataset.getVolunteerIconDataset(resources))
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as VolunteerActivity).updateActionBar(
                R.drawable.ic_home,
                getString(R.string.home)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}