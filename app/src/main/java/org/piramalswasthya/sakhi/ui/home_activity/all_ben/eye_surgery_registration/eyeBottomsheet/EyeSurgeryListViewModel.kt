package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.EyeSurgeryFormRepository
import org.piramalswasthya.sakhi.utils.Log
import javax.inject.Inject

@HiltViewModel
class EyeSurgeryListViewModel @Inject constructor(
    private val repository: EyeSurgeryFormRepository
) : ViewModel() {

    suspend fun getSavedVisits(benId: Long): List<EyeSurgeryFormResponseJsonEntity> {
        return repository.getAllVisitsByBenId(benId)
    }

    suspend fun getAvailableEyes(benId: Long): List<String> {
        val visits = repository.getAllVisitsByBenId(benId)
        Log.d("EyeList", "visits=${visits.size}")
        visits.forEach {
         Log.d("EyeList", "visit: id=${it.id} eyeSide=${it.eyeSide}")
        }

        val completed = visits.map { it.eyeSide.uppercase() }.toSet()
       Log.d("EyeList", "completed=$completed")

        val available = when {
            completed.contains("BOTH") -> emptyList()
            completed.contains("LEFT") && completed.contains("RIGHT") -> emptyList()
            completed.contains("LEFT") -> listOf("RIGHT")
            completed.contains("RIGHT") -> listOf("LEFT")
            else -> listOf("LEFT", "RIGHT", "BOTH")
        }
        Log.d("EyeList", "available=$available")
        return available
    }}