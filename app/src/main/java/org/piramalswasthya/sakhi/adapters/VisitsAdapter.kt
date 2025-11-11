package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R

class VisitsAdapter(
    private val visitNumbers: List<Int>,
    private val onVisitClick: (Int) -> Unit
) : RecyclerView.Adapter<VisitsAdapter.VisitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visitNumber = visitNumbers[position]
        holder.bind(visitNumber)
    }

    override fun getItemCount() = visitNumbers.size

    inner class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tvVisitNumber)
        private val container: View = itemView.findViewById(R.id.container)

        fun bind(visitNumber: Int) {
            textView.text = "Visit $visitNumber"
            container.setOnClickListener {
                onVisitClick(visitNumber)
            }
        }
    }
}