package org.piramalswasthya.sakhi.ui.home_activity.disease_control.kala_azar.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.KalaAzarFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.KalaAzarRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class KalaAzarFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext var context: Context,
    private val kalaAzarRepo: KalaAzarRepo,
    private val benRepo: BenRepo,
    private val maternalHealthRepo: MaternalHealthRepo,

    ) : ViewModel() {




    val benId =
        KalaAzarFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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

    private val dataset =
        KalaAzarFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var suspectedKalaAzar: String? = null

    var suspectedKalaAzarFamily: String? = null

    private lateinit var kalaazarScreeningCache: KalaAzarScreeningCache

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
    var isDeath = false
    var _isBeneficaryStatusDeath = MutableLiveData<Boolean>()

    val isBeneficaryStatusDeath: LiveData<Boolean>
        get() = _isBeneficaryStatusDeath
    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                kalaazarScreeningCache = KalaAzarScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId
                )
            }

            kalaAzarRepo.getKalaAzarScreening(benId)?.let {
                kalaazarScreeningCache = it
                _recordExists.value = true
                _isBeneficaryStatusDeath.value = kalaazarScreeningCache.beneficiaryStatus.equals("Death", ignoreCase = true)
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) kalaazarScreeningCache else null
            )

        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }


    /* fun getAlerts() {
         suspectedMaleria = dataset.()
         suspectedMaleriaFamily = dataset.isTbSuspectedFamily()
     }*/

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(kalaazarScreeningCache, 1)
                    if (kalaazarScreeningCache.beneficiaryStatus == "Death") {
                        isDeath = true
                        val reasons = context.resources.getStringArray(R.array.benificary_case_status_kalaazar)
                        val selectedValue = kalaazarScreeningCache.reasonForDeath
                        val position = reasons.indexOf(selectedValue)
                        maternalHealthRepo.getBenFromId(benId)?.let {
                            it.isDeath = true
                            it.isDeathValue = "Death"
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            val dateOfDeath = kalaazarScreeningCache.dateOfDeath?.let { d ->
                                dateFormat.format(Date(d))
                            } ?: ""
                            it.dateOfDeath = dateOfDeath
                            it.reasonOfDeath = kalaazarScreeningCache.reasonForDeath
                            it.reasonOfDeathId = position
                            it.placeOfDeath = kalaazarScreeningCache.placeOfDeath
                            it.placeOfDeathId = context.resources.getStringArray(R.array.death_place).indexOf(kalaazarScreeningCache.placeOfDeath)
                            it.otherPlaceOfDeath = kalaazarScreeningCache.otherPlaceOfDeath
                            if (it.processed != "N") it.processed = "U"
                            it.syncState = SyncState.UNSYNCED
                            benRepo.updateRecord(it)
                        }
                    }
                    kalaAzarRepo.saveKalaAzarScreening(kalaazarScreeningCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving kala azar  data failed!! $e")
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