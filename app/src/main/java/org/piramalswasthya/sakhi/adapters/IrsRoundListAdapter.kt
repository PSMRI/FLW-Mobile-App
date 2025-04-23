package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemIrsRoundBinding
import org.piramalswasthya.sakhi.model.BenWithScreeningRoundDomain
import org.piramalswasthya.sakhi.model.IRSRoundScreening

class IrsRoundListAdapter () :
    ListAdapter<IRSRoundScreening, IrsRoundListAdapter.ViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<IRSRoundScreening>() {
        override fun areItemsTheSame(
            oldItem: IRSRoundScreening, newItem: IRSRoundScreening
        ) = oldItem.householdId == newItem.householdId

        override fun areContentsTheSame(
            oldItem: IRSRoundScreening, newItem: IRSRoundScreening
        ) = oldItem == newItem

    }

    class ViewHolder private constructor(private val binding: RvItemIrsRoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemIrsRoundBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: IRSRoundScreening
        ) {
            binding.irs = item
            binding.position = position
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }



}