package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemTptFollowupListBinding
import org.piramalswasthya.sakhi.helpers.getLocalizedAge
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tpt.list.TPTListItem

class TPTListAdapter : ListAdapter<TPTListItem, TPTListAdapter.BenViewHolder>(DiffCallback) {

    private var clickListener: ClickListener? = null

    fun setClickListener(listener: ClickListener) {
        clickListener = listener
    }

    private object DiffCallback : DiffUtil.ItemCallback<TPTListItem>() {
        override fun areItemsTheSame(
            oldItem: TPTListItem,
            newItem: TPTListItem
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: TPTListItem,
            newItem: TPTListItem
        ) = oldItem == newItem
    }

    class BenViewHolder private constructor(
        private val binding: RvItemTptFollowupListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TPTListItem, clickListener: ClickListener?) {
            binding.benWithTb = item
            binding.clickListener = clickListener
            binding.nikshayLayout.visibility = View.VISIBLE
            binding.RCHlayout.visibility = View.GONE
            val hasTptFollowUp = item.followUpHistory.isNotEmpty()
            val isSynced = hasTptFollowUp && item.followUpHistory.all { it.isSynced }
            binding.ivSyncState.visibility = if (hasTptFollowUp) View.VISIBLE else View.GONE
            if (hasTptFollowUp) {
                binding.ivSyncState.setImageResource(
                    if (isSynced) R.drawable.cloud_upload else R.drawable.cloud_off
                )
            }
            binding.status.setImageResource(R.drawable.ic_verified)
            binding.age.text = getLocalizedAge(binding.root.context, item.ben.dob)
            val history = item.followUpHistory.mapNotNull { visit ->
                visit.followUpDate?.takeIf { it.isNotBlank() }?.let { "Follow-up ${visit.followUpNo}: $it" }
            }
            binding.tvTptFollowUpHistory.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
            binding.tvTptFollowUpHistory.text = history.joinToString(separator = "\n", prefix = "Follow-up history\n")

            val isTreatmentCompleted = item.followUpHistory.any { visit ->
                runCatching {
                    JSONObject(visit.fieldsJson).optString("treatment_completed")
                        .equals("Yes", ignoreCase = true)
                }.getOrDefault(false)
            }
            when {
                isTreatmentCompleted -> {
                    binding.btnFormTb.text = binding.root.context.getString(R.string.view_details)
                    binding.btnFormTb.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark)
                    )
                }
                item.followUpHistory.isNotEmpty() -> {
                    binding.btnFormTb.text = binding.root.context.getString(R.string.add_follow_up)
                    binding.btnFormTb.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.holo_orange_dark)
                    )
                }
                else -> {
                    binding.btnFormTb.text = binding.root.context.getString(R.string.start_tpt)
                    binding.btnFormTb.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark)
                    )
                }
            }

            when {
                item.ben.spouseName == "Not Available" && item.ben.fatherName == "Not Available" -> {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                }
                item.ben.gender == "MALE" || item.ben.ageInt <= 15 -> {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                }
                item.ben.gender == "FEMALE" -> {
                    binding.father = item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                    binding.husband = item.ben.spouseName != "Not Available"
                    binding.spouse = false
                }
                else -> {
                    binding.father = item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                    binding.spouse = item.ben.spouseName != "Not Available"
                    binding.husband = false
                }
            }

            if (item.ben.isDeath) {
                binding.linearTbScreeningListLayout.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_outline)
                )
            } else {
                binding.linearTbScreeningListLayout.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.md_theme_light_primary)
                )
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val binding = RvItemTptFollowupListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return BenViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedForm: ((hhId: Long, benId: Long,referralFollowUpDate: String) -> Unit)? = null

    ) {
        fun onClickForm(item: TPTListItem) {
            val referralFollowUpDate = runCatching {
                JSONObject(item.referralFollowUps.fieldsJson).optString("follow_up_date")
            }.getOrDefault("")
            clickedForm?.invoke(item.ben.hhId, item.ben.benId, referralFollowUpDate)
        }
    }
}
