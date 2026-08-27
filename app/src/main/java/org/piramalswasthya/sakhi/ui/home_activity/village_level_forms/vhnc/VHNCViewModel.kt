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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.VHNCDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltViewModel
class VHNCViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfReo: VLFRepo,
) : ViewModel() {
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED, DATE_ALREADY_FILLED
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
            if (_recordExists.value != true) {
                val meetings = runCatching { allVHNCList.first() }.getOrDefault(emptyList())
                dataset.restrictFilledMonths(meetings, vhncId)
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
                if (!isVhncDateAvailable()) {
                    _state.postValue(State.DATE_ALREADY_FILLED)
                    return@launch
                }
                dataset.mapValues(_vhncCache, 1)
                vlfReo.saveRecord(_vhncCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving VHND data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    /**
     * VHNC allows at most one meeting per calendar month. The date picker
     * min/max bounds cover the rolling two-month window, while this check
     * handles already-used months inside that window.
     */
    private suspend fun isVhncDateAvailable(): Boolean {
        val selectedDate = dataset.getVhncDate() ?: return false
        val selectedMonth = parseMonth(selectedDate) ?: return false

        return allVHNCList.first().none { meeting ->
            meeting.id != vhncId && parseMonth(meeting.vhncDate) == selectedMonth
        }
    }

    private fun parseMonth(value: String): Pair<Int, Int>? {
        return try {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
                isLenient = false
            }.parse(value) ?: return null
            Calendar.getInstance().apply { time = date }.let {
                it.get(Calendar.YEAR) to it.get(Calendar.MONTH)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }


}
