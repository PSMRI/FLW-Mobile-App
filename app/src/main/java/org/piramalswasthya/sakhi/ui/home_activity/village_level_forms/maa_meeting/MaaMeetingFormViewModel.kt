package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.maa_meeting

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.MaaMeetingDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo
import javax.inject.Inject

@HiltViewModel
class MaaMeetingFormViewModel @Inject constructor(
    pref: PreferenceDao,
    savedStateHandle: SavedStateHandle,
    private val repo: MaaMeetingRepo
) : ViewModel() {

    val dataset = MaaMeetingDataset(repo.appContext, pref.getCurrentLanguage())
    var id = MaaMeetingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).id
    val formList = dataset.listFlow

    private val _maaMeetings = repo.getAllMaaMeetings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maaMeetings: StateFlow<List<MaaMeetingEntity>> = _maaMeetings

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val meeting = repo.getMaaMeetingById(id)
            val recordExists = meeting != null

            if (recordExists) {
                dataset.meetingDate.value = meeting!!.meetingDate
                dataset.meetingPlace.value = meeting.place
                dataset.participants.value = meeting.participants?.toString()
                val imgs = meeting.meetingImages ?: emptyList()
                dataset.upload1.value = imgs.getOrNull(0)
                dataset.upload2.value = imgs.getOrNull(1)
                dataset.upload3.value = imgs.getOrNull(2)
                dataset.upload4.value = imgs.getOrNull(3)
                dataset.upload5.value = imgs.getOrNull(4)
            }

            _recordExists.postValue(recordExists)

            withContext(Dispatchers.Main) {
                dataset.setUpPage(recordExists)
            }
        }
    }


    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun setUploadUriFor(formId: Int, uri: Uri) {
        when (formId) {
            10 -> dataset.upload1.value = uri.toString()
            11 -> dataset.upload2.value = uri.toString()
            12 -> dataset.upload3.value = uri.toString()
            13 -> dataset.upload4.value = uri.toString()
            14 -> dataset.upload5.value = uri.toString()
        }
    }

    fun saveForm() {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = repo.buildEntity(
                date = dataset.meetingDate.value,
                place = dataset.meetingPlace.value,
                participants = dataset.participants.value?.toIntOrNull(),
                u1 = dataset.upload1.value,
                u2 = dataset.upload2.value,
                u3 = dataset.upload3.value,
                u4 = dataset.upload4.value,
                u5 = dataset.upload5.value
            )
            repo.save(entity)
            repo.tryUpsync()
            repo.downSyncAndPersist()
        }
    }

    suspend fun hasMeetingInSameQuarter(): Boolean {
        return repo.hasMeetingInSameQuarter(dataset.meetingDate.value)
    }
}