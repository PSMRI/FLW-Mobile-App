package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard

class VisitCardAdapter(
    private var visits: List<VisitCard>,
    private var isBenDead: Boolean,
    private val onVisitClick: (VisitCard) -> Unit
) : RecyclerView.Adapter<VisitCardAdapter.VisitViewHolder>() {

    inner class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVisitDay: TextView = view.findViewById(R.id.tvVisitDay)
        val tvVisitDate: TextView = view.findViewById(R.id.tvVisitDate)
        val btnView: View = view.findViewById(R.id.btnView)
        val btnAddVisit: View = view.findViewById(R.id.btnAddVisit)
        init {
            btnView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVisitClick(visits[position])
                }
            }
            btnAddVisit.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVisitClick(visits[position])
                }
            }
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view)
    }

    fun updateVisits(newVisits: List<VisitCard>) {
        visits = newVisits
        notifyDataSetChanged()
    }

    fun updateDeathStatus(dead: Boolean) {
        isBenDead = dead
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = visits[position]
        holder.tvVisitDay.text = visit.visitDay
        holder.tvVisitDate.text = visit.visitDate
        holder.btnView.visibility = View.GONE
        holder.btnAddVisit.visibility = View.GONE

        when {
            visit.isCompleted -> {
                holder.btnView.visibility = View.VISIBLE
                holder.btnView.setBackgroundResource(R.color.Quartenary)
                holder.itemView.setBackgroundResource(R.color.md_theme_dark_inversePrimary)
                holder.itemView.isEnabled = true
                holder.btnView.isEnabled = true
            }

            visit.isEditable -> {
                val enabled = !isBenDead
                holder.itemView.isEnabled = enabled
                holder.btnAddVisit.isEnabled = enabled
                holder.btnView.isEnabled = enabled

                holder.btnAddVisit.visibility = if (enabled) View.VISIBLE else View.GONE
                holder.btnAddVisit.setBackgroundResource(if (enabled) R.color.Quartenary else R.color.read_only)
                holder.itemView.setBackgroundResource(
                    if (enabled) R.color.md_theme_dark_inversePrimary else R.color.read_only
                )
            }


            else -> {
                holder.itemView.setBackgroundResource(R.color.read_only)
                holder.itemView.isEnabled = false
                holder.btnView.isEnabled = false
                holder.btnAddVisit.isEnabled = false
            }
        }
    }

    override fun getItemCount(): Int = visits.size
}
