package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.vhnc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.VHNCAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class VHNCListFragement : Fragment() {
    companion object {
        fun newInstance() = VHNCListFragement()
    }

    private val viewModel: VHNCViewModel by viewModels()

    private var _binding: FragmentVhndListBinding? = null
    private val binding: FragmentVhndListBinding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVhndListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.VISIBLE
        val vHNCAdapter = VHNCAdapter(
        clickListener = VHNCAdapter.VHNCClickListener { id ->
            findNavController().navigate(
                VHNCListFragementDirections.actionVHNCListFragementToVHNCFormFragement(
                    id
                )
            )
        })
        binding.rvAny.adapter = vHNCAdapter
        binding.btnNextPage.text=getString(R.string.icon_title_vhnc)
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_VHNCListFragement_to_VHNCFormFragement)
        }
        lifecycleScope.launch {
            viewModel.allVHNCList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                vHNCAdapter.submitList(it)
            }
        }


    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.icon_title_vhnc_list)
            )
        }
    }
}