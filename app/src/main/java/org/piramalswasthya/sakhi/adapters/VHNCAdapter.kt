package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.LayoutVhncItemBinding
import org.piramalswasthya.sakhi.model.VHNCCache

class VHNCAdapter(
    private val clickListener: VHNCClickListener? = null,
) :
    ListAdapter<VHNCCache, VHNCAdapter.VHNCViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<VHNCCache>() {
        override fun areItemsTheSame(
            oldItem: VHNCCache,
            newItem: VHNCCache
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: VHNCCache, newItem: VHNCCache)= oldItem == newItem


    }

    class VHNCViewHolder private constructor(private val binding: LayoutVhncItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): VHNCViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutVhncItemBinding.inflate(layoutInflater, parent, false)
                return VHNCViewHolder(binding)
            }
        }
        fun bind(item: VHNCCache, clickListener: VHNCClickListener?,) {
            binding.vhnd = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }



//        private fun setFormButtonColor(formNumber: Int, item: BenWithHRPADomain, role: Int?) {
//            var hasForm: Boolean = false
//            var completelyFilled: Boolean = false
//            var formEnabled: Boolean = false
//            buttonFlexibleWidth()
////            binding.btnForm3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_whatsapp, 0);
//
//
////            val formButton = when (formNumber) {
////                1 -> {
////                    binding.btnForm1.also {
////                        hasForm = item.assess != null
////                        item.assess?.let {
////                            completelyFilled =
////                                it.noOfDeliveries != null &&
////                                        it.timeLessThan18m != null &&
////                                        it.heightShort != null &&
////                                        it.age != null &&
////                                        it.rhNegative != null &&
////                                        it.homeDelivery != null &&
////                                        it.badObstetric != null &&
////                                        it.multiplePregnancy != null
////                        }
////
////                        formEnabled = true
////                    }
////                }
////
//////                2 -> {
//////                    binding.btnForm2.also {
//////                        hasForm = item.mbp != null
//////                        formEnabled = true
//////                    }
//////                }
//////
//////                3 -> {
//////                    binding.btnForm3.also {
//////                        hasForm = item.mbp != null
//////                        if (hasForm) {
//////
//////                            formEnabled = true
//////                            buttonFlexibleWidth()
//////
//////                        } else {
//////
//////                            formEnabled = false
//////                            (binding.btnForm2.layoutParams as LinearLayout.LayoutParams).weight=1.8f
//////                            (binding.btnForm1.layoutParams as LinearLayout.LayoutParams).weight=1.2f
//////
//////                        }
//////
//////                    }
//////                }
////
////                else -> throw IllegalStateException("FormNumber>3")
////            }
////            formButton.visibility = if (formEnabled) View.VISIBLE else View.GONE
//
//
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                if (hasForm) {
////                    if (completelyFilled) {
////                        formButton.setBackgroundColor(
////                            binding.root.resources.getColor(
////                                android.R.color.holo_green_dark,
////                                binding.root.context.theme
////                            )
////                        )
////                    } else {
////                        formButton.setBackgroundColor(
////                            binding.root.resources.getColor(
////                                android.R.color.holo_orange_dark,
////                                binding.root.context.theme
////                            )
////                        )
////                    }
////                    binding.btnForm3.setBackgroundColor(
////                        binding.btnForm3.resources.getColor(
////                            android.R.color.holo_purple,
////                            binding.root.context.theme
////                        )
////                    )
////
////                } else {
////                    formButton.setBackgroundColor(
////                        binding.root.resources.getColor(
////                            android.R.color.holo_red_light,
////                            binding.root.context.theme
////                        )
////                    )
////                }
////
////            } else
////                if (hasForm)
////                    formButton.setBackgroundColor(binding.root.resources.getColor(android.R.color.holo_green_dark))
////                else
////                    formButton.setBackgroundColor(
////                        binding.root.resources.getColor(
////                            android.R.color.holo_red_light,
////                        )
////                    )
////
////            formButton.setTextColor(
////                binding.root.resources.getColor(
////                    com.google.android.material.R.color.design_default_color_on_primary,
////                    binding.root.context.theme
////                )
////            )
//
//        }

//        private fun buttonFlexibleWidth() {
//            (binding.btnForm2.layoutParams as LinearLayout.LayoutParams).weight=1.2f
//            (binding.btnForm1.layoutParams as LinearLayout.LayoutParams).weight=0.8f
//            (binding.btnForm3.layoutParams as LinearLayout.LayoutParams).weight=1.0f
//        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        VHNCViewHolder.from(parent)

    override fun onBindViewHolder(holder: VHNCViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class VHNCClickListener(
        private val clickedForm: ((id: Int) -> Unit)? = null
    ) {
        fun onClickForm1(item: VHNCCache) = clickedForm?.let { it(item.id!!) }
    }

}