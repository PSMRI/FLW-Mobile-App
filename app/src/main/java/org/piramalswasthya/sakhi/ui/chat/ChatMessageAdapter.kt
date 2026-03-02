package org.piramalswasthya.sakhi.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.ItemChatMessageAssistantBinding
import org.piramalswasthya.sakhi.databinding.ItemChatMessageUserBinding
import org.piramalswasthya.sakhi.model.chat.ChatMessage

class ChatMessageAdapter(
    private val onFaqClick: (String) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_ASSISTANT = 1

        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) =
                a.messageId == b.messageId

            override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == "user") TYPE_USER else TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserViewHolder(
                ItemChatMessageUserBinding.inflate(inflater, parent, false)
            )
            else -> AssistantViewHolder(
                ItemChatMessageAssistantBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AssistantViewHolder -> holder.bind(message)
        }
    }

    class UserViewHolder(
        private val binding: ItemChatMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.content
        }
    }

    inner class AssistantViewHolder(
        private val binding: ItemChatMessageAssistantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.content

            val metadata = message.metadata

            // Low confidence disclaimer
            binding.tvLowConfidence.visibility =
                if (metadata?.confidence == "low") View.VISIBLE else View.GONE

            // Emergency referral card
            binding.cardEmergency.visibility =
                if (metadata?.isEmergencyReferral == true) View.VISIBLE else View.GONE

            // Escalation card
            binding.cardEscalation.visibility =
                if (metadata?.isEscalation == true) View.VISIBLE else View.GONE

            // FAQ suggestion chips
            val faqs = metadata?.relatedFaqs
            if (!faqs.isNullOrEmpty()) {
                binding.chipGroupFaqs.visibility = View.VISIBLE
                binding.chipGroupFaqs.removeAllViews()
                faqs.forEach { faq ->
                    val chip = Chip(binding.root.context).apply {
                        text = faq
                        isClickable = true
                        setChipBackgroundColorResource(R.color.colorPrimaryLight)
                        setTextColor(context.getColor(R.color.colorPrimary))
                        textSize = 12f
                        setOnClickListener { onFaqClick(faq) }
                    }
                    binding.chipGroupFaqs.addView(chip)
                }
            } else {
                binding.chipGroupFaqs.visibility = View.GONE
            }
        }
    }
}
