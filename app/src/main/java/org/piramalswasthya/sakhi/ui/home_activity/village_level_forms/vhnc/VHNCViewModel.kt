package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.vhnc

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.VHNCDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber

@HiltViewModel
class VHNCViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfReo: VLFRepo,
) : ViewModel() {
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    val allVHNCList = vlfReo.vhncList
    private val vhncId = VHNCFormFragementArgs.fromSavedStateHandle(savedStateHandle).id


    @RequiresApi(Build.VERSION_CODES.O)
    val isCurrentMonthFormFilled : Flow<Map<String, Boolean>> = vlfReo.isFormFilledForCurrentMonth()

    //
    private var lastImageFormId: Int = 0
    fun setCurrentImageFormId(id: Int) {
        lastImageFormId = id
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastImageFormId, dpUri)
//        Log.v("jnjdnjsadd>>","$dpUri")
    }
//    private val _dateVHND = MutableLiveData<String>()
//    val dateVHND: LiveData<String>
//        get() = _dateVHND
//
//    private val _noOfBenAttVHND = MutableLiveData<String>()
//    val noOfBenAttVHND: LiveData<String>
//        get() = _noOfBenAttVHND
//
//    private val _placeVHND = MutableLiveData<String>()
//    val placeVHND: LiveData<String>
//        get() = _placeVHND


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists
    private val dataset =
        VHNCDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    lateinit var _vhncCache: VHNCCache

    init {
        viewModelScope.launch {

            _vhncCache = VHNCCache(id = 0, vhncDate = "");
            val vhncIds = vlfReo.getVHNC(vhncId)
            vlfReo.getVHNC(vhncId)?.let {
                _vhncCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            dataset.setUpPage(
                if (recordExists.value == true) vhncIds else null
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
            try {
                _state.postValue(State.SAVING)
                dataset.mapValues(_vhncCache, 1)
                vlfReo.saveRecord(_vhncCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving VHND data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }


}