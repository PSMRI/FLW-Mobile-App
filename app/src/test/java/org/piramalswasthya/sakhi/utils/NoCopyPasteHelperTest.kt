package org.piramalswasthya.sakhi.utils

import android.view.ActionMode
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoCopyPasteHelperTest {

    @Test
    fun `disableCopyPaste disables long click and text selection`() {
        val editText = mockk<EditText>(relaxed = true)

        NoCopyPasteHelper.disableCopyPaste(editText)

        verify { editText.isLongClickable = false }
        verify { editText.setLongClickable(false) }
        verify { editText.setTextIsSelectable(false) }
    }

    @Test
    fun `disableCopyPaste blocks every action mode callback`() {
        val editText = mockk<EditText>(relaxed = true)
        val callbackSlot = slot<ActionMode.Callback>()
        every { editText.customSelectionActionModeCallback = capture(callbackSlot) } just Runs

        NoCopyPasteHelper.disableCopyPaste(editText)

        val callback = callbackSlot.captured
        val mode = mockk<ActionMode>(relaxed = true)
        assertFalse(callback.onCreateActionMode(mode, null))
        assertFalse(callback.onPrepareActionMode(mode, null))
        assertFalse(callback.onActionItemClicked(mode, null))
        callback.onDestroyActionMode(mode)
    }

    @Test
    fun `disableCopyPaste blocks long press and consumes it`() {
        val editText = mockk<EditText>(relaxed = true)
        val longClickSlot = slot<View.OnLongClickListener>()
        every { editText.setOnLongClickListener(capture(longClickSlot)) } just Runs

        NoCopyPasteHelper.disableCopyPaste(editText)

        assertTrue(longClickSlot.captured.onLongClick(editText))
    }

    @Test
    fun `disableCopyPaste cancels a pending long press on touch down`() {
        val editText = mockk<EditText>(relaxed = true)
        val touchSlot = slot<View.OnTouchListener>()
        every { editText.setOnTouchListener(capture(touchSlot)) } just Runs

        NoCopyPasteHelper.disableCopyPaste(editText)

        val downEvent = mockk<MotionEvent>()
        every { downEvent.action } returns MotionEvent.ACTION_DOWN

        assertFalse(touchSlot.captured.onTouch(editText, downEvent))
        verify { editText.cancelLongPress() }
    }

    @Test
    fun `disableCopyPaste ignores non-down touch events`() {
        val editText = mockk<EditText>(relaxed = true)
        val touchSlot = slot<View.OnTouchListener>()
        every { editText.setOnTouchListener(capture(touchSlot)) } just Runs

        NoCopyPasteHelper.disableCopyPaste(editText)

        val moveEvent = mockk<MotionEvent>()
        every { moveEvent.action } returns MotionEvent.ACTION_MOVE

        assertFalse(touchSlot.captured.onTouch(editText, moveEvent))
        verify(exactly = 0) { editText.cancelLongPress() }
    }
}
