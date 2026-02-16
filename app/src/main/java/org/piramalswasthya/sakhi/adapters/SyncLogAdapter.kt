package org.piramalswasthya.sakhi.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemSyncLogBinding
import org.piramalswasthya.sakhi.model.LogLevel
import org.piramalswasthya.sakhi.model.SyncLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncLogAdapter :
    ListAdapter<SyncLogEntry, SyncLogAdapter.ViewHolder>(DiffCallback) {

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
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 4f
                setColor(bgColor)
            }
            binding.tvLevel.background = bg

            binding.tvTimestamp.text = timeFormat.format(Date(item.timestamp))
            binding.tvMessage.text = item.message
            binding.tvMessage.setTextColor(textColor)
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
