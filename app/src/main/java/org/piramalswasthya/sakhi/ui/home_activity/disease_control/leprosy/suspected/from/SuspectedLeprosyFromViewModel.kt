package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.suspected.from

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
import org.piramalswasthya.sakhi.configuration.LeprosyFormDataset
import org.piramalswasthya.sakhi.configuration.LeprosySuspectedDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
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
class SuspectedLeprosyFromViewModel @Inject constructor(
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
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
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
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val username = preferenceDao.getLoggedInUser()?.userName ?: ""


    private val dataset = LeprosySuspectedDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    var _isBeneficaryStatusDeath = MutableLiveData<Boolean>()

    val isBeneficaryStatusDeath: LiveData<Boolean>
        get() = _isBeneficaryStatusDeath
    private lateinit var leprosyScreenCache: LeprosyScreeningCache


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                leprosyScreenCache = LeprosyScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                    createdBy = username,
                    modifiedBy = username,
                )
            }

            leprosyRepo.getLeprosyScreening(benId)?.let {
                leprosyScreenCache = it
                _recordExists.value = true
                _isBeneficaryStatusDeath.value = leprosyScreenCache.beneficiaryStatus.equals("Death", ignoreCase = true)
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) leprosyScreenCache else null
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
                    dataset.mapValues(leprosyScreenCache, 1)
                    if (leprosyScreenCache.beneficiaryStatus == "Death") {
                        isDeath = true
                        val reasons = context.resources.getStringArray(R.array.benificary_case_status)
                        val selectedValue = leprosyScreenCache.reasonForDeath
                        val position = reasons.indexOf(selectedValue)
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            val dateOfDeath = leprosyScreenCache.dateOfDeath?.let { d ->
                                dateFormat.format(Date(d))
                            } ?: ""
                            it.dateOfDeath = dateOfDeath
                            it.reasonOfDeath = leprosyScreenCache.reasonForDeath
                            it.reasonOfDeathId = position
                            it.placeOfDeath = leprosyScreenCache.placeOfDeath
                            it.placeOfDeathId = context.resources.getStringArray(R.array.death_place).indexOf(leprosyScreenCache.placeOfDeath)
                            it.otherPlaceOfDeath = leprosyScreenCache.otherPlaceOfDeath
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }
                    }

                    leprosyRepo.updateLeprosyScreening(leprosyScreenCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving  leprosy screening data  data failed!!")
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