package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.AadhaarConsentModel

class AadhaarConsentAdapterTest {

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<AadhaarConsentModel> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AadhaarConsentAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<AadhaarConsentModel>
    }

    @Test
    fun areItemsTheSame_comparesByTitle() {
        val callback = diffCallback()
        val old = AadhaarConsentModel(title = "Consent A", checked = false)
        val same = AadhaarConsentModel(title = "Consent A", checked = true)
        val different = AadhaarConsentModel(title = "Consent B")
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = AadhaarConsentModel(title = "Consent A", checked = false)
        val same = AadhaarConsentModel(title = "Consent A", checked = false)
        val different = AadhaarConsentModel(title = "Consent A", checked = true)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun consentClickListener_invokesLambdaWithConsentAndPosition() {
        var capturedConsent: AadhaarConsentModel? = null
        var capturedPosition: Int? = null
        val listener = AadhaarConsentAdapter.ConsentClickListener { consent, position ->
            capturedConsent = consent
            capturedPosition = position
        }
        val model = AadhaarConsentModel(title = "Consent A")
        listener.onClicked(model, 3)
        assertEquals(model, capturedConsent)
        assertEquals(3, capturedPosition)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = AadhaarConsentAdapter(AadhaarConsentAdapter.ConsentClickListener { _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
