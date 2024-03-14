package org.piramalswasthya.sakhi.ui.home_activity.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    syncDao: SyncDao,
    preferenceDao: PreferenceDao
) : ViewModel() {

    val syncStatus = syncDao.getSyncStatus()

    val lang = preferenceDao.getCurrentLanguage()
    fun getLocalNames(context: Context): Array<String> {
        return getLocalizedResources(context, lang).getStringArray(R.array.sync_records)
    }

    fun getEnglishNames(context: Context): Array<String> {
        return getLocalizedResources(
            context,
            Languages.ENGLISH
        ).getStringArray(R.array.sync_records)
    }
}