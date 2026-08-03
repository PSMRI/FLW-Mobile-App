package org.piramalswasthya.sakhi.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.repositories.NotificationRepository
import javax.inject.Inject

/**
 * Backs [NotificationPanelFragment]. Delegates to the shared [NotificationRepository] (Room-backed)
 * so the panel and the toolbar bell badge observe the same state. Actions mutate Room off the main
 * thread in [viewModelScope]; the observing LiveData refreshes automatically.
 */
@HiltViewModel
class NotificationPanelViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: LiveData<List<NotificationDomain>> = repository.notifications.asLiveData()
    val unreadCount: LiveData<Int> = repository.unreadCount.asLiveData()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch { repository.pullAndSaveNotifications() }

    fun markRead(notificationId: Long) = viewModelScope.launch { repository.markRead(notificationId) }

    fun dismiss(notificationId: Long) = viewModelScope.launch { repository.dismiss(notificationId) }

    fun clearAll() = viewModelScope.launch { repository.clearAll() }
}