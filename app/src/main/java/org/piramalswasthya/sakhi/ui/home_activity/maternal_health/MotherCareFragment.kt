package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import javax.inject.Inject
import org.piramalswasthya.sakhi.utils.safeNavigate

@AndroidEntryPoint
class MotherCareFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    companion object {
        fun newInstance() = MotherCareFragment()
    }

    private val viewModel: MotherCareViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels({ requireActivity() })
    private var _binding: RvIconGridBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = RvIconGridBinding.inflate(layoutInflater)
        _binding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Use the ViewModel
        setUpMotherCareIconRvAdapter()
    }

    private fun setUpMotherCareIconRvAdapter() {
        val binding = _binding ?: return
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(
            //IconDataset.getMotherCareDataset(),
            IconGridAdapter.GridIconClickListener {
                safeNavigate(it)
            },
            viewLifecycleOwner.lifecycleScope
        )
        binding.rvIconGrid.adapter = rvAdapter
        rvAdapter.submitList(iconDataset.getMotherCareDataset(resources))

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__maternal_health,
                getString(R.string.icon_title_mc)
            )
        }
    }

}