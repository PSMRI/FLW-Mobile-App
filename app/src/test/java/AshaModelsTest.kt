import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AshaModelsTest {

    private fun network() = AshaNetwork(
        userId = 4259,
        usrMappingId = 11,
        name = "Saurav Mishra",
        userName = "saurav",
        serviceId = 2,
        serviceName = "FLW",
        stateId = 3,
        stateName = "Chhattisgarh",
        workingDistrictId = 4,
        workingDistrictName = "Raipur",
        workingLocationId = 5,
        serviceProviderId = 6,
        locationName = "Village A",
        workingLocationAddress = "Addr 1",
        roleId = 7,
        roleName = "ASHA",
        providerServiceMapId = 8,
        agentId = "agent-1",
        psmStatusId = 9,
        psmStatus = "ACTIVE",
        userServciceRoleDeleted = false,
        userDeleted = false,
        serviceProviderDeleted = false,
        roleDeleted = false,
        providerServiceMappingDeleted = false,
        blockid = 10,
        blockname = "Block B",
        villageid = "V12",
        villagename = "Village A",
        national = true,
        inbound = "in",
        outbound = "out"
    )

    private fun cache() = AshaCache(
        userId = 4259,
        usrMappingId = 11,
        name = "Saurav Mishra",
        userName = "saurav",
        serviceId = 2,
        serviceName = "FLW",
        stateId = 3,
        stateName = "Chhattisgarh",
        workingDistrictId = 4,
        workingDistrictName = "Raipur",
        workingLocationId = 5,
        serviceProviderId = 6,
        locationName = "Village A",
        workingLocationAddress = "Addr 1",
        roleId = 7,
        roleName = "ASHA",
        providerServiceMapId = 8,
        blockid = 10,
        blockname = "Block B",
        villageid = "V12",
        villagename = "Village A"
    )

    @Test
    fun ashaNetwork_exposesEveryField() {
        val n = network()
        assertEquals(4259, n.userId)
        assertEquals(11, n.usrMappingId)
        assertEquals("Saurav Mishra", n.name)
        assertEquals("saurav", n.userName)
        assertEquals(2, n.serviceId)
        assertEquals("FLW", n.serviceName)
        assertEquals(3, n.stateId)
        assertEquals("Chhattisgarh", n.stateName)
        assertEquals(4, n.workingDistrictId)
        assertEquals("Raipur", n.workingDistrictName)
        assertEquals(5, n.workingLocationId)
        assertEquals(6, n.serviceProviderId)
        assertEquals("Village A", n.locationName)
        assertEquals("Addr 1", n.workingLocationAddress)
        assertEquals(7, n.roleId)
        assertEquals("ASHA", n.roleName)
        assertEquals(8, n.providerServiceMapId)
        assertEquals("agent-1", n.agentId)
        assertEquals(9, n.psmStatusId)
        assertEquals("ACTIVE", n.psmStatus)
        assertFalse(n.userServciceRoleDeleted)
        assertFalse(n.userDeleted)
        assertFalse(n.serviceProviderDeleted)
        assertFalse(n.roleDeleted)
        assertFalse(n.providerServiceMappingDeleted)
        assertEquals(10, n.blockid)
        assertEquals("Block B", n.blockname)
        assertEquals("V12", n.villageid)
        assertEquals("Village A", n.villagename)
        assertTrue(n.national)
        assertEquals("in", n.inbound)
        assertEquals("out", n.outbound)
    }

    @Test
    fun ashaNetwork_appliesDefaultsForOmittedFields() {
        val n = AshaNetwork(
            agentId = null,
            userServciceRoleDeleted = false,
            userDeleted = false,
            serviceProviderDeleted = false,
            roleDeleted = false,
            providerServiceMappingDeleted = false,
            national = false,
            inbound = null,
            outbound = null
        )
        assertEquals(0, n.userId)
        assertEquals(0, n.usrMappingId)
        assertEquals("", n.name)
        assertEquals("", n.userName)
        assertEquals(0, n.serviceId)
        assertEquals("", n.serviceName)
        assertEquals(0, n.stateId)
        assertEquals("", n.stateName)
        assertEquals(0, n.workingDistrictId)
        assertEquals("", n.workingDistrictName)
        assertEquals(0, n.workingLocationId)
        assertEquals(0, n.serviceProviderId)
        assertEquals("", n.locationName)
        assertEquals("", n.workingLocationAddress)
        assertEquals(0, n.roleId)
        assertEquals("", n.roleName)
        assertEquals(0, n.providerServiceMapId)
        assertEquals(0, n.psmStatusId)
        assertEquals("", n.psmStatus)
        assertEquals(0, n.blockid)
        assertEquals("", n.blockname)
        assertEquals("", n.villageid)
        assertEquals("", n.villagename)
        assertNull(n.agentId)
        assertNull(n.inbound)
        assertNull(n.outbound)
    }

    @Test
    fun ashaNetwork_fieldsAreMutable() {
        val n = network()
        n.userId = 1
        n.usrMappingId = 2
        n.name = "N"
        n.userName = "U"
        n.serviceId = 3
        n.serviceName = "S"
        n.stateId = 4
        n.stateName = "ST"
        n.workingDistrictId = null
        n.workingDistrictName = null
        n.workingLocationId = null
        n.serviceProviderId = 5
        n.locationName = null
        n.workingLocationAddress = null
        n.roleId = 6
        n.roleName = "R"
        n.providerServiceMapId = 7
        n.agentId = null
        n.psmStatusId = 8
        n.psmStatus = "P"
        n.userServciceRoleDeleted = true
        n.userDeleted = true
        n.serviceProviderDeleted = true
        n.roleDeleted = true
        n.providerServiceMappingDeleted = true
        n.blockid = 9
        n.blockname = "B"
        n.villageid = "V"
        n.villagename = "VN"
        n.national = false
        n.inbound = 1
        n.outbound = 2

        assertEquals(1, n.userId)
        assertEquals("N", n.name)
        assertNull(n.workingDistrictId)
        assertNull(n.locationName)
        assertTrue(n.userDeleted)
        assertTrue(n.roleDeleted)
        assertTrue(n.providerServiceMappingDeleted)
        assertFalse(n.national)
        assertEquals(1, n.inbound)
        assertEquals(2, n.outbound)
        assertEquals("VN", n.villagename)
    }

    @Test
    fun asCacheModel_copiesEveryMappedField() {
        val c = network().asCacheModel()
        assertEquals(0, c.id)
        assertEquals(4259, c.userId)
        assertEquals(11, c.usrMappingId)
        assertEquals("Saurav Mishra", c.name)
        assertEquals("saurav", c.userName)
        assertEquals(2, c.serviceId)
        assertEquals("FLW", c.serviceName)
        assertEquals(3, c.stateId)
        assertEquals("Chhattisgarh", c.stateName)
        assertEquals(4, c.workingDistrictId)
        assertEquals("Raipur", c.workingDistrictName)
        assertEquals(5, c.workingLocationId)
        assertEquals(6, c.serviceProviderId)
        assertEquals("Village A", c.locationName)
        assertEquals("Addr 1", c.workingLocationAddress)
        assertEquals(7, c.roleId)
        assertEquals("ASHA", c.roleName)
        assertEquals(8, c.providerServiceMapId)
        assertEquals(10, c.blockid)
        assertEquals("Block B", c.blockname)
        assertEquals("V12", c.villageid)
        assertEquals("Village A", c.villagename)
    }

    @Test
    fun asCacheModel_dropsFieldsWithNoCacheColumn() {
        val c = network().asCacheModel()
        assertEquals(cache(), c)
    }

    @Test
    fun asCacheModel_preservesNullableNulls() {
        val c = network().copy(
            workingDistrictId = null,
            workingDistrictName = null,
            workingLocationId = null,
            locationName = null,
            workingLocationAddress = null
        ).asCacheModel()
        assertNull(c.workingDistrictId)
        assertNull(c.workingDistrictName)
        assertNull(c.workingLocationId)
        assertNull(c.locationName)
        assertNull(c.workingLocationAddress)
    }

    @Test
    fun ashaNetwork_isAValueType() {
        val n = network()
        assertEquals(n, n.copy())
        assertEquals(n.hashCode(), n.copy().hashCode())
        assertNotEquals(n, n.copy(userId = 1))
        assertFalse(n.equals(null))
        assertFalse(n.equals(Any()))
        assertTrue(n.toString().contains("AshaNetwork"))
        assertEquals(4259, n.component1())
    }

    @Test
    fun ashaCache_exposesEveryFieldAndDefaultsItsId() {
        val c = cache()
        assertEquals(0, c.id)
        assertEquals(4259, c.userId)
        assertEquals("Saurav Mishra", c.name)
        assertEquals("V12", c.villageid)
        assertEquals(1234, cache().copy(id = 1234).id)
    }

    @Test
    fun ashaCache_fieldsAreMutable() {
        val c = cache()
        c.userId = 1
        c.usrMappingId = 2
        c.name = "N"
        c.userName = "U"
        c.serviceId = 3
        c.serviceName = "S"
        c.stateId = 4
        c.stateName = "ST"
        c.workingDistrictId = null
        c.workingDistrictName = null
        c.workingLocationId = null
        c.serviceProviderId = 5
        c.locationName = null
        c.workingLocationAddress = null
        c.roleId = 6
        c.roleName = "R"
        c.providerServiceMapId = 7
        c.blockid = 8
        c.blockname = "B"
        c.villageid = "V"
        c.villagename = "VN"
        assertEquals(1, c.userId)
        assertEquals("VN", c.villagename)
        assertNull(c.workingLocationId)
        assertEquals(8, c.blockid)
    }

    @Test
    fun ashaCache_isAValueType() {
        val c = cache()
        assertEquals(c, c.copy())
        assertEquals(c.hashCode(), c.copy().hashCode())
        assertNotEquals(c, c.copy(userId = 1))
        assertFalse(c.equals(null))
        assertTrue(c.toString().contains("AshaCache"))
        assertEquals(0, c.component1())
    }

    @Test
    fun ashaListResponse_wrapsTheNetworkPayload() {
        val response = AshaListResponse(data = network(), statusCode = 200, status = "Success")
        assertEquals(network(), response.data)
        assertEquals(200, response.statusCode)
        assertEquals("Success", response.status)
        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertNotEquals(response, response.copy(statusCode = 500))
        assertFalse(response.equals(Any()))
        assertTrue(response.toString().contains("AshaListResponse"))
        assertEquals(network(), response.component1())
        assertEquals(200, response.component2())
        assertEquals("Success", response.component3())
    }

    private fun ashasData() = Ashas.Data(
        userId = 1,
        usrMappingId = 2,
        name = "N",
        userName = "U",
        serviceId = 3,
        serviceName = "S",
        stateId = 4,
        stateName = "ST",
        workingDistrictId = 5,
        workingDistrictName = "D",
        workingLocationId = 6,
        serviceProviderId = 7,
        locationName = "L",
        workingLocationAddress = "A",
        roleId = 8,
        roleName = "R",
        providerServiceMapId = 9,
        agentId = "ag",
        psmStatusId = 10,
        psmStatus = "P",
        userServciceRoleDeleted = false,
        userDeleted = false,
        serviceProviderDeleted = false,
        roleDeleted = false,
        providerServiceMappingDeleted = false,
        blockid = 11,
        blockname = "B",
        villageid = "V",
        villagename = "VN",
        national = true,
        inbound = "i",
        outbound = "o"
    )

    @Test
    fun ashasData_exposesEveryField() {
        val d = ashasData()
        assertEquals(1, d.userId)
        assertEquals(2, d.usrMappingId)
        assertEquals("N", d.name)
        assertEquals("U", d.userName)
        assertEquals(3, d.serviceId)
        assertEquals("S", d.serviceName)
        assertEquals(4, d.stateId)
        assertEquals("ST", d.stateName)
        assertEquals(5, d.workingDistrictId)
        assertEquals("D", d.workingDistrictName)
        assertEquals(6, d.workingLocationId)
        assertEquals(7, d.serviceProviderId)
        assertEquals("L", d.locationName)
        assertEquals("A", d.workingLocationAddress)
        assertEquals(8, d.roleId)
        assertEquals("R", d.roleName)
        assertEquals(9, d.providerServiceMapId)
        assertEquals("ag", d.agentId)
        assertEquals(10, d.psmStatusId)
        assertEquals("P", d.psmStatus)
        assertFalse(d.userServciceRoleDeleted)
        assertFalse(d.userDeleted)
        assertFalse(d.serviceProviderDeleted)
        assertFalse(d.roleDeleted)
        assertFalse(d.providerServiceMappingDeleted)
        assertEquals(11, d.blockid)
        assertEquals("B", d.blockname)
        assertEquals("V", d.villageid)
        assertEquals("VN", d.villagename)
        assertTrue(d.national)
        assertEquals("i", d.inbound)
        assertEquals("o", d.outbound)
    }

    @Test
    fun ashasData_fieldsAreMutable() {
        val d = ashasData()
        d.userId = 99
        d.usrMappingId = 98
        d.name = "N2"
        d.userName = "U2"
        d.serviceId = 97
        d.serviceName = "S2"
        d.stateId = 96
        d.stateName = "ST2"
        d.workingDistrictId = null
        d.workingDistrictName = null
        d.workingLocationId = null
        d.serviceProviderId = 95
        d.locationName = null
        d.workingLocationAddress = null
        d.roleId = 94
        d.roleName = "R2"
        d.providerServiceMapId = 93
        d.agentId = null
        d.psmStatusId = 92
        d.psmStatus = "P2"
        d.userServciceRoleDeleted = true
        d.userDeleted = true
        d.serviceProviderDeleted = true
        d.roleDeleted = true
        d.providerServiceMappingDeleted = true
        d.blockid = 91
        d.blockname = "B2"
        d.villageid = "V2"
        d.villagename = "VN2"
        d.national = false
        d.inbound = 1
        d.outbound = 2

        assertEquals(99, d.userId)
        assertEquals("N2", d.name)
        assertNull(d.workingDistrictId)
        assertNull(d.agentId)
        assertTrue(d.userDeleted)
        assertFalse(d.national)
        assertEquals("VN2", d.villagename)
    }

    @Test
    fun ashasData_isAValueType() {
        val d = ashasData()
        assertEquals(d, d.copy())
        assertEquals(d.hashCode(), d.copy().hashCode())
        assertNotEquals(d, d.copy(userId = 2))
        assertFalse(d.equals(null))
        assertTrue(d.toString().contains("Data"))
        assertEquals(1, d.component1())
    }

    @Test
    fun ashas_wrapsAListOfData() {
        val ashas = Ashas(success = true, message = "ok", data = listOf(ashasData()))
        assertTrue(ashas.success)
        assertEquals("ok", ashas.message)
        assertEquals(1, ashas.data.size)
        assertEquals(ashasData(), ashas.data.first())
        assertEquals(ashas, ashas.copy())
        assertEquals(ashas.hashCode(), ashas.copy().hashCode())
        assertNotEquals(ashas, ashas.copy(success = false))
        assertFalse(ashas.equals(Any()))
        assertTrue(ashas.toString().contains("Ashas"))
        assertTrue(ashas.component1())
        assertEquals("ok", ashas.component2())
        assertEquals(1, ashas.component3().size)
    }

    @Test
    fun ashas_fieldsAreMutable() {
        val ashas = Ashas(success = true, message = null, data = emptyList())
        assertNull(ashas.message)
        assertTrue(ashas.data.isEmpty())
        ashas.success = false
        ashas.message = 42
        ashas.data = listOf(ashasData())
        assertFalse(ashas.success)
        assertEquals(42, ashas.message)
        assertEquals(1, ashas.data.size)
    }
}
