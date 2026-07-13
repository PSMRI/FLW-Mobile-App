package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.piramalswasthya.sakhi.model.NotificationDomain
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for in-app notifications, shared by the toolbar bell badge and the
 * notification panel so their state stays in sync.
 *
 * NOTE: currently backed by an in-memory seeded list so the UI is fully functional before the
 * data layer exists. In T7 the internals are replaced with Room (`NotificationDao`) + the poll
 * worker / FCM upserts; the public API below stays the same.
 */
@Singleton
class NotificationRepository @Inject constructor() {

    private val _notifications = MutableStateFlow(seedData())

    val notifications: StateFlow<List<NotificationDomain>> = _notifications.asStateFlow()

    val unreadCount: Flow<Int> = _notifications.map { list -> list.count { !it.read } }

    fun markRead(notificationId: Long) {
        _notifications.update { list ->
            list.map { if (it.notificationId == notificationId) it.copy(read = true) else it }
        }
    }

    fun dismiss(notificationId: Long) {
        _notifications.update { list -> list.filterNot { it.notificationId == notificationId } }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }

    // --- temporary seed data; removed when Room + sync are wired in T7 ---
    private fun seedData(): List<NotificationDomain> {
        val now = System.currentTimeMillis()
        val hour = 60 * 60 * 1000L
        return listOf(
            NotificationDomain(
                notificationId = 1001,
                eventType = "INCENTIVE_CLAIMED",
                title = "Incentive Claim Received",
                body = "ASHA Saurav Mishra has claimed for june month",
                createdTs = now - 1 * hour,
                read = false,
                navId = "INCENTIVE_APPROVAL",
                priority = "HIGH",
                senderUserId = 4259,
                receiverUserId = 140,
                beneficiaryId = 98765,
                activityId = 140,
                referenceId = 78954
            ),
            NotificationDomain(
                notificationId = 1, eventType = "ASHA_CLAIM_REJECTED",
                title = "Your monthly claim was rejected",
                body = "October 2026 · Reason: Incomplete documentation. Please correct and resubmit.",
                createdTs = now - 2 * hour, read = false
            ),
            NotificationDomain(
                notificationId = 2, eventType = "SUPERVISOR_VERIFICATION_REMINDER",
                title = "Pending verifications for October 2026",
                body = "3 ASHAs (12 activities) are pending your verification.",
                createdTs = now - 26 * hour, read = false
            ),
            NotificationDomain(
                notificationId = 3, eventType = "ASHA_STAGE_CHANGE",
                title = "Your claim is now Verified by Supervisor",
                body = "Your October 2026 claim has moved to the CHO stage.",
                createdTs = now - 3 * 24 * hour, read = true
            )
        )
    }
}