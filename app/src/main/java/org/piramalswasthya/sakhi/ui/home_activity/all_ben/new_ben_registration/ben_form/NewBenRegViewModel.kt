package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.BenRegFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenHealthIdDetails
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.BenRegKid
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AbhaGenerateAadhaarOtpRequest
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NewBenRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val benRepo: BenRepo,
    private val householdRepo: HouseholdRepo,
    userRepo: UserRepo
) : ViewModel() {
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    sealed class ListUpdateState {
        object Idle : ListUpdateState()

        object Updating : ListUpdateState()
        class Updated(val formElementId: Int) : ListUpdateState()
    }


    val hhId = NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId
    private val relToHeadId =
        NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).relToHeadId
    private val benGender =
        when (NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).gender) {
            1 -> Gender.MALE
            2 -> Gender.FEMALE
            3 -> Gender.TRANSGENDER
            else -> null
        }

    val isHoF = relToHeadId == 18

    var isOtpVerified = false
    private val benIdFromArgs =
        NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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

    fun setConsentAgreed() {
        isConsentAgreed = true
    }

    fun getIsConsentAgreed() = isConsentAgreed

    private lateinit var user: User
    private val dataset: BenRegFormDataset =
        BenRegFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    private lateinit var household: HouseholdCache
    private lateinit var ben: BenRegCache
    private lateinit var locationRecord: LocationRecord

    private var lastImageFormId: Int = 0
    var otp = 1234

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
                    isOtpVerified = ben.isVerified
                    dataset.setFirstPageToRead(
                        ben,
                        familyHeadPhoneNo = household.family?.familyHeadPhoneNo
                    )
                } else if (benIdFromArgs != 0L && recordExists.value != true) {
                    ben = benRepo.getBeneficiaryRecord(benIdFromArgs, hhId)!!

                    if (isHoF) dataset.setPageForHof(
                        if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                        household
                    ) else {
                        val familyList = benRepo.getBenListFromHousehold(hhId)
                        val hoFBen = familyList.firstOrNull { it.beneficiaryId == household.benId }
                        dataset.setPageForFamilyMember(
                            ben = if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                            household = household,
                            hoF = hoFBen, benGender = ben.gender!!,
                            relationToHeadId = relToHeadId,
                            hoFSpouse = familyList.filter { it.familyHeadRelationPosition == 5 || it.familyHeadRelationPosition == 6 }
                        )
                    }
                } else {
                    if (isHoF) dataset.setPageForHof(
                        if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                        household
                    ) else {
                        val familyList = benRepo.getBenListFromHousehold(hhId)
                        val hoFBen = familyList.firstOrNull { it.beneficiaryId == household.benId }
                        dataset.setPageForFamilyMember(
                            ben = if (this@NewBenRegViewModel::ben.isInitialized) ben else null,
                            household = household,
                            hoF = hoFBen, benGender = benGender!!,
                            relationToHeadId = relToHeadId,
                            hoFSpouse = familyList.filter { it.familyHeadRelationPosition == 5 || it.familyHeadRelationPosition == 6 }

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
                            householdId = hhId,
                            isAdult = false,
                            isKid = dataset.isKid(),
                            isDraft = true,
                            kidDetails = if(dataset.isKid()) BenRegKid() else null,
                            genDetails = BenRegGen(),
                            syncState = SyncState.UNSYNCED,
                            locationRecord = locationRecord,
                            isVerified = isOtpVerified
                        )
                    }
                    dataset.mapValues(ben, 2)
                    if (isHoF) {
                        dataset.updateHouseholdWithHoFDetails(household, ben)
                        householdRepo.updateHousehold(household)
                    }
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
                    otp = it.data.response.toInt();
                } else {


                }
            }
        }
    }


    fun validateOtp(mobileNo: String,otp:Int) : Boolean {
        viewModelScope.launch {
            benRepo.verifyOtp(mobileNo,otp)?.let {
                if (it.status.equals("Success")) {
                    isOtpVerified = true

                } else {

                    isOtpVerified = false
                }
            }
        }
        return isOtpVerified
    }
}