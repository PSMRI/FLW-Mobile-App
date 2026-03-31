package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemEyeSurgeryVisitBinding
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.model.EyeSurgeryVisitOption

class EyeSurgeryVisitListAdapter(
    private val clickListener: ClickListener
) : ListAdapter<EyeSurgeryVisitOption, EyeSurgeryVisitListAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<EyeSurgeryVisitOption>() {
            override fun areItemsTheSame(a: EyeSurgeryVisitOption, b: EyeSurgeryVisitOption) =
                a.title == b.title && a.eyeSide == b.eyeSide
            override fun areContentsTheSame(a: EyeSurgeryVisitOption, b: EyeSurgeryVisitOption) =
                a == b
        }
    }

    inner class ViewHolder(val binding: RvItemEyeSurgeryVisitBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemEyeSurgeryVisitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            tvVisitTitle.text = item.title
            tvVisitDate.text = item.visitDate
            btnAction.text = if (item.isAddNew) "Add" else "View/Edit"
            btnAction.setOnClickListener { clickListener.onClick(item) }
        }
    }

    class ClickListener(val block: (EyeSurgeryVisitOption) -> Unit) {
        fun onClick(option: EyeSurgeryVisitOption) = block(option)
    }
}