package org.piramalswasthya.sakhi.ui.common

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Attach an unsaved-changes guard to a Fragment.
 * Any object passed must implement DirtyState.
 */
fun Fragment.attachAdapterUnsavedGuard(
    dirtyState: DirtyState,
    lifecycleOwner: LifecycleOwner = viewLifecycleOwner,
    onSaveDraft: (() -> Unit)? = null,
    onDiscard: (() -> Unit)? = null
) {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val isDirty = dirtyState.isDirty.value == true

            if (!isDirty) {
                // No unsaved changes, go back
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                return
            }

            // Show Unsaved Changes dialog
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Unsaved changes")
                .setMessage(
                    "You have unsaved changes on this screen. " +
                            "Discard changes or stay on this screen?"
                )
                .setNegativeButton("Discard") { dialog, _ ->
                    dirtyState.clearDirty()
                    onDiscard?.invoke()
                    dialog.dismiss()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                .setPositiveButton("Stay") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    requireActivity()
        .onBackPressedDispatcher
        .addCallback(lifecycleOwner, callback)
}
