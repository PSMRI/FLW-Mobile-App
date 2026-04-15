package org.piramalswasthya.sakhi.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleConstantsTest {

    // =====================================================
    // Role Value Tests
    // =====================================================

    @Test
    fun `asha role value is correct`() {
        assertEquals("Asha", RoleConstants.ROLE_ASHA)
    }

    @Test
    fun `asha supervisor role value is correct`() {
        assertEquals("ASHA Supervisor", RoleConstants.ROLE_ASHA_SUPERVISOR)
    }

    @Test
    fun `provider admin role value is correct`() {
        assertEquals("ProviderAdmin", RoleConstants.ROLE_PROVIDER_ADMIN)
    }

    @Test
    fun `all roles are non empty`() {
        assertTrue(RoleConstants.ROLE_ASHA.isNotEmpty())
        assertTrue(RoleConstants.ROLE_ASHA_SUPERVISOR.isNotEmpty())
        assertTrue(RoleConstants.ROLE_PROVIDER_ADMIN.isNotEmpty())
    }

    @Test
    fun `all roles are distinct`() {
        val roles = listOf(
            RoleConstants.ROLE_ASHA,
            RoleConstants.ROLE_ASHA_SUPERVISOR,
            RoleConstants.ROLE_PROVIDER_ADMIN
        )
        assertEquals(roles.size, roles.distinct().size)
    }
}
