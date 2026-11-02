package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType

class FormInputAdapterTest {

    private fun formElement(
        id: Int = 1,
        inputType: InputType = InputType.EDIT_TEXT,
        required: Boolean = false,
        title: String = "Title",
        errorText: String? = null
    ) = FormElement(
        id = id,
        inputType = inputType,
        required = required,
        title = title,
        errorText = errorText
    )

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = FormInputAdapter.FormInputDiffCallBack
        val old = formElement(id = 1, title = "A")
        val same = formElement(id = 1, title = "B")
        val different = formElement(id = 2, title = "A")
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_comparesOnlyErrorText() {
        val callback = FormInputAdapter.FormInputDiffCallBack
        val old = formElement(id = 1, title = "A", errorText = null)
        val sameError = formElement(id = 1, title = "Different Title", errorText = null)
        val differentError = formElement(id = 1, title = "A", errorText = "Required")
        assertTrue(callback.areContentsTheSame(old, sameError))
        assertFalse(callback.areContentsTheSame(old, differentError))
    }

    @Test
    fun imageClickListener_invokesLambdaWithFormId() {
        var captured: Int? = null
        val listener = FormInputAdapter.ImageClickListener { formId -> captured = formId }
        listener.onImageClick(formElement(id = 7))
        assertEquals(7, captured)
    }

    @Test
    fun ageClickListener_invokesLambdaWithFormId() {
        var captured: Int? = null
        val listener = FormInputAdapter.AgeClickListener { formId -> captured = formId }
        listener.onAgeClick(formElement(id = 8))
        assertEquals(8, captured)
    }

    @Test
    fun formValueListener_invokesLambdaWithIdAndIndex() {
        var capturedId: Int? = null
        var capturedIndex: Int? = null
        val listener = FormInputAdapter.FormValueListener { id, index ->
            capturedId = id
            capturedIndex = index
        }
        listener.onValueChanged(formElement(id = 9), 2)
        assertEquals(9, capturedId)
        assertEquals(2, capturedIndex)
    }

    @Test
    fun selectUploadImageClickListener_invokesLambdaWithFormId() {
        var captured: Int? = null
        val listener = FormInputAdapter.SelectUploadImageClickListener { formId -> captured = formId }
        listener.onSelectImageClick(formElement(id = 10))
        assertEquals(10, captured)
    }

    @Test
    fun viewDocumentOnClick_invokesLambdaWithFormId() {
        var captured: Int? = null
        val listener = FormInputAdapter.ViewDocumentOnClick { formId -> captured = formId }
        listener.onViewDocumentClick(formElement(id = 11))
        assertEquals(11, captured)
    }

    @Test
    fun sendOtpClickListener_invokesLambdaWithAllArgs() {
        var capturedFormId: Int? = null
        var capturedEnabled: Boolean? = null
        var capturedPosition: Int? = null
        val generateOtp = mockk<MaterialButton>()
        val timerInSec = mockk<TextView>()
        val tilEditText = mockk<TextInputLayout>()
        val otpField = mockk<TextInputEditText>()
        val listener = FormInputAdapter.SendOtpClickListener { formId, _, _, _, isEnabled, adapterPosition, _ ->
            capturedFormId = formId
            capturedEnabled = isEnabled
            capturedPosition = adapterPosition
        }
        listener.onButtonClick(formElement(id = 12), generateOtp, timerInSec, tilEditText, true, 3, otpField)
        assertEquals(12, capturedFormId)
        assertEquals(true, capturedEnabled)
        assertEquals(3, capturedPosition)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FormInputAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
