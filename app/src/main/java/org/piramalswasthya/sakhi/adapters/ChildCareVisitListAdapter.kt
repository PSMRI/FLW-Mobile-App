package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemChildrenCareBottomSheetBinding
import org.piramalswasthya.sakhi.model.ChildOption

class ChildCareVisitListAdapter(
    private val clickListener: ChildOptionsClickListener
) : ListAdapter<ChildOption, ChildCareVisitListAdapter.ChildOptionViewHolder>(
    MyDiffUtilCallBack
) {


    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<ChildOption>() {
        override fun areItemsTheSame(oldItem: ChildOption, newItem: ChildOption): Boolean =
            oldItem.formType == newItem.formType

        override fun areContentsTheSame(oldItem: ChildOption, newItem: ChildOption): Boolean =
            oldItem == newItem
    }

    class ChildOptionViewHolder private constructor(
        private val binding: RvItemChildrenCareBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ChildOptionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemChildrenCareBottomSheetBinding.inflate(layoutInflater, parent, false)
                return ChildOptionViewHolder(binding)
            }
        }

        fun bind(item: ChildOption, clickListener: ChildOptionsClickListener) {
            binding.option = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildOptionViewHolder =
        ChildOptionViewHolder.from(parent)

    override fun onBindViewHolder(holder: ChildOptionViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ChildOptionsClickListener(
        private val clickedOption: (formType: String, isViewMode: Boolean) -> Unit
    ) {
        fun onAddClicked(item: ChildOption) = clickedOption(item.formType, false)
        fun onViewClicked(item: ChildOption) = clickedOption(item.formType, true)
    }
}
