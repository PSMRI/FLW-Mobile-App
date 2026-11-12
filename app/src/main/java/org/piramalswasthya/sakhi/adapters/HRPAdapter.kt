package org.piramalswasthya.sakhi.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemBenWithHrpaFormBinding
import org.piramalswasthya.sakhi.helpers.getLocalizedAge
import org.piramalswasthya.sakhi.model.BenWithHRPADomain
import org.piramalswasthya.sakhi.utils.RoleConstants

class HRPAdapter(
    private val clickListener: HRPAClickListener? = null,
    private vararg val formButtonText: String,
    private val role: Int? = 0,
    private val pref: PreferenceDao? = null
) :
    ListAdapter<BenWithHRPADomain, HRPAdapter.HRPAViewHolder>
        (BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenWithHRPADomain>() {
        override fun areItemsTheSame(
            oldItem: BenWithHRPADomain,
            newItem: BenWithHRPADomain
        ) = oldItem.ben.benId == newItem.ben.benId

        override fun areContentsTheSame(
            oldItem: BenWithHRPADomain,
            newItem: BenWithHRPADomain
        ) = oldItem == newItem

    }

    class HRPAViewHolder private constructor(private val binding: RvItemBenWithHrpaFormBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HRPAViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemBenWithHrpaFormBinding.inflate(layoutInflater, parent, false)
                return HRPAViewHolder(binding)
            }
        }

        fun bind(
            item: BenWithHRPADomain,
            clickListener: HRPAClickListener?,
            vararg btnText: String,
            role: Int?,
            pref: PreferenceDao?
        ) {

            if (pref?.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
//                binding.btnForm3.visibility = View.INVISIBLE
//                binding.btnForm2.visibility = View.INVISIBLE
//                binding.btnForm1.visibility = View.INVISIBLE
                // Redesign: GONE so the supervisor card doesn't keep an empty button strip
                binding.btnForm3.visibility = View.GONE
                binding.btnForm2.visibility = View.GONE
                binding.btnForm1.visibility = View.GONE
            } else {
                binding.btnForm3.visibility = View.VISIBLE
                binding.btnForm2.visibility = View.VISIBLE
                binding.btnForm1.visibility = View.VISIBLE
            }

            binding.benWithhrpa = item
            binding.hasLmp = false
            item.assess?.let {
                binding.hasLmp = it.lmpDate > 0L
            }
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
//                        binding.btnForm3.text = btnText[2]
                        // Redesign: Share is an icon-only WhatsApp button; label kept for TalkBack
                        binding.btnForm3.contentDescription = btnText[2]
                    }
                }
                for (i in btnText.indices) {
                    setFormButtonColor(i + 1, item, role)
                }
            }
//binding.age.text = getLocalizedAge(binding.root.context, item.ben.dob)
            binding.age.text = dsWifeOfAndAge(binding.root.context, item.ben.spouseName, getLocalizedAge(binding.root.context, item.ben.dob)) // Redesign meta line
            binding.executePendingBindings()

        }

        private fun setFormButtonColor(formNumber: Int, item: BenWithHRPADomain, role: Int?) {
            var hasForm: Boolean = false
            var completelyFilled: Boolean = false
            var formEnabled: Boolean = false
            buttonFlexibleWidth()
//            binding.btnForm3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_whatsapp, 0);
            // Redesign: WhatsApp icon comes from app:icon on btn_form3 (icon-only button)


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
                                        it.rhNegative != null &&
                                        it.homeDelivery != null &&
                                        it.badObstetric != null &&
                                        it.multiplePregnancy != null
                        }

                        formEnabled = true
                    }
                }

                2 -> {
                    binding.btnForm2.also {
                        hasForm = item.mbp != null
                        formEnabled = true
                    }
                }

                3 -> {
                    binding.btnForm3.also {
                        hasForm = item.mbp != null
                        if (hasForm) {

                            formEnabled = true
                            buttonFlexibleWidth()

                        } else {

                            formEnabled = false
//                            (binding.btnForm2.layoutParams as LinearLayout.LayoutParams).weight=1.8f
//                            (binding.btnForm1.layoutParams as LinearLayout.LayoutParams).weight=1.2f

                        }

                    }
                }

                else -> throw IllegalStateException("FormNumber>3")
            }
            formButton.visibility = if (formEnabled) View.VISIBLE else View.GONE


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
                    binding.btnForm3.setBackgroundColor(
                        binding.btnForm3.resources.getColor(
                            android.R.color.holo_purple,
                            binding.root.context.theme
                        )
                    )

                } else {
                    formButton.setBackgroundColor(
                        binding.root.resources.getColor(
                            android.R.color.holo_red_dark,
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
                            android.R.color.holo_red_dark,
                        )
                    )

            formButton.setTextColor(
                binding.root.resources.getColor(
                    com.google.android.material.R.color.design_default_color_on_primary,
                    binding.root.context.theme
                )
            )

            // Redesign (Figma M2.6): same form states, design-system colours —
            // not started = filled primary · partly filled = warning tonal · complete = success tonal; share = outlined
            formButton.dsFormState(if (!hasForm) DsFormState.NOT_STARTED else if (completelyFilled) DsFormState.COMPLETE else DsFormState.PARTIAL)
            if (hasForm) binding.btnForm3.dsFormState(DsFormState.SHARE)
        }

        private fun buttonFlexibleWidth() {
//            (binding.btnForm2.layoutParams as LinearLayout.LayoutParams).weight=1.2f
//            (binding.btnForm1.layoutParams as LinearLayout.LayoutParams).weight=0.8f
//            (binding.btnForm3.layoutParams as LinearLayout.LayoutParams).weight=1.0f
            // Redesign: the two form buttons share the row equally; the icon-only Share keeps its fixed 48dp
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        HRPAViewHolder.from(parent)

    override fun onBindViewHolder(holder: HRPAViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, btnText = formButtonText, role = role, pref = pref)
    }


    class HRPAClickListener(
        private val clickedBen: (benId: Long) -> Unit,
        private val clickedForm1: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm2: ((hhId: Long, benId: Long) -> Unit)? = null,
        private val clickedForm3: ((hhId: Long, benId: Long) -> Unit)? = null

    ) {
        fun onClickForm1(item: BenWithHRPADomain) =
            clickedForm1?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickForm2(item: BenWithHRPADomain) =
            clickedForm2?.let { it(item.ben.hhId, item.ben.benId) }

        fun onClickForm3(item: BenWithHRPADomain) =
            clickedForm3?.let { it(item.ben.hhId, item.ben.benId) }
    }

}