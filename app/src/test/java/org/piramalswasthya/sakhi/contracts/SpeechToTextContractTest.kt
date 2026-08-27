package org.piramalswasthya.sakhi.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SpeechToTextContractTest {

    private lateinit var contract: SpeechToTextContract
    private lateinit var context: Context

    @Before
    fun setUp() {
        contract = SpeechToTextContract()
        context = mockk(relaxed = true)
        mockkConstructor(Intent::class)
        every {
            anyConstructed<Intent>().putExtra(any<String>(), any<String>())
        } answers { self as Intent }
        every {
            anyConstructed<Intent>().putExtra(any<String>(), any<java.io.Serializable>())
        } answers { self as Intent }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun createIntent_setsFreeFormLanguageModelAndPrompt() {
        val result = contract.createIntent(context, Unit)

        verify {
            result.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
        verify { result.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text") }
    }

    @Test
    fun parseResult_returnsFirstRecognizedString_whenResultOk() {
        val intent = mockk<Intent>(relaxed = true)
        every {
            intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        } returns arrayListOf("hello world", "hello")

        val result = contract.parseResult(Activity.RESULT_OK, intent)

        assertEquals("hello world", result)
    }

    @Test
    fun parseResult_returnsEmptyString_whenExtraIsMissing() {
        val intent = mockk<Intent>(relaxed = true)
        every {
            intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        } returns null

        val result = contract.parseResult(Activity.RESULT_OK, intent)

        assertEquals("", result)
    }

    @Test
    fun parseResult_returnsEmptyString_whenResultNotOk() {
        val intent = mockk<Intent>(relaxed = true)
        every {
            intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        } returns arrayListOf("hello world")

        val result = contract.parseResult(Activity.RESULT_CANCELED, intent)

        assertEquals("", result)
    }

    @Test
    fun parseResult_returnsEmptyString_whenIntentIsNull() {
        val result = contract.parseResult(Activity.RESULT_OK, null)

        assertEquals("", result)
    }
}
