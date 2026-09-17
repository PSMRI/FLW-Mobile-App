package org.piramalswasthya.sakhi.adapters

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.badges.BadgeRepository.BadgeCard
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.badges.domain.BadgeKind
import org.piramalswasthya.sakhi.databinding.ItemBadgeCarouselBinding

/** One big, friendly badge per page — for the auto-moving dashboard carousel. */
class BadgeCarouselAdapter :
    ListAdapter<BadgeCard, BadgeCarouselAdapter.PageViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<BadgeCard>() {
            override fun areItemsTheSame(oldItem: BadgeCard, newItem: BadgeCard) =
                oldItem.definition.id == newItem.definition.id

            override fun areContentsTheSame(oldItem: BadgeCard, newItem: BadgeCard) =
                oldItem == newItem
        }
    }

    class PageViewHolder(private val binding: ItemBadgeCarouselBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: BadgeCard) {
            val res = binding.root.resources
            val def = card.definition
            val state = card.state

            val (iconRes, earnedLook) = BadgeDefinitions.displayIcon(def, state)
            binding.ivCarouselIcon.setImageResource(iconRes)
            binding.ivCarouselIcon.alpha = if (earnedLook) 1f else 0.85f
            binding.tvCarouselTitle.text = res.getString(def.titleRes)

            val target = (state?.nextTarget ?: def.milestones.first()).coerceAtLeast(1L)
            val progress = (state?.progress ?: 0L).coerceAtMost(target) // maxed badges never show "9 of 8"
            binding.pbCarouselProgress.max = target.toInt()
            // animated fill: the bar grows as the badge slides in
            ObjectAnimator.ofInt(
                binding.pbCarouselProgress, "progress",
                0, progress.coerceAtMost(target).toInt()
            ).setDuration(650).start()

            val level = state?.currentLevel ?: 0
            val earnedText = when {
                level <= 0 -> null
                def.kind == BadgeKind.QUARTERLY || def.kind == BadgeKind.PER_CASE ->
                    res.getString(R.string.badge_earned_chip)
                else -> "🏅 " + res.getString(R.string.badge_level, level)
            }
            val progressText =
                if (progress <= 0L && level == 0) res.getString(R.string.badge_not_started)
                else res.getString(R.string.badge_progress_of, progress, target)
            val streakText = when {
                state == null || state.streakCount <= 0L -> null
                def.kind == BadgeKind.STREAK_WEEKLY ->
                    res.getString(R.string.badge_streak_weeks, state.streakCount)
                def.kind == BadgeKind.STREAK_MONTHLY ->
                    res.getString(R.string.badge_streak_months, state.streakCount)
                else -> null
            }
            // nudge when within reach of the next milestone
            val almostThere = (progress in 1 until target &&
                    progress.toDouble() / target >= 0.6)
                .let { if (it) res.getString(R.string.badge_almost_there) else null }
            binding.tvCarouselStatus.text =
                listOfNotNull(earnedText, progressText, streakText, almostThere)
                    .joinToString(" · ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PageViewHolder(
            ItemBadgeCarouselBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) =
        holder.bind(getItem(position))
}
