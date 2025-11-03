package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.ChildrenUnderFiveYearsItemBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.CUFYListViewModel
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants

class CUFYAdapter(
    private val clickListener: ChildListClickListener,

) :
    ListAdapter<CUFYListViewModel.BenWithSamStatus, CUFYAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<CUFYListViewModel.BenWithSamStatus>() {
        override fun areItemsTheSame(
            oldItem: CUFYListViewModel.BenWithSamStatus, newItem: CUFYListViewModel.BenWithSamStatus
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: CUFYListViewModel.BenWithSamStatus, newItem: CUFYListViewModel.BenWithSamStatus
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
            item: CUFYListViewModel.BenWithSamStatus,
            clickListener: ChildListClickListener
        ) {
            binding.ben =  item.ben
            binding.clickListener = clickListener
            binding.samStatus = item.samStatus
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)

    }

    suspend fun updateSamStatus(benId: Long, status: String) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.ben.benId == benId }
        if (index != -1) {
            val updatedItem = currentList[index].copy(samStatus = status)
            currentList[index] = updatedItem
            submitList(currentList.toList())
        }
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