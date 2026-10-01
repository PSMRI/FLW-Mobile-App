package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.FormInputOld
import org.piramalswasthya.sakhi.model.InputType

class FormInputAdapterOldTest {

    private fun formInputOld(
        title: String = "Title",
        required: Boolean = false,
        errorText: String? = null
    ) = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = title,
        required = required,
        errorText = errorText
    )

    @Test
    fun areItemsTheSame_comparesByTitle() {
        val callback = FormInputAdapterOld.FormInputDiffCallBack
        val old = formInputOld(title = "A")
        val same = formInputOld(title = "A", required = true)
        val different = formInputOld(title = "B")
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = FormInputAdapterOld.FormInputDiffCallBack
        val old = formInputOld(title = "A", errorText = null)
        val same = formInputOld(title = "A", errorText = null)
        val different = formInputOld(title = "A", errorText = "Required")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun imageClickListener_invokesLambdaWithForm() {
        var captured: FormInputOld? = null
        val listener = FormInputAdapterOld.ImageClickListener { form -> captured = form }
        val form = formInputOld(title = "Photo")
        listener.onImageClick(form)
        assertEquals(form, captured)
    }

    @Test
    fun formValueListener_invokesLambdaWithIdAndIndex() {
        var capturedId: Int? = null
        var capturedIndex: Int? = null
        val listener = FormInputAdapterOld.FormValueListener { id, index ->
            capturedId = id
            capturedIndex = index
        }
        val formElement = FormElement(id = 5, inputType = InputType.EDIT_TEXT, required = false, title = "T")
        listener.onValueChanged(formElement, 4)
        assertEquals(5, capturedId)
        assertEquals(4, capturedIndex)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FormInputAdapterOld()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
