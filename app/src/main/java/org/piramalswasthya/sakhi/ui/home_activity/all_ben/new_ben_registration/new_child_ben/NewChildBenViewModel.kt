package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.new_child_ben

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
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
import org.piramalswasthya.sakhi.configuration.NewChildBenRegDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.BenRegKid
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.NewBenRegFragmentArgs
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NewChildBenViewModel@Inject constructor(
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

    companion object {
        var isOtpVerified = false
    }

    val benIdFromArgs =
        NewBenRegFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    private val SelectedbenIdFromArgs =
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

    fun setConsentAgreed() {
        isConsentAgreed = true
    }

    fun getIsConsentAgreed() = isConsentAgreed

    private lateinit var user: User
    val dataset: NewChildBenRegDataset =
        NewChildBenRegDataset(context, preferenceDao.getCurrentLanguage())
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


                    ben = benRepo.getBeneficiaryRecord(SelectedbenIdFromArgs, hhId)!!
                   _isDeath.postValue(ben.isDeath ?: false)
                    if (ben.genDetails?.maritalStatus == "Unmarried") {
                        isBenMarried = false
                    } else {
                        isBenMarried = true
                    }
                    isOtpVerified = ben.isConsent
                    val familyList = benRepo.getBenListFromHousehold(hhId)
                    val hoFBen = familyList.firstOrNull { it.beneficiaryId == household.benId }
                    val selectedben = familyList.firstOrNull { it.beneficiaryId == SelectedbenIdFromArgs }

                    dataset.setUpPage(
                        ben,
                        household = household,
                        hoF = hoFBen, benGender = ben.gender!!,
                        relationToHeadId = relToHeadId,
                        hoFSpouse = familyList.filter { it.familyHeadRelationPosition == 5 || it.familyHeadRelationPosition == 6 },
                        selectedben,
                        isAddspouse
                    )
                }

        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    if (this@NewChildBenViewModel::ben.isInitialized) {
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
                            isKid = true,
                            isDraft = true,
                            kidDetails = if(true) BenRegKid() else null,
                            genDetails = BenRegGen(),
                            syncState = SyncState.UNSYNCED,
                            locationRecord = locationRecord,
                            isConsent = isOtpVerified
                        )
                    }
                    dataset.mapValues(ben, 2)
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
                    Log.e("DATASET",hhId.toString())
                    Log.e("DATASET",SelectedbenIdFromArgs.toString())
                    Log.e("DATASET",ben.noOfChildren.toString())
                    Log.e("DATASET",ben.noOfAliveChildren.toString())
                    benRepo.updateBeneficiaryChildrenAdded(hhId,SelectedbenIdFromArgs)
                    benRepo.persistRecord(ben)

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



    fun updateValueByIdAndReturnListIndex(id: Int, value: String?): Int {
        dataset.setValueById(id, value)
        return dataset.getIndexById(id)
    }


    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }


    fun getIndexOfChildren(): Int {
        return dataset.getIndexOfChildren()
    }

    fun getIndexOfLiveChildren(): Int {
        return dataset.getIndexOfLiveChildren()
    }

    fun getIndexOfMaleChildren(): Int {
        return dataset.getIndexOfMaleChildren()
    }

    fun getIndexOfFeMaleChildren(): Int {
        return dataset.getIndexOfFeMaleChildren()
    }

    fun getIndexOfAge1(): Int {
        return dataset.getIndexOfAge1()
    }

    fun getIndexOfGap1(): Int {
        return dataset.getIndexOfGap1()
    }

    fun getIndexOfAge2(): Int {
        return dataset.getIndexOfAge2()
    }

    fun getIndexOfGap2(): Int {
        return dataset.getIndexOfGap2()
    }

    fun getIndexOfAge3(): Int {
        return dataset.getIndexOfAge3()
    }

    fun getIndexOfGap3(): Int {
        return dataset.getIndexOfGap3()
    }

    fun getIndexOfAge4(): Int {
        return dataset.getIndexOfAge4()
    }

    fun getIndexOfGap4(): Int {
        return dataset.getIndexOfGap4()
    }

    fun getIndexOfAge5(): Int {
        return dataset.getIndexOfAge5()
    }

    fun getIndexOfGap5(): Int {
        return dataset.getIndexOfGap5()
    }

    fun getIndexOfAge6(): Int {
        return dataset.getIndexOfAge6()
    }

    fun getIndexOfGap6(): Int {
        return dataset.getIndexOfGap6()
    }

    fun getIndexOfAge7(): Int {
        return dataset.getIndexOfAge7()
    }

    fun getIndexOfGap7(): Int {
        return dataset.getIndexOfGap7()
    }

    fun getIndexOfAge8(): Int {
        return dataset.getIndexOfAge8()
    }

    fun getIndexOfGap8(): Int {
        return dataset.getIndexOfGap8()
    }

    fun getIndexOfAge9(): Int {
        return dataset.getIndexOfAge9()
    }

    fun getIndexOfGap9(): Int {
        return dataset.getIndexOfGap9()
    }





   }