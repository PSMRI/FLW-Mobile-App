package org.piramalswasthya.sakhi.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemHrpTrackingListBinding
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenWithHRPTListDomain
import java.util.Calendar

class HRPTListAdapter(
    private val clickListener: HRPTClickListener,
    private vararg val formButtonText: String,
) :
    ListAdapter<BenWithHRPTListDomain, HRPTListAdapter.HRPTViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<BenWithHRPTListDomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithHRPTListDomain, newItem: BenWithHRPTListDomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithHRPTListDomain, newItem: BenWithHRPTListDomain
        ) = oldItem == newItem

    }

    class HRPTViewHolder private constructor(private val binding: RvItemHrpTrackingListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HRPTViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemHrpTrackingListBinding.inflate(layoutInflater, parent, false)
                return HRPTViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithHRPTListDomain,
            clickListener: HRPTClickListener,
            vararg btnText: String?
        ) {
            binding.item = item
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
                    setFormButtonColor(i + 1, item)
                }
            }

            binding.executePendingBindings()

        }

        private fun setFormButtonColor(formNumber: Int, item: BenWithHRPTListDomain) {
            var hasForm: Boolean = false
            var completelyFilled: Boolean = false
            var formEnabled: Boolean = false
            val formButton = when (formNumber) {
                1 -> {
                    binding.btnForm1.also {
                        var trackingDone = item.savedTrackings.size > 3
                        val maxDob = item.savedTrackings.maxByOrNull { it.visited!! }
                        maxDob?.visited?.let {
                            val calToday = Calendar.getInstance().setToStartOfTheDay()
                            val calMaxVisit = Calendar.getInstance()
                            calMaxVisit.timeInMillis = it
                            calMaxVisit.setToStartOfTheDay()

                            trackingDone =
                                trackingDone || ((calToday.timeInMillis - calMaxVisit.timeInMillis) / (1000 * 60 * 60 * 24) < 1)
                        }

                        hasForm = trackingDone
                        formEnabled = !trackingDone
                    }
                }

                2 -> {
                    binding.btnForm2.also {
                        hasForm = item.savedTrackings.isNotEmpty()
                        formEnabled = item.savedTrackings.isNotEmpty()
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
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_green_dark,
                            binding.root.context.theme
                        )
                    )

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
        parent: ViewGroup, viewType: Int
    ) = HRPTViewHolder.from(parent)

    override fun onBindViewHolder(holder: HRPTViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, btnText = formButtonText)
    }


    class HRPTClickListener(
        private val clickedBen: (benId: Long) -> Unit,
        private val clickedForm1: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm2: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm3: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        //        fun onClickedBen(item: HRPAViewHolder) = clickedBen?.let { it() }(item.benId)
        fun onClickForm1(item: BenWithHRPTListDomain) =
            clickedForm1?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickForm2(item: BenWithHRPTListDomain) =
            clickedForm2?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickForm3(item: BenWithHRPTListDomain) =
            clickedForm3?.let { it(item.ben.hhId, item.ben.benId) }
    }


}