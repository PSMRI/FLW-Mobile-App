package org.piramalswasthya.sakhi.ui.notifications

import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.model.NotificationEventType

/**
 * Renders the notification list ([R.layout.item_notification]).
 * Applies read/unread styling and maps each item's `eventType` to its icon.
 *
 * @param onItemClick invoked when a row is tapped (used later for mark-read / deeplink).
 */
class NotificationAdapter(
    private val onItemClick: (NotificationDomain) -> Unit
) : ListAdapter<NotificationDomain, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        itemView: View,
        private val onItemClick: (NotificationDomain) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)

        fun bind(item: NotificationDomain) {
            tvTitle.text = item.title
            tvMessage.text = item.body
            tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
                item.createdTs,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            ivIcon.setImageResource(NotificationEventType.fromKey(item.eventType).iconRes)

            // Read vs unread styling.
            viewUnreadDot.visibility = if (item.read) View.GONE else View.VISIBLE
            tvTitle.setTypeface(null, if (item.read) Typeface.NORMAL else Typeface.BOLD)
            val emphasis = if (item.read) 0.6f else 1.0f
            tvTitle.alpha = emphasis
            tvMessage.alpha = emphasis

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationDomain>() {
        override fun areItemsTheSame(oldItem: NotificationDomain, newItem: NotificationDomain) =
            oldItem.notificationId == newItem.notificationId

        override fun areContentsTheSame(oldItem: NotificationDomain, newItem: NotificationDomain) =
            oldItem == newItem
    }
}