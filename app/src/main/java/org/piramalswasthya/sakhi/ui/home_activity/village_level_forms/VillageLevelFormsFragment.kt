package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

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
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class VillageLevelFormsFragment : Fragment() {

    companion object {
        fun newInstance() = VillageLevelFormsFragment()
    }

    private val viewModel: VillageLevelFormsViewModel by viewModels()
    private val binding by lazy { RvIconGridBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Use the ViewModel
        setUpVillageLevelFormsIconRvAdapter()
    }

    private fun setUpVillageLevelFormsIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(context, requireContext().resources.getInteger(R.integer.icon_grid_span))
        binding.rvIconGrid.layoutManager = rvLayoutManager
        binding.rvIconGrid.adapter = IconGridAdapter(
            //IconDataset.getVillageLevelFormsDataset(),
            IconGridAdapter.GridIconClickListener {
                findNavController().navigate(it)
            })
    }
    override fun onStart() {
        super.onStart()
        activity?.let{
            (it as HomeActivity).setLogo(R.drawable.ic__village_level_form)
        }
    }
}