package org.piramalswasthya.sakhi.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemHouseholdBinding
import org.piramalswasthya.sakhi.model.HouseHoldBasicDomain


class HouseHoldListAdapter(private val diseaseType: String, private var isDisease: Boolean, val pref: PreferenceDao,private val isSoftDeleteEnabled:Boolean = false, private val clickListener: HouseholdClickListener) :
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

        fun bind(
            item: HouseHoldBasicDomain,
            clickListener: HouseholdClickListener,
            isDisease: Boolean,
            pref: PreferenceDao,
            diseaseType: String,
            isSoftDeleteEnabled: Boolean
        ) {
            binding.household = item
            binding.clickListener = clickListener
            binding.executePendingBindings()


            if (isDisease) {
                binding.button4.visibility = View.GONE
                if (diseaseType == "FILARIA") {
                    binding.btnMda.visibility = View.VISIBLE
                } else {
                    binding.btnMda.visibility = View.GONE
                }
            } else if (!isDisease) {
                binding.button4.visibility = View.VISIBLE
                binding.btnMda.visibility = View.GONE
            } else {
                binding.button4.visibility = View.GONE
                binding.btnMda.visibility = View.GONE
            }


            if (isSoftDeleteEnabled){
                binding.ivSoftDelete.visibility = View.VISIBLE

                if (item.isDeactivate){
                    //binding.ivSoftDelete.setImageResource(R.drawable.ic_group_on)
                    binding.parentCard.setBackgroundColor(ContextCompat.getColor(binding.parentCard.context, R.color.Quartenary))

                    binding.ivSoftDelete.visibility = View.GONE
                    binding.button4.visibility = View.INVISIBLE
                    binding.tvTitleDuplicaterecord.visibility = View.VISIBLE
                    binding.button3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_outline))

                } else{
                  //  binding.ivSoftDelete.setImageResource(R.drawable.ic_group_off)
                    binding.parentCard.setBackgroundColor(ContextCompat.getColor(binding.parentCard.context, R.color.md_theme_light_primary))

                    binding.ivSoftDelete.visibility = View.VISIBLE
                    binding.button4.visibility = View.VISIBLE
                    binding.tvTitleDuplicaterecord.visibility = View.GONE
                    binding.button3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.holo_green_dark))

                }

            } else {
                binding.ivSoftDelete.visibility = View.GONE
            }


            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseHoldViewHolder {
        return HouseHoldViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: HouseHoldViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener,isDisease, pref, diseaseType,isSoftDeleteEnabled)
    }


    class HouseholdClickListener(
        val hhDetails: (hh: HouseHoldBasicDomain) -> Unit,
        val showMember: (hh: HouseHoldBasicDomain) -> Unit,
        val newBen: (hh: HouseHoldBasicDomain) -> Unit,
        val addMDA: (hh: HouseHoldBasicDomain) -> Unit,
        val softDeleteHh: (hh: HouseHoldBasicDomain) -> Unit,
    ) {
        fun onClickedForHHDetails(item: HouseHoldBasicDomain) = hhDetails(item)
        fun onClickedForMembers(item: HouseHoldBasicDomain) = showMember(item)
        fun onClickedForNewBen(item: HouseHoldBasicDomain) = newBen(item)
        fun onClickedAddMDA(item: HouseHoldBasicDomain) = addMDA(item)
        fun onClickSoftDeleteHh(item: HouseHoldBasicDomain) = softDeleteHh(item)
    }
}