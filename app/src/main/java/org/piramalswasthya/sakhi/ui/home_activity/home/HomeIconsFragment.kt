package org.piramalswasthya.sakhi.ui.home_activity.home

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
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeIconsFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    private var _binding: RvIconGridBinding? = null
    private val binding: RvIconGridBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireActivity() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = RvIconGridBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHomeIconRvAdapter()

    }

    private fun setUpHomeIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(IconGridAdapter.GridIconClickListener { navDirections ->
            val navController = findNavController()

            try {
                val currentDestinationId = navController.currentDestination?.id

                // Skip navigation if already on destination
                if (currentDestinationId == R.id.villageLevelFormsFragment &&
                    navDirections == HomeFragmentDirections.actionNavHomeToVillageLevelFormsFragment()
                ) {
                    Timber.d("Already at destination: skipping navigation.")
                    return@GridIconClickListener
                }

                navController.navigate(navDirections)
            } catch (e: Exception) {
                Timber.e(e, "Navigation failed")
            }
           // findNavController().navigate(it)
        }, viewModel.scope)
        binding.rvIconGrid.adapter = rvAdapter
        viewModel.devModeEnabled.observe(viewLifecycleOwner) {
            Timber.d("update called!~~ $it")
            rvAdapter.submitList(iconDataset.getHomeIconDataset(resources))
        }

    }
}