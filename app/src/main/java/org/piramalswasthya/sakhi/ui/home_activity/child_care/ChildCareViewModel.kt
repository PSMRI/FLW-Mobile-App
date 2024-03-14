package org.piramalswasthya.sakhi.ui.home_activity.child_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class ChildCareViewModel @Inject constructor() : ViewModel() {

    val scope: CoroutineScope
        get() = viewModelScope
}