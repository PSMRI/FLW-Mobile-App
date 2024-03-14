package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenInfantRegBinding
import org.piramalswasthya.sakhi.model.InfantRegDomain

class InfantRegistrationAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<InfantRegDomain, InfantRegistrationAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<InfantRegDomain>() {
        override fun areItemsTheSame(
            oldItem: InfantRegDomain,
            newItem: InfantRegDomain
        ) =
            oldItem.motherBen.benId == newItem.motherBen.benId && oldItem.babyIndex == newItem.babyIndex

        override fun areContentsTheSame(
            oldItem: InfantRegDomain,
            newItem: InfantRegDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenInfantRegBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenInfantRegBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: InfantRegDomain,
            clickListener: ClickListener?,
        ) {
            binding.item = item
            binding.btnFormEc1.text = if (item.savedIr == null) "Register" else "View"

            binding.btnFormEc1.setBackgroundColor(binding.root.resources.getColor(if (item.savedIr == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
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
        private val clickedForm: ((benId: Long, babyIndex: Int) -> Unit)? = null

    ) {
        fun onClickForm(item: InfantRegDomain) =
            clickedForm?.let { it(item.motherBen.benId, item.babyIndex) }
    }

}