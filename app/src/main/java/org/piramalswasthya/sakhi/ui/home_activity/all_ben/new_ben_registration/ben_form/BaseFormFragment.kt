package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.piramalswasthya.sakhi.R

abstract class BaseFormFragment : Fragment() {

    protected var isFormDirty = false

    private val unsavedChangesAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.incomplete_form_found))
            .setMessage(resources.getString(R.string.do_you_want_to_save_draft))
            .setPositiveButton(resources.getString(R.string.agree)) { dialog, _ ->
                saveDraft()
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.disagree)) { dialog, _ ->
                findNavController().navigateUp()
                dialog.dismiss()
            }
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFormDirty) {
                    unsavedChangesAlert.show()
                } else {
                    findNavController().navigateUp()
                }
            }
        })
    }

    protected fun setFormAsDirty() {
        isFormDirty = true
    }

    protected fun setFormAsClean() {
        isFormDirty = false
    }

    abstract fun saveDraft()
}
