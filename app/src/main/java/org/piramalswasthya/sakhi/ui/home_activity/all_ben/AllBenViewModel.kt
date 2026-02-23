package org.piramalswasthya.sakhi.ui.home_activity.all_ben

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.ABHAGenratedRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

@HiltViewModel
class AllBenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recordsRepo: RecordsRepo,
    abhaGenratedRepo: ABHAGenratedRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    private var sourceFromArgs = AllBenFragmentArgs.fromSavedStateHandle(savedStateHandle).source

    private val filterOrg = MutableStateFlow("")
    private val kindOrg = MutableStateFlow(0)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val benList: Flow<PagingData<BenBasicDomain>> = combine(filterOrg, kindOrg) { text, kind ->
        Pair(text, kind)
    }.debounce { (text, _) ->
        if (text.isEmpty()) 0L else 300L
    }.flatMapLatest { (text, kind) ->
        Pager(
            config = PagingConfig(pageSize = 30, prefetchDistance = 10)
        ) {
            recordsRepo.searchBenPagedSource(text, kind, sourceFromArgs)
        }.flow.map { pagingData ->
            pagingData.map { it.asBasicDomainModel() }
        }
    }

    val childCounts: Flow<Map<Long, Int>> = recordsRepo.childCountsByBen

    private val _abha = MutableLiveData<String?>()
    val abha: LiveData<String?>
        get() = _abha

    private val _benId = MutableLiveData<Long?>()
    val benId: LiveData<Long?>
        get() = _benId

    private val _benRegId = MutableLiveData<Long?>()
    val benRegId: LiveData<Long?>
        get() = _benRegId

    fun filterText(text: String) {
        viewModelScope.launch {
            filterOrg.emit(text)
        }

    }

    fun filterType(type: Int) {
        viewModelScope.launch {
            kindOrg.emit(type)
        }

    }

    fun fetchAbha(benId: Long) {
        _abha.value = null
        _benRegId.value = null
        _benId.value = benId
        viewModelScope.launch {
            benRepo.getBenFromId(benId)?.let {
                _benRegId.value = it.benRegId
            }
        }
    }

    suspend fun getBenFromId(benId: Long):Long{
        var benRegId = 0L
             val result = benRepo.getBenFromId(benId)
             if (result != null) {
                 benRegId = result.benRegId
             }
         return benRegId
    }
    fun resetBenRegId() {
        _benRegId.value = null
    }

    fun downloadCsv(context: Context) {
        viewModelScope.launch {
            val users = recordsRepo.searchBenOnce(filterOrg.value, kindOrg.value, sourceFromArgs)
            if (users.isNotEmpty()) {
                createCsvFile(context, users)
            } else {
                Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createCsvFile(context: Context, users: List<BenBasicDomain>): File? {
        return try {
            val fileName = "ABHAUsers_${System.currentTimeMillis()}.csv"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)

            FileWriter(file).use { writer ->

                writer.append("Ben ID,Beneficiary Name,Mobile,ABHA ID,Age,IsNewAbha,RCH ID\n")
                for (user in users) {
                    writer.append("${user.benId}\t,${user.benFullName},${user.mobileNo},${user.abhaId},${user.age},${user.isNewAbha},${user.rchId}\t\n")
                }
            }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)

            Toast.makeText(context, "CSV Downloaded: ${file.name}", Toast.LENGTH_LONG).show()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
