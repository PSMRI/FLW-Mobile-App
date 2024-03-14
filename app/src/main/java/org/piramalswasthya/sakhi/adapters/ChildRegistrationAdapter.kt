package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenChildRegBinding
import org.piramalswasthya.sakhi.model.ChildRegDomain

class ChildRegistrationAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<ChildRegDomain, ChildRegistrationAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<ChildRegDomain>() {
        override fun areItemsTheSame(
            oldItem: ChildRegDomain,
            newItem: ChildRegDomain
        ) =
            oldItem.motherBen.benId == newItem.motherBen.benId && oldItem.infant.babyIndex == newItem.infant.babyIndex

        override fun areContentsTheSame(
            oldItem: ChildRegDomain,
            newItem: ChildRegDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenChildRegBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenChildRegBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: ChildRegDomain,
            clickListener: ClickListener?,
        ) {
            binding.item = item
            binding.btnForm.text = if (item.childBen == null) "Register" else "View"

            binding.btnForm.setBackgroundColor(binding.root.resources.getColor(if (item.childBen == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
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
        private val clickedForm: (hhId: Long, benId: Long, babyIndex: Int, childBenId: Long, childRelToHead: Int) -> Unit

    ) {
        fun onClickForm(item: ChildRegDomain) =
            clickedForm(
                item.motherBen.hhId,
                item.motherBen.benId,
                item.infant.babyIndex,
                item.childBen?.benId ?: 0L,
                item.childBen?.relToHeadId?.let { it - 1 } ?: 0
            )
    }

}