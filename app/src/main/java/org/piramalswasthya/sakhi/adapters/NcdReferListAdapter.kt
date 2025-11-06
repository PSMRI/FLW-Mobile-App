package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemNcdReferBinding
import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain

class NcdReferListAdapter(var userName: String) : ListAdapter<BenWithCbacReferDomain, NcdReferListAdapter.BenCbacViewHolder>(
    BenDiffUtilCallBack
) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithCbacReferDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithCbacReferDomain, newItem: BenWithCbacReferDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithCbacReferDomain, newItem: BenWithCbacReferDomain
        ) = oldItem == newItem

    }

    class BenCbacViewHolder private constructor(private val binding: RvItemNcdReferBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenCbacViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemNcdReferBinding.inflate(layoutInflater, parent, false)
                return BenCbacViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithCbacReferDomain,
            userName: String,
        ) {
            binding.benWithCbac = item
            binding.referredFrom.text = userName

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

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BenCbacViewHolder = BenCbacViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenCbacViewHolder, position: Int) {
        holder.bind(getItem(position) , userName)
    }






}