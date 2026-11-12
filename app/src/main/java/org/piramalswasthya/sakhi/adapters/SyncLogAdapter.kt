package org.piramalswasthya.sakhi.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.RvItemSyncLogBinding
import org.piramalswasthya.sakhi.model.LogLevel
import org.piramalswasthya.sakhi.model.SyncLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncLogAdapter :
    ListAdapter<SyncLogEntry, SyncLogAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private const val MAX_DISPLAY_LENGTH = 500
    }

    private object DiffCallback : DiffUtil.ItemCallback<SyncLogEntry>() {
        override fun areItemsTheSame(oldItem: SyncLogEntry, newItem: SyncLogEntry) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SyncLogEntry, newItem: SyncLogEntry) =
            oldItem == newItem
    }

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    class ViewHolder(
        private val binding: RvItemSyncLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncLogEntry, timeFormat: SimpleDateFormat) {
            val (label, bgColor, textColor) = when (item.level) {
                LogLevel.ERROR -> Triple("E", Color.parseColor("#D32F2F"), Color.parseColor("#D32F2F"))
                LogLevel.WARN -> Triple("W", Color.parseColor("#F57C00"), Color.parseColor("#F57C00"))
                LogLevel.INFO -> Triple("I", Color.parseColor("#1976D2"), Color.parseColor("#333333"))
                LogLevel.DEBUG -> Triple("D", Color.parseColor("#757575"), Color.parseColor("#757575"))
            }

            binding.tvLevel.text = label
//            val bg = GradientDrawable().apply {
//                shape = GradientDrawable.RECTANGLE
//                cornerRadius = 4f
//                setColor(bgColor)
//            }
//            binding.tvLevel.background = bg

            binding.tvTimestamp.text = "${timeFormat.format(Date(item.timestamp))}  ${item.tag}"
            binding.tvMessage.text = if (item.message.length > MAX_DISPLAY_LENGTH)
                item.message.substring(0, MAX_DISPLAY_LENGTH) + "…"
            else
                item.message
//            binding.tvMessage.setTextColor(textColor)
            dsLevelBadge(item.level)
        }

        /** Redesign (Figma M2.11-07 sync badge): level pill in error / warning / info / neutral tones, message #42474E (error red). */
        private fun dsLevelBadge(level: LogLevel) {
            val ctx = binding.root.context
            val (bgRes, fgRes) = when (level) {
                LogLevel.ERROR -> R.color.ds_error_container to R.color.ds_error
                LogLevel.WARN -> R.color.ds_warning_container to R.color.ds_warning
                LogLevel.INFO -> R.color.ds_primary_container to R.color.ds_primary
                LogLevel.DEBUG -> R.color.ds_surface_container_high to R.color.ds_on_surface_variant
            }
            binding.tvLevel.backgroundTintList = ContextCompat.getColorStateList(ctx, bgRes)
            binding.tvLevel.setTextColor(ContextCompat.getColor(ctx, fgRes))
            binding.tvMessage.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (level == LogLevel.ERROR) R.color.ds_error else R.color.ds_on_surface_variant
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemSyncLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), timeFormat)
    }
}
