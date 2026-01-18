package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutMdaCampaignItemBinding
import org.piramalswasthya.sakhi.model.dynamicModel.MDACampaignItem

class FilariaMdaCampaignAdapter (
    private val clickListener: MdaClickListener? = null,
) :
    ListAdapter<MDACampaignItem, FilariaMdaCampaignAdapter.PHCViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<MDACampaignItem>() {
        override fun areItemsTheSame(
            oldItem: MDACampaignItem,
            newItem: MDACampaignItem
        ) = oldItem.srNo == newItem.srNo

        override fun areContentsTheSame(oldItem: MDACampaignItem, newItem: MDACampaignItem)= oldItem == newItem


    }

    class PHCViewHolder private constructor(private val binding: LayoutMdaCampaignItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): PHCViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutMdaCampaignItemBinding.inflate(layoutInflater, parent, false)
                return PHCViewHolder(binding)
            }
        }
        fun bind(item: MDACampaignItem, clickListener: MdaClickListener?,) {
            binding.phc = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        PHCViewHolder.from(parent)

    override fun onBindViewHolder(holder: PHCViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class MdaClickListener(
        private val clickedForm: ((id: Int) -> Unit)? = null
    ) {
        fun onClickForm1(item: MDACampaignItem) = clickedForm?.let { it(item.srNo!!) }
    }

}