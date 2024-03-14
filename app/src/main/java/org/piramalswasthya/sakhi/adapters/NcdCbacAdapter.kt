package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemNcdCbacElementBinding
import org.piramalswasthya.sakhi.model.CbacDomain

class NcdCbacAdapter(private val clickListener: NcdCbacElementClickListener) :
    ListAdapter<CbacDomain, NcdCbacAdapter.NcdCbacElementViewHolder>(
        ImmunizationIconDiffCallback
    ) {
    object ImmunizationIconDiffCallback : DiffUtil.ItemCallback<CbacDomain>() {
        override fun areItemsTheSame(
            oldItem: CbacDomain,
            newItem: CbacDomain
        ) =
            oldItem.cbacId == newItem.cbacId

        override fun areContentsTheSame(
            oldItem: CbacDomain,
            newItem: CbacDomain
        ) =
            (oldItem == newItem)

    }


    class NcdCbacElementViewHolder private constructor(private val binding: RvItemNcdCbacElementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): NcdCbacElementViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemNcdCbacElementBinding.inflate(layoutInflater, parent, false)
                return NcdCbacElementViewHolder(binding)
            }
        }

        fun bind(item: CbacDomain, clickListener: NcdCbacElementClickListener) {
            binding.cbac = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NcdCbacElementViewHolder.from(parent)

    override fun onBindViewHolder(holder: NcdCbacElementViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class NcdCbacElementClickListener(val selectedListener: (cbacId: Int) -> Unit) {
        fun onClicked(cbac: CbacDomain) = selectedListener(cbac.cbacId)

    }
}