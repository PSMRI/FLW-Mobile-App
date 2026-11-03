package org.piramalswasthya.sakhi.ui.home_activity.disease_control.communicable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.communicable_diseases.CdViewModel
import org.piramalswasthya.sakhi.utils.safeNavigate
import javax.inject.Inject

/**
 * "Communicable Diseases" tile (General Check-up BRD FR1): Malaria, Kala Azar, AES/JE,
 * Filaria, Leprosy and TB, opening their existing modules unchanged.
 */
@AndroidEntryPoint
class CommunicableDiseasesGridFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    private val viewModel: CdViewModel by viewModels()
    private val binding by lazy { RvIconGridBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvIconGrid.layoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        val iconAdapter = IconGridAdapter(
            IconGridAdapter.GridIconClickListener {
                safeNavigate(it)
            },
            viewModel.scope
        )
        binding.rvIconGrid.adapter = iconAdapter
        iconAdapter.submitList(iconDataset.getCommunicableDiseasesGridDataset(resources))
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.covid__transmission_virus_human_infected,
                getString(R.string.icon_title_cd)
            )
        }
    }
}
