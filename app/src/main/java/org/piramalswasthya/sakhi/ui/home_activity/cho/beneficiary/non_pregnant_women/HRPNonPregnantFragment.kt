package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women

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
import org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.EligibleCoupleViewModel
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeViewModel
import javax.inject.Inject
import org.piramalswasthya.sakhi.utils.safeNavigate

@AndroidEntryPoint
class HRPNonPregnantFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    companion object {
        fun newInstance() = HRPNonPregnantFragment()
    }

    private val viewModel: EligibleCoupleViewModel by viewModels()
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
        setupHRPNonPW()
    }

    private fun setupHRPNonPW() {
        val binding = _binding ?: return
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                safeNavigate(it)
            },
            viewLifecycleOwner.lifecycleScope
        )
        binding.rvIconGrid.adapter = rvAdapter
        rvAdapter.submitList(iconDataset.getHRPNonPregnantWomenDataset(resources))
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__high_risk_non_prg,
                getString(R.string.non_pregnant_women)
            )
        }
    }
}