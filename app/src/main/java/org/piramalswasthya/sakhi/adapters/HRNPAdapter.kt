package org.piramalswasthya.sakhi.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenWithHrnpaFormBinding
import org.piramalswasthya.sakhi.model.BenWithHRNPADomain

class HRNPAdapter(
    private val clickListener: ClickListener? = null,
    private vararg val formButtonText: String,
    private val role: Int? = 0
) :
    ListAdapter<BenWithHRNPADomain, HRNPAdapter.HRPAViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithHRNPADomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithHRNPADomain,
            newItem: BenWithHRNPADomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithHRNPADomain,
            newItem: BenWithHRNPADomain
        ) = oldItem == newItem

    }

    class HRPAViewHolder private constructor(private val binding: RvItemBenWithHrnpaFormBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HRPAViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenWithHrnpaFormBinding.inflate(layoutInflater, parent, false)
                return HRPAViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithHRNPADomain,
            clickListener: ClickListener?,
            vararg btnText: String,
            role: Int?
        ) {
            binding.benWithhrnpa = item
            binding.clickListener = clickListener
            if (btnText.isNotEmpty()) {

                when (btnText.size) {
                    1 -> {
                        binding.btnForm1.text = btnText[0]
                    }

                    2 -> {
                        binding.btnForm1.text = btnText[0]
                        binding.btnForm2.text = btnText[1]
                    }

                    3 -> {
                        binding.btnForm1.text = btnText[0]
                        binding.btnForm2.text = btnText[1]
                        binding.btnForm3.text = btnText[2]
                    }
                }
                for (i in btnText.indices) {
                    setFormButtonColor(i + 1, item, role)
                }
            }
            binding.executePendingBindings()

        }

        private fun setFormButtonColor(formNumber: Int, item: BenWithHRNPADomain, role: Int?) {
            var hasForm: Boolean = false
            var completelyFilled: Boolean = false
            var formEnabled: Boolean = false
            val formButton = when (formNumber) {
                1 -> {
                    binding.btnForm1.also {
                        hasForm = item.assess != null
                        item.assess?.let {
                            completelyFilled =
                                it.noOfDeliveries != null &&
                                        it.timeLessThan18m != null &&
                                        it.heightShort != null &&
                                        it.age != null &&
                                        it.misCarriage != null &&
                                        it.homeDelivery != null &&
                                        it.medicalIssues != null &&
                                        it.pastCSection != null
                        }

                        formEnabled = true
                    }
                }

                2 -> {
                    binding.btnForm2.also {
//                        hasForm = item.ben.form2Filled
//                        formEnabled = item.ben.form2Enabled
                    }
                }

                3 -> {
                    binding.btnForm3.also {
//                        hasForm = item.assess.noOfDeliveries.form3Filled
//                        formEnabled = item.ben.form3Enabled
                    }
                }

                else -> throw IllegalStateException("FormNumber>3")
            }
            formButton.visibility = if (formEnabled) View.VISIBLE else View.INVISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (hasForm) {
                    if (completelyFilled) {
                        formButton.setBackgroundColor(
                            binding.root.resources.getColor(
                                android.R.color.holo_green_dark,
                                binding.root.context.theme
                            )
                        )
                    } else {
                        formButton.setBackgroundColor(
                            binding.root.resources.getColor(
                                android.R.color.holo_orange_dark,
                                binding.root.context.theme
                            )
                        )
                    }

                } else {
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_red_light,
                            binding.root.context.theme
                        )
                    )
                }

            } else
                if (hasForm)
                    formButton.setBackgroundColor(binding.root.resources.getColor(android.R.color.holo_green_dark))
                else
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_red_light,
                        )
                    )

            formButton.setTextColor(
                binding.root.resources.getColor(
                    com.google.android.material.R.color.design_default_color_on_primary,
                    binding.root.context.theme
                )
            )

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        HRPAViewHolder.from(parent)

    override fun onBindViewHolder(holder: HRPAViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, btnText = formButtonText, role = role)
    }


    class ClickListener(
        private val clickedBen: (benId: Long) -> Unit,
        private val clickedForm1: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm2: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm3: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        //        fun onClickedBen(item: HRPAViewHolder) = clickedBen?.let { it() }(item.benId)
        fun onClickForm1(item: BenWithHRNPADomain) =
            clickedForm1?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickForm2(item: BenWithHRNPADomain) =
            clickedForm2?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickForm3(item: BenWithHRNPADomain) =
            clickedForm3?.let { it(item.ben.hhId, item.ben.benId) }
    }

}