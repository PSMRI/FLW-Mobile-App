package org.piramalswasthya.sakhi.ui.asha_supervisor.dialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.piramalswasthya.sakhi.R

class NoInternetDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.no_internet_connection))
            .setMessage(getString(R.string.please_check_your_network_settings_this_dialog_will_close_automatically_when_connectivity_is_restored))
            .setIcon(R.drawable.no_wifi)
            .create()
    }

    companion object {
        const val TAG = "NoInternetDialog"
    }
}