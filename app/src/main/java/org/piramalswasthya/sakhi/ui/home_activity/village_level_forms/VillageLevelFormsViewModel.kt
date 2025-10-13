package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.model.Icon
import org.piramalswasthya.sakhi.repositories.VLFRepo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VillageLevelFormsViewModel @Inject constructor(
    private val iconDataset: IconDataset,
    private val vlfRepo: VLFRepo,
) : ViewModel() {
    val scope = viewModelScope

    private val _iconsWithRedFlags = MutableStateFlow<List<Pair<Icon, Boolean>>>(emptyList())
    val iconsWithRedFlags: StateFlow<List<Pair<Icon, Boolean>>> = _iconsWithRedFlags

    private val formIdMap = mapOf(
        "VHND" to "vhnd",
        "VHNC" to "vhnc",
        "PHC Review Meeting" to "phc_review",
        "AHD" to "ahd",
        "National Deworming Day" to "deworming",
        "U-win Sessions" to "uwin"
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadIcons(resources: Resources) {
        viewModelScope.launch {
            val icons = iconDataset.getVLFDataset(resources)
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

            val result = icons.map { icon ->
                val formId = formIdMap[icon.title]
                val lastDateString = formId?.let { vlfRepo.getLastSubmissionDate(it).firstOrNull() }

                val lastDate = try {
                    lastDateString?.let { LocalDate.parse(it, formatter) }
                } catch (e: Exception) {
                    null
                }

                // ✅ Overdue only if: today > 7th AND form NOT submitted in current month
                val isOverdue = if (currentDate.dayOfMonth > 7) {
                    lastDate == null || lastDate.month != currentDate.month || lastDate.year != currentDate.year
                } else {
                    false
                }

                icon to isOverdue
            }

            _iconsWithRedFlags.value = result
        }
    }


}