package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenEcWithFormBinding
import org.piramalswasthya.sakhi.model.BenWithEcrDomain

class ECRegistrationAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<BenWithEcrDomain, ECRegistrationAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithEcrDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithEcrDomain,
            newItem: BenWithEcrDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithEcrDomain,
            newItem: BenWithEcrDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenEcWithFormBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenEcWithFormBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithEcrDomain,
            clickListener: ClickListener?,
        ) {
            binding.benWithEcr = item

            binding.ivSyncState.visibility = if (item.ecr == null) View.INVISIBLE else View.VISIBLE
            binding.btnFormEc1.text = if (item.ecr == null) "Register" else "View"

            binding.btnFormEc1.setBackgroundColor(binding.root.resources.getColor(if (item.ecr == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
            binding.clickListener = clickListener

            binding.executePendingBindings()

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class ClickListener(
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickForm(item: BenWithEcrDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }

}