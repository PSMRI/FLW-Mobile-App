package org.piramalswasthya.sakhi.ui.home_activity.lms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class LmsViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {
    private val filter = MutableStateFlow("")

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    val scope: CoroutineScope
        get() = viewModelScope
}