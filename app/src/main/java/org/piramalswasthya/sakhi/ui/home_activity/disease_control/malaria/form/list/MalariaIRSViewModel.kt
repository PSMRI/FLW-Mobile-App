package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

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
import org.piramalswasthya.sakhi.configuration.IRSRoundDataSet
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import timber.log.Timber
import javax.inject.Inject
@HiltViewModel
class MalariaIRSViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val malariaRepo: MalariaRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    var hhId = MalariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

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
        IRSRoundDataSet(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    private lateinit var irsRoundScreening: IRSRoundScreening


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenListFromHousehold(hhId).also { ben ->
                irsRoundScreening = IRSRoundScreening(
                    id = 0,
                    householdId = hhId,
                )
            }

            dataset.setUpPage(
                if (recordExists.value == true) irsRoundScreening else null
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
                    dataset.mapValues(irsRoundScreening, 1)
                    malariaRepo.saveIRSScreening(irsRoundScreening)
                    malariaRepo.getAllActiveIRSRecords(hhId).apply {
                        malariaRepo.updateIRSRecord(toTypedArray())
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving irs data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }



    fun resetState() {
        _state.value = State.IDLE
    }


    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }

    fun getIndexOfDate(): Int {
        return dataset.getIndexOfDate()
    }
}