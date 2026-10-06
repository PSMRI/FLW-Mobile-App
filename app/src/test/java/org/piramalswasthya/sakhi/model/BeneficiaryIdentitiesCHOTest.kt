package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BeneficiaryIdentitiesCHOTest {

    @Test
    fun `constructor uses default values when omitted`() {
        val identity = BeneficiaryIdentitiesCHO(
            identityType = "Aadhar",
            createdBy = "ashaWorker"
        )

        assertNotNull(identity)
        assertEquals(0, identity.govtIdentityNo)
        assertEquals(0, identity.govtIdentityTypeID)
        assertEquals(null, identity.govtIdentityTypeName)
    }
}
