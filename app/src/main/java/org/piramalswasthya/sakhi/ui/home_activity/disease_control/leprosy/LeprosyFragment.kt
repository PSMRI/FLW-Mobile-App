package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy

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
import javax.inject.Inject
import kotlin.getValue
import org.piramalswasthya.sakhi.utils.safeNavigate

@AndroidEntryPoint
class LeprosyFragment: Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    companion object {
        fun newInstance() = LeprosyFragment()

    }

    private val viewModel: LeprosyViewModel by viewModels()

    private var _binding: RvIconGridBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return RvIconGridBinding.inflate(layoutInflater).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLeprosyIconRvAdapter()
    }

    private fun setUpLeprosyIconRvAdapter() {
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
        rvAdapter.submitList(iconDataset.getLeprosyDataset(resources))

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hh,
                getString(R.string.icon_title_leprosy)
            )
        }
    }

}