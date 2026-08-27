package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI
import java.text.NumberFormat
import java.util.Locale

data class ActivityGroupUI(
    val groupName: String,
    val totalAmount: Int,
    val totalClaims: Int,
    val activities: List<ClaimedIncentiveUI>,
    val isExpanded: Boolean = true
)

fun List<ClaimedIncentiveUI>.toActivityGroups(
    previouslyExpanded: Map<String, Boolean> = emptyMap()
): List<ActivityGroupUI> {
    return groupBy { it.groupName ?: "Others" }
        .map { (groupName, activities) ->
            ActivityGroupUI(
                groupName = groupName,
                totalAmount = activities.sumOf { it.totalAmount },
                totalClaims = activities.sumOf { it.claimCount },
                activities = activities,
                isExpanded = previouslyExpanded[groupName] ?: true
            )
        }
}

class GroupedActivityAdapter(
    private val onActivityClick: (ClaimedIncentiveUI) -> Unit,
    private val isSelected: (ClaimedIncentiveUI) -> Boolean = { false },
    private val onSelectionChanged: (ClaimedIncentiveUI, Boolean) -> Unit = { _, _ -> },
    private val showCheckbox: () -> Boolean = { false }
) : ListAdapter<ActivityGroupUI, GroupedActivityAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_group, parent, false)
        return GroupViewHolder(view, onActivityClick, isSelected, onSelectionChanged, showCheckbox)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group) {
            val updated = currentList.toMutableList()
            updated[position] = group.copy(isExpanded = !group.isExpanded)
            submitList(updated)
        }
    }

    class GroupViewHolder(
        itemView: View,
        private val onActivityClick: (ClaimedIncentiveUI) -> Unit,
        isSelected: (ClaimedIncentiveUI) -> Boolean,
        onSelectionChanged: (ClaimedIncentiveUI, Boolean) -> Unit,
        showCheckbox: () -> Boolean
    ) : RecyclerView.ViewHolder(itemView) {

        private val layoutGroupHeader: View = itemView.findViewById(R.id.layoutGroupHeader)
        private val tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        private val tvGroupClaimCount: TextView = itemView.findViewById(R.id.tvGroupClaimCount)
        private val tvGroupAmount: TextView = itemView.findViewById(R.id.tvGroupAmount)
        private val imgGroupExpand: ImageView = itemView.findViewById(R.id.imgGroupExpand)
        private val rvGroupActivities: RecyclerView = itemView.findViewById(R.id.rvGroupActivities)

        private val childAdapter = ActivityAdapter(onActivityClick, isSelected, onSelectionChanged, showCheckbox)

        init {
            rvGroupActivities.layoutManager = LinearLayoutManager(itemView.context)
            rvGroupActivities.adapter = childAdapter
            rvGroupActivities.isNestedScrollingEnabled = false
        }

        fun bind(group: ActivityGroupUI, onToggleExpand: () -> Unit) {
            tvGroupName.text = group.groupName
            tvGroupClaimCount.text = itemView.context.getString(
                R.string.claims_count_placeholder, group.totalClaims
            )
            tvGroupAmount.text = formatAmount(group.totalAmount)

            childAdapter.submitList(group.activities)
            rvGroupActivities.visibility = if (group.isExpanded) View.VISIBLE else View.GONE
            imgGroupExpand.rotation = if (group.isExpanded) 180f else 0f

            layoutGroupHeader.setOnClickListener { onToggleExpand() }
        }

        private fun formatAmount(amount: Int): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            formatter.maximumFractionDigits = 0
            return formatter.format(amount)
        }
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<ActivityGroupUI>() {
        override fun areItemsTheSame(oldItem: ActivityGroupUI, newItem: ActivityGroupUI) =
            oldItem.groupName == newItem.groupName

        override fun areContentsTheSame(oldItem: ActivityGroupUI, newItem: ActivityGroupUI) =
            oldItem == newItem
    }
}
