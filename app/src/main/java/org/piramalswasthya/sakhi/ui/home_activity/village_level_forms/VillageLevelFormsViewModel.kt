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
        "National Deworming Day" to "deworming"
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadIcons(resources: Resources) {
        viewModelScope.launch {
            val icons = iconDataset.getVLFDataset(resources)
            val currentDate = LocalDate.now() // Get current date
            val firstOfMonth = currentDate.withDayOfMonth(1)  // First day of the current month
            val seventhOfMonth = currentDate.withDayOfMonth(7)  // Seventh day of the current month
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

            // Perform the overdue check only if the current date is 7th or later in the month
            val result =
                if (currentDate.isAfter(seventhOfMonth) || currentDate.isEqual(seventhOfMonth)) {
                    // If it's after the 7th, perform the overdue check
                    icons.map { icon ->
                        val formId = formIdMap[icon.title]
                        val lastDateString =
                            formId?.let { vlfRepo.getLastSubmissionDate(it).firstOrNull() }
                        Log.d("OverdueCheck", "Last submission date: $lastDateString")

                        // Parse the date if it's not null
                        val lastDate = try {
                            lastDateString?.let { LocalDate.parse(it, formatter) }
                        } catch (e: Exception) {
                            null
                        }

                        // Check if there is no submission in the current month or if the submission date is outside the 1st-7th range
                        val isOverdue = lastDate == null ||
                                lastDate.month != currentDate.month || // No submission in current month
                                lastDate.isBefore(firstOfMonth) ||    // Submission before the 1st of the month
                                lastDate.isAfter(seventhOfMonth)      // Submission after the 7th of the month

//                        Log.d(
//                            "OverdueCheck",
//                            "Is overdue: $isOverdue, lastDate: $lastDate, deadline: $firstOfMonth - $seventhOfMonth"
//                        )

                        icon to isOverdue
                    }
                } else {
                    // If it's before the 7th, no overdue check, so return the icons without any overdue status
                    icons.map { icon -> icon to false }
                }

            // Update the live data with icons and their overdue status
            _iconsWithRedFlags.value = result
        }
    }

}