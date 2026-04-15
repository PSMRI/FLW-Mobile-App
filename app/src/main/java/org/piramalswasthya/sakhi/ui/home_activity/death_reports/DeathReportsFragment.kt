package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.FragmentDeathReportsBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject

@AndroidEntryPoint
class DeathReportsFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    private val viewModel: DeathReportsViewModel by viewModels()

    private var _binding: FragmentDeathReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeathReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpDeathReportBindings()
    }

    private fun setUpDeathReportBindings() {

        val icons = iconDataset.getDeathReportDataset(resources)


        if (icons.size >= 4) {
            binding.generalIcon = icons[0]
            binding.maternalIcon = icons[1]
            binding.nonMaternalIcon = icons[2]
            binding.childIcon = icons[3]
        }

        binding.clickListener = IconGridAdapter.GridIconClickListener {
            findNavController().navigate(it)
        }


        binding.scope = viewModel.scope
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__death,
                getString(R.string.icon_title_dr)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
