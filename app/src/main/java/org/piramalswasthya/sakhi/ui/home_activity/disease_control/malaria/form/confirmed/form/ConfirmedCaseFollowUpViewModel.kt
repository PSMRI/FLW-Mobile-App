package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.confirmed.form

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.MalariaConfirmCasesDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfirmedCaseFollowUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val malariaRepo: MalariaRepo,
    private val benRepo: BenRepo,
    private val recordsRepo: RecordsRepo,
) : ViewModel() {
    private var slideTest: String? = "Pv"
    val benId =
        ConfirmedCaseFollowUpFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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
        MalariaConfirmCasesDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    private lateinit var malariaConfirmedCasesCache: MalariaConfirmedCasesCache


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                malariaConfirmedCasesCache = MalariaConfirmedCasesCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                )
            }

            malariaRepo.getMalariaConfirmed(benId)?.let {
                malariaConfirmedCasesCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            viewModelScope.launch {
                recordsRepo.malariaConfirmedCasesList.collect { list ->
                    val item = list.find { it.ben.benId == benId }
                    val slide = item?.slideTestName ?: "Pv"

                    dataset.setUpPage(
                        ben,
                        slide,
                        if (_recordExists.value == true) malariaConfirmedCasesCache else null
                    )
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
                    dataset.mapValues(malariaConfirmedCasesCache, 1)
                    malariaRepo.saveMalariaConfirmed(malariaConfirmedCasesCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving Filaria data failed!!")
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

}