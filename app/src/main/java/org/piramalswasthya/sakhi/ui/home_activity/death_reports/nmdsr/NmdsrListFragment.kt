package org.piramalswasthya.sakhi.ui.home_activity.death_reports.nmdsr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentNmdsrListBinding
import org.piramalswasthya.sakhi.ui.home_activity.death_reports.BaseListFragment
import org.piramalswasthya.sakhi.ui.home_activity.death_reports.mdsr.MdsrListFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class NmdsrListFragment : BaseListFragment<FragmentNmdsrListBinding>() {


    @Inject
    override lateinit var prefDao: PreferenceDao
    override val viewModel: NmdsrListViewModel by viewModels()
    override val iconResId = R.drawable.ic__death
    override val titleResId = R.string.non_maternal_deaths
    override val isGeneralForm = true

    override val layoutInflaterBinding: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNmdsrListBinding
        get() = FragmentNmdsrListBinding::inflate
    override val recyclerView get() = binding.rvAny
    override val emptyStateView get() = binding.flEmpty
    override val searchEditText get() = binding.searchView

    override fun getNavDirection(hhId: Long, benId: Long): NavDirections {
        return MdsrListFragmentDirections.actionMdsrListFragmentToMdsrObjectFragment(hhId, benId)
    }
}
