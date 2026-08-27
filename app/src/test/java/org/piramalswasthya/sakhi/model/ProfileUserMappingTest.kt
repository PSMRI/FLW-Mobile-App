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

    @Test fun `UserNetwork asCacheModel passes through location lists`() {
        val state = LocationEntity(id = 2, name = "State")
        val district = LocationEntity(id = 3, name = "District")
        val block = LocationEntity(id = 4, name = "Block")
        val village = LocationEntity(id = 5, name = "Village")
        val cache = UserNetwork(
            userId = 11,
            userName = "u",
            password = "p",
            states = mutableListOf(state),
            districts = mutableListOf(district),
            blocks = mutableListOf(block),
            villages = mutableListOf(village)
        ).asCacheModel()
        assertEquals(listOf(state), cache.states)
        assertEquals(listOf(district), cache.districts)
        assertEquals(listOf(block), cache.blocks)
        assertEquals(listOf(village), cache.villages)
    }

    @Test fun `UserNetwork copy and equality`() {
        val a = UserNetwork(
            userId = 11,
            userName = "u",
            password = "p",
            serviceMapId = 1,
            serviceId = 2,
            servicePointId = 3,
            parkingPlaceId = 4,
            zoneId = 5,
            vanId = 6,
            parkingPlaceName = "pp",
            servicePointName = "sp",
            zoneName = "zn",
            country = LocationEntity(id = 1, name = "India"),
            states = mutableListOf(LocationEntity(id = 2, name = "State")),
            districts = mutableListOf(LocationEntity(id = 3, name = "District")),
            blocks = mutableListOf(LocationEntity(id = 4, name = "Block")),
            villages = mutableListOf(LocationEntity(id = 5, name = "Village")),
            emergencyContactNo = "12345",
            userType = "CHO",
            loggedIn = true
        )
        val b = a.copy(userName = "u2")
        assertEquals("u2", b.userName)
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertTrue(a.toString().contains("UserNetwork"))

        assertFalse(a == a.copy(userId = 999))
        assertFalse(a == a.copy(userName = "other"))
        assertFalse(a == a.copy(password = "other"))
        assertFalse(a == a.copy(serviceMapId = 999))
        assertFalse(a == a.copy(serviceId = 999))
        assertFalse(a == a.copy(servicePointId = 999))
        assertFalse(a == a.copy(parkingPlaceId = 999))
        assertFalse(a == a.copy(zoneId = 999))
        assertFalse(a == a.copy(vanId = 999))
        assertFalse(a == a.copy(parkingPlaceName = "other"))
        assertFalse(a == a.copy(servicePointName = "other"))
        assertFalse(a == a.copy(zoneName = "other"))
        assertFalse(a == a.copy(country = LocationEntity(id = 99, name = "Other")))
        assertFalse(a == a.copy(states = mutableListOf()))
        assertFalse(a == a.copy(districts = mutableListOf()))
        assertFalse(a == a.copy(blocks = mutableListOf()))
        assertFalse(a == a.copy(villages = mutableListOf()))
        assertFalse(a == a.copy(emergencyContactNo = "other"))
        assertFalse(a == a.copy(userType = "other"))
        assertFalse(a == a.copy(loggedIn = false))
    }

    @Test fun `UserNetwork getters and setters read back assigned values`() {
        val u = UserNetwork(userId = 11, userName = "u", password = "p")
        assertEquals(11, u.userId)
        assertEquals("p", u.password)

        u.serviceMapId = 101
        assertEquals(101, u.serviceMapId)
        u.serviceId = 102
        assertEquals(102, u.serviceId)
        u.servicePointId = 103
        assertEquals(103, u.servicePointId)
        u.parkingPlaceId = 104
        assertEquals(104, u.parkingPlaceId)
        u.zoneId = 105
        assertEquals(105, u.zoneId)
        u.vanId = 106
        assertEquals(106, u.vanId)
        u.parkingPlaceName = "ppn"
        assertEquals("ppn", u.parkingPlaceName)
        u.servicePointName = "spn"
        assertEquals("spn", u.servicePointName)
        u.zoneName = "zn"
        assertEquals("zn", u.zoneName)
        val loc = LocationEntity(id = 7, name = "Country")
        u.country = loc
        assertEquals(loc, u.country)
        val states = mutableListOf(LocationEntity(id = 8, name = "State"))
        u.states = states
        assertEquals(states, u.states)
        val districts = mutableListOf(LocationEntity(id = 9, name = "District"))
        u.districts = districts
        assertEquals(districts, u.districts)
        val blocks = mutableListOf(LocationEntity(id = 10, name = "Block"))
        u.blocks = blocks
        assertEquals(blocks, u.blocks)
        val villages = mutableListOf(LocationEntity(id = 11, name = "Village"))
        u.villages = villages
        assertEquals(villages, u.villages)
        u.emergencyContactNo = "999"
        assertEquals("999", u.emergencyContactNo)
        u.userType = "ANM"
        assertEquals("ANM", u.userType)
        u.loggedIn = true
        assertTrue(u.loggedIn)
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
        val cache = ProfileCache(activity = activity)
        assertSame(activity, cache.activity)
        val domain = cache.asDomainModel()
        assertSame(activity, domain.activity)
        assertEquals(77L, domain.activity.id)
        assertEquals("P", domain.activity.name)
    }
}
