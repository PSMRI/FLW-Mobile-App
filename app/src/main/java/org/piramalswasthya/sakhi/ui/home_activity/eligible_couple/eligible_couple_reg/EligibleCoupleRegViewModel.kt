package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.eligible_couple_reg

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.EligibleCoupleRegistrationDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleRegViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val ecrRepo: EcrRepo,
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
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    //    private lateinit var user: UserDomain
    private val dataset =
        EligibleCoupleRegistrationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var ecrForm: EligibleCoupleRegCache

    init {
        viewModelScope.launch {
            val ben = ecrRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                ecrForm = EligibleCoupleRegCache(
                    benId = ben.beneficiaryId,
                    syncState = SyncState.UNSYNCED
                )
            }

            ecrRepo.getSavedRecord(benId)?.let {
                ecrForm = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) ecrForm else null
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

                    dataset.mapValues(ecrForm, 1)
                    ecrRepo.persistRecord(ecrForm)
                    ecrRepo.getBenFromId(benId)?.let {
                        val hasBenUpdated = dataset.mapValueToBenRegId(it)
                        if (hasBenUpdated)
                            benRepo.persistRecord(it)
                    }
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving ecr data failed!!")
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
}