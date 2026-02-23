package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.RejectionReason

class RejectionReasonAdapter(
    private val onReasonSelected: (RejectionReason, Boolean) -> Unit
) : RecyclerView.Adapter<RejectionReasonAdapter.ReasonViewHolder>() {

    private var reasons = mutableListOf<RejectionReason>()

    fun submitList(newReasons: List<RejectionReason>) {
        reasons.clear()
        reasons.addAll(newReasons)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = reasons.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rejection_reason, parent, false)
        return ReasonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReasonViewHolder, position: Int) {
        holder.bind(reasons[position], onReasonSelected)
    }

    class ReasonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cbReason: CheckBox = itemView.findViewById(R.id.cbReason)

        fun bind(reason: RejectionReason, onReasonSelected: (RejectionReason, Boolean) -> Unit) {
            // First, remove any existing listeners
            cbReason.setOnCheckedChangeListener(null)
            cbReason.setOnClickListener(null)
            itemView.setOnClickListener(null)

            // Set the state
            cbReason.text = reason.reason
            cbReason.isChecked = reason.isSelected

            // Set click listener on checkbox
            cbReason.setOnClickListener {
                val newState = cbReason.isChecked
                reason.isSelected = newState
                onReasonSelected(reason, newState)
            }

            // Also make the whole item clickable
            itemView.setOnClickListener {
                cbReason.performClick()
            }
        }
    }
}