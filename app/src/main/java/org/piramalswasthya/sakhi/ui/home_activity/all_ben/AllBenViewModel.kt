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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
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
    recordsRepo: RecordsRepo,
    abhaGenratedRepo: ABHAGenratedRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    private var sourceFromArgs = AllBenFragmentArgs.fromSavedStateHandle(savedStateHandle).source

    private val allBenList = when (sourceFromArgs) {
        1 -> {
            recordsRepo.allBenWithAbhaList
        }
        2 -> {
            recordsRepo.allBenWithRchList
        }
        3 -> {
            recordsRepo.allBenAboveThirtyList
        }
        4 -> {
            recordsRepo.allBenWARAList
        }
        else -> {
            recordsRepo.allBenList
        }
    }

    private val filterOrg = MutableStateFlow("")
    private val kindOrg = MutableStateFlow(0)

    val benList = allBenList.combine(kindOrg) { list, kind ->
        filterBenList(list, kind)
    }.combine(filterOrg) { list, filter ->
        filterBenList(list, filter)
            .sortedWith(
                compareBy<BenBasicDomain> {
                    when {
                        !it.isDeath && !it.isDeactivate -> 0
                        it.isDeath && !it.isDeactivate -> 1
                        it.isDeactivate -> 2
                        else -> 4
                    }
                }
            )
    }

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

    fun createCsvFile(context: Context, users: List<BenBasicDomain>): File? {
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