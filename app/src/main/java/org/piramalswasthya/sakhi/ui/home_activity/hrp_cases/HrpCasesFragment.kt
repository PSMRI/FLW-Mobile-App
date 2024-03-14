package org.piramalswasthya.sakhi.ui.home_activity.hrp_cases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.RvIconGridHrpBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject

@AndroidEntryPoint
class HrpCasesFragment : Fragment() {
    @Inject
    lateinit var iconDataset: IconDataset

    companion object {
        fun newInstance() = HrpCasesFragment()
    }

    private val viewModel: HrpCasesViewModel by viewModels()
    private val binding by lazy { RvIconGridHrpBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHrpIconsDataset()
    }

    private fun getHrpIconsDataset() {
        val rvLayoutManager1 = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid1.layoutManager = rvLayoutManager1
        val rvAdapter1 = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                findNavController().navigate(it)
            },
            viewModel.scope
        )
        binding.rvIconGrid1.adapter = rvAdapter1
        rvAdapter1.submitList(iconDataset.getHRPPregnantWomenDataset(resources))

        val rvLayoutManager2 = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid2.layoutManager = rvLayoutManager2
        val rvAdapter2 = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                findNavController().navigate(it)
            },
            viewModel.scope
        )
        binding.rvIconGrid2.adapter = rvAdapter2
        rvAdapter2.submitList(iconDataset.getHRPNonPregnantWomenDataset(resources))

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hrp,
                getString(R.string.icon_title_hrp)
            )
        }
    }
}