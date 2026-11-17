package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.badges.BadgeRepository.BadgeCard
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
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

        /**
         * Name, artwork and one line on what earns the badge — in the app's current
         * language, since both strings come from resources. Deliberately no counts:
         * the dashboard card carries progress, this screen is the catalogue.
         */
        fun bind(card: BadgeCard) {
            val res = binding.root.resources
            val def = card.definition

            val (iconRes, earnedLook) = BadgeDefinitions.displayIcon(def, card.state)
            binding.ivBadgeIcon.setImageResource(iconRes)
            // locked tiers render dimmed until the level is actually earned
            binding.ivBadgeIcon.alpha = if (earnedLook) 1f else 0.85f
            binding.tvBadgeTitle.text = res.getString(def.titleRes)
            binding.tvBadgeDesc.text = res.getString(def.descRes)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BadgeViewHolder.from(parent)

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) =
        holder.bind(getItem(position))
}
