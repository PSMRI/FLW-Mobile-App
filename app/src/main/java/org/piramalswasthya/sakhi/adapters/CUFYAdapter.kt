package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ChildrenUnderFiveYearsItemBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants

class CUFYAdapter(
    private val clickListener: ChildListClickListener
) :
    ListAdapter<BenBasicDomain, CUFYAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomain>() {
        override fun areItemsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: ChildrenUnderFiveYearsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChildrenUnderFiveYearsItemBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenBasicDomain,
            clickListener: ChildListClickListener
        ) {
            binding.ben = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class ChildListClickListener(
        val goToForm: (benId: Long, hhId: Long, dob: Long, type: String) -> Unit

    ) {
        fun onClickedSAM(item: BenBasicDomain) = goToForm(
            item.benId, item.hhId, item.dob, FormConstants.SAM_FORM_NAME
        )
        fun onClickedORS(item: BenBasicDomain) = goToForm(
            item.benId, item.hhId, item.dob, FormConstants.ORS_FORM_NAME
        )
        fun onClickedIFA(item: BenBasicDomain) = goToForm(
            item.benId, item.hhId, item.dob, FormConstants.IFA_FORM_NAME
        )
    }

}