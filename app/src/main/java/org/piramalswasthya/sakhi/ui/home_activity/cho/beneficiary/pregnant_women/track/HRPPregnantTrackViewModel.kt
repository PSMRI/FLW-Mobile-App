package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.track

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
import org.piramalswasthya.sakhi.configuration.HRPPregnantTrackDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import timber.log.Timber

@HiltViewModel
class HRPPregnantTrackViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val hrpReo: HRPRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        HRPPregnantTrackFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    val trackId =
        HRPPregnantTrackFragmentArgs.fromSavedStateHandle(savedStateHandle).trackId

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

    private val _trackingDone = MutableLiveData<Boolean>()
    val trackingDone: LiveData<Boolean>
        get() = _trackingDone

    //    private lateinit var user: UserDomain
    private val dataset =
        HRPPregnantTrackDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isHighRisk: Boolean = false


    private lateinit var hrpPregnantTrackCache: HRPPregnantTrackCache

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                hrpPregnantTrackCache = HRPPregnantTrackCache(
                    benId = ben.beneficiaryId,
                )
            }

            hrpReo.getHrPregTrackList(benId)?.let {
                when (it.size) {
                    0 -> hrpPregnantTrackCache.visit = "1st ANC"
                    1 -> hrpPregnantTrackCache.visit = "1st PMSMA"
                    2 -> hrpPregnantTrackCache.visit = "2nd PMSMA"
                    3 -> hrpPregnantTrackCache.visit = "3rd PMSMA"
                    else -> _trackingDone.value = trackId == 0
                }
            }

            hrpReo.getHRPTrack(trackId = trackId.toLong())?.let {
                if (trackId > 0) {
                    hrpPregnantTrackCache = it
                    _recordExists.value = true
                }

            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) hrpPregnantTrackCache else null,
                hrpReo.getMaxDoVhrp(benId)
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

                    dataset.mapValues(hrpPregnantTrackCache, 1)
                    hrpReo.saveRecord(hrpPregnantTrackCache)
//                    isHighRisk = true
//                    if (isHighRisk) {
//                        // save
//                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PWR data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}