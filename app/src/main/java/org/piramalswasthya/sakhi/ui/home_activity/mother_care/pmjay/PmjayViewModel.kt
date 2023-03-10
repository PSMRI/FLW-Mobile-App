package org.piramalswasthya.sakhi.ui.home_activity.mother_care.pmjay

import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.configuration.PMJAYFormDataset
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.model.*
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.PmjayRepo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PmjayViewModel @Inject constructor(
    state: SavedStateHandle,
    context: Application,
    private val database: InAppDb,
    private val pmjayRepo: PmjayRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SUCCESS,
        FAIL
    }

    private val benId = PmjayFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = PmjayFragmentArgs.fromSavedStateHandle(state).hhId
    private lateinit var ben: BenRegCache
    private lateinit var household: HouseholdCache
    private lateinit var user: UserCache
    private var pmjay: PMJAYCache? = null

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender
    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _exists = MutableLiveData<Boolean>()
    val exists: LiveData<Boolean>
        get() = _exists

    private val dataset = PMJAYFormDataset(context)

    private fun toggleFieldOnTrigger(
        causeField: FormInput,
        effectField: FormInput,
        value: String?,
        adapter: FormInputAdapter
    ) {
        value?.let {
            if (it == "Hospital") {
                val list = adapter.currentList.toMutableList()
                if(!list.contains(effectField)) {
                    list.add(
                        adapter.currentList.indexOf(causeField) + 1,
                        effectField
                    )
                    adapter.submitList(list)
                }
            } else {
                if (adapter.currentList.contains(effectField)) {
                    val list = adapter.currentList.toMutableList()
                    list.remove(effectField)
                    adapter.submitList(list)
                }
            }
        }
    }

    fun submitForm() {
        _state.value = State.LOADING
        val pmjayCache = PMJAYCache(benId = benId, hhId = hhId, processed = "N", createdBy = user.userName)
//        dataset.mapValues(cdrCache)
        viewModelScope.launch {
            val saved = pmjayRepo.savePmjayData(pmjayCache)
            if (saved)
                _state.value = State.SUCCESS
            else
                _state.value = State.FAIL
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ben = benRepo.getBeneficiary(benId, hhId)!!
                household = benRepo.getHousehold(hhId)!!
                user = database.userDao.getLoggedInUser()!!
                pmjay = database.pmjayDao.getPmjay(hhId, benId)
            }
            _benName.value = "${ben.firstName} ${if(ben.lastName== null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _address.value = getAddress(household)
            _exists.value = pmjay != null
        }
    }

    private fun getAddress(household: HouseholdCache): String {
        val houseNo = household.houseNo
        val wardNo = household.wardNo
        val name = household.wardName
        val mohalla = household.mohallaName
        val district = household.district
        val city = household.village
        val state = household.state

        var address = "$houseNo, $wardNo, $name, $mohalla, $city, $district, $state"
        address = address.replace(", ,", ",")
        address = address.replace(",,", ",")
        address = address.replace(" ,", "")
        address = address.replace("null, ", "")
        address = address.replace(", null", "")

        return address
    }

    suspend fun getFirstPage(adapter: FormInputAdapter): List<FormInput> {
        viewModelScope.launch {
//            launch{
//                dataset.placeOfDeath.value.collect {
//                    toggleFieldOnTrigger(
//                        dataset.placeOfDeath,
//                        dataset.hospitalName,
//                        it,
//                        adapter
//                    )
//                }
//            }
        }
        return dataset.firstPage
    }

    fun setAddress(it: String?, adapter: FormInputAdapter) {
        dataset.patientAddress.value.value = it
//        dataset.childName.value.value = ben.firstName
//        dataset.gender.value.value = when (ben.gender){
//            Gender.MALE -> "Male"
//            Gender.FEMALE -> "Female"
//            Gender.TRANSGENDER -> "Transgender"
//            else -> "Other"
//        }
//        dataset.age.value.value = "${ben.age} ${ben.ageUnit?.name}"
//        dataset.dateOfBirth.value.value = getDateFromLong(ben.dob)
//        dataset.firstInformant.value.value = user.userName
//        dataset.motherName.value.value = ben.motherName
//        dataset.fatherName.value.value = ben.fatherName
//        dataset.mobileNumber.value.value = ben.contactNumber.toString()
//        dataset.visitDate.value.value = getDateFromLong(System.currentTimeMillis())
//        dataset.visitDate.value.value = getDateFromLong(System.currentTimeMillis())
//        dataset.dateOfNotification.value.value = getDateFromLong(System.currentTimeMillis())
//        dataset.dateOfDeath.min = ben.dob
//        dataset.dateOfNotification.min = ben.
    }

    private fun getDateFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        dateLong?.let {
            return dateFormat.format(dateLong)
        } ?: run {
            return null
        }
    }

    fun setExistingValues() {
//        dataset.address.value.value = cdr?.address
//        dataset.childName.value.value = cdr?.childName
//        dataset.gender.value.value = cdr?.gender
//        dataset.age.value.value = "${cdr?.age} ${ben.ageUnit?.name}"
//        dataset.dateOfBirth.value.value = getDateFromLong(cdr?.dateOfBirth)
//        dataset.firstInformant.value.value = cdr?.firstInformant
//        dataset.motherName.value.value = cdr?.motherName
//        dataset.fatherName.value.value = cdr?.fatherName
//        dataset.mobileNumber.value.value = cdr?.mobileNumber.toString()
//        dataset.dateOfNotification.value.value = getDateFromLong(cdr?.dateOfNotification)
//        dataset.childName.value.value = cdr?.childName
//        dataset.visitDate.value.value = getDateFromLong(cdr?.visitDate)
//        dataset.houseNumber.value.value = cdr?.houseNumber
//        dataset.mohalla.value.value = cdr?.mohalla
//        dataset.landmarks.value.value = cdr?.landmarks
//        dataset.pincode.value.value = cdr?.pincode.toString()
//        dataset.landline.value.value = cdr?.landline.toString()
//        dataset.dateOfDeath.value.value = getDateFromLong(cdr?.dateOfDeath)
//        dataset.timeOfDeath.value.value = cdr?.timeOfDeath?.toString()
//        dataset.ashaSign.value.value = cdr?.ashaSign
//        dataset.placeOfDeath.value.value = cdr?.placeOfDeath
    }
}