    package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.annotation.RequiresApi
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.GridLayoutManager
    import dagger.hilt.android.AndroidEntryPoint
    import org.piramalswasthya.sakhi.R
    import org.piramalswasthya.sakhi.adapters.IconGridAdapter
    import org.piramalswasthya.sakhi.configuration.IconDataset
    import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
    import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
    import javax.inject.Inject

    @AndroidEntryPoint
    class VillageLevelFormsFragment : Fragment() {

        companion object {
            fun newInstance() = VillageLevelFormsFragment()
        }
        @Inject
        lateinit var iconDataset: IconDataset
        private val viewModel: VillageLevelFormsViewModel by viewModels()
        private val binding by lazy { RvIconGridBinding.inflate(layoutInflater) }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return binding.root
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setUpVillageLevelFormsIconRvAdapter()
            viewModel.loadIcons(resources)
            collectIconOverdueStatus()

        }


        private fun setUpVillageLevelFormsIconRvAdapter() {
            val rvLayoutManager = GridLayoutManager(
                context,
                requireContext().resources.getInteger(R.integer.icon_grid_span)
            )
            binding.rvIconGrid.layoutManager = rvLayoutManager


            val rvAdapter1 = IconGridAdapter(
                IconGridAdapter.GridIconClickListener {
                    findNavController().navigate(it)
                },
                viewModel.scope
            )
            binding.rvIconGrid.adapter = rvAdapter1
            rvAdapter1.submitList(iconDataset.getVLFDataset(resources))




        }
        private fun collectIconOverdueStatus() {
            // Collect overdue status from ViewModel and update UI
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.iconsWithRedFlags.collect { iconsWithOverdueStatus ->
                    // Update icons based on overdue status
                    val updatedIcons = iconsWithOverdueStatus.map { (icon, isOverdue) ->
//                        Log.d("OverdueCheck", "Is overdue: $isOverdue")

                        icon.copy(
//                            colorPrimary = !isOverdue, // Update colorPrimary based on overdue status
                            allowRedBorder = isOverdue // Allow red border if overdue
                        )

                    }

                    // Update RecyclerView with the updated icons list
                    (binding.rvIconGrid.adapter as? IconGridAdapter)?.submitList(updatedIcons)

                }
            }
        }

        override fun onStart() {
            super.onStart()
            activity?.let {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__village_level_form,
                    getString(R.string.icon_title_vlf)
                )
            }
        }
    }