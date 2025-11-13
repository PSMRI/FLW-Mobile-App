package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm

abstract class BaseListViewModel : ViewModel() {
    abstract val benList: Flow<List<BenBasicDomainForForm>>
    abstract fun filterText(text: String)
}
