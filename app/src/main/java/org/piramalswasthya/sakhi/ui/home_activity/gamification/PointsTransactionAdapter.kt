package org.piramalswasthya.sakhi.ui.home_activity.gamification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ItemPointsTransactionBinding
import org.piramalswasthya.sakhi.model.PointsTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PointsTransactionAdapter :
    ListAdapter<PointsTransaction, PointsTransactionAdapter.TxViewHolder>(DiffCallback) {

    inner class TxViewHolder(private val binding: ItemPointsTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tx: PointsTransaction) {
            binding.tvReason.text = tx.reason
            binding.tvPoints.text = "+${tx.points} XP"
            binding.tvDate.text = SimpleDateFormat("dd MMM", Locale.ENGLISH)
                .format(Date(tx.createdAt))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxViewHolder {
        val binding = ItemPointsTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PointsTransaction>() {
        override fun areItemsTheSame(a: PointsTransaction, b: PointsTransaction) = a.id == b.id
        override fun areContentsTheSame(a: PointsTransaction, b: PointsTransaction) = a == b
    }
}
