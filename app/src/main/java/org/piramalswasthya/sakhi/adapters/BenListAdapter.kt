package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.RvItemBenBinding
import org.piramalswasthya.sakhi.helpers.getDateFromLong
import org.piramalswasthya.sakhi.helpers.getPatientTypeByAge
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.utils.RoleConstants


class BenListAdapter(
    private val clickListener: BenClickListener? = null,
    private val showBeneficiaries: Boolean = false,
    private val showRegistrationDate: Boolean = false,
    private val showSyncIcon: Boolean = false,
    private val showAbha: Boolean = false,
    private val showCall: Boolean = false,
    private val role: Int? = 0,
    private val pref: PreferenceDao? = null,
    var context: FragmentActivity,
    private val isSoftDeleteEnabled:Boolean = false,
) :
    ListAdapter<BenBasicDomain, BenListAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomain>() {
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
            isSoftDeleteEnabled:Boolean,
            pref: PreferenceDao?,
            context : FragmentActivity,
            benIdList: List<Long>,
            childCountMap: Map<Long, Int> = emptyMap()
        ) {

            if (pref?.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
                binding.btnAbha.visibility = View.GONE
            } else {
                binding.btnAbha.visibility = View.VISIBLE
            }
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

            val isMatched = benIdList.contains(item.benId)
            binding.isMatched = isMatched

            binding.btnAbove30.text = if (isMatched) {
                binding.root.context.getString(R.string.view_edit_eye_surgery)
            } else {
                binding.root.context.getString(R.string.add_eye_surgery)
            }

            binding.executePendingBindings()

           var gender = item.gender.toString()

            if (item.relToHeadId == 19) {
                binding.HOF.visibility = View.VISIBLE
            } else {
                binding.HOF.visibility = View.GONE
            }

            if (item.dob != null) {
                val type = getPatientTypeByAge(getDateFromLong(item.dob))
                when (type) {

                    "new_born_baby" -> {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_icon_baby)
                    }

                    "infant" -> {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_infant)
                    }

                    "child", "adolescence" -> {
                        when (gender) {
                            Gender.MALE.name -> {
                                binding.ivHhLogo.setImageResource(R.drawable.ic_icon_boy_ben)
                            }
                            Gender.FEMALE.name -> {
                                binding.ivHhLogo.setImageResource(R.drawable.ic_girl)
                            }
                            else -> {
                                // Intentionally left blank (no icon change)
                            }
                        }
                    }

                    "adult" -> {
                        when (gender) {
                            Gender.MALE.name -> {
                                binding.ivHhLogo.setImageResource(R.drawable.ic_males)
                            }
                            Gender.FEMALE.name -> {
                                binding.ivHhLogo.setImageResource(R.drawable.ic_icon_female_2)
                            }
                            else -> {
                                binding.ivHhLogo.setImageResource(R.drawable.ic_unisex)
                            }
                        }
                    }
                }

            }

            val effectiveChildCount = childCountMap[item.benId] ?: item.noOfChildren

            when {
                item.gender == "MALE" && !item.isSpouseAdded && item.isMarried -> {
                    binding.btnAddSpouse.visibility = View.VISIBLE
                    binding.btnAddChildren.visibility = View.INVISIBLE
                    binding.llAddSpouseBtn.visibility = View.VISIBLE
                    binding.btnAddSpouse.text = context.getString(R.string.add_wife)
                    binding.btnAddSpouse.setOnClickListener {
                        clickListener?.onClickedWifeBen(item)
                    }
                }

                item.gender == "FEMALE" && !item.isSpouseAdded && item.isMarried -> {
                    binding.btnAddSpouse.visibility = View.VISIBLE
                    binding.btnAddChildren.visibility = View.INVISIBLE
                    binding.llAddSpouseBtn.visibility = View.VISIBLE
                    binding.btnAddSpouse.text = context.getString(R.string.add_husband)
                    binding.btnAddSpouse.setOnClickListener {
                        clickListener?.onClickedHusbandBen(item)
                    }
                }

                item.gender == "FEMALE" &&
                        item.isMarried &&
                        effectiveChildCount == 0  -> {

                    binding.btnAddChildren.visibility = View.VISIBLE
                    binding.btnAddSpouse.visibility = View.GONE
                    binding.llAddSpouseBtn.visibility = View.VISIBLE
                    binding.btnAddChildren.setOnClickListener {
                        clickListener?.onClickChildBen(item)
                    }
                }

               /* item.gender == "FEMALE" &&
                        item.isMarried &&
                        !item.doYouHavechildren &&
                        !item.isChildrenAdded -> {

                    binding.btnAddChildren.visibility = View.INVISIBLE
                    binding.btnAddSpouse.visibility = View.GONE
                    binding.llAddSpouseBtn.visibility = View.VISIBLE
                }*/

                item.gender == "FEMALE" &&
                        item.isMarried &&
                        effectiveChildCount != 0  -> {

                    binding.btnAddChildren.visibility = View.VISIBLE
                    binding.btnAddChildren.text = context.getString(R.string.view_children)
                    binding.btnAddSpouse.visibility = View.GONE
                    binding.llAddSpouseBtn.visibility = View.VISIBLE
                    binding.btnAddChildren.setOnClickListener {
                        clickListener?.onClickChildBen(item)
                    }
                }

                else -> {
                    binding.btnAddSpouse.visibility = View.GONE
                    binding.btnAddChildren.visibility = View.INVISIBLE
                    binding.llAddSpouseBtn.visibility = View.GONE
                }
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
            if (item.isDeath){
                binding.contentLayout.setBackgroundColor(ContextCompat.getColor(binding.contentLayout.context, R.color.md_theme_dark_outline))
                binding.ivCall.visibility = View.GONE
            }else{
                binding.contentLayout.setBackgroundColor(ContextCompat.getColor(binding.contentLayout.context, R.color.md_theme_light_primary))
            }

            if (isSoftDeleteEnabled){
                binding.ivSoftDelete.visibility = View.VISIBLE

                if (item.isDeactivate){
                    binding.contentLayout.setBackgroundColor(ContextCompat.getColor(binding.contentLayout.context, R.color.Quartenary))
                    binding.ivSoftDelete.visibility = View.GONE

                    binding.btnAbha.visibility = View.INVISIBLE
                    binding.tvTitleDuplicaterecord.visibility = View.VISIBLE
                    binding.ivCall.visibility = View.INVISIBLE
                    binding.ivSyncState.visibility = View.INVISIBLE
                    binding.llBenDetails4.visibility = View.GONE
                    binding.llAddSpouseBtn.visibility = View.GONE


                }else{
                    binding.contentLayout.setBackgroundColor(ContextCompat.getColor(binding.contentLayout.context, R.color.md_theme_light_primary))

                    binding.btnAbha.visibility = View.VISIBLE
                    binding.tvTitleDuplicaterecord.visibility = View.GONE
                    binding.llBenDetails4.visibility = View.VISIBLE
                    binding.ivCall.visibility = View.VISIBLE
                    binding.ivSyncState.visibility = View.VISIBLE
                    binding.llAddSpouseBtn.visibility = View.VISIBLE

                }
            }else{
                binding.ivSoftDelete.visibility = View.GONE
            }


            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    private val benIds = mutableListOf<Long>()

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            clickListener,
            showAbha,
            showSyncIcon,
            showRegistrationDate,
            showBeneficiaries,
            role,
            showCall,
            isSoftDeleteEnabled,
            pref,
            context,
            benIds
        )
    }
    fun submitBenIds(list: List<Long>) {
        val oldIds = benIds.toSet()
        benIds.clear()
        benIds.addAll(list)
        val newIds = benIds.toSet()
        val changed = (oldIds - newIds) + (newIds - oldIds)
        if (changed.isNotEmpty()) {
            currentList.forEachIndexed { index, item ->
                if (item.benId in changed) {
                    notifyItemChanged(index)
                }
            }
        }
    }



    class BenClickListener(
        private val clickedBen: (item:BenBasicDomain,hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedWifeBen: (item:BenBasicDomain,hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedHusbandBen: (item:BenBasicDomain,hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedChildben: (item:BenBasicDomain,hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedHousehold: (item:BenBasicDomain,hhId: Long) -> Unit,
        private val clickedABHA: (item:BenBasicDomain,benId: Long, hhId: Long) -> Unit,
        private val clickedAddAllBenBtn: (item:BenBasicDomain,benId: Long, hhId: Long, isViewMode: Boolean, isIFA: Boolean) -> Unit,
        private val callBen: (ben: BenBasicDomain) -> Unit,
        private val softDeleteBen: (ben: BenBasicDomain) -> Unit
    ) {
        fun onClickedBen(item: BenBasicDomain) = clickedBen(
            item,
            item.hhId,
            item.benId,
            item.relToHeadId - 1
        )


        fun onClickedWifeBen(item: BenBasicDomain) = clickedWifeBen(
            item,
            item.hhId,
            item.benId,
            item.relToHeadId
        )



        fun onClickedHusbandBen(item: BenBasicDomain) = clickedHusbandBen(
            item,
            item.hhId,
            item.benId,
            item.relToHeadId
        )

        fun onClickChildBen(item: BenBasicDomain) = clickedChildben(
            item,
            item.hhId,
            item.benId,
            item.relToHeadId
        )
        fun onClickedHouseHold(item: BenBasicDomain) = clickedHousehold(item,item.hhId)
        fun onClickABHA(item: BenBasicDomain) = clickedABHA(item,item.benId, item.hhId)
        fun clickedAddAllBenBtn(item: BenBasicDomain, isMatched: Boolean, isIFA: Boolean) = clickedAddAllBenBtn(item,item.benId, item.hhId, isMatched,isIFA)
        fun onClickedForCall(item: BenBasicDomain) = callBen(item)
        fun onClickSoftDeleteBen(item: BenBasicDomain) = softDeleteBen(item)
    }
}
