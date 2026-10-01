package org.piramalswasthya.sakhi.activity_contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RDServiceInitContractTest {

    private lateinit var contract: RDServiceInitContract
    private lateinit var context: Context

    @Before
    fun setUp() {
        contract = RDServiceInitContract()
        context = mockk(relaxed = true)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setAction(any()) } answers { self as Intent }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createIntent_setsInitAction() {
        contract.createIntent(context, Unit)

        verify { anyConstructed<Intent>().action = "in.secugen.rdservice.INIT" }
    }

    @Test
    fun parseResult_alwaysReturnsNull_whenResultOkAndIntentPresent() {
        val intent = mockk<Intent>(relaxed = true)

        val result = contract.parseResult(Activity.RESULT_OK, intent)

        assertNull(result)
    }

    @Test
    fun parseResult_alwaysReturnsNull_whenResultNotOk() {
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
