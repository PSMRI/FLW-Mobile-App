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
        val btnView: View = view.findViewById(R.id.btnView)
        val btnAddVisit: View = view.findViewById(R.id.btnAddVisit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view)
    }

//    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
//        val visit = visits[position]
//        holder.tvVisitDay.text = visit.visitDay
//        holder.tvVisitDate.text = visit.visitDate
//
//        holder.tvStatus.text = when {
//            visit.isCompleted -> "Completed (View)"
//            visit.isEditable -> "Eligible (Fill Form)"
//            else -> "Locked"
//        }
//
//        holder.itemView.isEnabled = visit.isCompleted || visit.isEditable
//        holder.itemView.alpha = if (holder.itemView.isEnabled) 1.0f else 0.5f
//
//        holder.itemView.setOnClickListener {
//            if (holder.itemView.isEnabled) {
//                onVisitClick(visit)
//            }
//        }
//    }
override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
    val visit = visits[position]
    holder.tvVisitDay.text = visit.visitDay
    holder.tvVisitDate.text = visit.visitDate

    // Hide both buttons
    holder.btnView.visibility = View.GONE
    holder.btnAddVisit.visibility = View.GONE



    when {
        visit.isCompleted -> {
            holder.btnView.visibility = View.VISIBLE
            holder.itemView.setBackgroundResource(R.color.holo_green_dark)
            holder.itemView.isEnabled = true
            holder.itemView.setOnClickListener {
                onVisitClick(visit)
            }
        }
        visit.isEditable -> {
            holder.btnAddVisit.visibility = View.VISIBLE
            holder.itemView.setBackgroundResource(R.color.Quartenary)
            holder.itemView.isEnabled = true
            holder.itemView.setOnClickListener {
                onVisitClick(visit)
            }
        }
        else -> {
            holder.itemView.setBackgroundResource(R.color.read_only)
            holder.itemView.isEnabled = false
//            holder.itemView.alpha = if (holder.itemView.isEnabled) 1.0f else 0.5f
            holder.itemView.setOnClickListener(null)
        }
    }
}
    override fun getItemCount(): Int = visits.size
}
