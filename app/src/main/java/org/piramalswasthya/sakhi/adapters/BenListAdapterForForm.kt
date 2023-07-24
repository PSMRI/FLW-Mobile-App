package org.piramalswasthya.sakhi.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.databinding.RvItemBenWithFormBinding
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm

class BenListAdapterForForm(
    private val clickListener: ClickListener? = null,
    private vararg val formButtonText: String
) :
    ListAdapter<BenBasicDomainForForm, BenListAdapterForForm.BenViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomainForForm>() {
        override fun areItemsTheSame(
            oldItem: BenBasicDomainForForm,
            newItem: BenBasicDomainForForm
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: BenBasicDomainForForm,
            newItem: BenBasicDomainForForm
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: RvItemBenWithFormBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenWithFormBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: BenBasicDomainForForm,
            clickListener: ClickListener?,
            vararg btnText: String,
        ) {
            binding.ben = item
            binding.clickListener = clickListener
            if (btnText.isNotEmpty()) {
                binding.btnForm1.text = btnText[0]
                for (i in btnText.indices) {
                    setFormButtonColor(i + 1, item)
                }
            }
            binding.executePendingBindings()

        }

        private fun setFormButtonColor(formNumber: Int, item: BenBasicDomainForForm) {
            var hasForm: Boolean
            var formEnabled: Boolean
            val formButton = when (formNumber) {
                1 -> binding.btnForm1.also {
                    hasForm = item.form1Filled
                    formEnabled = item.form1Enabled
                }

                2 -> binding.btnForm2.also {
                    hasForm = item.form2Filled
                    formEnabled = item.form1Enabled
                }

                3 -> binding.btnForm3.also {
                    hasForm = item.form3Filled
                    formEnabled = item.form1Enabled
                }

                else -> throw IllegalStateException("FormNumber>3")
            }
            formButton.visibility = if (formEnabled) View.VISIBLE else View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (hasForm)
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_green_light,
                            binding.root.context.theme
                        )
                    )
                else
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_red_light,
                            binding.root.context.theme
                        )
                    )
            } else
                if (hasForm)
                    formButton.setBackgroundColor(binding.root.resources.getColor(android.R.color.holo_green_light))
                else
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_red_light,
                        )
                    )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, btnText = formButtonText)
    }


    class ClickListener(
        private val clickedBen: (benId: Long) -> Unit,
        private val clickedForm1: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm2: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm3: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickedBen(item: BenBasicDomainForForm) = clickedBen(item.benId)
        fun onClickForm1(item: BenBasicDomainForForm) =
            clickedForm1?.let { it(item.hhId, item.benId) }

        fun onClickForm2(item: BenBasicDomainForForm) =
            clickedForm2?.let { it(item.hhId, item.benId) }

        fun onClickForm3(item: BenBasicDomainForForm) =
            clickedForm3?.let { it(item.hhId, item.benId) }
    }

}