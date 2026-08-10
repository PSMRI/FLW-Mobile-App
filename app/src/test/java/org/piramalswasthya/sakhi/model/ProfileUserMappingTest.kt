package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class ProfileUserMappingTest {

    // =====================================================
    // UwinCache.asDomainModel()
    // =====================================================

    private fun uwinCache() = UwinCache(
        id = 1,
        sessionDate = 100L,
        place = "x",
        participantsCount = 5,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `UwinCache asDomainModel maps scalar fields`() {
        val net = uwinCache().asDomainModel()
        assertEquals(1, net.id)
        assertEquals(100L, net.sessionDate)
        assertEquals("x", net.place)
        assertEquals(5, net.participantsCount)
    }

    @Test fun `UwinCache asDomainModel maps createdBy and updatedBy`() {
        val net = uwinCache().asDomainModel()
        assertEquals("creator", net.createdBy)
        assertEquals("modifier", net.updatedBy)
    }

    @Test fun `UwinCache asDomainModel formats dates as datetime strings`() {
        val net = uwinCache().asDomainModel()
        assertNotNull(net.createdDate)
        assertTrue(net.createdDate.contains("T"))
        assertTrue(net.updatedDate.contains("T"))
    }

    @Test fun `UwinCache asDomainModel passes through uploaded files`() {
        val net = uwinCache().copy(uploadedFiles1 = "f1", uploadedFiles2 = "f2").asDomainModel()
        assertEquals("f1", net.uploadedFiles1)
        assertEquals("f2", net.uploadedFiles2)
    }

    // =====================================================
    // UwinNetwork.asCacheModel()
    // =====================================================

    private fun uwinNetwork() = UwinNetwork(
        id = 2,
        sessionDate = 200L,
        place = "y",
        participantsCount = 9,
        createdBy = "creator",
        createdDate = "2024-01-01",
        updatedBy = "modifier",
        updatedDate = "2024-02-02"
    )

    @Test fun `UwinNetwork asCacheModel maps scalar fields`() {
        val cache = uwinNetwork().asCacheModel()
        assertEquals(2, cache.id)
        assertEquals(200L, cache.sessionDate)
        assertEquals("y", cache.place)
        assertEquals(9, cache.participantsCount)
    }

    @Test fun `UwinNetwork asCacheModel sets processed to P and syncState SYNCED`() {
        val cache = uwinNetwork().asCacheModel()
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `UwinNetwork asCacheModel parses date strings into longs`() {
        val cache = uwinNetwork().asCacheModel()
        assertTrue(cache.createdDate > 0L)
        assertTrue(cache.updatedDate > 0L)
    }

    @Test fun `UwinNetwork asCacheModel maps created and updated by`() {
        val cache = uwinNetwork().asCacheModel()
        assertEquals("creator", cache.createdBy)
        assertEquals("modifier", cache.updatedBy)
    }

    // =====================================================
    // UserCache.asDomainModel()
    // =====================================================

    private fun userCache() = UserCache(
        userId = 7,
        userName = "user",
        password = "pass",
        serviceMapId = 3,
        country = LocationEntity(id = 1, name = "India"),
        states = listOf(LocationEntity(id = 2, name = "State")),
        districts = listOf(LocationEntity(id = 3, name = "District")),
        blocks = listOf(LocationEntity(id = 4, name = "Block")),
        villages = listOf(LocationEntity(id = 5, name = "Village")),
        emergencyContactNo = "9999999999",
        userType = "ASHA",
        loggedIn = true
    )

    @Test fun `UserCache asDomainModel maps identity fields`() {
        val domain = userCache().asDomainModel()
        assertEquals(7, domain.userId)
        assertEquals("user", domain.userName)
        assertEquals("pass", domain.password)
    }

    @Test fun `UserCache asDomainModel maps contactNo from emergencyContactNo`() {
        val domain = userCache().asDomainModel()
        assertEquals("9999999999", domain.contactNo)
        assertEquals("ASHA", domain.userType)
        assertTrue(domain.loggedIn)
    }

    @Test fun `UserCache asDomainModel passes through location lists`() {
        val domain = userCache().asDomainModel()
        assertEquals("India", domain.country.name)
        assertEquals(1, domain.states.size)
        assertEquals("State", domain.states[0].name)
        assertEquals("Village", domain.villages[0].name)
    }

    // =====================================================
    // UserNetwork.asCacheModel()
    // =====================================================

    @Test fun `UserNetwork asCacheModel maps required fields`() {
        val cache = UserNetwork(userId = 11, userName = "u", password = "p").asCacheModel()
        assertEquals(11, cache.userId)
        assertEquals("u", cache.userName)
        assertEquals("p", cache.password)
    }

    @Test fun `UserNetwork asCacheModel defaults country to India when null`() {
        val cache = UserNetwork(userId = 11, userName = "u", password = "p").asCacheModel()
        assertEquals(1, cache.country.id)
        assertEquals("India", cache.country.name)
    }

    @Test fun `UserNetwork asCacheModel defaults null contact and userType to empty`() {
        val cache = UserNetwork(userId = 11, userName = "u", password = "p").asCacheModel()
        assertEquals("", cache.emergencyContactNo)
        assertEquals("", cache.userType)
        assertFalse(cache.loggedIn)
    }

    @Test fun `UserNetwork asCacheModel keeps provided country and contact`() {
        val cache = UserNetwork(
            userId = 11,
            userName = "u",
            password = "p",
            country = LocationEntity(id = 99, name = "Bharat"),
            emergencyContactNo = "12345",
            userType = "CHO",
            loggedIn = true
        ).asCacheModel()
        assertEquals(99, cache.country.id)
        assertEquals("12345", cache.emergencyContactNo)
        assertEquals("CHO", cache.userType)
        assertTrue(cache.loggedIn)
    }

    // =====================================================
    // UserDetailsInResponse.toUser(password)
    // =====================================================

    private fun userDetails() = UserDetailsInResponse(
        userId = 21,
        name = "Full Name",
        userName = "login",
        stateId = 5,
        stateName = "StateX",
        serviceProviderId = 8,
        roleId = 2,
        roleName = "ASHA",
        providerServiceMapId = 44,
        blockId = 6,
        blockName = "BlockY",
        villageId = "1,2",
        villageName = "VillA,VillB"
    )

    @Test fun `toUser places password argument into user`() {
        val user = userDetails().toUser("secret")
        assertEquals("secret", user.password)
    }

    @Test fun `toUser maps identity and role`() {
        val user = userDetails().toUser("secret")
        assertEquals(21, user.userId)
        assertEquals("Full Name", user.name)
        assertEquals("login", user.userName)
        assertEquals("ASHA", user.role)
        assertEquals(44, user.serviceMapId)
    }

    @Test fun `toUser maps state and block location entities`() {
        val user = userDetails().toUser("secret")
        assertEquals(5, user.state.id)
        assertEquals("StateX", user.state.name)
        assertEquals(6, user.block.id)
        assertEquals("BlockY", user.block.name)
    }

    @Test fun `toUser splits villages by comma`() {
        val user = userDetails().toUser("secret")
        assertEquals(2, user.villages.size)
        assertEquals(1, user.villages[0].id)
        assertEquals("VillA", user.villages[0].name)
        assertEquals(2, user.villages[1].id)
        assertEquals("VillB", user.villages[1].name)
    }

    // =====================================================
    // ProfileActivityNetwork.asCacheModel()
    // =====================================================

    @Test fun `ProfileActivityNetwork asCacheModel maps id and name`() {
        val cache = ProfileActivityNetwork(id = 55L, name = "Asha Name").asCacheModel()
        assertEquals(55L, cache.id)
        assertEquals("Asha Name", cache.name)
    }

    @Test fun `ProfileActivityNetwork asCacheModel converts null strings via toString`() {
        val cache = ProfileActivityNetwork(id = 1L, village = "Vill", mobileNumber = "12345").asCacheModel()
        assertEquals("Vill", cache.village)
        assertEquals("12345", cache.mobileNumber)
    }

    @Test fun `ProfileActivityNetwork asCacheModel isFatherOrSpouse false when input null`() {
        val cache = ProfileActivityNetwork(id = 1L).asCacheModel()
        assertFalse(cache.isFatherOrSpouse)
    }

    @Test fun `ProfileActivityNetwork asCacheModel isFatherOrSpouse true when input false`() {
        val cache = ProfileActivityNetwork(id = 1L, isFatherOrSpouse = false).asCacheModel()
        assertTrue(cache.isFatherOrSpouse)
    }

    @Test fun `ProfileActivityNetwork asCacheModel maps supervisor and counts`() {
        val cache = ProfileActivityNetwork(
            id = 1L,
            populationCovered = 250,
            supervisorName = "Sup",
            supervisorMobile = "88888"
        ).asCacheModel()
        assertEquals(250, cache.populationCovered)
        assertEquals("Sup", cache.supervisorName)
        assertEquals("88888", cache.supervisorMobile)
    }

    // =====================================================
    // ProfileCache.asDomainModel()
    // =====================================================

    @Test fun `ProfileCache asDomainModel wraps activity`() {
        val activity = ProfileActivityCache(id = 77L, name = "P")
        val domain = ProfileCache(activity = activity).asDomainModel()
        assertSame(activity, domain.activity)
        assertEquals(77L, domain.activity.id)
        assertEquals("P", domain.activity.name)
    }
}
