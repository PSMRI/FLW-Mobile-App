package org.piramalswasthya.sakhi.ui.asha_supervisor.dialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.piramalswasthya.sakhi.R

class NoInternetDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false          // user cannot dismiss manually
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("No Internet Connection")
            .setMessage("Please check your network settings. This dialog will close automatically when connectivity is restored.")
            .setIcon(R.drawable.no_wifi)
            .create()
    }

    companion object {
        const val TAG = "NoInternetDialog"
    }
}