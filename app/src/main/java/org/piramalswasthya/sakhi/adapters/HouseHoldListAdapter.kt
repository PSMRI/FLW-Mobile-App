package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemHouseholdBinding
import org.piramalswasthya.sakhi.model.HouseHoldBasicDomain
import javax.inject.Inject


class HouseHoldListAdapter(val pref: PreferenceDao, private val clickListener: HouseholdClickListener) :
    ListAdapter<HouseHoldBasicDomain, HouseHoldListAdapter.HouseHoldViewHolder>(
        HouseHoldDiffUtilCallBack
    ) {

    private object HouseHoldDiffUtilCallBack : DiffUtil.ItemCallback<HouseHoldBasicDomain>() {
        override fun areItemsTheSame(
            oldItem: HouseHoldBasicDomain,
            newItem: HouseHoldBasicDomain
        ) = oldItem.hhId == newItem.hhId

        override fun areContentsTheSame(
            oldItem: HouseHoldBasicDomain,
            newItem: HouseHoldBasicDomain
        ) = oldItem == newItem

    }

    class HouseHoldViewHolder private constructor(private val binding: RvItemHouseholdBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HouseHoldViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemHouseholdBinding.inflate(layoutInflater, parent, false)
                return HouseHoldViewHolder(binding)
            }
        }

        fun bind(item: HouseHoldBasicDomain, clickListener: HouseholdClickListener, pref: PreferenceDao) {
            binding.household = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

            if (pref.getLoggedInUser()?.role.equals("asha", true)) {
                binding.button4.visibility = View.VISIBLE
            } else {
                binding.button4.visibility = View.GONE
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseHoldViewHolder {
        return HouseHoldViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HouseHoldViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, pref)
    }


    class HouseholdClickListener(
        val hhDetails: (hhId: Long) -> Unit,
        val showMember: (hhId: Long) -> Unit,
        val newBen: (hh: HouseHoldBasicDomain) -> Unit
    ) {
        fun onClickedForHHDetails(item: HouseHoldBasicDomain) = hhDetails(item.hhId)
        fun onClickedForMembers(item: HouseHoldBasicDomain) = showMember(item.hhId)
        fun onClickedForNewBen(item: HouseHoldBasicDomain) = newBen(item)

    }
}