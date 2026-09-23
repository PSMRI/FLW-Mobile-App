package org.piramalswasthya.sakhi.helpers.otpview

import android.text.Selection
import android.text.Spannable
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DefaultMovementMethodTest {

    private lateinit var widget: TextView
    private lateinit var text: Spannable

    @Before
    fun setUp() {
        widget = mockk(relaxed = true)
        text = mockk(relaxed = true)
        mockkStatic(Selection::class)
        every { Selection.setSelection(any<Spannable>(), any<Int>()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `instance returns a non-null singleton`() {
        assertNotNull(DefaultMovementMethod.instance)
    }

    @Test
    fun `instance returns the same object on repeated access`() {
        val first = DefaultMovementMethod.instance
        val second = DefaultMovementMethod.instance
        assertSame(first, second)
    }

    @Test
    fun `initialize sets selection to start of text`() {
        DefaultMovementMethod.instance!!.initialize(widget, text)

        verify { Selection.setSelection(text, 0) }
    }

    @Test
    fun `onKeyDown always returns false`() {
        val event: KeyEvent = mockk(relaxed = true)
        val result = DefaultMovementMethod.instance!!.onKeyDown(widget, text, KeyEvent.KEYCODE_A, event)
        assertFalse(result)
    }

    @Test
    fun `onKeyUp always returns false`() {
        val event: KeyEvent = mockk(relaxed = true)
        val result = DefaultMovementMethod.instance!!.onKeyUp(widget, text, KeyEvent.KEYCODE_A, event)
        assertFalse(result)
    }

    @Test
    fun `onKeyOther always returns false`() {
        val event: KeyEvent = mockk(relaxed = true)
        val result = DefaultMovementMethod.instance!!.onKeyOther(widget, text, event)
        assertFalse(result)
    }

    @Test
    fun `onTakeFocus does nothing and does not throw`() {
        DefaultMovementMethod.instance!!.onTakeFocus(widget, text, 0)
    }

    @Test
    fun `onTrackballEvent always returns false`() {
        val event: MotionEvent = mockk(relaxed = true)
        val result = DefaultMovementMethod.instance!!.onTrackballEvent(widget, text, event)
        assertFalse(result)
    }

    @Test
    fun `onTouchEvent always returns false`() {
        val event: MotionEvent = mockk(relaxed = true)
        val result = DefaultMovementMethod.instance!!.onTouchEvent(widget, text, event)
        assertFalse(result)
    }

    @Test
    fun `onGenericMotionEvent always returns false`() {
        val event: MotionEvent = mockk(relaxed = true)
        val result = DefaultMovementMethod.instance!!.onGenericMotionEvent(widget, text, event)
        assertFalse(result)
    }

    @Test
    fun `canSelectArbitrarily always returns false`() {
        assertFalse(DefaultMovementMethod.instance!!.canSelectArbitrarily())
    }
}
