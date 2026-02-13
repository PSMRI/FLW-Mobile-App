package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.BenRegFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.BenRegKid
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.PreviewItem
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NewBenRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val benRepo: BenRepo,
    private val householdRepo: HouseholdRepo,
    private val ecrRepo: EcrRepo,
    userRepo: UserRepo
) : ViewModel() {
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private var selectedBeneficiaryIdForEcr = 0L
    lateinit var countDownTimer : CountDownTimer


    sealed class ListUpdateState {
        object Idle : ListUpdateState()

        object Updating : ListUpdateState()
        class Updated(val formElementId: Int) : ListUpdateState()
    }


    val hhId = NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId
     val relToHeadId =
        NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).relToHeadId
    private val benGender =
        when (NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).gender) {
            1 -> Gender.MALE
            2 -> Gender.FEMALE
            3 -> Gender.TRANSGENDER
            else -> null
        }

    val isHoF = relToHeadId == 18

    var isBenMarried = false

    private var parentName = ""
    private var parentFirstName = ""

    companion object {
        var isOtpVerified = false
    }

    val benIdFromArgs =
        NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
     val SelectedbenIdFromArgs =
        NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).selectedBenId

    private val isAddspouse = NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).isAddSpouse

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _listUpdateState: MutableLiveData<ListUpdateState> =
        MutableLiveData(ListUpdateState.Idle)
    val listUpdateState: LiveData<ListUpdateState>
        get() = _listUpdateState

    private val _recordExists = MutableLiveData(benIdFromArgs != 0L)
    val recordExists: LiveData<Boolean>
        get() = _recordExists


    private var isConsentAgreed = false
    var isEditClicked = false

    fun setConsentAgreed() {
        isConsentAgreed = true
    }

    fun getIsConsentAgreed() = isConsentAgreed

    private lateinit var user: User
    val dataset: BenRegFormDataset =
        BenRegFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    private lateinit var household: HouseholdCache
    private lateinit var ben: BenRegCache
    private lateinit var locationRecord: LocationRecord

    private var lastImageFormId: Int = 0
    var otp = 1234

    fun getIndexOfBirthCertificateFront() = dataset.getIndexOfBirthCertificateFrontPath()

    fun getIndexOfBirthCertificateBack() = dataset.getIndexOfBirthCertificateBackPath()

    private var lastDocumentFormId: Int = 0

    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }
    fun getDocumentFormId():Int {
        return lastDocumentFormId
    }
    private lateinit var ecrForm: EligibleCoupleRegCache

    var oldChildCount = 0
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setUpPage()
            }
        }
    }


    suspend fun setUpPage() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                user = preferenceDao.getLoggedInUser()!!
                household = benRepo.getHousehold(hhId)!!
                locationRecord = preferenceDao.getLocationRecord()!!

                if (benIdFromArgs != 0L && recordExists.value == true) {
                    ben = benRepo.getBeneficiaryRecord(benIdFromArgs, hhId)!!
                    _isDeath.postValue(ben.isDeath ?: false)
                    if (ben.genDetails?.maritalStatus == "Unmarried") {
                        isBenMarried = false
                    } else {
                        isBenMarried = true
                    }
                    isOtpVerified = ben.isConsent
                    parentName = ben.firstName + " " + ben.lastName
                    parentFirstName = ben.firstName.toString()
                    dataset.setFirstPageToRead(
                        ben,
                        familyHeadPhoneNo = household.family?.familyHeadPhoneNo
                    )
                } else if (benIdFromArgs != 0L && recordExists.value != true) {
                    ben = benRepo.getBeneficiaryRecord(benIdFromArgs, hhId)!!
                    isOtpVerified = ben.isConsent
                    if (isHoF) dataset.setPageForHof(
                        if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                        household
                    ) else {
                        val familyList = benRepo.getBenListFromHousehold(hhId)
                        val hoFBen = familyList.firstOrNull { it.beneficiaryId == household.benId }
                        val selectedben = familyList.firstOrNull { it.beneficiaryId == SelectedbenIdFromArgs }

                        dataset.setPageForFamilyMember(
                            ben = if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                            household = household,
                            hoF = hoFBen, benGender = ben.gender!!,
                            relationToHeadId = relToHeadId,
                            hoFSpouse = familyList.filter { it.familyHeadRelationPosition == 5 || it.familyHeadRelationPosition == 6 },
                            selectedben,
                            isAddspouse,
                        )
                    }
                } else {

                    if (isHoF) dataset.setPageForHof(
                        if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                        household
                    ) else {
                        val familyList = benRepo.getBenListFromHousehold(hhId)
                        val hoFBen = familyList.firstOrNull { it.beneficiaryId == household.benId }
                        val selectedben = familyList.firstOrNull { it.beneficiaryId == SelectedbenIdFromArgs }

                         selectedBeneficiaryIdForEcr = if (hoFBen?.gender == Gender.FEMALE) {
                            hoFBen.beneficiaryId

                        } else {
                            val femaleOfHouse = familyList.firstOrNull {
                                it.familyHeadRelationPosition == 5 || it.familyHeadRelationPosition == 6
                            }
                            femaleOfHouse?.beneficiaryId ?: hoFBen!!.beneficiaryId
                        }


                        dataset.setPageForFamilyMember(
                            ben = if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                            household = household,
                            hoF = hoFBen, benGender = benGender!!,
                            relationToHeadId = relToHeadId,
                            hoFSpouse = familyList.filter { it.familyHeadRelationPosition == 5 || it.familyHeadRelationPosition == 6 },
                            selectedben,
                            isAddspouse,


                            )
                    }
                }
            }
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    if (!this@NewBenRegViewModel::ben.isInitialized) {
                        val benIdToSet = minOf(benRepo.getMinBenId() - 1L, -1L)
                        ben = BenRegCache(
                            ashaId = user.userId,
                            beneficiaryId = benIdToSet,
                            isDeath = false,
                            isDeathValue = "",
                            dateOfDeath = "",
                            timeOfDeath = "",
                            reasonOfDeath = "",
                            reasonOfDeathId = -1,
                            placeOfDeath = "",
                            placeOfDeathId = -1,
                            otherPlaceOfDeath = "",
                            householdId = hhId,
                            isAdult = false,
                            isKid = dataset.isKid(),
                            isDraft = true,
                            kidDetails = if(dataset.isKid()) BenRegKid() else null,
                            genDetails = BenRegGen(),
                            syncState = SyncState.UNSYNCED,
                            locationRecord = locationRecord,
                            isConsent = isOtpVerified,

                        )
                    }
                    dataset.mapValues(ben, 2)
                    if (ben.familyHeadRelationPosition == 5 || ben.familyHeadRelationPosition == 6 ) {
                        benRepo.updateHousehold(ben.householdId, SyncState.UNSYNCED)
                    } else if (isAddspouse == 1) {
                        benRepo.updateBeneficiarySpouseAdded(ben.householdId,SelectedbenIdFromArgs,SyncState.UNSYNCED)
                    }
                    if (isHoF) {
                        if (ben.gender == Gender.MALE) {
                            benRepo.updateFatherInChildren(ben.firstName + " " + ben.lastName, ben.householdId, parentName, SyncState.UNSYNCED)
                        } else {
                            benRepo.updateMotherInChildren(ben.firstName.toString(), ben.householdId, parentFirstName, SyncState.UNSYNCED)
                        }
                        benRepo.updateSpouseOfHoF(ben.firstName.toString(), ben.householdId, parentFirstName, SyncState.UNSYNCED)
                        household.family?.familyHeadName = ben.firstName
                        household.family?.familyName = ben.lastName
                        household.family?.familyHeadPhoneNo = ben.contactNumber
                        dataset.updateHouseholdWithHoFDetails(household, ben)
                        householdRepo.updateHousehold(household)
                        householdRepo.updateHouseholdToSync(household.householdId)
                    }
                    if (ben.gender == Gender.MALE) {
                        benRepo.updateFather(ben.firstName + " " + ben.lastName, ben.householdId, parentName, SyncState.UNSYNCED)
                    } else {
                        benRepo.updateBabyName("Baby of " + ben.firstName, ben.householdId, parentFirstName, SyncState.UNSYNCED)
                        benRepo.updateMother(ben.firstName.toString(), ben.householdId, parentFirstName, SyncState.UNSYNCED)
                    }
                    benRepo.updateChildrenLastName(ben.lastName.toString(), ben.householdId, parentName, SyncState.UNSYNCED)
                    benRepo.updateSpouse(ben.firstName.toString(), ben.householdId, parentFirstName, SyncState.UNSYNCED)
                    benRepo.updateSpouseLastName(ben.lastName.toString(), ben.householdId, parentName, SyncState.UNSYNCED)
                    ben.apply {
                        if (beneficiaryId < 0L) {
                            serverUpdatedStatus = 1
                            processed = "N"
                        } else {
                            serverUpdatedStatus = 2
                            processed = "U"
                        }
                        syncState = SyncState.UNSYNCED

                        if (createdDate == null) {
                            createdDate = System.currentTimeMillis()
                            createdBy = user.userName
                        }
                        updatedDate = System.currentTimeMillis()
                        updatedBy = user.userName
                    }
                    benRepo.persistRecord(ben)
                    if (isHoF) {
                        household.benId = ben.beneficiaryId
                        householdRepo.updateHousehold(household)
                    }


                    if (relToHeadId == 8 || relToHeadId == 9) {
                        val benForEcr =
                            benRepo.getBeneficiaryRecord(selectedBeneficiaryIdForEcr, hhId)
                        benForEcr?.let { childBen ->
                            val calDob = Calendar.getInstance()
                            calDob.timeInMillis = childBen.dob

                            var existingEcr = ecrRepo.getSavedRecord(selectedBeneficiaryIdForEcr)
                            val isNew = existingEcr == null

                            if (isNew) {
                                existingEcr = EligibleCoupleRegCache(
                                    benId = selectedBeneficiaryIdForEcr,
                                    createdBy = user.userName,
                                    updatedBy = user.userName,
                                    syncState = SyncState.UNSYNCED,
                                    lmp_date = 0L,

                                )
                            }

                            val nextSlot = when {
                                existingEcr!!.dob1 == null -> 1
                                existingEcr.dob2 == null -> 2
                                existingEcr.dob3 == null -> 3
                                existingEcr.dob4 == null -> 4
                                existingEcr.dob5 == null -> 5
                                existingEcr.dob6 == null -> 6
                                existingEcr.dob7 == null -> 7
                                existingEcr.dob8 == null -> 8
                                existingEcr.dob9 == null -> 9
                                else -> 9 // Maximum 9 children allowed
                            }

                            // 5️⃣ Insert child into next available slot
                            when (nextSlot) {
                                1 -> { existingEcr.dob1 = ben.dob; existingEcr.gender1 = ben.gender; existingEcr.age1 = ben.age }
                                2 -> { existingEcr.dob2 = ben.dob; existingEcr.gender2 = ben.gender; existingEcr.age2 = ben.age }
                                3 -> { existingEcr.dob3 = ben.dob; existingEcr.gender3 = ben.gender; existingEcr.age3 = ben.age }
                                4 -> { existingEcr.dob4 = ben.dob; existingEcr.gender4 = ben.gender; existingEcr.age4 = ben.age }
                                5 -> { existingEcr.dob5 = ben.dob; existingEcr.gender5 = ben.gender; existingEcr.age5 = ben.age }
                                6 -> { existingEcr.dob6 = ben.dob; existingEcr.gender6 = ben.gender; existingEcr.age6 = ben.age }
                                7 -> { existingEcr.dob7 = ben.dob; existingEcr.gender7 = ben.gender; existingEcr.age7 = ben.age }
                                8 -> { existingEcr.dob8 = ben.dob; existingEcr.gender8 = ben.gender; existingEcr.age8 = ben.age }
                                9 -> { existingEcr.dob9 = ben.dob; existingEcr.gender9 = ben.gender; existingEcr.age9 = ben.age }
                            }

                            existingEcr.noOfChildren = nextSlot
                            existingEcr.noOfLiveChildren++
                            if (ben.gender == Gender.MALE) existingEcr.noOfMaleChildren++
                            if (ben.gender == Gender.FEMALE) existingEcr.noOfFemaleChildren++

                            existingEcr.updatedBy = user.userName
                            existingEcr.syncState = SyncState.UNSYNCED


                            ecrRepo.persistRecord(existingEcr)
                            ecrRepo.getBenFromId(selectedBeneficiaryIdForEcr)?.let {
                                val hasBenUpdated = dataset.mapValueToBen(it)
                                if (hasBenUpdated) {
                                    benRepo.updateRecord(it)

                                }
                            }
                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: IllegalAccessError) {
                    Timber.d("saving Ben data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }


    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            _listUpdateState.value = ListUpdateState.Updating
            dataset.updateList(formId, index)
            _listUpdateState.value = ListUpdateState.Updated(formId)
        }

    }

    fun resetListUpdateState() {
        _listUpdateState.value = ListUpdateState.Idle
    }

    private val _isDeath = MutableLiveData<Boolean>()
    val isDeath: LiveData<Boolean> get() = _isDeath

    fun getBenGender() = ben.gender
    fun getBenName() = "${ben.firstName} ${ben.lastName ?: ""}"
    fun isHoFMarried() = isHoF && ben.genDetails?.maritalStatusId == 2


    fun setCurrentImageFormId(id: Int) {
        lastImageFormId = id
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastImageFormId, dpUri)

    }

    fun updateValueByIdAndReturnListIndex(id: Int, value: String?): Int {
        dataset.setValueById(id, value)
        return dataset.getIndexById(id)
    }

    fun getIndexOfAgeAtMarriage() = dataset.getIndexOfAgeAtMarriage()
    fun getIndexOfMaritalStatus() = dataset.getIndexOfMaritalStatus()
    fun getIndexOfContactNumber() = dataset.getIndexOfContactNumber()
    fun getIndexofTempraryNumber() = dataset.getTempMobileNoStatus()

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }

     fun sentOtp(mobileNo: String) {
        viewModelScope.launch {
            benRepo.sendOtp(mobileNo)?.let {
                if (it.status == "Success") {

                } else {


                }
            }
            }
    }

    fun resendOtp(mobileNo: String) {
        viewModelScope.launch {
            benRepo.resendOtp(mobileNo)?.let {
                if (it.status == "Success") {

                } else {


                }
            }
        }
    }


    fun validateOtp(
        mobileNo: String,
        otp: Int,
        context: FragmentActivity,
        otpField: TextInputEditText,
        button: MaterialButton,
        timerInsec: TextView
    ) : Boolean {
        var memberOtpVerified = false
        viewModelScope.launch {
            benRepo.verifyOtp(mobileNo,otp)?.let {
                if (it.status.equals("Success")) {
                    Toast.makeText(context,"Otp Verified", Toast.LENGTH_SHORT).show()
                    otpField.isEnabled = false
                    button.text = context.resources.getString(R.string.verified)
                    button.isEnabled = true
                    isOtpVerified = true

                } else {
                    Toast.makeText(context,"Invalid Otp", Toast.LENGTH_SHORT).show()
                    memberOtpVerified = false
                    isOtpVerified = false
                }
            }
        }
        return memberOtpVerified
    }

    suspend fun getFormPreviewData(): List<PreviewItem> = withContext(Dispatchers.Default) {
        val elements = dataset.listFlow.first()
        val out = mutableListOf<PreviewItem>()
        for (el in elements) {
            if (el.inputType.name == "IMAGE_VIEW" || el.inputType.toString() == "IMAGE_VIEW") {
                val uri = try {
                    el.value?.let { Uri.parse(it.toString()) }
                } catch (e: Exception) {
                    null
                }
                out.add(
                    PreviewItem(
                        label = el.title ?: "",
                        value = "",
                        isImage = true,
                        imageUri = uri
                    )
                )
                continue
            }

            val display = when {
                el.value == null -> "-"
                el.value is String && el.value.toString().isBlank() -> "-"
                el.value is String && el.value.toString().contains(",") -> el.value.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString(", ")
                else -> el.value.toString()
            }
            val trimmed = if (display.length > 400) display.substring(0, 400) + "…" else display
            out.add(PreviewItem(label = el.title ?: "", value = trimmed, isImage = false))
        }
        out
    }

    override fun onCleared() {
        super.onCleared()
        isOtpVerified = false

    }
}