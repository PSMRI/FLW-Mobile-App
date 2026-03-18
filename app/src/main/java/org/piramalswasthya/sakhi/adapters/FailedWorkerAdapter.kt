package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemFailedWorkerBinding
import org.piramalswasthya.sakhi.model.FailedWorkerInfo

class FailedWorkerAdapter :
    ListAdapter<FailedWorkerInfo, FailedWorkerAdapter.ViewHolder>(DiffCallback) {

    private object DiffCallback : DiffUtil.ItemCallback<FailedWorkerInfo>() {
        override fun areItemsTheSame(oldItem: FailedWorkerInfo, newItem: FailedWorkerInfo) =
            oldItem.workerName == newItem.workerName

        override fun areContentsTheSame(oldItem: FailedWorkerInfo, newItem: FailedWorkerInfo) =
            oldItem == newItem
    }

    class ViewHolder(
        private val binding: RvItemFailedWorkerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FailedWorkerInfo) {
            binding.tvWorkerName.text = item.workerName
            binding.tvErrorReason.text = item.error
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemFailedWorkerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
