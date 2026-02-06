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
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MaaMeetingFormViewModel @Inject constructor(
    private val pref: PreferenceDao,
    savedStateHandle: SavedStateHandle,
    private val repo: MaaMeetingRepo
) : ViewModel() {

    val dataset = MaaMeetingDataset(repo.appContext, pref.getCurrentLanguage())
    var id = MaaMeetingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).id
    val formList = dataset.listFlow

    private val _maaMeetings = repo.getAllMaaMeetings()
        .map { list ->
            list.sortedByDescending { item ->
                try {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(item.meetingDate)
                } catch (e: Exception) {
                    null
                }
            }
        }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maaMeetings: StateFlow<List<MaaMeetingEntity>> = _maaMeetings

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _draftExists = MutableLiveData<MaaMeetingEntity?>(null)
    val draftExists: LiveData<MaaMeetingEntity?>
        get() = _draftExists

    private var maaMeetingEntity: MaaMeetingEntity? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val ashaId = pref.getLoggedInUser()?.userId ?: 0
            val meeting = repo.getMaaMeetingById(id)
            val recordExists = meeting != null

            if (recordExists) {
                maaMeetingEntity = meeting
                populateDataset(meeting!!)
            } else {
                _recordExists.postValue(false)
                dataset.setUpPage(false)
                checkDraft(ashaId)
            }

            _recordExists.postValue(recordExists)

            if (recordExists) {
                withContext(Dispatchers.Main) {
                    dataset.setUpPage(recordExists)
                }
            }
        }
    }

    private suspend fun checkDraft(ashaId: Int) {
        repo.getDraftMaaMeeting(ashaId)?.let {
            _draftExists.postValue(it)
        }
    }

    fun restoreDraft(draft: MaaMeetingEntity) {
        viewModelScope.launch {
            maaMeetingEntity = draft
            populateDataset(draft)
            dataset.setUpPage(false)
            _draftExists.value = null
        }
    }

    private fun populateDataset(meeting: MaaMeetingEntity) {
        dataset.meetingDate.value = meeting.meetingDate
        dataset.meetingPlace.value = meeting.place
        dataset.villageName.value = meeting.villageName
        dataset.noOfPW.value = meeting.noOfPragnentWomen
        dataset.noOfLM.value = meeting.noOfLactingMother
        dataset.duringBreastfeeding.value = valueToIndexCsv(meeting.mitaninActivityCheckList, dataset.duringBreastfeeding.entries!!)
        dataset.participants.value = meeting.participants?.toString()
        val imgs = meeting.meetingImages ?: emptyList()
        dataset.upload1.value = imgs.getOrNull(0)
        dataset.upload2.value = imgs.getOrNull(1)
        dataset.upload3.value = imgs.getOrNull(2)
        dataset.upload4.value = imgs.getOrNull(3)
        dataset.upload5.value = imgs.getOrNull(4)
    }

    fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                repo.deleteById(it.id)
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val ashaId = pref.getLoggedInUser()?.userId ?: 0
                    val entity = repo.buildEntity(
                        date = dataset.meetingDate.value,
                        place = dataset.meetingPlace.value,
                        villageName = dataset.villageName.value,
                        noOfPragnentWoment = dataset.noOfPW.value,
                        noOfLactingMother = dataset.noOfLM.value,
                        mitaninActivityCheckList = toCsv(dataset.duringBreastfeeding.value, dataset.duringBreastfeeding.entries!!),
                        participants = dataset.participants.value?.toIntOrNull(),
                        u1 = dataset.upload1.value,
                        u2 = dataset.upload2.value,
                        u3 = dataset.upload3.value,
                        u4 = dataset.upload4.value,
                        u5 = dataset.upload5.value
                    ).copy(isDraft = true)

                    _draftExists.value?.let {
                        repo.save(entity.copy(id = it.id))
                    } ?: repo.save(entity)

                } catch (e: Exception) {
                    Timber.e("saving draft failed!! $e")
                }
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
            ).copy(isDraft = false)

            _draftExists.value?.let {
                repo.save(entity.copy(id = it.id))
            } ?: repo.save(entity)

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