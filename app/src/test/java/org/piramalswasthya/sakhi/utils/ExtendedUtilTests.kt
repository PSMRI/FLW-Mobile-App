package org.piramalswasthya.sakhi.utils

import org.junit.Assert.*
import org.junit.Test

class ExtendedUtilTests {

    // =====================================================
    // RoleConstants Tests (extended)
    // =====================================================

    @Test fun `RoleConstants ROLE_ASHA exists`() { assertNotNull(RoleConstants.ROLE_ASHA) }
    @Test fun `RoleConstants ROLE_ASHA_SUPERVISOR exists`() { assertNotNull(RoleConstants.ROLE_ASHA_SUPERVISOR) }
    @Test fun `RoleConstants ROLE_PROVIDER_ADMIN exists`() { assertNotNull(RoleConstants.ROLE_PROVIDER_ADMIN) }
    @Test fun `RoleConstants ROLE_ASHA is Asha`() { assertEquals("Asha", RoleConstants.ROLE_ASHA) }
    @Test fun `RoleConstants ROLE_ASHA_SUPERVISOR is ASHA Supervisor`() { assertEquals("ASHA Supervisor", RoleConstants.ROLE_ASHA_SUPERVISOR) }
    @Test fun `RoleConstants ROLE_PROVIDER_ADMIN is ProviderAdmin`() { assertEquals("ProviderAdmin", RoleConstants.ROLE_PROVIDER_ADMIN) }
    @Test fun `RoleConstants all roles are not blank`() {
        assertTrue(RoleConstants.ROLE_ASHA.isNotBlank())
        assertTrue(RoleConstants.ROLE_ASHA_SUPERVISOR.isNotBlank())
        assertTrue(RoleConstants.ROLE_PROVIDER_ADMIN.isNotBlank())
    }
    @Test fun `RoleConstants all roles are distinct`() {
        val roles = listOf(RoleConstants.ROLE_ASHA, RoleConstants.ROLE_ASHA_SUPERVISOR, RoleConstants.ROLE_PROVIDER_ADMIN)
        assertEquals(roles.size, roles.distinct().size)
    }

    // =====================================================
    // CampaignDateUtil Tests (extended)
    // =====================================================

    @Test fun `CampaignDateUtil exists`() {
        assertNotNull(CampaignDateUtil)
    }

    // =====================================================
    // StringMappingUtil Tests (extended)
    // =====================================================

    @Test fun `StringMappingUtil exists`() {
        assertNotNull(StringMappingUtil)
    }

    // =====================================================
    // HelperUtil Tests (extended)
    // =====================================================

    @Test fun `HelperUtil exists`() {
        assertNotNull(HelperUtil)
    }
}
