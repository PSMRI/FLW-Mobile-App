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

class RDServiceCapturePIDContractTest {

    private lateinit var contract: RDServiceCapturePIDContract
    private lateinit var context: Context

    @Before
    fun setUp() {
        contract = RDServiceCapturePIDContract()
        context = mockk(relaxed = true)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setAction(any()) } answers { self as Intent }
        every { anyConstructed<Intent>().putExtra(any<String>(), any<String>()) } answers { self as Intent }
        mockkStatic(Base64::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createIntent_setsCaptureActionAndPidOptionsExtra() {
        contract.createIntent(context, Unit)

        verify { anyConstructed<Intent>().action = "in.gov.uidai.rdservice.fp.CAPTURE" }
        verify { anyConstructed<Intent>().putExtra("PID_OPTIONS", any<String>()) }
    }

    @Test
    fun parseResult_returnsBase64EncodedPidData_whenResultOk() {
        every { Base64.encodeToString(any(), Base64.NO_WRAP) } returns "encoded-value"
        val intent = mockk<Intent>(relaxed = true)
        every { intent.getStringExtra("PID_DATA") } returns "raw-pid-data"

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

    @Test
    fun parseResult_encodesNullPidData_whenExtraMissing() {
        every { Base64.encodeToString(null, Base64.NO_WRAP) } returns "empty-encoded"
        val intent = mockk<Intent>(relaxed = true)
        every { intent.getStringExtra("PID_DATA") } returns null

        val result = contract.parseResult(Activity.RESULT_OK, intent)

        assertEquals("empty-encoded", result)
    }
}
