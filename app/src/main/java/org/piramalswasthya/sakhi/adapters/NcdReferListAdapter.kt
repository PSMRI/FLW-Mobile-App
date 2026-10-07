    package org.piramalswasthya.sakhi.adapters

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.DiffUtil
    import androidx.recyclerview.widget.ListAdapter
    import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemNcdReferBinding
import org.piramalswasthya.sakhi.R
    import org.piramalswasthya.sakhi.helpers.getLocalizedAge
    import org.piramalswasthya.sakhi.model.BenBasicDomain
    import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain
    import org.piramalswasthya.sakhi.model.ReferalCache

    class NcdReferListAdapter(var userName: String, private val listener: NcdReferallickListener, private val visible: Boolean ) : ListAdapter<BenWithCbacReferDomain, NcdReferListAdapter.BenCbacViewHolder>(
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
                listener: NcdReferallickListener,
                visible: Boolean
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

                if (item.ben.isDeath) {
                    binding.contstraintLayoutV.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.md_theme_dark_outline)
                    )
                        binding.btnFollowUp.visibility = View.INVISIBLE

                } else {
                    binding.contstraintLayoutV.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.md_theme_light_primary)
                    )
                    binding.btnFollowUp.visibility = if (visible) View.VISIBLE else View.GONE
//                    binding.btnFollowUp.setBackgroundColor(binding.root.resources.getColor(if (item.tbrefferalFollowUp == null || item.tbrefferalFollowUp.isEmpty()) android.R.color.holo_red_dark else android.R.color.holo_green_dark))

                    val isFollowUpPending = item.tbrefferalFollowUp.isNullOrEmpty()

                    binding.btnFollowUp.apply {
                        text = context.getString(
                            if (isFollowUpPending) R.string.start_follow_up else R.string.view_follow_up
                        )
                        setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                if (isFollowUpPending) android.R.color.holo_red_dark else android.R.color.holo_green_dark
                            )
                        )
                    }

                }

                binding.executePendingBindings()
                binding.btnFollowUp.setOnClickListener {
                    listener.onClickedFollowUp(item.ben,item.referalCac)
                }
                binding.age.text = getLocalizedAge(binding.root.context, item.ben.dob)


            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): BenCbacViewHolder = BenCbacViewHolder.from(parent)

            override fun onBindViewHolder(holder: BenCbacViewHolder, position: Int) {
            holder.bind(getItem(position) , userName,listener,visible)
        }



        class NcdReferallickListener(
            val goToFollowUp: (benId: Long,hhId:Long,referReason:String?,referredDate:Long) -> Unit

        ) {
            fun onClickedFollowUp(item: BenBasicDomain, referalCac: ReferalCache) = goToFollowUp(
                item.benId,item.hhId,referalCac.referralReason,referalCac.revisitDate
            )
        }




    }
