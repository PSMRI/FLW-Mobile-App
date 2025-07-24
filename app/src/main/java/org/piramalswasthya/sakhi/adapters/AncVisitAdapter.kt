package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemPregnancyAncBinding
import org.piramalswasthya.sakhi.model.AncStatus

class AncVisitAdapter(private val clickListener: AncVisitClickListener, private val pref: PreferenceDao? = null) :
    ListAdapter<AncStatus, AncVisitAdapter.AncViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<AncStatus>() {
        override fun areItemsTheSame(
            oldItem: AncStatus, newItem: AncStatus
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: AncStatus, newItem: AncStatus
        ) = oldItem == newItem

    }

    class AncViewHolder private constructor(private val binding: RvItemPregnancyAncBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AncViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPregnancyAncBinding.inflate(layoutInflater, parent, false)
                return AncViewHolder(binding)
            }
        }

        fun bind(
            item: AncStatus, clickListener: AncVisitClickListener, pref: PreferenceDao?, isLastItem: Boolean
        ) {

            if (pref?.getLoggedInUser()?.role.equals("asha", true)) {
                binding.btnView.visibility = View.VISIBLE
            } else {
                binding.btnView.visibility = View.GONE
            }

            binding.visit = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.btnView.setOnClickListener {
                clickListener.onClickedVisit(item, isLastItem)
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = AncViewHolder.from(parent)

    override fun onBindViewHolder(holder: AncViewHolder, position: Int) {
        val isLastItem = position == itemCount - 1
        holder.bind(getItem(position), clickListener, pref, isLastItem)
    }


    class AncVisitClickListener(
        private val clickedForm: (benId: Long, visitNumber: Int, isLast: Boolean) -> Unit
        ) {
        fun onClickedVisit(item: AncStatus, isLast: Boolean) = clickedForm(
            item.benId, item.visitNumber, isLast
        )
    }

}