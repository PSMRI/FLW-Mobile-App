package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class LeprosyViewModel @Inject constructor() : ViewModel() {

    val scope: CoroutineScope
        get() = viewModelScope

}

