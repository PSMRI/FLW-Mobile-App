package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationModelsTest {

    private fun fcmData(
        id: String? = "1001",
        type: String? = "INCENTIVE_CLAIMED",
        navId: String? = "INCENTIVE_APPROVAL",
        priority: String? = "HIGH",
        sender: String? = "4259",
        receiver: String? = "140",
        beneficiary: String? = "98765",
        activity: String? = "140",
        reference: String? = "78954"
    ): Map<String, String> = buildMap {
        id?.let { put(NotificationKeys.NOTIFICATION_ID, it) }
        type?.let { put(NotificationKeys.NOTIFICATION_TYPE, it) }
        navId?.let { put(NotificationKeys.NAV_ID, it) }
        priority?.let { put(NotificationKeys.PRIORITY, it) }
        sender?.let { put(NotificationKeys.SENDER_USER_ID, it) }
        receiver?.let { put(NotificationKeys.RECEIVER_USER_ID, it) }
        beneficiary?.let { put(NotificationKeys.BENEFICIARY_ID, it) }
        activity?.let { put(NotificationKeys.ACTIVITY_ID, it) }
        reference?.let { put(NotificationKeys.REFERENCE_ID, it) }
    }

    private fun entity() = NotificationEntity(
        notificationId = 1001L,
        userId = 140L,
        role = "ASHA",
        eventType = "INCENTIVE_CLAIMED",
        navId = "INCENTIVE_APPROVAL",
        title = "Claim received",
        body = "Saurav claimed for June",
        priority = "HIGH",
        createdTs = 1_700_000_000_000L,
        read = true,
        cleared = false,
        viewed = true,
        senderUserId = 4259L,
        receiverUserId = 140L,
        beneficiaryId = 98765L,
        activityId = 140L,
        referenceId = 78954L,
        appType = "FLW",
        redirect = "INCENTIVE",
        readDate = "2026-01-01T10:00:00.000+05:30"
    )

    private fun dto(
        id: Long = 1001L,
        createdDate: String? = "2026-01-02T10:15:30.000+05:30",
        read: Boolean = true
    ) = NotificationListItemDto(
        id = id,
        appType = "FLW",
        senderUserId = 4259L,
        receiverUserId = 140L,
        title = "Claim received",
        body = "Saurav claimed for June",
        notificationType = "INCENTIVE_CLAIMED",
        navId = "INCENTIVE_APPROVAL",
        redirect = "INCENTIVE",
        priority = "HIGH",
        createdDate = createdDate,
        readDate = "2026-01-03T10:15:30.000+05:30",
        read = read
    )

    @Test
    fun notificationKeys_matchBackendContract() {
        assertEquals("notification_id", NotificationKeys.NOTIFICATION_ID)
        assertEquals("notification_type", NotificationKeys.NOTIFICATION_TYPE)
        assertEquals("nav_id", NotificationKeys.NAV_ID)
        assertEquals("sender_user_id", NotificationKeys.SENDER_USER_ID)
        assertEquals("receiver_user_id", NotificationKeys.RECEIVER_USER_ID)
        assertEquals("beneficiary_id", NotificationKeys.BENEFICIARY_ID)
        assertEquals("activity_id", NotificationKeys.ACTIVITY_ID)
        assertEquals("reference_id", NotificationKeys.REFERENCE_ID)
        assertEquals("priority", NotificationKeys.PRIORITY)
    }

    @Test
    fun notificationFromFcm_mapsEveryField() {
        val domain = notificationFromFcm(fcmData(), "Title", "Body", 999L)!!
        assertEquals(1001L, domain.notificationId)
        assertEquals("INCENTIVE_CLAIMED", domain.eventType)
        assertEquals("Title", domain.title)
        assertEquals("Body", domain.body)
        assertEquals(999L, domain.createdTs)
        assertFalse(domain.read)
        assertEquals("INCENTIVE_APPROVAL", domain.navId)
        assertEquals("HIGH", domain.priority)
        assertEquals(4259L, domain.senderUserId)
        assertEquals(140L, domain.receiverUserId)
        assertEquals(98765L, domain.beneficiaryId)
        assertEquals(140L, domain.activityId)
        assertEquals(78954L, domain.referenceId)
    }

    @Test
    fun notificationFromFcm_returnsNull_whenIdMissing() {
        assertNull(notificationFromFcm(fcmData(id = null), "t", "b", 1L))
    }

    @Test
    fun notificationFromFcm_returnsNull_whenIdNotNumeric() {
        assertNull(notificationFromFcm(fcmData(id = "abc"), "t", "b", 1L))
    }

    @Test
    fun notificationFromFcm_fallsBackToEmptyStrings_whenTitleAndBodyNull() {
        val domain = notificationFromFcm(fcmData(), null, null, 1L)!!
        assertEquals("", domain.title)
        assertEquals("", domain.body)
    }

    @Test
    fun notificationFromFcm_leavesOptionalIdsNull_whenAbsent() {
        val domain = notificationFromFcm(
            mapOf(NotificationKeys.NOTIFICATION_ID to "7"),
            "t",
            "b",
            1L
        )!!
        assertEquals("", domain.eventType)
        assertNull(domain.navId)
        assertNull(domain.priority)
        assertNull(domain.senderUserId)
        assertNull(domain.receiverUserId)
        assertNull(domain.beneficiaryId)
        assertNull(domain.activityId)
        assertNull(domain.referenceId)
    }

    @Test
    fun notificationFromFcm_leavesIdsNull_whenNotNumeric() {
        val domain = notificationFromFcm(
            fcmData(sender = "x", receiver = "y", beneficiary = "z", activity = "w", reference = "q"),
            "t",
            "b",
            1L
        )!!
        assertNull(domain.senderUserId)
        assertNull(domain.receiverUserId)
        assertNull(domain.beneficiaryId)
        assertNull(domain.activityId)
        assertNull(domain.referenceId)
    }

    @Test
    fun notificationEntityFromFcm_mapsEveryField() {
        val e = notificationEntityFromFcm(fcmData(), "Title", "Body", 140L, 999L)!!
        assertEquals(1001L, e.notificationId)
        assertEquals(140L, e.userId)
        assertEquals("INCENTIVE_CLAIMED", e.eventType)
        assertEquals("INCENTIVE_APPROVAL", e.navId)
        assertEquals("Title", e.title)
        assertEquals("Body", e.body)
        assertEquals("HIGH", e.priority)
        assertEquals(999L, e.createdTs)
        assertEquals(4259L, e.senderUserId)
        assertEquals(140L, e.receiverUserId)
        assertEquals(98765L, e.beneficiaryId)
        assertEquals(140L, e.activityId)
        assertEquals(78954L, e.referenceId)
        assertFalse(e.read)
        assertFalse(e.cleared)
        assertFalse(e.viewed)
    }

    @Test
    fun notificationEntityFromFcm_returnsNull_whenIdUnparseable() {
        assertNull(notificationEntityFromFcm(fcmData(id = "nope"), "t", "b", 1L, 1L))
        assertNull(notificationEntityFromFcm(fcmData(id = null), "t", "b", 1L, 1L))
    }

    @Test
    fun entityToDomain_carriesRenderingAndRoutingFields() {
        val d = entity().toDomain()
        assertEquals(1001L, d.notificationId)
        assertEquals("INCENTIVE_CLAIMED", d.eventType)
        assertEquals("Claim received", d.title)
        assertEquals("Saurav claimed for June", d.body)
        assertEquals(1_700_000_000_000L, d.createdTs)
        assertTrue(d.read)
        assertEquals("INCENTIVE_APPROVAL", d.navId)
        assertEquals("HIGH", d.priority)
        assertEquals(4259L, d.senderUserId)
        assertEquals(140L, d.receiverUserId)
        assertEquals(98765L, d.beneficiaryId)
        assertEquals(140L, d.activityId)
        assertEquals(78954L, d.referenceId)
    }

    @Test
    fun dtoToEntity_usesParsedCreatedDate() {
        val e = dto().toEntity(userId = 140L, fallbackTs = 5L)
        assertEquals(1001L, e.notificationId)
        assertEquals(140L, e.userId)
        assertEquals("INCENTIVE_CLAIMED", e.eventType)
        assertEquals("Claim received", e.title)
        assertEquals("Saurav claimed for June", e.body)
        assertEquals("FLW", e.appType)
        assertEquals("INCENTIVE", e.redirect)
        assertTrue(e.read)
        assertNotEquals(5L, e.createdTs)
        assertTrue(e.createdTs > 0L)
    }

    @Test
    fun dtoToEntity_fallsBackToGivenTimestamp_whenCreatedDateNull() {
        assertEquals(5L, dto(createdDate = null).toEntity(userId = 1L, fallbackTs = 5L).createdTs)
    }

    @Test
    fun dtoToEntity_fallsBackToGivenTimestamp_whenCreatedDateBlank() {
        assertEquals(7L, dto(createdDate = "   ").toEntity(userId = 1L, fallbackTs = 7L).createdTs)
    }

    @Test
    fun dtoToEntity_fallsBackToGivenTimestamp_whenCreatedDateUnparseable() {
        assertEquals(9L, dto(createdDate = "not-a-date").toEntity(userId = 1L, fallbackTs = 9L).createdTs)
    }

    @Test
    fun dtoToEntity_usesEmptyStrings_whenTextFieldsNull() {
        val bare = NotificationListItemDto(id = 3L)
        val e = bare.toEntity(userId = 1L, fallbackTs = 2L)
        assertEquals("", e.eventType)
        assertEquals("", e.title)
        assertEquals("", e.body)
        assertNull(e.navId)
        assertNull(e.priority)
        assertFalse(e.read)
    }

    @Test
    fun responseToEntities_mapsEveryNotification() {
        val response = NotificationListResponse(
            data = NotificationListData(
                notifications = listOf(dto(id = 1L), dto(id = 2L)),
                unreadCount = 1,
                totalCount = 2,
                page = 0,
                size = 20
            ),
            statusCode = 200,
            status = "Success"
        )
        val entities = response.toEntities(userId = 140L, createdTs = 5L)
        assertEquals(2, entities.size)
        assertEquals(listOf(1L, 2L), entities.map { it.notificationId })
        assertTrue(entities.all { it.userId == 140L })
    }

    @Test
    fun responseToEntities_isEmpty_whenDataMissing() {
        assertTrue(NotificationListResponse().toEntities(1L, 1L).isEmpty())
    }

    @Test
    fun responseToEntities_isEmpty_whenNotificationListNull() {
        val response = NotificationListResponse(data = NotificationListData())
        assertTrue(response.toEntities(1L, 1L).isEmpty())
    }

    @Test
    fun eventType_resolvesKnownServerKeys() {
        assertEquals(
            NotificationEventType.INCENTIVE_CLAIMED,
            NotificationEventType.fromKey("INCENTIVE_CLAIMED")
        )
        assertEquals(
            NotificationEventType.ASHA_CLAIM_REJECTED,
            NotificationEventType.fromKey("asha_claim_rejected")
        )
        assertEquals(
            NotificationEventType.CHO_VERIFICATION_REMINDER,
            NotificationEventType.fromKey("CHO_VERIFICATION_REMINDER")
        )
    }

    @Test
    fun eventType_fallsBackToGeneric_whenKeyUnknownOrNull() {
        assertEquals(NotificationEventType.GENERIC, NotificationEventType.fromKey("WHATEVER"))
        assertEquals(NotificationEventType.GENERIC, NotificationEventType.fromKey(null))
    }

    @Test
    fun eventType_everyEntryHasAnIcon() {
        NotificationEventType.values().forEach { assertNotEquals(0, it.iconRes) }
    }

    @Test
    fun navTarget_resolvesKnownNavIds() {
        assertEquals(
            NotificationNavTarget.INCENTIVE_APPROVAL,
            NotificationNavTarget.fromNavId("INCENTIVE_APPROVAL")
        )
        assertEquals(
            NotificationNavTarget.INCENTIVE_APPROVAL,
            NotificationNavTarget.fromNavId("incentive_approval")
        )
    }

    @Test
    fun navTarget_fallsBackToNone_whenNavIdUnknownOrNull() {
        assertEquals(NotificationNavTarget.NONE, NotificationNavTarget.fromNavId("OTHER"))
        assertEquals(NotificationNavTarget.NONE, NotificationNavTarget.fromNavId(null))
        assertEquals("", NotificationNavTarget.NONE.navId)
    }

    @Test
    fun listRequest_exposesIncrementalSyncMarkers() {
        val request = NotificationListRequest(userId = 140L, role = "ASHA", sinceId = 9L, sinceTs = 8L)
        assertEquals(140L, request.userId)
        assertEquals("ASHA", request.role)
        assertEquals(9L, request.sinceId)
        assertEquals(8L, request.sinceTs)
        val bare = NotificationListRequest(userId = 1L)
        assertNull(bare.role)
        assertNull(bare.sinceId)
        assertNull(bare.sinceTs)
    }

    @Test
    fun idsAndUserRequests_areValueTypes() {
        val ids = NotificationIdsRequest(userId = 1L, notificationIds = listOf(1L, 2L))
        assertEquals(ids, ids.copy())
        assertEquals(ids.hashCode(), ids.copy().hashCode())
        assertEquals(listOf(1L, 2L), ids.notificationIds)
        assertEquals(1L, ids.userId)
        assertTrue(ids.toString().contains("NotificationIdsRequest"))
        val user = NotificationUserRequest(userId = 7L)
        assertEquals(user, NotificationUserRequest(userId = 7L))
        assertEquals(7L, user.userId)
        assertNotEquals(user, NotificationUserRequest(userId = 8L))
        assertFalse(user.equals(null))
    }

    @Test
    fun entity_isAValueTypeWithDefaults() {
        val e = entity()
        assertEquals(e, e.copy())
        assertEquals(e.hashCode(), e.copy().hashCode())
        assertNotEquals(e, e.copy(notificationId = 2L))
        assertFalse(e.equals(Any()))
        assertTrue(e.toString().contains("NotificationEntity"))
        assertEquals(1001L, e.component1())
        val bare = NotificationEntity(
            notificationId = 1L,
            userId = 2L,
            eventType = "T",
            title = "t",
            body = "b",
            createdTs = 3L
        )
        assertNull(bare.role)
        assertNull(bare.navId)
        assertNull(bare.priority)
        assertFalse(bare.read)
        assertFalse(bare.cleared)
        assertFalse(bare.viewed)
        assertNull(bare.appType)
        assertNull(bare.redirect)
        assertNull(bare.readDate)
    }

    @Test
    fun domain_isAValueTypeWithDefaults() {
        val d = entity().toDomain()
        assertEquals(d, d.copy())
        assertEquals(d.hashCode(), d.copy().hashCode())
        assertNotEquals(d, d.copy(title = "other"))
        assertFalse(d.equals(null))
        assertTrue(d.toString().contains("NotificationDomain"))
        val bare = NotificationDomain(
            notificationId = 1L,
            eventType = "T",
            title = "t",
            body = "b",
            createdTs = 2L,
            read = false
        )
        assertNull(bare.navId)
        assertNull(bare.priority)
        assertNull(bare.senderUserId)
        assertNull(bare.referenceId)
    }

    @Test
    fun listItemDto_isAValueTypeWithDefaults() {
        val d = dto()
        assertEquals(d, d.copy())
        assertEquals(d.hashCode(), d.copy().hashCode())
        assertNotEquals(d, d.copy(id = 2L))
        assertFalse(d.equals(Any()))
        assertTrue(d.toString().contains("NotificationListItemDto"))
        val bare = NotificationListItemDto(id = 1L)
        assertNull(bare.appType)
        assertNull(bare.senderUserId)
        assertNull(bare.receiverUserId)
        assertNull(bare.title)
        assertNull(bare.body)
        assertNull(bare.notificationType)
        assertNull(bare.navId)
        assertNull(bare.redirect)
        assertNull(bare.priority)
        assertNull(bare.createdDate)
        assertNull(bare.readDate)
        assertFalse(bare.read)
    }

    @Test
    fun listDataAndResponse_areValueTypesWithDefaults() {
        val data = NotificationListData(
            notifications = listOf(dto()),
            unreadCount = 1,
            totalCount = 2,
            page = 0,
            size = 20
        )
        assertEquals(data, data.copy())
        assertEquals(data.hashCode(), data.copy().hashCode())
        assertNotEquals(data, data.copy(page = 1))
        assertTrue(data.toString().contains("NotificationListData"))
        val bareData = NotificationListData()
        assertNull(bareData.notifications)
        assertNull(bareData.unreadCount)
        assertNull(bareData.totalCount)
        assertNull(bareData.page)
        assertNull(bareData.size)
        val response = NotificationListResponse(data = data, statusCode = 200, status = "OK")
        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertNotEquals(response, response.copy(statusCode = 500))
        assertFalse(response.equals(null))
        assertTrue(response.toString().contains("NotificationListResponse"))
        val bareResponse = NotificationListResponse()
        assertNull(bareResponse.data)
        assertNull(bareResponse.statusCode)
        assertNull(bareResponse.status)
    }
}
