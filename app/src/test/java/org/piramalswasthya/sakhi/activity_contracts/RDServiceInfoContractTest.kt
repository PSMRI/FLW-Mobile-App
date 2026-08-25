package org.piramalswasthya.sakhi.activity_contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Base64
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RDServiceInfoContractTest {

    private lateinit var contract: RDServiceInfoContract
    private lateinit var context: Context

    @Before
    fun setUp() {
        contract = RDServiceInfoContract()
        context = mockk(relaxed = true)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setAction(any()) } answers { self as Intent }
        mockkStatic(Base64::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createIntent_setsInfoAction() {
        contract.createIntent(context, Unit)

        verify { anyConstructed<Intent>().action = "in.gov.uidai.rdservice.fp.INFO" }
    }

    @Test
    fun parseResult_returnsBase64EncodedPidData_whenResultOk() {
        every { Base64.encodeToString(any(), Base64.NO_WRAP) } returns "encoded-value"
        val intent = mockk<Intent>(relaxed = true)
        every { intent.getStringExtra("PID_DATA") } returns "raw-pid-data"
        every { intent.getStringExtra("DEVICE_INFO") } returns "device-info"
        every { intent.getStringExtra("RD_SERVICE_INFO") } returns "rd-service-info"

        val result = contract.parseResult(Activity.RESULT_OK, intent)

        assertEquals("encoded-value", result)
    }

    @Test
    fun parseResult_returnsNull_whenResultNotOk() {
        val intent = mockk<Intent>(relaxed = true)

        val result = contract.parseResult(Activity.RESULT_CANCELED, intent)

        assertNull(result)
    }

    @Test
    fun parseResult_returnsNull_whenIntentIsNull() {
        val result = contract.parseResult(Activity.RESULT_OK, null)

        assertNull(result)
    }
}
