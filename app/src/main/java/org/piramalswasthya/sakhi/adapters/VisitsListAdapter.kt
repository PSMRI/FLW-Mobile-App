package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemVisitsBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.getDateStrFromLong
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Locale


class VisitsListAdapter :
    ListAdapter<MalariaScreeningCache, VisitsListAdapter.FollowUpViewHolder>(DiffCallback) {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowUpViewHolder {
        val binding = RvItemVisitsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FollowUpViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowUpViewHolder, position: Int) {
        val followUp = getItem(position)
        holder.bind(followUp)
    }

    inner class FollowUpViewHolder(private val binding: RvItemVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(followUp: MalariaScreeningCache) {
            val ctx = binding.root.context
            val englishArray = HelperUtil.getLocalizedResources(ctx, Languages.ENGLISH)
                .getStringArray(R.array.dc_case_status)
            val localizedArray = ctx.resources.getStringArray(R.array.dc_case_status)
            val idx = englishArray.indexOf(followUp.caseStatus)
            binding.tvVisitNumber.text = ctx.getString(R.string.visit_format, followUp.visitId)
            binding.tvFollowUpDate.text = getDateStrFromLong(followUp.caseDate)
            binding.tvTreatmentStatus.text = if (idx >= 0) localizedArray[idx] else localizedArray[0]
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MalariaScreeningCache>() {
        override fun areItemsTheSame(oldItem: MalariaScreeningCache, newItem: MalariaScreeningCache): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MalariaScreeningCache, newItem: MalariaScreeningCache): Boolean {
            return oldItem == newItem
        }
    }
}