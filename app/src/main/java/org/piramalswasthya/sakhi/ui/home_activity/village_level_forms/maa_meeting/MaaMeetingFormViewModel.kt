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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.MaaMeetingDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo
import java.text.SimpleDateFormat
import java.util.Locale
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
        .map { list ->
            list.sortedByDescending { item ->
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(item.meetingDate)
            }
        }
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
                dataset.villageName.value = meeting.villageName
                dataset.noOfPW.value = meeting.noOfPragnentWomen
                dataset.noOfLM.value = meeting.noOfLactingMother
                dataset.duringBreastfeeding.value = valueToIndexCsv(meeting.mitaninActivityCheckList,dataset.duringBreastfeeding.entries!!)
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

    suspend fun saveForm() {
        withContext(Dispatchers.IO) {
            val entity = repo.buildEntity(
                date = dataset.meetingDate.value,
                place = dataset.meetingPlace.value,
                villageName = dataset.villageName.value,
                noOfPragnentWoment = dataset.noOfPW.value?.takeIf { it.isNotEmpty() } ?: "0",
                noOfLactingMother = dataset.noOfLM.value?.takeIf { it.isNotEmpty() } ?: "0",
                mitaninActivityCheckList = toCsv(dataset.duringBreastfeeding.value, dataset.duringBreastfeeding.entries!!),
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


    suspend fun hasMeetingInSameQuarter(date: String?): Boolean {
        return repo.isThreeMonthsPassedSinceLastMeeting(date)
    }
    fun toCsv(selected: String?, entries: Array<String>): String {
        if (selected.isNullOrEmpty()) return ""
        return selected.split("|")
            .mapNotNull { it.toIntOrNull() }
            .filter { it in entries.indices }
            .map { entries[it] }
            .joinToString("|")
    }
    fun valueToIndexCsv(
        valueCsv: String?,
        entries: Array<String>
    ): String {
        if (valueCsv.isNullOrEmpty()) return ""
        val values = valueCsv.split("|").map { it.trim() }
        return values.mapNotNull { value ->
            val index = entries.indexOfFirst { it.trim() == value }
            if (index >= 0) index else null
        }.joinToString("|")
    }


}