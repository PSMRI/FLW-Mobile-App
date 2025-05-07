package org.piramalswasthya.sakhi.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemBenWithFormBinding
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm

class BenListAdapterForForm(
    private val clickListener: ClickListener? = null,
    private vararg val formButtonText: String,
    private val role: Int? = 0,
    private val pref: PreferenceDao? = null
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
            role: Int?,
            pref: PreferenceDao?
        ) {

            if (pref?.getLoggedInUser()?.role.equals("asha", true)) {
                binding.btnForm1.visibility = View.VISIBLE
                binding.btnForm2.visibility = View.VISIBLE
                binding.btnForm3.visibility = View.VISIBLE
            } else {
                binding.btnForm1.visibility = View.INVISIBLE
                binding.btnForm2.visibility = View.INVISIBLE
                binding.btnForm3.visibility = View.INVISIBLE
            }

            binding.ben = item
            binding.clickListener = clickListener
            if (btnText.isNotEmpty()) {

                when (btnText.size) {
                    1 -> {
                        binding.btnForm1.text = btnText[0]
//                        binding.btnForm1Cho.text = btnText[0]
                    }

                    2 -> {
                        binding.btnForm1.text = btnText[0]
//                        binding.btnForm1Cho.text = btnText[0]
                        binding.btnForm2.text = btnText[1]
//                        binding.btnForm2Cho.text = btnText[1]
                    }

                    3 -> {
                        binding.btnForm1.text = btnText[0]
//                        binding.btnForm1Cho.text = btnText[0]
                        binding.btnForm2.text = btnText[1]
//                        binding.btnForm2Cho.text = btnText[1]
                        binding.btnForm3.text = btnText[2]
//                        binding.btnForm3Cho.text = btnText[2]
                    }
                }
                for (i in btnText.indices) {
                    setFormButtonColor(i + 1, item, role)
                }
            }
            binding.role = role
            binding.hasLmp = !item.lastMenstrualPeriod.isNullOrEmpty()
            binding.executePendingBindings()

        }

        private fun setFormButtonColor(formNumber: Int, item: BenBasicDomainForForm, role: Int?) {
            var hasForm: Boolean
            var formEnabled: Boolean
            val formButton = when (formNumber) {
                1 -> {
                    binding.btnForm1.also {
                        hasForm = item.form1Filled
                        formEnabled = item.form1Enabled
                    }
                }

                2 -> {
                    binding.btnForm2.also {
                        hasForm = item.form2Filled
                        formEnabled = item.form2Enabled
                    }
                }

                3 -> {
                    binding.btnForm3.also {
                        hasForm = item.form3Filled
                        formEnabled = item.form3Enabled
                    }
                }

                else -> throw IllegalStateException("FormNumber>3")
            }
            formButton.visibility = if (formEnabled) View.VISIBLE else View.INVISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (hasForm)
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_green_dark,
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
        BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, btnText = formButtonText, role = role, pref = pref)
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