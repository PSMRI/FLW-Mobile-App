package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.badges.BadgeRepository.BadgeCard
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.databinding.ItemBadgeCarouselBinding

/** One compact badge per page — for the auto-moving dashboard carousel. */
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

            // Artwork still carries the earned/locked distinction: the highest earned
            // tier, dimmed while the badge is still locked.
            val (iconRes, earnedLook) = BadgeDefinitions.displayIcon(def, card.state)
            binding.ivCarouselIcon.setImageResource(iconRes)
            binding.ivCarouselIcon.alpha = if (earnedLook) 1f else 0.85f

            binding.tvCarouselTitle.text = res.getString(def.titleRes)
            // One line on what earns this badge. Counts, streaks and "almost there"
            // nudges live on the badge shelf, one tap away.
            binding.tvCarouselDesc.text = res.getString(def.descRes)
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
