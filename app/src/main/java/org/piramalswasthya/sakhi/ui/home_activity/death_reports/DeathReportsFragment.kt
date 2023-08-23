package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentDeathReportsBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class DeathReportsFragment : Fragment() {

    private val binding: FragmentDeathReportsBinding by lazy {
        FragmentDeathReportsBinding.inflate(layoutInflater)
    }

    private val viewModel: DeathReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToCdrList.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    DeathReportsFragmentDirections.actionDeathReportsFragmentToCdrListFragment()
                )
                viewModel.navigateToCdrListCompleted()
            }
        }
        viewModel.navigateToMdsrList.observe(viewLifecycleOwner){
            if(it) {
                findNavController().navigate(
                    DeathReportsFragmentDirections.actionDeathReportsFragmentToMdsrListFragment()
                )
                viewModel.navigateToMdsrListCompleted()
            }
        }

        binding.btnContinue.setOnClickListener {
            when (binding.rgDeathType.checkedRadioButtonId) {
                binding.rbCdr.id -> {
                    viewModel.navigateToDeathReportList(isChild = true)
                }
                binding.rbMdsr.id -> {
                    viewModel.navigateToDeathReportList(isChild = false)
                }
                else -> Toast.makeText(context, resources.getString(R.string.please_select_type_of_beneficiary), Toast.LENGTH_SHORT).show()
            }

        }
    }
    override fun onStart() {
        super.onStart()
        activity?.let{
            (it as HomeActivity).updateActionBar(R.drawable.ic__death)
        }
    }

}