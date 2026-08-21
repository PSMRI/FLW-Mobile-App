package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.badges.BadgeRepository.BadgeCard
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.badges.domain.BadgeKind
import org.piramalswasthya.sakhi.databinding.ItemBadgeBinding

class BadgeShelfAdapter :
    ListAdapter<BadgeCard, BadgeShelfAdapter.BadgeViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BadgeCard>() {
            override fun areItemsTheSame(oldItem: BadgeCard, newItem: BadgeCard) =
                oldItem.definition.id == newItem.definition.id

            override fun areContentsTheSame(oldItem: BadgeCard, newItem: BadgeCard) =
                oldItem == newItem
        }
    }

    class BadgeViewHolder private constructor(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): BadgeViewHolder {
                val binding = ItemBadgeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return BadgeViewHolder(binding)
            }
        }

        fun bind(card: BadgeCard) {
            val res = binding.root.resources
            val def = card.definition
            val state = card.state

            val (iconRes, earnedLook) = BadgeDefinitions.displayIcon(def, state)
            binding.ivBadgeIcon.setImageResource(iconRes)
            // locked tiers render dimmed until the level is actually earned
            binding.ivBadgeIcon.alpha = if (earnedLook) 1f else 0.4f
            binding.tvBadgeTitle.text = res.getString(def.titleRes)
            binding.tvBadgeDesc.text = res.getString(def.descRes)

            val progress = state?.progress ?: 0L
            val target = (state?.nextTarget ?: def.milestones.first()).coerceAtLeast(1L)

            binding.tvBadgeLevel.text = when {
                def.kind == BadgeKind.QUARTERLY || def.kind == BadgeKind.PER_CASE ->
                    if (card.timesEarned > 0)
                        res.getString(R.string.badge_earned_times, card.timesEarned)
                    else ""

                (state?.currentLevel ?: 0) > 0 ->
                    res.getString(R.string.badge_level, state!!.currentLevel)

                else -> ""
            }

            // never-zero rule: 0 progress shows an invitation, not "0 of N"
            binding.tvBadgeProgress.text =
                if (progress <= 0L) res.getString(R.string.badge_not_started)
                else res.getString(R.string.badge_progress_of, progress, target)

            binding.pbBadgeProgress.max = target.toInt()
            binding.pbBadgeProgress.progress = progress.coerceAtMost(target).toInt()

            binding.tvBadgeStreak.text = when {
                state == null || state.streakCount <= 0 -> ""
                def.kind == BadgeKind.STREAK_WEEKLY -> listOfNotNull(
                    res.getString(R.string.badge_streak_weeks, state.streakCount),
                    res.getString(R.string.badge_grace_remaining, state.graceRemaining)
                        .takeIf { state.graceRemaining > 0 }
                ).joinToString(" · ")

                def.kind == BadgeKind.STREAK_MONTHLY ->
                    res.getString(R.string.badge_streak_months, state.streakCount)

                else -> ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BadgeViewHolder.from(parent)

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) =
        holder.bind(getItem(position))
}
