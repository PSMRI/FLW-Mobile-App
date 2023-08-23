package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicDomain


class BenListAdapter(
    private val clickListener: BenClickListener? = null,
    private val showAbha: Boolean = false
, private val role: Int? = 0) :
    ListAdapter<BenBasicDomain, BenListAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomain>() {
        override fun areItemsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenBasicDomain, clickListener: BenClickListener?, showAbha: Boolean, role: Int?
        ) {
            binding.ben = item
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.hasAbha = !item.abhaId.isNullOrEmpty()
            binding.role = role
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, showAbha, role)
    }


    class BenClickListener(
        private val clickedBen: (hhId: Long, benId: Long, isKid: Boolean) -> Unit,
        private val clickedHousehold: (hhId: Long) -> Unit,
        private val clickedABHA: (benId: Long, hhId: Long) -> Unit,
    ) {
        fun onClickedBen(item: BenBasicDomain) = clickedBen(
            item.hhId,
            item.benId,
            (item.ageUnit != AgeUnit.YEARS || item.ageInt <= Konstants.maxAgeForAdolescent)
        )

        fun onClickedHouseHold(item: BenBasicDomain) = clickedHousehold(item.hhId)

        fun onClickABHA(item: BenBasicDomain) = clickedABHA(item.benId, item.hhId)
    }

}