package org.piramalswasthya.sakhi.ui.Fragments

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.MaaMeetingDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.MaaMeetingRepo
import javax.inject.Inject

@HiltViewModel
class MaaMeetingFormViewModel @Inject constructor(
    pref: PreferenceDao,
    private val repo: MaaMeetingRepo
) : ViewModel() {

    val dataset = MaaMeetingDataset(repo.appContext, pref.getCurrentLanguage())

    val formList = dataset.listFlow

    init {
        viewModelScope.launch {
            dataset.setUpPage()
            prefillIfAvailable()
        }
    }

    private suspend fun prefillIfAvailable() {
        withContext(Dispatchers.IO) {
            val latest = repo.getLatest()
            if (latest != null) {
                dataset.meetingDate.value = latest.meetingDate
                dataset.meetingPlace.value = latest.place
                dataset.participants.value = latest.participants?.toString()
                val imgs = latest.meetingImages ?: emptyList()
                dataset.upload1.value = imgs.getOrNull(0)
                dataset.upload2.value = imgs.getOrNull(1)
                dataset.upload3.value = imgs.getOrNull(2)
                dataset.upload4.value = imgs.getOrNull(3)
                dataset.upload5.value = imgs.getOrNull(4)
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
            withContext(Dispatchers.Main) { prefillIfAvailable() }
        }
    }

    suspend fun hasMeetingInSameQuarter(): Boolean {
        return repo.hasMeetingInSameQuarter(dataset.meetingDate.value)
    }
}


