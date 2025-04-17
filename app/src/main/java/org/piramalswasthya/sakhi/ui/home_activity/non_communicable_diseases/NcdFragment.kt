package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases

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
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NcdFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    companion object {
        fun newInstance() = NcdFragment()
    }

    private val viewModel: NcdViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })
    private val binding by lazy { RvIconGridBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpNcdCasesRvAdapter()
    }

    private fun setUpNcdCasesRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val iconAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                findNavController().navigate(it)
            },
            viewModel.scope
        )
        binding.rvIconGrid.adapter = iconAdapter
        iconAdapter.submitList(iconDataset.getNCDDataset(resources))
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__ncd,
                getString(R.string.icon_title_ncd)
            )
        }
    }

}