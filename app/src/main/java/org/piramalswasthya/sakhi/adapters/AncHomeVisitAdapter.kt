package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemAncHomeVisitBinding
import org.piramalswasthya.sakhi.model.HomeVisitDomain

class AncHomeVisitAdapter(
    private val clickListener: HomeVisitClickListener? = null
) : ListAdapter<HomeVisitDomain,  AncHomeVisitAdapter.HomeVisitViewHolder>(MyDiffUtilCallBack) {

    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<HomeVisitDomain>() {
        override fun areItemsTheSame(oldItem: HomeVisitDomain, newItem: HomeVisitDomain) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HomeVisitDomain, newItem: HomeVisitDomain) =
            oldItem == newItem
    }

    class HomeVisitViewHolder private constructor(private val binding: ItemAncHomeVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): HomeVisitViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAncHomeVisitBinding.inflate(layoutInflater, parent, false)
                return HomeVisitViewHolder(binding)
            }
        }

        fun bind(item: HomeVisitDomain, clickListener: HomeVisitClickListener?) {
            binding.visit = item
            binding.clickListener = clickListener
            binding.executePendingBindings()




            binding.btnView.setOnClickListener {
                clickListener?.onViewClick(item)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HomeVisitViewHolder.from(parent)

    override fun onBindViewHolder(holder: HomeVisitViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class HomeVisitClickListener(
        private val onViewClick: (HomeVisitDomain) -> Unit
    ) {
        fun onViewClick(item: HomeVisitDomain) = onViewClick.invoke(item)
    }
}