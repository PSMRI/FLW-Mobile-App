package org.piramalswasthya.sakhi.ui.asha_facilitator_activity.facilitator_home.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.FacilitatorActivityFromDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import javax.inject.Inject

@HiltViewModel
class FacilitatorFormViewModel  @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val ecrRepo: EcrRepo,
    private val benRepo: BenRepo,
    private val hrpRepo: HRPRepo
) : ViewModel() {

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
        FacilitatorActivityFromDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    init {
        viewModelScope.launch {
            dataset.setUpPage()
            _recordExists.value = true

        }
    }
    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            _recordExists.value = true
            dataset.updateList(formId, index)

        }

    }
    fun resetState() {
        _state.value = State.IDLE
    }

}