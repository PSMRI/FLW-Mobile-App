package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemKalaAzarMemberListBinding
import org.piramalswasthya.sakhi.model.BenWithKALAZARScreeningDomain

class KalaAzarMemberListAdapter(
    private val clickListener: ClickListener? = null
) :
    ListAdapter<BenWithKALAZARScreeningDomain, KalaAzarMemberListAdapter.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithKALAZARScreeningDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithKALAZARScreeningDomain,
            newItem: BenWithKALAZARScreeningDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithKALAZARScreeningDomain,
            newItem: BenWithKALAZARScreeningDomain
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemKalaAzarMemberListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemKalaAzarMemberListBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithKALAZARScreeningDomain,
            clickListener: ClickListener?,
        ) {
            binding.benWithKalaAzar = item

            /*if(item.tb?.historyOfTb == true){
                binding.cvContent.visibility = View.GONE
            }*/

            binding.ivSyncState.visibility = if (item.kala == null) View.GONE else View.VISIBLE

            if (item.ben.spouseName == "Not Available" && item.ben.fatherName == "Not Available") {
                binding.father = true
                binding.husband = false
                binding.spouse = false
            } else {
                if (item.ben.gender == "MALE") {
                    binding.father = true
                    binding.husband = false
                    binding.spouse = false
                } else if (item.ben.gender == "FEMALE") {
                    if (item.ben.ageInt > 15) {
                        binding.father =
                            item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                        binding.husband = item.ben.spouseName != "Not Available"
                        binding.spouse = false
                    } else {
                        binding.father = true
                        binding.husband = false
                        binding.spouse = false
                    }
                } else {
                    binding.father =
                        item.ben.fatherName != "Not Available" && item.ben.spouseName == "Not Available"
                    binding.spouse = item.ben.spouseName != "Not Available"
                    binding.husband = false
                }
            }
            if ( item.kala != null && item.ben.isDeath) {
                binding.btnFormTb.visibility = View.VISIBLE
            } else if (item.kala == null && !item.ben.isDeath){
                binding.btnFormTb.visibility = View.VISIBLE
            } else if (item.kala != null && !item.ben.isDeath){
                binding.btnFormTb.visibility = View.VISIBLE
            }
            else {
                binding.btnFormTb.visibility = View.INVISIBLE
            }
            binding.btnFormTb.text = if (item.kala == null) "Register" else "View"
            binding.btnFormTb.setBackgroundColor(binding.root.resources.getColor(if (item.kala == null) android.R.color.holo_red_dark else android.R.color.holo_green_dark))
            binding.clickListener = clickListener

            binding.executePendingBindings()

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class ClickListener(
        private val clickedForm: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickForm(item: BenWithKALAZARScreeningDomain) =
            clickedForm?.let { it(item.ben.hhId, item.ben.benId) }
    }

}