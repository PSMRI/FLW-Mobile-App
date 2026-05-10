package org.piramalswasthya.sakhi.ui.home_activity.gamification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemBadgeBinding
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.GamificationBadge
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao

class BadgeAdapter : ListAdapter<GamificationBadge, BadgeAdapter.BadgeViewHolder>(DiffCallback) {

    inner class BadgeViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: GamificationBadge) {
            // Show badge name in current app language
            binding.tvBadgeName.text = badge.badgeNameEn
            binding.tvBadgeEmoji.text = emojiForBadge(badge.badgeType)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun emojiForBadge(badgeType: String): String = when (badgeType) {
        "FIRST_HOUSEHOLD"       -> "🏠"
        "FIRST_BENEFICIARY"     -> "👤"
        "STREAK_3"              -> "🔥"
        "STREAK_7"              -> "⚡"
        "STREAK_30"             -> "🏆"
        "ANC_HERO"              -> "🤰"
        "IMMUNIZATION_GUARDIAN" -> "💉"
        "HRP_IDENTIFIER"        -> "❤️"
        "NCD_CHAMPION"          -> "🩺"
        "LEVEL_2"               -> "⭐"
        "LEVEL_5"               -> "🌟"
        else                    -> "🏅"
    }

    companion object DiffCallback : DiffUtil.ItemCallback<GamificationBadge>() {
        override fun areItemsTheSame(a: GamificationBadge, b: GamificationBadge) = a.id == b.id
        override fun areContentsTheSame(a: GamificationBadge, b: GamificationBadge) = a == b
    }
}
