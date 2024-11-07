package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenChildCareInfantBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import java.util.concurrent.TimeUnit


class InfantListAdapter(
    private val clickListener: InfantListClickListener,
    private val showSyncIcon: Boolean = false
) :
    ListAdapter<BenBasicDomain, InfantListAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomain>() {
        override fun areItemsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenChildCareInfantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenChildCareInfantBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenBasicDomain,
            clickListener: InfantListClickListener,
            showSyncIcon: Boolean,
        ) {
            if (!showSyncIcon) item.syncState = null
            binding.ben = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
            setOverDueStatus(item.dob)
        }

        private fun setOverDueStatus(dob: Long)  {

                val diffLong = System.currentTimeMillis() - dob
                if (TimeUnit.MILLISECONDS.toDays(diffLong).toInt() > 42) {
                    binding.dueIcon.visibility = View.VISIBLE
                } else {
                    binding.dueIcon.visibility = View.GONE
                }

            }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, showSyncIcon)

    }


    class InfantListClickListener(
        val goToHbnc: (benId: Long, hhId: Long) -> Unit

    ) {
        fun onClickedHbnc(item: BenBasicDomain) = goToHbnc(
            item.benId, item.hhId
        )
    }

}