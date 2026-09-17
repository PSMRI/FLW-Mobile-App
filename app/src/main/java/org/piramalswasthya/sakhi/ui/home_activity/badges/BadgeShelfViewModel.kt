package org.piramalswasthya.sakhi.ui.home_activity.badges

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.badges.BadgeRepository
import javax.inject.Inject

@HiltViewModel
class BadgeShelfViewModel @Inject constructor(
    badgeRepository: BadgeRepository
) : ViewModel() {

    val cards: LiveData<List<BadgeRepository.BadgeCard>> =
        badgeRepository.shelf.asLiveData()
}
