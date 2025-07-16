package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard

class VisitCardAdapter(
    private val visits: List<VisitCard>,
    private val onVisitClick: (VisitCard) -> Unit
) : RecyclerView.Adapter<VisitCardAdapter.VisitViewHolder>() {

    inner class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVisitDay: TextView = view.findViewById(R.id.tvVisitDay)
        val tvVisitDate: TextView = view.findViewById(R.id.tvVisitDate)
        val tvStatus: TextView = view.findViewById(R.id.tvVisitStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.tvVisitDay.text = visit.visitDay
        holder.tvVisitDate.text = visit.visitDate

        holder.tvStatus.text = when {
            visit.isCompleted -> "Completed (View)"
            visit.isEditable -> "Eligible (Fill Form)"
            else -> "Locked"
        }

        holder.itemView.isEnabled = visit.isCompleted || visit.isEditable
        holder.itemView.alpha = if (holder.itemView.isEnabled) 1.0f else 0.5f

        holder.itemView.setOnClickListener {
            if (holder.itemView.isEnabled) {
                onVisitClick(visit)
            }
        }
    }

    override fun getItemCount(): Int = visits.size
}
