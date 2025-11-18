package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.confirmed.form

import android.content.Context
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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.LeprosyConfirmedDataset
import org.piramalswasthya.sakhi.configuration.LeprosySuspectedDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.form.LeprosyFormFragmentArgs
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LeprosyConfirmedFromViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext var context: Context,
    private val leprosyRepo: LeprosyRepo,
    private val benRepo: BenRepo,
    var maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {
    var isDeath = true
    val benId =
        LeprosyFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED, VISIT_COMPLETED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    private val username = preferenceDao.getLoggedInUser()?.userName ?: ""
    val recordExists: LiveData<Boolean>
        get() = _recordExists


    private val _visitInfo = MutableLiveData<String>()
    val visitInfo: LiveData<String>
        get() = _visitInfo

    private val _followUpDates = MutableLiveData<List<LeprosyFollowUpCache>>()
    val followUpDates: LiveData<List<LeprosyFollowUpCache>>
        get() = _followUpDates

    private val _lastFollowUp = MutableLiveData<LeprosyFollowUpCache?>()
    val lastFollowUp: LiveData<LeprosyFollowUpCache?>
        get() = _lastFollowUp


    private val dataset = LeprosyConfirmedDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    var _isBeneficaryStatusDeath = MutableLiveData<Boolean>()

    val isBeneficaryStatusDeath: LiveData<Boolean>
        get() = _isBeneficaryStatusDeath

    private lateinit var leprosyScreenCache: LeprosyScreeningCache
    private lateinit var screeningData: LeprosyScreeningCache
    private lateinit var screening : LeprosyScreeningCache
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage



    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                leprosyScreenCache = LeprosyScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                    createdBy = preferenceDao.getLoggedInUser()?.userName ?: "",
                    modifiedBy = preferenceDao.getLoggedInUser()?.userName?:"",

                )
            }
             screening = leprosyRepo.getLeprosyScreening(benId) ?: throw IllegalStateException("No screening data found for confirmed case with benId: $benId")

            leprosyScreenCache = screening
            screeningData = screening
            _recordExists.value = true
            _isBeneficaryStatusDeath.value = screening.beneficiaryStatus.equals("Death", ignoreCase = true)

            val currentVisitNumber = screening.currentVisitNumber
            _visitInfo.value = "Visit - $currentVisitNumber"

            val allFollowUps = leprosyRepo.getAllFollowUpsForBeneficiary(benId)
            _followUpDates.value = allFollowUps

            val lastFollowUpForCurrentVisit = getLastFollowUpForCurrentVisit(allFollowUps, currentVisitNumber)
            _lastFollowUp.value = lastFollowUpForCurrentVisit

            dataset.setUpPage(
                ben,
                screening,
                followUp = lastFollowUpForCurrentVisit
            )
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }
    private fun getLastFollowUpForCurrentVisit(
        followUps: List<LeprosyFollowUpCache>,
        currentVisitNumber: Int
    ): LeprosyFollowUpCache? {
        return followUps
            .filter { it.visitNumber == currentVisitNumber }
            .maxByOrNull { it.followUpDate }
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }




    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    val validationError = dataset.validateForm()
                    if (validationError != null) {
                        _state.postValue(State.SAVE_FAILED)
                        // Post error message to show in fragment
                        _errorMessage.postValue(validationError)
                        return@withContext
                    }

                    val newFollowUp = LeprosyFollowUpCache(
                        benId = benId,
                        visitNumber = screeningData.currentVisitNumber,
                        followUpDate = System.currentTimeMillis(),
                        createdBy =username ,
                        modifiedBy = username,
                    )

                    dataset.mapValues(newFollowUp,  1)
                    newFollowUp.homeVisitDate = screening.homeVisitDate
                    newFollowUp.leprosySymptoms = screening.leprosySymptoms
                    newFollowUp.typeOfLeprosy = screening.typeOfLeprosy
                    newFollowUp.leprosySymptomsPosition = screening.leprosySymptomsPosition
                    newFollowUp.visitLabel = screening.visitLabel
                    newFollowUp.leprosyStatus = screening.leprosyStatus
                    newFollowUp.referredTo = screening.referredTo
                    newFollowUp.referToName = screening.referToName
                    newFollowUp.treatmentStartDate = screening.treatmentStartDate

                    leprosyRepo.saveFollowUp(newFollowUp)

                    if (newFollowUp.treatmentStatus == "Treatment Completed" &&
                        newFollowUp.treatmentCompleteDate > 0) {

                        val success = leprosyRepo.completeVisitAndStartNext(benId)
                        if (success) {
                            _state.postValue(State.VISIT_COMPLETED)

                            val updatedScreening = leprosyRepo.getLeprosyScreening(benId)
                            if (updatedScreening != null) {
                                screeningData = updatedScreening
                            }
                        } else {
                            _state.postValue(State.SAVE_SUCCESS)
                        }
                    } else {
                        _state.postValue(State.SAVE_SUCCESS)
                    }

                    val updatedFollowUps = leprosyRepo.getAllFollowUpsForBeneficiary(benId)
                    _followUpDates.postValue(updatedFollowUps)

                    val newLastFollowUp = getLastFollowUpForCurrentVisit(
                        updatedFollowUps,
                        screeningData.currentVisitNumber
                    )
                    _lastFollowUp.postValue(newLastFollowUp)

                } catch (e: Exception) {
                    Timber.e(e, "Saving leprosy follow-up data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }



    fun resetState() {
        _state.value = State.IDLE
    }

    fun getIndexOfDate(): Int {
        return dataset.getIndexOfDate()
    }
    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }


}