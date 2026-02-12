package org.piramalswasthya.sakhi.ui.common

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Interface to be implemented by ViewModels that support draft handling.
 */
interface DraftViewModel<T> {
    fun restoreDraft(draft: T)
    fun ignoreDraft()
}

/**
 * Common logic to show a draft restoration dialog in a Fragment.
 */
fun <T> Fragment.showDraftRestoreDialog(
    draft: T?,
    viewModel: DraftViewModel<T>,
    onDialogDismissed: (() -> Unit)? = null
) {
    draft?.let {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Draft Found")
            .setMessage("You have a saved draft for this form. Do you want to restore it?")
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restoreDraft(it)
                onDialogDismissed?.invoke()
            }
            .setNegativeButton("Ignore") { _, _ ->
                viewModel.ignoreDraft()
                onDialogDismissed?.invoke()
            }
            .setCancelable(false)
            .show()
    }
}
