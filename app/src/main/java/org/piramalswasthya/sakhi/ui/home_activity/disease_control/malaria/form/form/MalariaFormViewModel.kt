package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.MalariaFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MalariaFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    var malariaDao: MalariaDao,
    @ApplicationContext var context: Context,
    private val malariaRepo: MalariaRepo,
    private val benRepo: BenRepo,
    private val maternalHealthRepo: MaternalHealthRepo,

    ) : ViewModel() {

    val benId =
        MalariaFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _visitNo = MutableLiveData<String>()
    val visitNo: LiveData<String>
        get() = _visitNo

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val dataset =
        MalariaFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isSuspected = false
    var isDeath = false
    var allVisitsList = malariaDao.getAllVisitsForBen(benId)

    private lateinit var malariaScreeningCache: MalariaScreeningCache
    var _isBeneficaryStatusDeath = MutableLiveData<Boolean>()

    val isBeneficaryStatusDeath: LiveData<Boolean>
        get() = _isBeneficaryStatusDeath


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"

                malariaScreeningCache = MalariaScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                    visitId = 1
                )
            }

            malariaRepo.getLatestVisitForBen(benId)?.let {
                malariaScreeningCache = it
                _visitNo.value = malariaScreeningCache.visitId.toString()
                isSuspected = isSuspectedCase(malariaScreeningCache)
                _recordExists.value = true
                _isBeneficaryStatusDeath.value = malariaScreeningCache.beneficiaryStatus.equals("Death", ignoreCase = true)
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) malariaScreeningCache else null
            )

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
                    dataset.mapValues(malariaScreeningCache, 1)
                    if (malariaScreeningCache.beneficiaryStatus == "Death") {
                        isDeath = true
                        val reasons = context.resources.getStringArray(R.array.benificary_case_status)
                        val selectedValue = malariaScreeningCache.reasonForDeath
                        val position = reasons.indexOf(selectedValue)
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            val dateOfDeath = malariaScreeningCache.dateOfDeath?.let { d ->
                                dateFormat.format(Date(d))
                            } ?: ""
                            it.dateOfDeath = dateOfDeath
                            it.reasonOfDeath = malariaScreeningCache.reasonForDeath
                            it.reasonOfDeathId = position
                            it.placeOfDeath = malariaScreeningCache.placeOfDeath
                            it.placeOfDeathId = context.resources.getStringArray(R.array.death_place).indexOf(malariaScreeningCache.placeOfDeath)
                            it.otherPlaceOfDeath = malariaScreeningCache.otherPlaceOfDeath
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }
                    }
                    val lastVisitId = malariaRepo.getlastvisitIdforBen(malariaScreeningCache.benId) ?: 0L
                    val nextVisitId = lastVisitId + 1
                    val newScreening = malariaScreeningCache.copy(id = 0, visitId = nextVisitId)
                    malariaRepo.saveMalariaScreening(newScreening)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving malaria data failed!!")
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
    fun isSuspectedCase(malariaScreeningCache: MalariaScreeningCache): Boolean {
        return malariaScreeningCache.caseStatus == "Suspected"
    }
}