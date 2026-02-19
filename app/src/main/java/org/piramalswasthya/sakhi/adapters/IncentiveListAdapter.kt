package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.databinding.RvItemIncentiveBinding
import org.piramalswasthya.sakhi.model.IncentiveDomain


class IncentiveListAdapter( private val fileClickListener: FileClickListener) :
    ListAdapter<IncentiveDomain, IncentiveListAdapter.IncentiveViewHolder>(IncentiveDiffUtilCallBack) {
    private val isMitanin = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)

    private object IncentiveDiffUtilCallBack : DiffUtil.ItemCallback<IncentiveDomain>() {
        override fun areItemsTheSame(
            oldItem: IncentiveDomain, newItem: IncentiveDomain
        ) = oldItem.record.id == newItem.record.id

        override fun areContentsTheSame(
            oldItem: IncentiveDomain, newItem: IncentiveDomain
        ) = oldItem == newItem

    }

    class IncentiveViewHolder private constructor(private val binding: RvItemIncentiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): IncentiveViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemIncentiveBinding.inflate(layoutInflater, parent, false)
                return IncentiveViewHolder(binding)
            }
        }

        fun bind(
            item: IncentiveDomain,serialNo : Int, clickListener: FileClickListener,isMitanin: Boolean
        ) {
            binding.item = item
            binding.serialNo = serialNo
            binding.clickListener = clickListener
            binding.isMitanin = isMitanin

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = IncentiveViewHolder.from(parent)

    override fun onBindViewHolder(holder: IncentiveViewHolder, position: Int) {
        holder.bind(
            getItem(position),position+1,fileClickListener,isMitanin

        )
    }

    class FileClickListener(
        private val onUpload: (item: IncentiveDomain) -> Unit,
        private val onView: (item: IncentiveDomain) -> Unit,
        private val onSubmit: (item: IncentiveDomain) -> Unit
    ) {
        fun onUploadClick(item: IncentiveDomain) = onUpload(item)
        fun onViewClick(item: IncentiveDomain) = onView(item)
        fun onSubmitClick(item: IncentiveDomain) = onSubmit(item)
    }

//
//    class BenClickListener(
//        private val clickedBen: (hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
//        private val clickedHousehold: (hhId: Long) -> Unit,
//        private val clickedABHA: (benId: Long, hhId: Long) -> Unit,
//    ) {
//        fun onClickedBen(item: BenBasicDomain) = clickedBen(
//            item.hhId,
//            item.benId,
//            item.relToHeadId - 1
//        )
//
//        fun onClickedHouseHold(item: BenBasicDomain) = clickedHousehold(item.hhId)
//
//        fun onClickABHA(item: BenBasicDomain) = clickedABHA(item.benId, item.hhId)
//    }

}