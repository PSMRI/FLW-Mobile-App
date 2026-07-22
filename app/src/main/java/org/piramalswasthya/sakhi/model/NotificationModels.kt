package org.piramalswasthya.sakhi.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.R

/**
 * Backend notification contract. A push / list entry looks like:
 *
 * ```
 * {
 *   "title": "Incentive Claim Received",
 *   "body": "ASHA Saurav Mishra has claimed for june month",
 *   "data": {
 *     "notification_id": 1001,
 *     "notification_type": "INCENTIVE_CLAIMED",
 *     "nav_id": "INCENTIVE_APPROVAL",
 *     "sender_user_id": 4259,
 *     "receiver_user_id": 140,
 *     "beneficiary_id": 98765,
 *     "activity_id": 140,
 *     "reference_id": 78954,
 *     "priority": "HIGH"
 *   }
 * }
 * ```
 *
 * Two arrival paths share this shape:
 *  - **FCM push** — `RemoteMessage.data` is a flat `Map<String,String>` (the `data` object
 *    with every value stringified); `title`/`body` come from `RemoteMessage.notification`.
 *    Parse via [notificationFromFcm].
 *  - **Poll / list API** — typed JSON deserialized by Moshi into [NotificationDto].
 */

// ============================================================
// Network / FCM DTOs
// ============================================================

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "data") val data: NotificationDataDto? = null
)

@JsonClass(generateAdapter = true)
data class NotificationDataDto(
    @Json(name = "notification_id") val notificationId: Long,
    @Json(name = "notification_type") val notificationType: String? = null,
    @Json(name = "nav_id") val navId: String? = null,
    @Json(name = "sender_user_id") val senderUserId: Long? = null,
    @Json(name = "receiver_user_id") val receiverUserId: Long? = null,
    @Json(name = "beneficiary_id") val beneficiaryId: Long? = null,
    @Json(name = "activity_id") val activityId: Long? = null,
    @Json(name = "reference_id") val referenceId: Long? = null,
    @Json(name = "priority") val priority: String? = null
)

// ------------------------------------------------------------
// List / interaction API DTOs (T8)
//
// NOTE: the backend list/mark/clear contract is NOT yet confirmed — the endpoints in
// AmritApiService are placeholders ("dummy") and these request/response shapes mirror the
// standard AMRIT envelope ({ data, statusCode, status }) and reuse the confirmed push item
// shape [NotificationDto]. Reconcile field names when the backend contract lands.
// ------------------------------------------------------------

/** `POST notification/list` body. `sinceId`/`sinceTs` are the incremental-sync markers. */
@JsonClass(generateAdapter = true)
data class NotificationListRequest(
    @Json(name = "userId") val userId: Long,
    @Json(name = "role") val role: String? = null,
    @Json(name = "sinceId") val sinceId: Long? = null,
    @Json(name = "sinceTs") val sinceTs: Long? = null
)

/** `notification/list` response envelope; `data` is the list of notifications. */
@JsonClass(generateAdapter = true)
data class NotificationListResponse(
    @Json(name = "data") val data: List<NotificationDto>? = null,
    @Json(name = "statusCode") val statusCode: Int? = null,
    @Json(name = "status") val status: String? = null
)

/** `POST notification/markRead` and `notification/clear` body (id-scoped). */
@JsonClass(generateAdapter = true)
data class NotificationIdsRequest(
    @Json(name = "userId") val userId: Long,
    @Json(name = "notificationIds") val notificationIds: List<Long>
)

/** `POST notification/markAllRead` and `notification/clearAll` body (user-scoped). */
@JsonClass(generateAdapter = true)
data class NotificationUserRequest(
    @Json(name = "userId") val userId: Long
)

/** Data-object keys, shared by the Moshi DTO and the FCM string-map parser. */
object NotificationKeys {
    const val NOTIFICATION_ID = "notification_id"
    const val NOTIFICATION_TYPE = "notification_type"
    const val NAV_ID = "nav_id"
    const val SENDER_USER_ID = "sender_user_id"
    const val RECEIVER_USER_ID = "receiver_user_id"
    const val BENEFICIARY_ID = "beneficiary_id"
    const val ACTIVITY_ID = "activity_id"
    const val REFERENCE_ID = "reference_id"
    const val PRIORITY = "priority"
}

// ============================================================
// Room entity (persistent store — single source of truth)
// ============================================================

/**
 * Persisted notification row. `notificationId` is the server id (PK). `userId` scopes rows to the
 * locally logged-in user; `read`/`cleared`/`viewed` are local interaction flags (soft-clear so a
 * poll can't resurrect a cleared row). Deeplink context is flat scalars (`navId` + the ids), so no
 * TypeConverter is needed. `createdTs` is receive/fetch time — the payload carries no timestamp.
 */
@Entity(tableName = "NOTIFICATION")
data class NotificationEntity(
    @PrimaryKey val notificationId: Long,
    val userId: Long,
    val role: String? = null,
    val eventType: String,
    val navId: String? = null,
    val title: String,
    val body: String,
    val priority: String? = null,
    val createdTs: Long,
    val read: Boolean = false,
    val cleared: Boolean = false,
    val viewed: Boolean = false,
    val senderUserId: Long? = null,
    val receiverUserId: Long? = null,
    val beneficiaryId: Long? = null,
    val activityId: Long? = null,
    val referenceId: Long? = null
)

// ============================================================
// UI / domain model (what the adapter + ViewModel bind to)
// ============================================================

/**
 * UI model for a single in-app notification. The first six fields drive the row rendering;
 * the rest carry the routing context needed for deeplinking (Phase 2) and interaction reports.
 * `createdTs` is stamped at receive/fetch time — the backend payload carries no timestamp.
 */
data class NotificationDomain(
    val notificationId: Long,
    val eventType: String,          // == data.notification_type
    val title: String,
    val body: String,
    val createdTs: Long,
    val read: Boolean,
    val navId: String? = null,      // == data.nav_id — deeplink target
    val priority: String? = null,
    val senderUserId: Long? = null,
    val receiverUserId: Long? = null,
    val beneficiaryId: Long? = null,
    val activityId: Long? = null,
    val referenceId: Long? = null
)

/**
 * Maps a backend `notification_type` to its display icon. `serverKey`s follow the backend's
 * SCREAMING_SNAKE convention (confirmed for [INCENTIVE_CLAIMED]); the remaining keys mirror the
 * event types in the BRD and should be reconciled with the backend as each trigger goes live.
 */
enum class NotificationEventType(
    val serverKey: String,
    @DrawableRes val iconRes: Int
) {
    INCENTIVE_CLAIMED("INCENTIVE_CLAIMED", R.drawable.ic_notifications),
    ASHA_CLAIM_REJECTED("ASHA_CLAIM_REJECTED", R.drawable.ic_notif_rejected),
    SUPERVISOR_VERIFICATION_REMINDER("SUPERVISOR_VERIFICATION_REMINDER", R.drawable.ic_notif_reminder),
    ASHA_SUBMISSION_REMINDER("ASHA_SUBMISSION_REMINDER", R.drawable.ic_notif_reminder),
    ASHA_STAGE_CHANGE("ASHA_STAGE_CHANGE", R.drawable.ic_notifications),
    SUPERVISOR_AUTO_ROUTING("SUPERVISOR_AUTO_ROUTING", R.drawable.ic_notifications),
    CHO_VERIFICATION_REMINDER("CHO_VERIFICATION_REMINDER", R.drawable.ic_notif_reminder),
    ANM_VERIFICATION_REMINDER("ANM_VERIFICATION_REMINDER", R.drawable.ic_notif_reminder),
    GENERIC("", R.drawable.ic_notifications);

    companion object {
        fun fromKey(key: String?): NotificationEventType =
            values().firstOrNull { it.serverKey.equals(key, ignoreCase = true) } ?: GENERIC
    }
}

/**
 * Deeplink target carried by `nav_id`. Consumed by the DeeplinkRouter (Phase 2 / T17).
 * Only [INCENTIVE_APPROVAL] is confirmed so far; more targets are added with FLW-1083/1084.
 */
enum class NotificationNavTarget(val navId: String) {
    INCENTIVE_APPROVAL("INCENTIVE_APPROVAL"),
    NONE("");

    companion object {
        fun fromNavId(navId: String?): NotificationNavTarget =
            values().firstOrNull { it.navId.equals(navId, ignoreCase = true) } ?: NONE
    }
}

// ============================================================
// Mappers
// ============================================================

/**
 * Poll / list API path: typed DTO → domain. Returns null if the payload has no `data` block.
 * @param createdTs receive/fetch time (the payload carries no timestamp).
 */
fun NotificationDto.toDomain(createdTs: Long, read: Boolean = false): NotificationDomain? {
    val d = data ?: return null
    return NotificationDomain(
        notificationId = d.notificationId,
        eventType = d.notificationType.orEmpty(),
        title = title.orEmpty(),
        body = body.orEmpty(),
        createdTs = createdTs,
        read = read,
        navId = d.navId,
        priority = d.priority,
        senderUserId = d.senderUserId,
        receiverUserId = d.receiverUserId,
        beneficiaryId = d.beneficiaryId,
        activityId = d.activityId,
        referenceId = d.referenceId
    )
}

/**
 * FCM push path: a flat `Map<String,String>` (from `RemoteMessage.data`) → domain. Numeric fields
 * arrive stringified, so they are parsed with [String.toLongOrNull]. Returns null if the required
 * `notification_id` is missing/unparseable.
 * @param title/body from `RemoteMessage.notification` (fall back to the data map upstream if absent).
 * @param receivedTs time the push was received.
 */
fun notificationFromFcm(
    data: Map<String, String>,
    title: String?,
    body: String?,
    receivedTs: Long
): NotificationDomain? {
    val id = data[NotificationKeys.NOTIFICATION_ID]?.toLongOrNull() ?: return null
    return NotificationDomain(
        notificationId = id,
        eventType = data[NotificationKeys.NOTIFICATION_TYPE].orEmpty(),
        title = title.orEmpty(),
        body = body.orEmpty(),
        createdTs = receivedTs,
        read = false,
        navId = data[NotificationKeys.NAV_ID],
        priority = data[NotificationKeys.PRIORITY],
        senderUserId = data[NotificationKeys.SENDER_USER_ID]?.toLongOrNull(),
        receiverUserId = data[NotificationKeys.RECEIVER_USER_ID]?.toLongOrNull(),
        beneficiaryId = data[NotificationKeys.BENEFICIARY_ID]?.toLongOrNull(),
        activityId = data[NotificationKeys.ACTIVITY_ID]?.toLongOrNull(),
        referenceId = data[NotificationKeys.REFERENCE_ID]?.toLongOrNull()
    )
}

/** Room row → UI model. */
fun NotificationEntity.toDomain(): NotificationDomain = NotificationDomain(
    notificationId = notificationId,
    eventType = eventType,
    title = title,
    body = body,
    createdTs = createdTs,
    read = read,
    navId = navId,
    priority = priority,
    senderUserId = senderUserId,
    receiverUserId = receiverUserId,
    beneficiaryId = beneficiaryId,
    activityId = activityId,
    referenceId = referenceId
)

/**
 * Poll / list API path → Room row, scoped to [userId]. Returns null if the payload has no `data`.
 * @param createdTs receive/fetch time (payload carries no timestamp).
 */
fun NotificationDto.toEntity(userId: Long, createdTs: Long, read: Boolean = false): NotificationEntity? {
    val d = data ?: return null
    return NotificationEntity(
        notificationId = d.notificationId,
        userId = userId,
        eventType = d.notificationType.orEmpty(),
        navId = d.navId,
        title = title.orEmpty(),
        body = body.orEmpty(),
        priority = d.priority,
        createdTs = createdTs,
        read = read,
        senderUserId = d.senderUserId,
        receiverUserId = d.receiverUserId,
        beneficiaryId = d.beneficiaryId,
        activityId = d.activityId,
        referenceId = d.referenceId
    )
}

/**
 * List API response → Room rows, scoped to [userId]. Items with no `data` block are dropped.
 * @param createdTs receive/fetch time applied to every row (payload carries no timestamp). NOTE:
 * the poll consumer (T10) must preserve any existing local `read`/`cleared`/`createdTs` rather than
 * blindly replacing rows, so a soft-cleared notification isn't resurrected.
 */
fun NotificationListResponse.toEntities(userId: Long, createdTs: Long): List<NotificationEntity> =
    data.orEmpty().mapNotNull { it.toEntity(userId = userId, createdTs = createdTs) }

/**
 * FCM push path (`RemoteMessage.data` string map) → Room row, scoped to [userId].
 * Returns null if the required `notification_id` is missing/unparseable.
 */
fun notificationEntityFromFcm(
    data: Map<String, String>,
    title: String?,
    body: String?,
    userId: Long,
    receivedTs: Long
): NotificationEntity? {
    val id = data[NotificationKeys.NOTIFICATION_ID]?.toLongOrNull() ?: return null
    return NotificationEntity(
        notificationId = id,
        userId = userId,
        eventType = data[NotificationKeys.NOTIFICATION_TYPE].orEmpty(),
        navId = data[NotificationKeys.NAV_ID],
        title = title.orEmpty(),
        body = body.orEmpty(),
        priority = data[NotificationKeys.PRIORITY],
        createdTs = receivedTs,
        senderUserId = data[NotificationKeys.SENDER_USER_ID]?.toLongOrNull(),
        receiverUserId = data[NotificationKeys.RECEIVER_USER_ID]?.toLongOrNull(),
        beneficiaryId = data[NotificationKeys.BENEFICIARY_ID]?.toLongOrNull(),
        activityId = data[NotificationKeys.ACTIVITY_ID]?.toLongOrNull(),
        referenceId = data[NotificationKeys.REFERENCE_ID]?.toLongOrNull()
    )
}