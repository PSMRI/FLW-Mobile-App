package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.HouseHoldBasicDomain


class BenListAdapter(
    private val clickListener: BenClickListener? = null,
    private val showBeneficiaries: Boolean = false,
    private val showRegistrationDate: Boolean = false,
    private val showSyncIcon: Boolean = false,
    private val showAbha: Boolean = false,
    private val showCall: Boolean = false,
    private val role: Int? = 0
) :
    ListAdapter<BenBasicDomain, BenListAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomain>() {
        override fun areItemsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: BenBasicDomain, newItem: BenBasicDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenBasicDomain,
            clickListener: BenClickListener?,
            showAbha: Boolean,
            showSyncIcon: Boolean,
            showRegistrationDate: Boolean,
            showBeneficiaries: Boolean, role: Int?,
            showCall: Boolean,
        ) {
            if (!showSyncIcon) item.syncState = null
            binding.ben = item
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.showRegistrationDate = showRegistrationDate
            binding.registrationDate.visibility =
                if (showRegistrationDate) View.VISIBLE else View.INVISIBLE
            binding.blankSpace.visibility =
                if (showRegistrationDate) View.VISIBLE else View.INVISIBLE
            binding.hasAbha = !item.abhaId.isNullOrEmpty()
            binding.role = role

            if (showCall) {
                binding.ivCall.visibility = View.VISIBLE
            } else {
                binding.ivCall.visibility = View.GONE
            }

            if (showBeneficiaries) {
                if (item.spouseName == "Not Available" && item.fatherName == "Not Available") {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                } else {
                    if (item.gender == "MALE") {
                        binding.father = true
                        binding.husband = false
                        binding.spouse = false
                    } else if (item.gender == "FEMALE") {
                        if (item.ageInt > 15) {
                            binding.father =
                                item.fatherName != "Not Available" && item.spouseName == "Not Available"
                            binding.husband = item.spouseName != "Not Available"
                            binding.spouse = false
                        } else {
                            binding.father = true
                            binding.husband = false
                            binding.spouse = false
                        }
                    } else {
                        binding.father =
                            item.fatherName != "Not Available" && item.spouseName == "Not Available"
                        binding.spouse = item.spouseName != "Not Available"
                        binding.husband = false
                    }
                }
            } else {
                binding.father = false
                binding.husband = false
                binding.spouse = false
            }
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            clickListener,
            showAbha,
            showSyncIcon,
            showRegistrationDate,
            showBeneficiaries,
            role,
            showCall
        )
    }


    class BenClickListener(
        private val clickedBen: (hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedHousehold: (hhId: Long) -> Unit,
        private val clickedABHA: (benId: Long, hhId: Long) -> Unit,
        private val callBen: (ben: BenBasicDomain) -> Unit
    ) {
        fun onClickedBen(item: BenBasicDomain) = clickedBen(
            item.hhId,
            item.benId,
            item.relToHeadId - 1
        )

        fun onClickedHouseHold(item: BenBasicDomain) = clickedHousehold(item.hhId)

        fun onClickABHA(item: BenBasicDomain) = clickedABHA(item.benId, item.hhId)

        fun onClickedForCall(item: BenBasicDomain) = callBen(item)
    }

}