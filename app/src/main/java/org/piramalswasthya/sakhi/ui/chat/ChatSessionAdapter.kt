package org.piramalswasthya.sakhi.ui.chat

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemChatSessionBinding
import org.piramalswasthya.sakhi.model.chat.ChatSession
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ChatSessionAdapter(
    private val onSessionClick: (ChatSession) -> Unit
) : ListAdapter<ChatSession, ChatSessionAdapter.ViewHolder>(DIFF) {

    private var selectedSessionId: String? = null

    fun setSelectedSession(sessionId: String?) {
        val old = selectedSessionId
        selectedSessionId = sessionId
        currentList.forEachIndexed { index, session ->
            if (session.sessionId == old || session.sessionId == sessionId) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemChatSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ChatSession) {
            binding.tvSessionTitle.text = session.title ?: "New Chat"
            binding.tvSessionTime.text = getRelativeTime(session.updatedAt)

            val isSelected = session.sessionId == selectedSessionId
            binding.root.alpha = if (isSelected) 1f else 0.7f
            binding.root.setOnClickListener { onSessionClick(session) }
        }

        private fun getRelativeTime(isoDate: String): CharSequence {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(isoDate)
                if (date != null) {
                    DateUtils.getRelativeTimeSpanString(
                        date.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                } else isoDate
            } catch (_: Exception) {
                isoDate
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatSession>() {
            override fun areItemsTheSame(a: ChatSession, b: ChatSession) =
                a.sessionId == b.sessionId

            override fun areContentsTheSame(a: ChatSession, b: ChatSession) = a == b
        }
    }
}
