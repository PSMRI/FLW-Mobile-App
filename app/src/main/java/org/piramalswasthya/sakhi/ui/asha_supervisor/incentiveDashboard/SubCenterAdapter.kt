package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutListItemBinding

class SubCenterAdapter(
) : RecyclerView.Adapter<SubCenterAdapter.SubCenterViewHolder>() {

    inner class SubCenterViewHolder(val binding: LayoutListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCenterViewHolder {
        val binding = LayoutListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubCenterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubCenterViewHolder, position: Int) {
//        val item = list[position]

        holder.binding.subCenterName.text = ""

        holder.binding.root.setOnClickListener {
//            onClick("")
        }
    }

    override fun getItemCount(): Int = 5

    interface OnItemClickListener {
//        fun onItemClick(subCenter: SubCenter)
    }
}