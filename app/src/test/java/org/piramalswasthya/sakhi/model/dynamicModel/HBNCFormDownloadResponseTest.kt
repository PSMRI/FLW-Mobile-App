package org.piramalswasthya.sakhi.model.dynamicModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HBNCFormDownloadResponseTest {

    private fun response() = HBNCFormDownloadResponse(
        id = 1,
        beneficiaryId = 2,
        visitDate = "2023-01-01",
        fields = mapOf("weight" to 3.5, "note" to "ok", "flag" to null)
    )

    @Test fun `HBNCFormDownloadResponse exposes constructor values`() {
        val res = response()
        assertEquals(1, res.id)
        assertEquals(2, res.beneficiaryId)
        assertEquals("2023-01-01", res.visitDate)
        assertEquals(3.5, res.fields["weight"])
        assertEquals("ok", res.fields["note"])
        assertEquals(null, res.fields["flag"])
    }

    @Test fun `HBNCFormDownloadResponse tolerates an empty fields map`() {
        val res = response().copy(fields = emptyMap())
        assertTrue(res.fields.isEmpty())
    }

    @Test fun `HBNCFormDownloadResponse equals and copy`() {
        val a = response()
        val b = response()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("HBNCFormDownloadResponse"))
        assertNotEquals(a, a.copy(visitDate = "2023-02-02"))
    }
}
