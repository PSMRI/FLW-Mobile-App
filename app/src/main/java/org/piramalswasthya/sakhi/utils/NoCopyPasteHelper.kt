package org.piramalswasthya.sakhi.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.EditText

object NoCopyPasteHelper {

    fun disableCopyPaste(editText: EditText) {

        // Disable long press menu
        editText.isLongClickable = false
        editText.setLongClickable(false)
        editText.setTextIsSelectable(false)

        // Block Copy / Paste / Cut menu actions
        editText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            override fun onDestroyActionMode(mode: ActionMode?) {
                /*
                Empty by design; required override with no behavior needed in this implementation.
*/
            }
        }

        // Additional paste block (some devices bypass actionMode)
        editText.setOnLongClickListener { true }

        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.cancelLongPress()
            }
            false
        }
    }
}
