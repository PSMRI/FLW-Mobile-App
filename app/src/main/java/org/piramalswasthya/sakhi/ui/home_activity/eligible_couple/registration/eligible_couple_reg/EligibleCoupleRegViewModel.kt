package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_reg

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
import org.piramalswasthya.sakhi.configuration.EligibleCoupleRegistrationDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil.getDiffYears
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val ecrRepo: EcrRepo,
    private val hrpRepo: HRPRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val benId =
        EligibleCoupleRegFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName

    private val _childCount = MutableLiveData<Int>()
    val childCount: LiveData<Int>
        get() = _childCount

    private val _childBelow15Count = MutableLiveData<Int>()
    val childBelow15Count: LiveData<Int>
        get() = _childBelow15Count

    private val _childAbove15Count = MutableLiveData<Int>()
    val childAbove15Count: LiveData<Int>
        get() = _childAbove15Count
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _draftExists = MutableLiveData<EligibleCoupleRegCache?>(null)
    val draftExists: LiveData<EligibleCoupleRegCache?>
        get() = _draftExists

    private val _isEcrCompleted = MutableLiveData<Boolean>()
    val isEcrCompleted: LiveData<Boolean>
        get() = _isEcrCompleted

    val showDialogEvent = MutableLiveData<String>()
    private val dataset =
        EligibleCoupleRegistrationDataset(context,context, preferenceDao.getCurrentLanguage(),showDialogEvent)
    val formList = dataset.listFlow

    private lateinit var ecrForm: EligibleCoupleRegCache

    private var lastDocumentFormId: Int = 0

    private var assess: HRPNonPregnantAssessCache? = null

    fun getIndexofAshaKitPhotoFirst() = dataset.getIndexofAshaKitPhotoOne()
    fun getIndexofAshaKitPhotoSecond() = dataset.getIndexofAshaKitPhoto()



    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }
    fun getDocumentFormId():Int {
        return lastDocumentFormId
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)


    }

    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = ecrRepo.getBenFromId(benId)?.also { ben ->
                val calDob = Calendar.getInstance()
                calDob.timeInMillis = ben.dob
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${
                    getDiffYears(
                        calDob,
                        Calendar.getInstance()
                    )
                } ${ben.ageUnit?.name} | ${ben.gender?.name}"
                ecrForm = EligibleCoupleRegCache(
                    benId = ben.beneficiaryId,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                    lmp_date = calDob.timeInMillis,
                    isKitHandedOver = false,
                )
            }

            assess = hrpRepo.getNonPregnantAssess(benId)

            assess?.let {
                ecrForm.createdDate = it.visitDate
            }
            
            val childList = ben?.let {
                benRepo.getChildBenListFromHousehold(it.householdId, benId, it.firstName)
            } ?: emptyList()
            _childCount.value = childList.size.coerceAtMost(9)
            
            val below15Childcount = ben?.let {
                benRepo.getChildBelow15(it.householdId, benId, it.firstName)
            } ?: 0
            _childBelow15Count.value = below15Childcount.coerceAtMost(9)

            val above15Childcount = ben?.let {
                benRepo.getChildAbove15(it.householdId, benId, it.firstName)
            } ?: 0
            _childAbove15Count.value = above15Childcount.coerceAtMost(9)

            ecrRepo.getSavedRecord(benId)?.let {
                ecrForm = it
                _recordExists.value = true
                _isEcrCompleted.value = ecrForm.lmpDate != 0L
                dataset.setUpPage(ben, assess, ecrForm, childList)
            } ?: run {
                _recordExists.value = false
                _isEcrCompleted.value = false
                dataset.setUpPage(ben, assess, null, childList)
                checkDraft()
            }
        }
    }

    private suspend fun checkDraft() {
        ecrRepo.getDraftRecord(benId)?.let {
            _draftExists.postValue(it)
        }
    }

    fun restoreDraft(draft: EligibleCoupleRegCache) {
        viewModelScope.launch {
            ecrForm = draft
            val ben = ecrRepo.getBenFromId(benId)
            val childList = ben?.let {
                benRepo.getChildBenListFromHousehold(it.householdId, benId, it.firstName)
            } ?: emptyList()
            dataset.setUpPage(ben, assess, draft, childList)
            _draftExists.value = null
        }
    }

    fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                ecrRepo.deleteById(it.id)
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    dataset.mapValues(ecrForm, 1)
                    ecrForm.isDraft = true
                    ecrRepo.persistRecord(ecrForm)
                } catch (e: Exception) {
                    Timber.e("saving draft failed!! $e")
                }
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(ecrForm, 1)
                    ecrForm.isDraft = false
                    ecrRepo.persistRecord(ecrForm)
                    ecrRepo.getBenFromId(benId)?.let {
                        val hasBenUpdated = dataset.mapValueToBen(it)
                        if (hasBenUpdated) {
                            benRepo.updateRecord(it)

                        }
                    }
                    if (assess == null) {
                        assess =
                            HRPNonPregnantAssessCache(benId = benId, syncState = SyncState.UNSYNCED)
                    }
                    dataset.mapValuesToAssess(assess, 1)
                    assess?.let {
                        hrpRepo.saveRecord(it)
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving ecr data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
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

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }

    fun getIndexOfTimeLessThan18() = dataset.getIndexOfTimeLessThan18m()
    fun getIndexOfChildLabel() = dataset.getIndexOfChildLabel()

    fun getIndexOfPhysicalObservationLabel() = dataset.getIndexOfPhysicalObservationLabel()

    fun getIndexOfObstetricHistoryLabel() = dataset.getIndexOfObstetricHistoryLabel()
}