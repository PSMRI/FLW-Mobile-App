package org.piramalswasthya.sakhi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


class BenListAdapter(
    private val clickListener: BenClickListener? = null,
    private val showBeneficiaries: Boolean = false,
    private val showRegistrationDate: Boolean = false,
    private val showSyncIcon: Boolean = false,
    private val showAbha: Boolean = false,
    private val showCall: Boolean = false,
    private val role: Int? = 0,
    private val pref: PreferenceDao? = null,
    var context: FragmentActivity
) :
    ListAdapter<BenBasicDomain, BenListAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<BenBasicDomain>() {
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
            pref: PreferenceDao?,
            context : FragmentActivity,
            benIdList: List<Long>
        ) {

            var gender = ""

            if (pref?.getLoggedInUser()?.role.equals("asha", true)) {
                binding.btnAbha.visibility = View.VISIBLE
            } else {
                binding.btnAbha.visibility = View.GONE
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

            gender = item.gender.toString()

            if (item.relToHeadId == 19) {
                binding.HOF.visibility = View.VISIBLE
            } else {
                binding.HOF.visibility = View.GONE
            }

            if (item.dob != null) {
                val type = getPatientTypeByAge(getDateFromLong(item.dob!!))
                if (type == "new_born_baby") {
                    binding.ivHhLogo.setImageResource(R.drawable.ic_new_born_baby)
                } else if (type == "infant") {
                    binding.ivHhLogo.setImageResource(R.drawable.ic_infant)
                } else if (type == "child") {
                    if (gender == Gender.MALE.name) {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_boy)
                    } else if (gender == Gender.FEMALE.name) {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_girl)
                    } else {

                    }

                } else if (type == "adolescence") {
                    if (gender == Gender.MALE.name) {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_boy)
                    } else if (gender == Gender.FEMALE.name) {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_girl)
                    } else {

                    }

                } else if (type == "adult") {
                    if (gender == Gender.MALE.name) {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_males)
                    } else if (gender == Gender.FEMALE.name) {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_female)
                    } else {
                        binding.ivHhLogo.setImageResource(R.drawable.ic_unisex)
                    }
                }
            }



            if (item.gender == "MALE" &&  !item.isSpouseAdded && item.isMarried) {
                binding.btnAddSpouse.visibility = View.VISIBLE
                binding.btnAddChildren.visibility = View.INVISIBLE
                binding.llAddSpouseBtn.visibility = View.VISIBLE
                binding.btnAddSpouse.text = context.getString(R.string.add_wife)
                binding.btnAddSpouse.setOnClickListener {
                    clickListener?.onClickedWifeBen(item)
                }

            } else if ((item.gender == "FEMALE" && !item.isSpouseAdded && item.isMarried) ) {
                binding.btnAddSpouse.visibility = View.VISIBLE
                binding.btnAddChildren.visibility = View.INVISIBLE
                binding.llAddSpouseBtn.visibility = View.VISIBLE
                binding.btnAddSpouse.text = context.getString(R.string.add_husband)
                binding.btnAddSpouse.setOnClickListener {
                    clickListener?.onClickedHusbandBen(item)
                }




            } else  if (item.gender == "FEMALE" && item.isMarried && item.doYouHavechildren && !item.isChildrenAdded) {
                binding.btnAddChildren.visibility = View.VISIBLE
                binding.btnAddSpouse.visibility = View.GONE
                binding.llAddSpouseBtn.visibility = View.VISIBLE
                binding.btnAddChildren.setOnClickListener {
                    clickListener?.onClickChildBen(item)
                }

            } else {
                binding.btnAddSpouse.visibility = View.GONE
                binding.btnAddChildren.visibility = View.INVISIBLE
                binding.llAddSpouseBtn.visibility = View.GONE

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
            pref,
            context,
            benIds
        )
    }
    fun submitBenIds(list: List<Long>) {
        benIds.clear()
        benIds.addAll(list)
        notifyDataSetChanged()
    }



    class BenClickListener(
        private val clickedBen: (hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedWifeBen: (hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedHusbandBen: (hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedChildben: (hhId: Long, benId: Long, relToHeadId: Int) -> Unit,
        private val clickedHousehold: (hhId: Long) -> Unit,
        private val clickedABHA: (benId: Long, hhId: Long) -> Unit,
        private val clickedAddAllBenBtn: (benId: Long, hhId: Long, isViewMode: Boolean, isIFA: Boolean) -> Unit,
        private val callBen: (ben: BenBasicDomain) -> Unit
    ) {
        fun onClickedBen(item: BenBasicDomain) = clickedBen(
            item.hhId,
            item.benId,
            item.relToHeadId - 1
        )


        fun onClickedWifeBen(item: BenBasicDomain) = clickedWifeBen(
            item.hhId,
            item.benId,
            item.relToHeadId
        )



        fun onClickedHusbandBen(item: BenBasicDomain) = clickedHusbandBen(
            item.hhId,
            item.benId,
            item.relToHeadId
        )

        fun onClickChildBen(item: BenBasicDomain) = clickedChildben(
            item.hhId,
            item.benId,
            item.relToHeadId
        )
        fun onClickedHouseHold(item: BenBasicDomain) = clickedHousehold(item.hhId)
        fun onClickABHA(item: BenBasicDomain) = clickedABHA(item.benId, item.hhId)
        fun clickedAddAllBenBtn(item: BenBasicDomain, isMatched: Boolean, isIFA: Boolean) = clickedAddAllBenBtn(item.benId, item.hhId, isMatched,isIFA)
        fun onClickedForCall(item: BenBasicDomain) = callBen(item)
    }
}