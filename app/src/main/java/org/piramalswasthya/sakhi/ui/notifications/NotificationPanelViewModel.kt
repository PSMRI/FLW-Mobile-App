package org.piramalswasthya.sakhi.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.repositories.NotificationRepository
import javax.inject.Inject

/**
 * Backs [NotificationPanelFragment]. Delegates to the shared [NotificationRepository] so the
 * panel and the toolbar bell badge observe the same state.
 */
@HiltViewModel
class NotificationPanelViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: LiveData<List<NotificationDomain>> = repository.notifications.asLiveData()
    val unreadCount: LiveData<Int> = repository.unreadCount.asLiveData()

    fun markRead(notificationId: Long) = repository.markRead(notificationId)

    fun dismiss(notificationId: Long) = repository.dismiss(notificationId)

    fun clearAll() = repository.clearAll()
}